package egovframework.erp.approval.domain;

/**
 * approval_line_rule.condition_expr에 들어갈 수 있는 조건 식별자. 완전한 조건식 엔진을
 * 두는 대신(YAGNI), 알려진 조건을 열거형으로 제한한다.
 * <p>
 * FR-2-4 원안의 예산 소진율 조건(BUDGET_80_EXCEEDED)은 ADR-011로 EXPENSE 결재라인이
 * 금액 기준으로 대체되며 실제 소비처가 없어져 폐기했다 (docs/adr/ADR-012 참고).
 */
public enum ApprovalCondition {
    /** 금액이 100만원 이상이면 포함 (docs/adr/ADR-011). */
    AMOUNT_GTE_1M,
    /** 금액이 500만원 이상이면 포함 (docs/adr/ADR-011). */
    AMOUNT_GTE_5M
}
