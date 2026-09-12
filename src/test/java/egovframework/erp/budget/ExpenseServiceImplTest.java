package egovframework.erp.budget;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetAllocationVO;
import egovframework.erp.budget.mapper.BudgetAllocationMapper;
import egovframework.erp.budget.mapper.BudgetExpenseRequestMapper;
import egovframework.erp.budget.service.impl.ExpenseServiceImpl;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** docs/adr/ADR-012: 80% 경고(통과)/100% 차단/고액 예외승인 경로 검증. */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private BudgetExpenseRequestMapper budgetExpenseRequestMapper;
    @Mock
    private BudgetAllocationMapper budgetAllocationMapper;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private EmployeeRepository employeeRepository;

    private ExpenseServiceImpl service() {
        return new ExpenseServiceImpl(budgetExpenseRequestMapper, budgetAllocationMapper, approvalService, employeeRepository);
    }

    @Test
    void 배정이_없으면_상신할_수_없다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(11L)));
        when(budgetAllocationMapper.selectOne(11L, AccountCategory.TRAVEL, LocalDate.now().getYear())).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                service().requestExpense(1L, "출장", "부산 출장", AccountCategory.TRAVEL, new BigDecimal("500000"), null));
    }

    @Test
    void 소진율_80퍼센트대는_경고없이_통과한다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(11L)));
        allocationOf(1000000);
        committedOf(700000); // 70만 + 10만 신청 = 80만 -> 80%

        service().requestExpense(1L, "소모품", "사무용품", AccountCategory.SUPPLIES, new BigDecimal("100000"), null);

        verify(approvalService).draftExpense(any(), any(), any(), any());
        verify(budgetExpenseRequestMapper).insertRequest(any());
    }

    @Test
    void 소진율_100퍼센트_초과_소액건은_차단된다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(11L)));
        allocationOf(1000000);
        committedOf(950000); // 95만 + 10만 = 105만 -> 100% 초과

        assertThrows(BusinessException.class, () ->
                service().requestExpense(1L, "소모품", "사무용품", AccountCategory.SUPPLIES, new BigDecimal("100000"), null));

        verify(approvalService, never()).draftExpense(any(), any(), any(), any());
    }

    @Test
    void 소진율_100퍼센트_초과_고액건은_예외사유_없으면_차단된다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(11L)));
        allocationOf(1000000);
        committedOf(950000);

        assertThrows(BusinessException.class, () ->
                service().requestExpense(1L, "장비", "고가장비", AccountCategory.SUPPLIES, new BigDecimal("6000000"), null));
    }

    @Test
    void 소진율_100퍼센트_초과_고액건도_예외사유가_있으면_통과한다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(11L)));
        allocationOf(1000000);
        committedOf(950000);

        service().requestExpense(1L, "장비", "고가장비", AccountCategory.SUPPLIES,
                new BigDecimal("6000000"), "긴급 설비 교체 승인 요청");

        verify(approvalService).draftExpense(any(), any(), any(), any());
    }

    private void allocationOf(long amount) {
        BudgetAllocationVO allocation = new BudgetAllocationVO();
        allocation.setAmount(new BigDecimal(amount));
        lenient().when(budgetAllocationMapper.selectOne(eq(11L), any(), eq(LocalDate.now().getYear()))).thenReturn(allocation);
    }

    private void committedOf(long amount) {
        lenient().when(budgetExpenseRequestMapper.sumAmountByStatuses(eq(11L), any(), eq(LocalDate.now().getYear()), anyList()))
                .thenReturn(new BigDecimal(amount));
    }

    private static Employee employee(Long orgUnitId) {
        Employee employee = new Employee("테스트", Position.STAFF, orgUnitId, LocalDate.of(2020, 1, 1));
        try {
            Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, 1L);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return employee;
    }
}
