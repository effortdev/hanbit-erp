package egovframework.erp.approval.domain;

import java.util.List;

/** approval_step 목록(액션 결과 포함)으로부터 문서의 표시 상태를 계산한다. */
public final class DocumentStatusCalculator {

    private DocumentStatusCalculator() {
    }

    public static DocumentStatus calculate(List<ApprovalStepVO> steps) {
        if (steps.isEmpty()) {
            // 결재라인 전 단계가 자기결재로 스킵된 경우 (docs/adr/ADR-006)
            return DocumentStatus.APPROVED;
        }
        boolean anyRejected = steps.stream().anyMatch(s -> s.getActionResult() == ActionType.REJECT);
        if (anyRejected) {
            return DocumentStatus.REJECTED;
        }
        boolean allApproved = steps.stream().allMatch(s -> s.getActionResult() == ActionType.APPROVE);
        if (allApproved) {
            return DocumentStatus.APPROVED;
        }
        boolean noneActedYet = steps.stream().allMatch(s -> s.getActionResult() == null);
        return noneActedYet ? DocumentStatus.WAITING : DocumentStatus.IN_PROGRESS;
    }
}
