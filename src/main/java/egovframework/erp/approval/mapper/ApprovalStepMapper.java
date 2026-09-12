package egovframework.erp.approval.mapper;

import egovframework.erp.approval.domain.ApprovalStepVO;

import java.util.List;

public interface ApprovalStepMapper {

    void insertStep(ApprovalStepVO step);

    /** step_order 오름차순. 결재자명/처리 결과(action)까지 조인해서 반환한다. */
    List<ApprovalStepVO> selectByDocumentId(Long documentId);
}
