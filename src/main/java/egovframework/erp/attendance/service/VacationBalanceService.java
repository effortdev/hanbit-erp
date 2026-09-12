package egovframework.erp.attendance.service;

/** FR-3-3: 사원별 연차 잔여일수 조회 (docs/adr/ADR-009). */
public interface VacationBalanceService {

    int getAnnualDays(Long employeeId);

    /** 부여일수 - 승인완료 사용일수 - 상신중 임시차감분 (올해 기준). */
    int getRemainingDays(Long employeeId);
}
