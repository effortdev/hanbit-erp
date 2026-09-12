package egovframework.erp.inventory.mapper;

import egovframework.erp.inventory.domain.PurchaseRequestStatus;
import egovframework.erp.inventory.domain.PurchaseRequestVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PurchaseRequestMapper {

    void insertRequest(PurchaseRequestVO request);

    PurchaseRequestVO selectById(Long id);

    List<PurchaseRequestVO> selectByEmployeeId(Long employeeId);

    /** ADR-008/ADR-015와 동일한 패턴: 현재 상태를 나타내는 컬럼이라 update가 맞다 (append-only 아님). */
    void updateStatusByApprovalDocumentId(@Param("approvalDocumentId") Long approvalDocumentId,
                                           @Param("status") PurchaseRequestStatus status);

    void updateStatus(@Param("id") Long id, @Param("status") PurchaseRequestStatus status);
}
