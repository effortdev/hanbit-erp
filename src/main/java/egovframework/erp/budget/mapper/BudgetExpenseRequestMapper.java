package egovframework.erp.budget.mapper;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetExpenseRequestVO;
import egovframework.erp.budget.domain.BudgetExpenseStatus;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BudgetExpenseRequestMapper {

    void insertRequest(BudgetExpenseRequestVO request);

    List<BudgetExpenseRequestVO> selectByEmployeeId(Long employeeId);

    /** ADR-012: 승인+상신중 합계로 "커밋된" 사용액을 계산한다 (statuses에 PENDING/APPROVED를 함께 넘김). */
    BigDecimal sumAmountByStatuses(@Param("orgUnitId") Long orgUnitId,
                                              @Param("accountCategory") AccountCategory accountCategory,
                                              @Param("fiscalYear") int fiscalYear,
                                              @Param("statuses") List<BudgetExpenseStatus> statuses);

    /** ADR-013: 분기별 실적 — created_at에서 파생, 별도 컬럼 없음. */
    List<Map<String, Object>> sumByQuarter(@Param("orgUnitId") Long orgUnitId,
                                            @Param("accountCategory") AccountCategory accountCategory,
                                            @Param("fiscalYear") int fiscalYear);

    /** ADR-008과 동일한 패턴: Module 2 이벤트 수신 시 현재 상태를 갱신한다 (append-only가 아님). */
    void updateStatusByApprovalDocumentId(@Param("approvalDocumentId") Long approvalDocumentId,
                                           @Param("status") BudgetExpenseStatus status);
}
