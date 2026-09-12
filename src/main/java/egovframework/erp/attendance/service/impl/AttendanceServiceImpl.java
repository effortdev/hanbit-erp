package egovframework.erp.attendance.service.impl;

import egovframework.erp.attendance.domain.AttendanceRecordVO;
import egovframework.erp.attendance.mapper.AttendanceRecordMapper;
import egovframework.erp.attendance.service.AttendanceService;
import egovframework.erp.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 서버 시각 기준 자가입력 출퇴근 기록 (docs/adr/ADR-010). */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    public AttendanceServiceImpl(AttendanceRecordMapper attendanceRecordMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
    }

    @Override
    @Transactional
    public void checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        if (attendanceRecordMapper.selectByEmployeeAndDate(employeeId, today) != null) {
            throw new BusinessException("이미 출근 처리되었습니다.");
        }
        attendanceRecordMapper.insertCheckIn(employeeId, today, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void checkOut(Long employeeId) {
        AttendanceRecordVO record = attendanceRecordMapper.selectByEmployeeAndDate(employeeId, LocalDate.now());
        if (record == null) {
            throw new BusinessException("출근 기록이 없어 퇴근 처리를 할 수 없습니다.");
        }
        if (record.getCheckOutTime() != null) {
            throw new BusinessException("이미 퇴근 처리되었습니다.");
        }
        attendanceRecordMapper.updateCheckOut(record.getId(), LocalDateTime.now());
    }

    @Override
    public List<AttendanceRecordVO> getMyRecords(Long employeeId) {
        return attendanceRecordMapper.selectByEmployeeId(employeeId);
    }

    @Override
    public AttendanceRecordVO getTodayRecord(Long employeeId) {
        return attendanceRecordMapper.selectByEmployeeAndDate(employeeId, LocalDate.now());
    }
}
