package egovframework.erp.approval.domain;

/**
 * approval_line_rule.condition_expr에 들어갈 수 있는 조건 식별자. 완전한 조건식 엔진을
 * 두는 대신(YAGNI), 알려진 조건을 열거형으로 제한한다.
 */
public enum ApprovalCondition {
    /**
     * FR-2-4 원안(예산 소진율 조건). Module 4 도입 후 EXPENSE 결재라인은 금액 기준
     * (AMOUNT_GTE_1M/5M)으로 대체되어 지금은 어떤 시드 데이터도 이 조건을 쓰지 않지만,
     * 판정 메커니즘 자체는 유지한다 (docs/adr/ADR-011 참고).
     */
    BUDGET_80_EXCEEDED,
    /** 금액이 100만원 이상이면 포함 (docs/adr/ADR-011). */
    AMOUNT_GTE_1M,
    /** 금액이 500만원 이상이면 포함 (docs/adr/ADR-011). */
    AMOUNT_GTE_5M
}
