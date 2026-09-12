package egovframework.erp.approval.domain;

/**
 * 화면에 보여줄 문서 상태 (FR-2-3: 대기/진행중/승인/반려). 별도 컬럼으로 저장하지 않고
 * approval_step + approval_action으로부터 매번 파생시킨다 — append-only 로그가 유일한 진실
 * 소스이고, 상태 컬럼과 로그가 어긋나는 정합성 문제 자체가 생기지 않는다.
 */
public enum DocumentStatus {
    WAITING("대기"),
    IN_PROGRESS("진행중"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String label;

    DocumentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
