package egovframework.erp.attendance.mapper;

import egovframework.erp.attendance.domain.AttendanceRecordVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordMapper {

    AttendanceRecordVO selectByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    List<AttendanceRecordVO> selectByEmployeeId(Long employeeId);

    void insertCheckIn(@Param("employeeId") Long employeeId,
                        @Param("workDate") LocalDate workDate,
                        @Param("checkInTime") LocalDateTime checkInTime);

    void updateCheckOut(@Param("id") Long id, @Param("checkOutTime") LocalDateTime checkOutTime);
}
