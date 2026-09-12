package egovframework.erp.approval.domain;

/**
 * 결재라인 정의. 문서 상신 시점에 1회 계산되어 고정되며, 이후 절대 수정되지 않는다
 * (docs/adr/ADR-006 — 조직 개편이 있어도 이미 상신된 문서의 결재라인은 바뀌지 않는다).
 */
public class ApprovalStepVO {

    private Long id;
    private Long documentId;
    private int stepOrder;
    private ApproverLevel approverLevel;
    private Long approverEmployeeId;

    // 화면 표시용 부가 정보 (조인 결과)
    private String approverName;
    private ActionType actionResult; // 아직 처리 안 됐으면 null
    private String actionComment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public ApproverLevel getApproverLevel() {
        return approverLevel;
    }

    public void setApproverLevel(ApproverLevel approverLevel) {
        this.approverLevel = approverLevel;
    }

    public Long getApproverEmployeeId() {
        return approverEmployeeId;
    }

    public void setApproverEmployeeId(Long approverEmployeeId) {
        this.approverEmployeeId = approverEmployeeId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public ActionType getActionResult() {
        return actionResult;
    }

    public void setActionResult(ActionType actionResult) {
        this.actionResult = actionResult;
    }

    public String getActionComment() {
        return actionComment;
    }

    public void setActionComment(String actionComment) {
        this.actionComment = actionComment;
    }
}
