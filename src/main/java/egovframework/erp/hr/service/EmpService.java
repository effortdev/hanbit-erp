package egovframework.erp.hr.service;

import egovframework.erp.hr.domain.EmpHistoryVO;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.Position;

import java.time.LocalDate;
import java.util.List;

public interface EmpService {

    List<Employee> getEmployees();

    Employee getEmployee(Long id);

    Employee register(String name, Position position, Long orgUnitId, LocalDate hireDate);

    /** 발령(부서이동). Employee 갱신과 발령 이력 기록이 하나의 트랜잭션으로 처리된다 (docs/adr/ADR-003). */
    void transfer(Long employeeId, Long newOrgUnitId, String changedBy);

    /** 발령(승진). */
    void promote(Long employeeId, Position newPosition, String changedBy);

    List<EmpHistoryVO> getHistory(Long employeeId);
}
