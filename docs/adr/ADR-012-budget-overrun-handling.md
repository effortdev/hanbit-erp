# ADR-012: 예산 초과 처리 방식

- 상태: 승인됨 (단, `BudgetThresholdPolicy` 관련 부분은 아래 "후속 기록 — 폐기됨" 참고)
- 범위: Module 4 (예산/지출관리), Module 2 `BudgetThresholdPolicy` 실제 구현

## 상황

Module 2는 `BudgetThresholdPolicy` 인터페이스만 정의해두고, `NoBudgetModuleYetPolicy`(항상 `false`)로 자리만 채워뒀다. Module 4를 만들면서 이걸 실제로 구현해야 한다. 동시에 FR-4-3(소진율 조회)·FR-4-4(초과 시 처리)를 지출결의서 상신 흐름에 어떻게 연결할지 정해야 한다.

## 결정

지출결의서 상신 시점에 **예산 소진율을 3단계로 판정**한다 (`ExpenseServiceImpl.requestExpense(...)`가 `approvalService.draftExpense(...)`를 호출하기 전에 검사).

| 소진율(이 지출 포함 시 예상치) | 처리 |
|---|---|
| 80% 미만 | 통과 |
| 80% 이상 100% 미만 | 통과하되 화면에 경고 표시 (상신은 막지 않음) |
| 100% 이상 | **원칙적으로 상신 차단** (`BusinessException`) |
| 100% 이상 **+ 금액 500만원 이상**(ADR-011 기준 대표이사 결재 단계 포함 건) | "예외 승인 사유"를 입력하면 상신 허용 |

```java
BigDecimal projectedRate = (usedSoFar.add(amount)).divide(allocated, 4, RoundingMode.HALF_UP);
boolean isCeoTier = amount.compareTo(FIVE_MILLION) >= 0; // ADR-011과 동일 기준선 재사용

if (projectedRate.compareTo(ONE) >= 0) {
    if (!isCeoTier) {
        throw new BusinessException("예산을 초과하여 상신할 수 없습니다. (예상 소진율 " + ... + ")");
    }
    if (exceptionReason == null || exceptionReason.isBlank()) {
        throw new BusinessException("예산 초과 고액 지출은 예외 승인 사유를 입력해야 합니다.");
    }
    // 사유를 budget_expense_request.exception_reason에 남기고 상신 허용
}
```

`usedSoFar`는 **승인 완료(APPROVED) + 상신 중(PENDING)** 금액의 합이다 — Module 3(ADR-009)에서 "상신 중인 것도 임시로 차감해 중복 초과를 막는다"고 정한 원칙을 그대로 가져왔다. 반려되면 상태가 `PENDING → REJECTED`로 바뀌어 합계에서 자동으로 빠지므로, 여기서도 별도 롤백 로직이 필요 없다.

`BudgetThresholdPolicy`(Module 2 인터페이스)는 이번에 실제 구현체로 교체한다:

```java
@Component
public class BudgetThresholdPolicyImpl implements BudgetThresholdPolicy {
    public boolean isExceeded(Long orgUnitId, BigDecimal amount) {
        // "80% 이상"을 그대로 의미 — 결재라인에 조건부 단계를 추가할지 판단하는 원래 용도
        return getProjectedRate(orgUnitId, amount) >= 0.8;
    }
}
```

다만 ADR-011에서 EXPENSE 결재라인의 대표이사 단계 조건을 금액 기준(`AMOUNT_GTE_5M`)으로 바꿨기 때문에, 지금 이 구현체를 실제로 소비하는 결재라인 규칙은 없다 — 그래도 인터페이스 계약대로 정직하게 구현해두는 이유는 ①Module 2가 정의한 계약을 지키는 것 자체가 이 ADR의 목적이고, ②향후 다른 문서 유형(예: 품의서)에 "예산 소진율 조건부 단계"가 필요해지면 바로 재사용 가능해서다.

