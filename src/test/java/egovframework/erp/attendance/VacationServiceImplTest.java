package egovframework.erp.attendance;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import egovframework.erp.attendance.service.VacationBalanceService;
import egovframework.erp.attendance.service.impl.VacationServiceImpl;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** docs/adr/ADR-009: 상신 시점에 잔여 연차 부족을 막는다 ("안전하게 막는다" 원칙). */
@ExtendWith(MockitoExtension.class)
class VacationServiceImplTest {

    @Mock
    private VacationRequestMapper vacationRequestMapper;
    @Mock
    private VacationBalanceService vacationBalanceService;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void 잔여연차보다_많이_신청하면_거부되고_결재문서도_생성되지_않는다() {
        VacationServiceImpl service = new VacationServiceImpl(vacationRequestMapper, vacationBalanceService, approvalService, employeeRepository);
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(vacationBalanceService.getRemainingDays(1L)).thenReturn(2);

        // 월요일~금요일 5일 신청, 잔여는 2일뿐
        LocalDate start = LocalDate.of(2026, 9, 14);
        LocalDate end = LocalDate.of(2026, 9, 18);

        assertThrows(BusinessException.class, () -> service.requestVacation(1L, start, end, "여행"));

        verify(approvalService, never()).draftVacation(any(), any(), any(), any(), any());
        verify(vacationRequestMapper, never()).insertRequest(any());
    }
}
