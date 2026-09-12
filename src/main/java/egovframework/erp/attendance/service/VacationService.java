package egovframework.erp.attendance.service;

import egovframework.erp.attendance.domain.VacationRequestVO;

import java.time.LocalDate;
import java.util.List;

/** FR-3-2: 휴가신청은 전자결재를 통해 상신된다 (docs/adr/ADR-008). */
public interface VacationService {

    /** @return 생성된 attendance_vacation_request의 id */
    Long requestVacation(Long employeeId, LocalDate startDate, LocalDate endDate, String reason);

    List<VacationRequestVO> getMyRequests(Long employeeId);
}
