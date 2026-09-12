package egovframework.erp.approval;

import egovframework.erp.approval.domain.ActionType;
import egovframework.erp.approval.domain.ApprovalStepRules;
import egovframework.erp.approval.domain.ApprovalStepVO;
import egovframework.erp.approval.domain.ApproverLevel;
import egovframework.erp.approval.domain.DocumentStatus;
import egovframework.erp.approval.domain.DocumentStatusCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalStepRulesTest {

    @Test
    void 첫단계는_아무도_처리안했으면_대기상태다() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, null), step(2, 2, 20L, null));
        assertEquals(DocumentStatus.WAITING, DocumentStatusCalculator.calculate(steps));
    }

    @Test
    void 일부만_승인되면_진행중이다() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, ActionType.APPROVE), step(2, 2, 20L, null));
        assertEquals(DocumentStatus.IN_PROGRESS, DocumentStatusCalculator.calculate(steps));
    }

    @Test
    void 전부_승인되면_승인상태다() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, ActionType.APPROVE), step(2, 2, 20L, ActionType.APPROVE));
        assertEquals(DocumentStatus.APPROVED, DocumentStatusCalculator.calculate(steps));
    }

    @Test
    void 하나라도_반려되면_반려상태다() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, ActionType.APPROVE), step(2, 2, 20L, ActionType.REJECT));
        assertEquals(DocumentStatus.REJECTED, DocumentStatusCalculator.calculate(steps));
    }

    @Test
    void 결재라인이_없으면_자동승인이다() {
        assertEquals(DocumentStatus.APPROVED, DocumentStatusCalculator.calculate(List.of()));
    }

    @Test
    void 앞단계_승인전에는_다음단계_처리불가() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, null), step(2, 2, 20L, null));
        assertFalse(ApprovalStepRules.isActionable(steps, steps.get(1)));
        assertTrue(ApprovalStepRules.isActionable(steps, steps.get(0)));
    }

    @Test
    void 반려된_문서는_뒷단계_처리불가() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, ActionType.REJECT), step(2, 2, 20L, null));
        assertFalse(ApprovalStepRules.isActionable(steps, steps.get(1)));
    }

    @Test
    void 내가_처리할_단계를_찾는다() {
        List<ApprovalStepVO> steps = List.of(step(1, 1, 10L, ActionType.APPROVE), step(2, 2, 20L, null));
        Optional<Long> actionable = ApprovalStepRules.findActionableStepFor(steps, 20L);
        assertTrue(actionable.isPresent());
        assertEquals(2L, actionable.get());
    }

    private static ApprovalStepVO step(long id, int order, Long approverId, ActionType result) {
        ApprovalStepVO vo = new ApprovalStepVO();
        vo.setId(id);
        vo.setStepOrder(order);
        vo.setApproverLevel(ApproverLevel.TEAM_LEADER);
        vo.setApproverEmployeeId(approverId);
        vo.setActionResult(result);
        return vo;
    }
}
