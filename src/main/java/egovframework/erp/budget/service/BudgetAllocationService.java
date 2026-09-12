package egovframework.erp.budget.service;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetUsageVO;

import java.math.BigDecimal;
import java.util.List;

/** FR-4-1(배정/조회), FR-4-3(소진율 조회) — docs/adr/ADR-013. */
public interface BudgetAllocationService {

    void allocate(Long orgUnitId, AccountCategory accountCategory, int fiscalYear, BigDecimal amount);

    /** 해당 조직의 올해 계정과목별 배정/사용/잔여/소진율. */
    List<BudgetUsageVO> getUsage(Long orgUnitId);
}
