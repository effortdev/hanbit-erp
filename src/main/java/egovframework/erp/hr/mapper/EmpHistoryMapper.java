package egovframework.erp.hr.mapper;

import egovframework.erp.hr.domain.EmpHistoryVO;

import java.util.List;

/**
 * 발령 이력 매퍼. 의도적으로 insert/select만 제공하고 update/delete는 두지 않는다
 * (append-only 원칙 — docs/adr/ADR-003 참고).
 */
public interface EmpHistoryMapper {

    void insertHistory(EmpHistoryVO history);

    List<EmpHistoryVO> selectByEmployeeId(Long employeeId);
}
