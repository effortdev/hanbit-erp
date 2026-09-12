package egovframework.erp.hr.domain;

/**
 * 직급. 전자결재 모듈(Module 2)의 결재라인 자동 구성이 이 순서(오름차순)를 기준으로 삼는다.
 */
public enum Position {
    STAFF("사원"),
    ASSISTANT_MANAGER("대리"),
    MANAGER("과장"),
    DEPUTY_GENERAL_MANAGER("차장"),
    TEAM_LEADER("팀장"),
    DIVISION_HEAD("본부장"),
    CEO("대표이사");

    private final String label;

    Position(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
