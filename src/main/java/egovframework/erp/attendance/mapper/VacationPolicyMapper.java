package egovframework.erp.attendance.mapper;

/** 근속연수별 연차 부여일수 규칙 조회 (docs/adr/ADR-009). */
public interface VacationPolicyMapper {

    /** 해당 근속연수에 맞는 부여일수. 규칙이 없으면 null(호출측에서 BusinessException 처리). */
    Integer selectAnnualDays(int yearsOfService);
}
