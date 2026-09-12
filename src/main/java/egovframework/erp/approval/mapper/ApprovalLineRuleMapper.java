package egovframework.erp.approval.mapper;

import egovframework.erp.approval.domain.ApprovalLineRuleVO;
import egovframework.erp.approval.domain.DocumentType;

import java.util.List;

public interface ApprovalLineRuleMapper {

    List<ApprovalLineRuleVO> selectByDocumentType(DocumentType documentType);
}