> **이 판단은 아래 "후속 기록 — 폐기됨"에서 뒤집혔다.** ②의 "향후 재사용" 근거를 요구사항 문서 기준으로 다시 확인해보니 실제로 그런 계획이 없었다.

## 근거

- 80%는 "주의를 환기하되 실제 자금 집행 유연성은 해치지 않는" 임계값으로, 사전 경고 이상의 강제력을 두지 않는다.
- 100% 초과를 원칙적으로 차단하는 것은 "잘못된 상태로 진행되느니 안전하게 막는다"는 이 프로젝트 전반의 원칙(Module 2의 리더 공석 처리, Module 3의 연차 초과 차단)과 일관된다.
- 대표이사 결재가 걸린 고액 건에 한해 예외 승인 경로를 열어둔 것은, 실제 기업의 지출 통제가 "규정은 있지만 최고 의사결정권자 재가로 예외를 허용"하는 일반적인 패턴을 반영한다. 소액 건까지 예외를 열어주면 100% 차단 자체가 무력화되므로 고액 건(어차피 대표이사가 직접 결재)으로 한정했다.

## 트레이드오프

- "예상 소진율"은 승인 이전 시점의 계산이라, 여러 부서 담당자가 동시에 비슷한 시점에 지출을 상신하면(동시성) 각자에게는 괜찮아 보여도 합쳐서 초과되는 경합 상황이 있을 수 있다. 트랜잭션 격리 수준을 올리거나 비관적 락을 걸지 않는 한 이론적으로 남아있는 문제이며, 이번 포트폴리오 범위에서는 다루지 않는다.
- 예외 승인 사유는 시스템이 "적절한 사유인지" 검증하지 않는다(공란만 아니면 통과). 실제 서비스라면 별도 승인자가 이 사유 자체를 재검토하는 절차가 있어야 하지만, 지금은 대표이사가 어차피 마지막 결재 단계에서 사유를 보고 판단한다는 전제로 단순화했다.

## 후속 기록 — 폐기됨 (`BudgetThresholdPolicy` / `BUDGET_80_EXCEEDED`)

- **폐기일**: Module 4 완료 직후, 코드 감사 과정에서.
- **대상**: `egovframework.erp.approval.service.BudgetThresholdPolicy` 인터페이스, `BudgetThresholdPolicyImpl`(Module 4 구현체), `ApprovalCondition.BUDGET_80_EXCEEDED` — 전부 삭제했다. `ApprovalLineResolverImpl`의 생성자에서도 `BudgetThresholdPolicy` 의존성을 제거했다.
- **폐기 사유**: 이 ADR을 쓸 당시 "①계약 준수 ②향후 재사용" 두 가지를 존치 근거로 들었다. 그런데 실제로 요구사항 문서(`erp_requirements.md`)의 Module 5(재고/구매관리)·Module 6(영업/매출관리) FR/NFR을 전부 다시 확인한 결과, 예산 소진율이나 예산 조건부 결재 단계를 언급하는 요구사항이 전혀 없었다. 즉 ②의 "향후 재사용될 것"이라는 전제 자체가 근거 없는 추측이었다 — 실제로 쓰일 곳이 없는 채로 인터페이스와 빈(bean)만 유지하는 것은 계약 준수가 아니라 죽은 코드였다.
- **영향도**: `ApprovalLineResolverImplTest`의 예산 조건 관련 테스트 2건을 삭제했다(둘 다 이 메커니즘 자체를 검증하는 테스트였음). 나머지 테스트(리더 공석, 자기결재 스킵, 대표이사 단수성, 금액 구간 3종)는 전혀 영향받지 않았고, 전체 테스트 재실행으로 회귀 없음을 확인했다(38건 → 36건, 실패 0).
- **재도입 조건**: 만약 향후 실제 요구사항으로 "예산 소진율에 따라 결재 단계가 조건부로 추가"되는 문서 유형이 생기면, 이번에 지운 것과 같은 형태(인터페이스 + `ApprovalCondition` 상수 + 리졸버 분기)로 다시 만들면 된다 — 이 ADR과 git 이력에 원래 구현이 남아있다.

