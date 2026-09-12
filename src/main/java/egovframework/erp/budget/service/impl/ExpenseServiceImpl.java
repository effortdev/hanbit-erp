package egovframework.erp.budget.service.impl;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetAllocationVO;
import egovframework.erp.budget.domain.BudgetExpenseRequestVO;
import egovframework.erp.budget.domain.BudgetExpenseStatus;
import egovframework.erp.budget.mapper.BudgetAllocationMapper;
import egovframework.erp.budget.mapper.BudgetExpenseRequestMapper;
import egovframework.erp.budget.service.ExpenseService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 근태 모듈(VacationServiceImpl)과 같은 구조: 자체 사전 검증 후 Module 2의 공개 서비스를
 * 직접 호출하는 정방향 의존이다 (docs/adr/ADR-008 패턴 재사용, docs/adr/ADR-012).
 */
@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final BigDecimal FIVE_MILLION = new BigDecimal("5000000");
    private static final BigDecimal FULL_RATE = BigDecimal.ONE;
    private static final List<BudgetExpenseStatus> COMMITTED_STATUSES =
            Arrays.asList(BudgetExpenseStatus.APPROVED, BudgetExpenseStatus.PENDING);

    private final BudgetExpenseRequestMapper budgetExpenseRequestMapper;
    private final BudgetAllocationMapper budgetAllocationMapper;
    private final ApprovalService approvalService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ExpenseServiceImpl(BudgetExpenseRequestMapper budgetExpenseRequestMapper,
                               BudgetAllocationMapper budgetAllocationMapper,
                               ApprovalService approvalService,
                               EmployeeRepository employeeRepository) {
        this.budgetExpenseRequestMapper = budgetExpenseRequestMapper;
        this.budgetAllocationMapper = budgetAllocationMapper;
        this.approvalService = approvalService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Long requestExpense(Long employeeId, String title, String content, AccountCategory accountCategory,
                                BigDecimal amount, String exceptionReason) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("지출 금액은 0보다 커야 합니다.");
        }
        Employee drafter = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId));
        Long orgUnitId = drafter.getOrgUnitId();
        int fiscalYear = LocalDate.now().getYear();

        BudgetAllocationVO allocation = budgetAllocationMapper.selectOne(orgUnitId, accountCategory, fiscalYear);
        if (allocation == null) {
            throw new BusinessException(accountCategory.getLabel() + " 항목에 배정된 예산이 없습니다. 회계팀에 문의하세요.");
        }

        BigDecimal committedSoFar = budgetExpenseRequestMapper.sumAmountByStatuses(
                orgUnitId, accountCategory, fiscalYear, COMMITTED_STATUSES);
        BigDecimal projected = committedSoFar.add(amount);
        BigDecimal projectedRate = projected.divide(allocation.getAmount(), 4, RoundingMode.HALF_UP);

        if (projectedRate.compareTo(FULL_RATE) >= 0) {
            boolean isCeoTier = amount.compareTo(FIVE_MILLION) >= 0; // ADR-011과 동일 기준선
            if (!isCeoTier) {
                throw new BusinessException("예산을 초과하여 상신할 수 없습니다. (예상 소진율 "
                        + projectedRate.multiply(new BigDecimal("100")) + "%)");
            }
            if (exceptionReason == null || exceptionReason.isBlank()) {
                throw new BusinessException("예산 초과 상태에서 500만원 이상 지출을 상신하려면 예외 승인 사유를 입력해야 합니다.");
            }
        } else {
            exceptionReason = null; // 초과 상태가 아니면 예외 사유는 의미가 없으므로 저장하지 않는다
        }

        Long documentId = approvalService.draftExpense(employeeId, title, content, amount);

        BudgetExpenseRequestVO request = new BudgetExpenseRequestVO(
                employeeId, orgUnitId, accountCategory, amount, content, exceptionReason, documentId);
        budgetExpenseRequestMapper.insertRequest(request);
        return request.getId();
    }

    @Override
    public List<BudgetExpenseRequestVO> getMyRequests(Long employeeId) {
        return budgetExpenseRequestMapper.selectByEmployeeId(employeeId);
    }
}
