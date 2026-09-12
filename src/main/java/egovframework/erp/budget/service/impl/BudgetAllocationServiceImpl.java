package egovframework.erp.budget.service.impl;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetAllocationVO;
import egovframework.erp.budget.domain.BudgetExpenseStatus;
import egovframework.erp.budget.domain.BudgetUsageVO;
import egovframework.erp.budget.mapper.BudgetAllocationMapper;
import egovframework.erp.budget.mapper.BudgetExpenseRequestMapper;
import egovframework.erp.budget.service.BudgetAllocationService;
import egovframework.erp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class BudgetAllocationServiceImpl implements BudgetAllocationService {

    private static final List<BudgetExpenseStatus> COMMITTED_STATUSES =
            Arrays.asList(BudgetExpenseStatus.APPROVED, BudgetExpenseStatus.PENDING);

    private final BudgetAllocationMapper budgetAllocationMapper;
    private final BudgetExpenseRequestMapper budgetExpenseRequestMapper;

    @Autowired
    public BudgetAllocationServiceImpl(BudgetAllocationMapper budgetAllocationMapper,
                                        BudgetExpenseRequestMapper budgetExpenseRequestMapper) {
        this.budgetAllocationMapper = budgetAllocationMapper;
        this.budgetExpenseRequestMapper = budgetExpenseRequestMapper;
    }

    @Override
    @Transactional
    public void allocate(Long orgUnitId, AccountCategory accountCategory, int fiscalYear, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("배정액은 0보다 커야 합니다.");
        }
        if (budgetAllocationMapper.selectOne(orgUnitId, accountCategory, fiscalYear) != null) {
            throw new BusinessException("이미 해당 조직/계정과목/연도에 배정된 예산이 있습니다.");
        }
        BudgetAllocationVO allocation = new BudgetAllocationVO();
        allocation.setOrgUnitId(orgUnitId);
        allocation.setAccountCategory(accountCategory);
        allocation.setFiscalYear(fiscalYear);
        allocation.setAmount(amount);
        budgetAllocationMapper.insertAllocation(allocation);
    }

    @Override
    public List<BudgetUsageVO> getUsage(Long orgUnitId) {
        int currentYear = LocalDate.now().getYear();
        List<BudgetAllocationVO> allocations = budgetAllocationMapper.selectByOrgUnit(orgUnitId, currentYear);
        return allocations.stream()
                .map(a -> new BudgetUsageVO(a.getAccountCategory(), a.getAmount(),
                        budgetExpenseRequestMapper.sumAmountByStatuses(orgUnitId, a.getAccountCategory(), currentYear,
                                COMMITTED_STATUSES)))
                .toList();
    }
}
