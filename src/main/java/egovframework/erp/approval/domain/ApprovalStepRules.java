package egovframework.erp.approval.domain;

import java.util.List;
import java.util.Optional;

/**
 * "지금 이 단계를 처리할 수 있는가"를 판단하는 규칙. 서비스 계층(act() 검증)과 화면
 * (승인/반려 버튼 노출 여부)이 같은 판단 로직을 공유하도록 한 곳에 모았다.
 */
public final class ApprovalStepRules {

    private ApprovalStepRules() {
    }

    public static boolean isActionable(List<ApprovalStepVO> steps, ApprovalStepVO target) {
        if (target.getActionResult() != null) {
            return false;
        }
        boolean anyRejectedSoFar = steps.stream().anyMatch(s -> s.getActionResult() == ActionType.REJECT);
        if (anyRejectedSoFar) {
            return false;
        }
        return steps.stream()
                .filter(s -> s.getStepOrder() < target.getStepOrder())
                .allMatch(s -> s.getActionResult() == ActionType.APPROVE);
    }

    /** 이 사원이 지금 처리해야 할 단계가 있으면 그 stepId를 반환한다 (없으면 empty). */
    public static Optional<Long> findActionableStepFor(List<ApprovalStepVO> steps, Long employeeId) {
        return steps.stream()
                .filter(s -> s.getApproverEmployeeId().equals(employeeId))
                .filter(s -> isActionable(steps, s))
                .map(ApprovalStepVO::getId)
                .findFirst();
    }
}
