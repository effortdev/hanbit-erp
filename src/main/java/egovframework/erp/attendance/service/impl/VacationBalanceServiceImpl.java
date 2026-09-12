package egovframework.erp.attendance.service.impl;

import egovframework.erp.attendance.domain.VacationStatus;
import egovframework.erp.attendance.mapper.VacationPolicyMapper;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import egovframework.erp.attendance.service.VacationBalanceService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * 회계연도(매년 1/1) 기준 일괄 부여 방식 (docs/adr/ADR-009).
 */
@Service
public class VacationBalanceServiceImpl implements VacationBalanceService {

    private final EmployeeRepository employeeRepository;
    private final VacationPolicyMapper vacationPolicyMapper;
    private final VacationRequestMapper vacationRequestMapper;

    @Autowired
    public VacationBalanceServiceImpl(EmployeeRepository employeeRepository,
                                       VacationPolicyMapper vacationPolicyMapper,
                                       VacationRequestMapper vacationRequestMapper) {
        this.employeeRepository = employeeRepository;
        this.vacationPolicyMapper = vacationPolicyMapper;
        this.vacationRequestMapper = vacationRequestMapper;
    }

    @Override
    public int getAnnualDays(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId));

        int currentYear = LocalDate.now().getYear();
        LocalDate referenceJan1 = LocalDate.of(currentYear, 1, 1);
        // 연중 입사자(참조일 이후 입사)는 근속연수를 0년으로 바닥 처리한다 — 월할 비례 계산은
        // 배제하되(ADR-009), 입사 첫 해에 0일을 주는 비상식적인 결과는 피한다.
        int yearsOfService = Math.max(0, Period.between(employee.getHireDate(), referenceJan1).getYears());

        Integer annualDays = vacationPolicyMapper.selectAnnualDays(yearsOfService);
        if (annualDays == null) {
            throw new BusinessException("근속연수 " + yearsOfService + "년에 해당하는 연차 정책이 없습니다. 설정을 확인하세요.");
        }
        return annualDays;
    }

    @Override
    public int getRemainingDays(Long employeeId) {
        int annualDays = getAnnualDays(employeeId);
        int currentYear = LocalDate.now().getYear();
        int usedDays = vacationRequestMapper.sumDaysByStatus(employeeId, VacationStatus.APPROVED, currentYear);
        int pendingDays = vacationRequestMapper.sumDaysByStatus(employeeId, VacationStatus.PENDING, currentYear);
        return annualDays - usedDays - pendingDays;
    }
}
