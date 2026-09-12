package egovframework.erp.budget.service.impl;

import egovframework.erp.approval.service.BudgetThresholdPolicy;
import egovframework.erp.budget.domain.BudgetAllocationVO;
import egovframework.erp.budget.domain.BudgetExpenseStatus;
import egovframework.erp.budget.mapper.BudgetAllocationMapper;
import egovframework.erp.budget.mapper.BudgetExpenseRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Module 2의 {@link BudgetThresholdPolicy} 계약을 실제로 구현한다 (docs/adr/ADR-012).
 * <p>
 * 이 인터페이스는 Module 2 소속이라 Module 4의 {@code AccountCategory}를 알 수 없다
 * (의존 방향상 Module 2가 Module 4를 참조할 수 없음). 그래서 특정 계정과목이 아니라
 * 이 조직의 <b>전체 계정과목 합계</b> 기준으로 소진율을 판단한다 — 카테고리별 정밀도는
 * 떨어지지만, 인터페이스 시그니처를 Module 2 쪽에서 바꾸지 않고도 실제 데이터로 동작하는
 * 정직한 구현이다. 현재 어떤 {@code approval_line_rule}도 이 조건을 쓰지 않아(ADR-011로
 * EXPENSE가 금액 기준으로 대체됨) 실질적으로 호출되지 않지만, 계약은 실제로 지켜둔다.
 */
@Component
public class BudgetThresholdPolicyImpl implements BudgetThresholdPolicy {

    private static final BigDecimal WARNING_RATE = new BigDecimal("0.8");
    private static final List<BudgetExpenseStatus> COMMITTED_STATUSES =
            Arrays.asList(BudgetExpenseStatus.APPROVED, BudgetExpenseStatus.PENDING);

    private final BudgetAllocationMapper budgetAllocationMapper;
    private final BudgetExpenseRequestMapper budgetExpenseRequestMapper;

    @Autowired
    public BudgetThresholdPolicyImpl(BudgetAllocationMapper budgetAllocationMapper,
                                      BudgetExpenseRequestMapper budgetExpenseRequestMapper) {
        this.budgetAllocationMapper = budgetAllocationMapper;
        this.budgetExpenseRequestMapper = budgetExpenseRequestMapper;
    }

    @Override
    public boolean isExceeded(Long orgUnitId, BigDecimal amount) {
        int fiscalYear = LocalDate.now().getYear();
        List<BudgetAllocationVO> allocations = budgetAllocationMapper.selectByOrgUnit(orgUnitId, fiscalYear);
        if (allocations.isEmpty()) {
            return false; // 배정 자체가 없으면 판단할 근거가 없다 - 막지 않는다
        }

        BigDecimal totalAllocated = allocations.stream()
                .map(BudgetAllocationVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommitted = allocations.stream()
                .map(a -> budgetExpenseRequestMapper.sumAmountByStatuses(orgUnitId, a.getAccountCategory(), fiscalYear, COMMITTED_STATUSES))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAllocated.signum() == 0) {
            return false;
        }
        BigDecimal projectedRate = totalCommitted.add(amount).divide(totalAllocated, 4, RoundingMode.HALF_UP);
        return projectedRate.compareTo(WARNING_RATE) >= 0;
    }
}
