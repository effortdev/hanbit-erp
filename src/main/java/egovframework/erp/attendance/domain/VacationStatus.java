package egovframework.erp.attendance.domain;

/** attendance_vacation_request의 현재 상태. Module 2 이벤트 수신으로 갱신된다 (docs/adr/ADR-008). */
public enum VacationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
