package egovframework.erp.budget.mapper;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetAllocationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BudgetAllocationMapper {

    void insertAllocation(BudgetAllocationVO allocation);

    BudgetAllocationVO selectOne(@Param("orgUnitId") Long orgUnitId,
                                  @Param("accountCategory") AccountCategory accountCategory,
                                  @Param("fiscalYear") int fiscalYear);

    List<BudgetAllocationVO> selectByOrgUnit(@Param("orgUnitId") Long orgUnitId, @Param("fiscalYear") int fiscalYear);
}
