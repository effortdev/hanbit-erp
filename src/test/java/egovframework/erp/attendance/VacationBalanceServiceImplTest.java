package egovframework.erp.attendance;

import egovframework.erp.attendance.domain.VacationStatus;
import egovframework.erp.attendance.mapper.VacationPolicyMapper;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import egovframework.erp.attendance.service.impl.VacationBalanceServiceImpl;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * docs/adr/ADR-009: 회계연도 기준 근속연수 계산, 정책 미존재 시 예외,
 * "부여 - 승인 - 상신중" 잔여 계산 검증.
 */
@ExtendWith(MockitoExtension.class)
class VacationBalanceServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private VacationPolicyMapper vacationPolicyMapper;
    @Mock
    private VacationRequestMapper vacationRequestMapper;

    @Test
    void 근속연수에_맞는_정책이_없으면_예외() {
        VacationBalanceServiceImpl service = new VacationBalanceServiceImpl(employeeRepository, vacationPolicyMapper, vacationRequestMapper);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(LocalDate.of(2000, 1, 1))));
        when(vacationPolicyMapper.selectAnnualDays(anyInt())).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.getAnnualDays(1L));
    }

    @Test
    void 연중입사자는_근속연수_0년으로_처리된다() {
        VacationBalanceServiceImpl service = new VacationBalanceServiceImpl(employeeRepository, vacationPolicyMapper, vacationRequestMapper);
        // 올해 하반기 입사자 -> 올해 1/1 시점 기준으로는 아직 입사 전 (음수) -> 0년으로 바닥 처리
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(LocalDate.now().plusMonths(1))));
        when(vacationPolicyMapper.selectAnnualDays(0)).thenReturn(11);

        assertEquals(11, service.getAnnualDays(1L));
    }

    @Test
    void 잔여연차는_부여일수에서_승인분과_상신중분을_뺀다() {
        VacationBalanceServiceImpl service = new VacationBalanceServiceImpl(employeeRepository, vacationPolicyMapper, vacationRequestMapper);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(LocalDate.of(2020, 1, 1))));
        lenient().when(vacationPolicyMapper.selectAnnualDays(anyInt())).thenReturn(15);
        int year = LocalDate.now().getYear();
        when(vacationRequestMapper.sumDaysByStatus(1L, VacationStatus.APPROVED, year)).thenReturn(5);
        when(vacationRequestMapper.sumDaysByStatus(1L, VacationStatus.PENDING, year)).thenReturn(3);

        assertEquals(7, service.getRemainingDays(1L)); // 15 - 5 - 3
    }

    private static Employee employee(LocalDate hireDate) {
        Employee employee = new Employee("테스트", Position.STAFF, 11L, hireDate);
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
