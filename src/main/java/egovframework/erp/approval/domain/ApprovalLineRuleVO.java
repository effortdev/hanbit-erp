package egovframework.erp.approval.domain;

/** "몇 단계를 거치는지"를 정의하는 규칙 (docs/adr/ADR-006). "누가"는 여기 없다 — 실시간 조직도 조회로 채운다. */
public class ApprovalLineRuleVO {

    private Long id;
    private DocumentType documentType;
    private int stepOrder;
    private ApproverLevel approverLevel;
    private ApprovalCondition conditionExpr;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
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

    public ApprovalCondition getConditionExpr() {
        return conditionExpr;
    }

    public void setConditionExpr(ApprovalCondition conditionExpr) {
        this.conditionExpr = conditionExpr;
    }
}
