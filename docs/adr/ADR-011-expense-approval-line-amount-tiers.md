# ADR-011: 지출결의서 결재라인의 금액 조건

- 상태: 승인됨
- 범위: Module 4 (예산/지출관리), Module 2 `approval_line_rule` 시드 데이터 교체

## 상황

기존 EXPENSE 결재라인(Module 2에서 임시로 넣어둔 것)은 "팀장→본부장 고정 + 예산 80% 초과 시 대표이사 조건부 추가"였다. 하지만 Module 4를 실제로 설계해보니, 요구사항이 원하는 것은 예산 소진율이 아니라 **지출 금액 자체의 구간**에 따라 결재 단계 수가 달라지는 것이었다.

- 100만원 미만: 팀장 단독 승인
- 100만원 이상 500만원 미만: 팀장 → 본부장
- 500만원 이상: 팀장 → 본부장 → 대표이사

## 결정

`approval_line_rule.condition_expr`에 금액 조건 두 가지를 추가하고, EXPENSE 규칙을 교체한다.

```sql
-- 기존 (Module 2/3 시점 임시값)
-- ('EXPENSE', 3, 'CEO', 'BUDGET_80_EXCEEDED')

-- 교체 후
('EXPENSE', 1, 'TEAM_LEADER',   NULL),
('EXPENSE', 2, 'DIVISION_HEAD', 'AMOUNT_GTE_1M'),
('EXPENSE', 3, 'CEO',           'AMOUNT_GTE_5M')
```

```java
public enum ApprovalCondition {
    BUDGET_80_EXCEEDED,  // 더 이상 시드 데이터에서 쓰이지 않지만 메커니즘 자체는 유지 (아래 "영향 확인" 참고)
    AMOUNT_GTE_1M,
    AMOUNT_GTE_5M
}
```

`ApprovalLineResolverImpl`의 조건 판정을 일반화했다(기존엔 `BUDGET_80_EXCEEDED`만 특수 처리):

```java
private boolean isConditionMet(ApprovalCondition condition, Employee drafter, BigDecimal amount) {
    switch (condition) {
        case BUDGET_80_EXCEEDED: return budgetThresholdPolicy.isExceeded(drafter.getOrgUnitId(), amount);
        case AMOUNT_GTE_1M: return amount != null && amount.compareTo(ONE_MILLION) >= 0;
        case AMOUNT_GTE_5M: return amount != null && amount.compareTo(FIVE_MILLION) >= 0;
    }
}
```

`AMOUNT_GTE_1M`/`AMOUNT_GTE_5M`은 이미 `resolve()`에 인자로 들어오는 `amount`만으로 판단되므로(다른 모듈 조회가 필요 없음), `BudgetThresholdPolicy` 같은 별도 인터페이스 없이 리졸버 내부에서 바로 계산한다 — 외부 모듈 데이터가 필요한 조건(예산 소진율)과, 입력값만으로 계산 가능한 조건(금액 구간)을 구분한 것이다.

### 기존 데이터/테스트에 미치는 영향 확인

- **`ApprovalLineResolverImplTest`**: `BUDGET_80_EXCEEDED` 관련 테스트 2건은 규칙 데이터를 테스트 코드 안에서 직접 만들어 `ruleMapper`를 목(mock) 처리하고 있어, `schema.sql`의 실제 시드값과 무관하게 동작한다. 즉 EXPENSE 시드를 바꿔도 이 테스트들은 깨지지 않는다 — `BUDGET_80_EXCEEDED`라는 조건 판정 메커니즘 자체(예산 정책 위임 호출)는 여전히 유효하고 테스트도 통과한다. 그래서 `ApprovalCondition`에서 `BUDGET_80_EXCEEDED`를 **제거하지 않고 남겨뒀다** — 지금은 어떤 시드 데이터도 참조하지 않지만, 향후 다른 문서 유형에 "예산 소진율 조건부 단계"가 필요해지면 재사용할 수 있다.
- **Module 3(근태) 관련 테스트**: VACATION 규칙(`TEAM_LEADER`만, 조건 없음)은 이번 변경과 무관해 영향 없음.
- 변경 후 `mvn test` 전체 재실행으로 회귀 여부를 확인한다(아래 "검증" 참고).

## 근거

- Module 2에서 구매요청서(PURCHASE)에 이미 "금액/유형에 따라 결재 단계 수가 달라진다"는 아이디어를 조건부 규칙(`condition_expr`)으로 구현해 둔 적이 있어, 같은 메커니즘을 그대로 재사용하는 것이 일관적이다.
- 예산 소진율 기반 조건(`BUDGET_80_EXCEEDED`)은 "이 조직 예산이 이미 빠듯한가"를 묻는 것이고, 금액 구간 조건은 "이 지출 자체가 큰가"를 묻는 것이라 서로 다른 질문이다. 후자가 실무적으로 더 흔한 결재 규정 형태(고액 지출일수록 상급자 결재)와 일치한다.

## 트레이드오프

- 금액 임계값(100만/500만)이 하드코딩된 상수(`ONE_MILLION`, `FIVE_MILLION`)로 리졸버 코드에 들어간다 — 규칙 테이블처럼 데이터로 완전히 분리되어 있지는 않다. 다만 `condition_expr` 자체는 데이터(어떤 조건을 적용할지)이고, 그 조건의 "숫자 임계값"만 코드에 있는 것이라 규칙의 단계 수/유형 변경(데이터)과 임계값 변경(코드)의 책임이 분리되어 있다는 점은 유지된다.
