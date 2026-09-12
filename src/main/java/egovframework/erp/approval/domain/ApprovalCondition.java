package egovframework.erp.approval.domain;

/**
 * approval_line_rule.condition_expr에 들어갈 수 있는 조건 식별자. 완전한 조건식 엔진을
 * 두는 대신(YAGNI — 현재 요구사항은 조건이 하나뿐), 알려진 조건을 열거형으로 제한한다.
 */
public enum ApprovalCondition {
    /** FR-2-4: 지출 금액이 부서 예산의 80%를 초과하면 이 단계(대표이사)를 포함한다. */
    BUDGET_80_EXCEEDED
}
