package egovframework.erp.approval.domain;

/** 결재 단계의 결재자 레벨. "누가"는 이 레벨을 Module 1 조직도로 실시간 조회해 판별한다 (docs/adr/ADR-006). */
public enum ApproverLevel {
    TEAM_LEADER,
    DIVISION_HEAD,
    CEO
}
