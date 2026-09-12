# ADR-014: 구매요청서 결재라인 및 품목 조건

- 상태: 승인됨
- 범위: Module 5 (재고/구매관리), Module 2 `approval_line_rule` PURCHASE 시드 데이터 교체

## 상황

Module 2에서 PURCHASE 결재라인은 "물류본부 소속만 기안 가능 + 팀장→본부장→대표이사 고정 3단계"로 만들어뒀다(금액과 무관하게 항상 3단계). Module 5를 설계하며 이 고정 3단계가 실제로 맞는지, 그리고 품목 종류(원자재/소모품 등)에 따라 결재라인이 더 세분화되어야 하는지 확인이 필요했다.

## 결정

### 결재라인: EXPENSE와 동일한 금액 구간 재사용

```sql
-- 기존 (Module 2 시점, 금액 무관 고정 3단계)
-- ('PURCHASE', 1, 'TEAM_LEADER',   NULL),
-- ('PURCHASE', 2, 'DIVISION_HEAD', NULL),
-- ('PURCHASE', 3, 'CEO',           NULL),

-- 교체 후 (ADR-011의 EXPENSE 구간과 동일 기준)
('PURCHASE', 1, 'TEAM_LEADER',   NULL),
('PURCHASE', 2, 'DIVISION_HEAD', 'AMOUNT_GTE_1M'),
('PURCHASE', 3, 'CEO',           'AMOUNT_GTE_5M'),
```

물류본부 소속 검증(`requireLogisticsDivision`)은 Module 2의 `ApprovalServiceImpl.draftPurchase(...)`가 이미 하고 있으므로 그대로 재사용한다 — Module 5는 이 검증을 다시 구현하지 않고, `ApprovalService.draftPurchase(...)`를 호출하기만 한다(위반 시 Module 2가 `BusinessException`을 던지고 그대로 전파된다).

### 품목별 추가 조건: 도입하지 않음

"원자재는 별도 승인 경로를 거친다" 같은 품목 종류별 결재라인 세분화는 이번 범위에서 배제한다. `item` 테이블에 품목을 분류하는 카테고리 필드조차 두지 않았다(표시용으로도 불필요).

## 근거

- 결재라인을 EXPENSE와 동일한 금액 구간으로 맞춘 것은 이미 ADR-011에서 검증된 패턴(규칙 테이블 + 리졸버의 `AMOUNT_GTE_1M`/`AMOUNT_GTE_5M` 조건 판정)을 그대로 재사용하는 것이라 별도 코드 변경 없이 시드 데이터만 바꾸면 된다 — `ApprovalLineResolverImpl`은 이미 문서 유형에 무관하게 동작하도록 만들어져 있다.
- 품목별 조건을 넣지 않은 이유는 Module 4에서 얻은 교훈 때문이다: `BudgetThresholdPolicy`를 "언젠가 필요할 것"이라는 추측만으로 만들어뒀다가, 실제로는 어떤 요구사항도 그것을 필요로 하지 않아 결국 폐기했다(ADR-012 "후속 기록 — 폐기됨"). `erp_requirements.md`의 Module 5 FR/NFR 어디에도 품목 종류별 결재 분기를 요구하는 문장이 없다 — 근거 없는 조건을 미리 만들지 않는다.

## 트레이드오프

- 실제 기업의 구매 프로세스는 원자재·설비·소모품마다 결재 경로가 다른 경우가 흔하지만, 이번 포트폴리오는 그 세분화를 다루지 않는다. 필요해지면 EXPENSE/PURCHASE와 같은 방식(규칙 테이블에 조건 추가)으로 확장하면 되고, 그때는 실제 요구사항 문구를 근거로 남긴다.
