package egovframework.erp.approval.mapper;

import egovframework.erp.approval.domain.ApprovalDocumentVO;

import java.util.List;

public interface ApprovalDocumentMapper {

    void insertDocument(ApprovalDocumentVO document);

    ApprovalDocumentVO selectById(Long id);

    List<ApprovalDocumentVO> selectByDrafterId(Long drafterId);

    /** 이 사원이 결재라인 어딘가에 포함된 문서 후보 목록 (실제로 "지금 내 차례"인지는 서비스 계층에서 판단). */
    List<ApprovalDocumentVO> selectByApproverCandidate(Long employeeId);
}
