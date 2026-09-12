package egovframework.erp.hr.repository;

import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.EmployeeStatus;
import egovframework.erp.hr.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 사원 기본 CRUD 전용 JPA 리포지토리 (docs/adr/ADR-003 — 단순 CRUD 예외 적용).
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByOrgUnitId(Long orgUnitId);

    /** Module 2 결재라인 판별: 대표이사는 조직 리더가 아니라 단일 사원 조회로 특정한다 (docs/adr/ADR-006). */
    List<Employee> findByPositionAndStatus(Position position, EmployeeStatus status);
}
