package egovframework.erp.attendance.service.impl;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.attendance.domain.BusinessDayCounter;
import egovframework.erp.attendance.domain.VacationRequestVO;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import egovframework.erp.attendance.service.VacationBalanceService;
import egovframework.erp.attendance.service.VacationService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 근태 모듈이 전자결재(Module 2)의 공개 서비스를 직접 호출하는 정방향 의존이다
 * (docs/adr/ADR-008 — "나중에 만들어진 모듈이 먼저 만들어진 모듈을 호출"하는 일반적인 방향).
 */
@Service
public class VacationServiceImpl implements VacationService {

    private final VacationRequestMapper vacationRequestMapper;
    private final VacationBalanceService vacationBalanceService;
    private final ApprovalService approvalService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public VacationServiceImpl(VacationRequestMapper vacationRequestMapper,
                                VacationBalanceService vacationBalanceService,
                                ApprovalService approvalService,
                                EmployeeRepository employeeRepository) {
        this.vacationRequestMapper = vacationRequestMapper;
        this.vacationBalanceService = vacationBalanceService;
        this.approvalService = approvalService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Long requestVacation(Long employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BusinessException("휴가 시작일/종료일이 올바르지 않습니다.");
        }
        if (!employeeRepository.existsById(employeeId)) {
            throw new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId);
        }

        int days = BusinessDayCounter.count(startDate, endDate);
        int remaining = vacationBalanceService.getRemainingDays(employeeId);
        if (days > remaining) {
            // 상신 중인 신청까지 포함한 값이라 "안전하게 막는다"는 원칙 (docs/adr/ADR-009)
            throw new BusinessException("잔여 연차가 부족합니다. 신청일수=" + days + ", 잔여일수=" + remaining);
        }

        String title = "휴가신청(" + startDate + "~" + endDate + ")";
        Long documentId = approvalService.draftVacation(employeeId, title, reason, startDate, endDate);

        VacationRequestVO request = new VacationRequestVO(employeeId, startDate, endDate, days, reason, documentId);
        vacationRequestMapper.insertRequest(request);
        return request.getId();
    }

    @Override
    public List<VacationRequestVO> getMyRequests(Long employeeId) {
        return vacationRequestMapper.selectByEmployeeId(employeeId);
    }
}
