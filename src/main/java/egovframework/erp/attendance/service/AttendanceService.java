package egovframework.erp.attendance.service;

import egovframework.erp.attendance.domain.AttendanceRecordVO;

import java.util.List;

/** FR-3-1: 출퇴근 기록 등록/조회 (docs/adr/ADR-010). */
public interface AttendanceService {

    void checkIn(Long employeeId);

    void checkOut(Long employeeId);

    List<AttendanceRecordVO> getMyRecords(Long employeeId);

    AttendanceRecordVO getTodayRecord(Long employeeId);
}
