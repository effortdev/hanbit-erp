# ADR-013: 예산 배정 단위와 기간

- 상태: 승인됨
- 범위: Module 4 (예산/지출관리)

> **참고**: 이 ADR의 지시 문장이 "지출 발생 시점(월/분기)을 함" 부분에서 끊긴 채 전달되었다. 문맥(연간 배정 기본 + 분기별 실적 조회 가능)으로 미루어 "지출 발생 시점(월/분기 정보)을 함께 기록해 분기별 조회를 지원한다"는 의도로 이해하고 작성했다. 의도와 다르면 알려주면 다시 정리하겠다.

## 상황

FR-4-1(부서별/연도별 예산 배정), FR-4-3(부서별 예산 소진율 조회)을 구현하려면 예산을 "어떤 단위로, 어떤 기간으로" 배정할지 정해야 한다.

## 결정

**배정 단위: 조직(본부/팀) × 계정과목(비용 항목) 조합.** 계정과목은 사전 정의된 카테고리로 제한한다(인건비/출장비/소모품비/접대비/기타).

```sql
CREATE TABLE budget_allocation (
    org_unit_id      BIGINT NOT NULL,
    account_category VARCHAR(20) NOT NULL,  -- LABOR|TRAVEL|SUPPLIES|ENTERTAINMENT|OTHER
    fiscal_year      INT NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    UNIQUE KEY uq_budget_allocation (org_unit_id, account_category, fiscal_year)
);
```

**배정 기간: 연간 배정이 기본.** 회계연도(1년) 단위로 배정액을 등록하고, 소진율도 연 단위로 계산한다(Module 3의 연차 정책과 같은 "회계연도 일괄" 전제 — ADR-009).

**분기별 실적 조회는 별도 컬럼 없이 파생한다.** `budget_expense_request`에 지출 "구간"을 나타내는 컬럼을 추가로 두지 않고, 이미 기록되는 `created_at`(상신 시각)에서 분기를 즉시 계산한다.

```sql
SELECT QUARTER(created_at) AS quarter, SUM(amount) AS total
FROM budget_expense_request
WHERE org_unit_id = #{orgUnitId} AND account_category = #{category}
  AND YEAR(created_at) = #{year} AND status IN ('APPROVED', 'PENDING')
GROUP BY QUARTER(created_at)
```

## 근거

- 조직×계정과목 조합은 "어느 부서가 어떤 항목에 얼마를 썼는지"를 구분해야 하는 FR-4-3의 요구와 정확히 맞아떨어진다. 조직만으로 배정하면 인건비와 접대비가 뒤섞여 소진율이 무의미해진다.
- 계정과목을 자유 텍스트가 아니라 사전 정의 목록(enum)으로 제한한 것은, Module 1의 `Position`/Module 2의 `DocumentType`과 같은 방식이다 — 오탈자로 다른 카테고리가 생기는 것을 막고 집계 쿼리를 단순하게 유지한다.
- 연간 배정을 기본으로 한 것은 Module 3에서 이미 "회계연도 일괄" 방식을 검증했고(ADR-009), 실제 기업 예산 편성도 연 단위가 일반적이라 큰 이견 없이 재사용했다.
- 분기 정보를 별도 컬럼으로 저장하지 않고 `created_at`에서 파생시키는 것은, Module 1~3에서 일관되게 지켜온 "원천 데이터에서 파생 계산, 중복 상태 컬럼 지양" 원칙(ADR-003, ADR-006, ADR-009)의 연장이다.

## 트레이드오프

- 배정 단위가 조직×계정과목으로 세분화되어 있어, 신규 조직이 생기거나 카테고리가 늘어날 때마다 배정을 놓치면(배정 자체가 없으면) 그 조합에 대한 지출결의서 상신이 전부 막힌다(ADR-012의 "배정이 없으면 예외" 처리). 실제로는 회계연도 초에 전체 조직×카테고리 배정을 일괄 등록하는 절차가 필요하다는 뜻이며, 이번 범위에서는 그 일괄 등록 UI까지는 만들지 않는다(개별 등록 폼만 제공).
- "지출 발생 시점"을 상신 시각(`created_at`)으로 근사했다 — 실제 지출이 발생한 날짜(예: 출장 다녀온 날짜)와 상신일이 다를 수 있는 케이스는 반영하지 않는다. 이번 요구사항 범위에서는 상신일 기준으로 충분하다고 판단했다.
