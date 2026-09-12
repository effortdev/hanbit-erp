package egovframework.erp.hr.repository;

import egovframework.erp.hr.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 사원 기본 CRUD 전용 JPA 리포지토리 (docs/adr/ADR-003 — 단순 CRUD 예외 적용).
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByOrgUnitId(Long orgUnitId);
}
