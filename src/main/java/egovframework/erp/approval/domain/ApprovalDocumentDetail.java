package egovframework.erp.approval.domain;

import java.util.List;

/** 문서 상세 화면에 필요한 것을 한 번에 담아 반환하는 조회 전용 뷰. */
public class ApprovalDocumentDetail {

    private final ApprovalDocumentVO document;
    private final List<ApprovalStepVO> steps;
    private final DocumentStatus status;
    private final Long actionableStepId;

    public ApprovalDocumentDetail(ApprovalDocumentVO document, List<ApprovalStepVO> steps, Long viewerId) {
        this.document = document;
        this.steps = steps;
        this.status = DocumentStatusCalculator.calculate(steps);
        this.actionableStepId = ApprovalStepRules.findActionableStepFor(steps, viewerId).orElse(null);
    }

    public ApprovalDocumentVO getDocument() {
        return document;
    }

    public List<ApprovalStepVO> getSteps() {
        return steps;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    /** 로그인 사용자가 지금 처리할 수 있는 단계의 id (없으면 null — 화면에서 승인/반려 버튼 노출 여부 판단용). */
    public Long getActionableStepId() {
        return actionableStepId;
    }
}
