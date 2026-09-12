package egovframework.erp.approval.mapper;

import egovframework.erp.approval.domain.ApprovalActionVO;

/** 승인/반려 행위 로그. insert/select만 두고 update/delete는 두지 않는다 (append-only, NFR-2-3). */
public interface ApprovalActionMapper {

    void insertAction(ApprovalActionVO action);

    ApprovalActionVO selectByStepId(Long stepId);
}
