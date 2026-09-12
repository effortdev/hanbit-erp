package egovframework.erp.hr.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * 사원. 등록/조회/수정이 단건 위주인 단순 CRUD라 JPA로 관리한다 (docs/adr/ADR-003 참고).
 * 조직(org_unit)은 MyBatis로 관리되는 별도 도메인이라, 여기서는 연관관계 매핑 대신
 * orgUnitId만 값으로 들고 있는다 (JPA/MyBatis 경계를 테이블 FK로만 연결).
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    protected Employee() {
        // JPA
    }

    public Employee(String name, Position position, Long orgUnitId, LocalDate hireDate) {
        this.name = name;
        this.position = position;
        this.orgUnitId = orgUnitId;
        this.hireDate = hireDate;
        this.status = EmployeeStatus.ACTIVE;
    }

    /** 발령(부서이동): 소속 조직만 변경. 이력 기록은 EmpServiceImpl에서 별도 트랜잭션 참여자로 처리한다. */
    public void changeOrgUnit(Long newOrgUnitId) {
        this.orgUnitId = newOrgUnitId;
    }

    /** 발령(승진): 직급만 변경. */
    public void promote(Position newPosition) {
        this.position = newPosition;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public EmployeeStatus getStatus() {
        return status;
    }
}
