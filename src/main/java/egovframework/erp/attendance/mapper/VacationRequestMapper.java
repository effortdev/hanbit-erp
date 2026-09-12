package egovframework.erp.attendance.mapper;

import egovframework.erp.attendance.domain.VacationRequestVO;
import egovframework.erp.attendance.domain.VacationStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VacationRequestMapper {

    void insertRequest(VacationRequestVO request);

    List<VacationRequestVO> selectByEmployeeId(Long employeeId);

    /** ADR-009: 특정 연도·상태의 사용일수 합계 (신청 시점 스냅샷인 days 컬럼을 합산). */
    int sumDaysByStatus(@Param("employeeId") Long employeeId,
                         @Param("status") VacationStatus status,
                         @Param("year") int year);

    /** ADR-008: Module 2의 승인/반려 이벤트를 받아 상태를 갱신한다. append-only가 아니라
     * 현재 상태를 나타내는 컬럼이라 update가 맞다 (employee.position과 같은 성격). */
    void updateStatusByApprovalDocumentId(@Param("approvalDocumentId") Long approvalDocumentId,
                                           @Param("status") VacationStatus status);
}
