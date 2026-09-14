package egovframework.erp.hr.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.EmpHistoryVO;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.mapper.EmpHistoryMapper;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.hr.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 사원 CRUD는 JPA(EmployeeRepository), 발령이력은 MyBatis(EmpHistoryMapper)로 처리한다.
 * transfer/promote는 두 기술에 걸친 단일 트랜잭션의 대표 사례다 (docs/adr/ADR-003 참고).
 */
@Service
public class EmpServiceImpl implements EmpService {

    private final EmployeeRepository employeeRepository;
    private final EmpHistoryMapper empHistoryMapper;
    private final OrgMapper orgMapper;

    @Autowired
    public EmpServiceImpl(EmployeeRepository employeeRepository, EmpHistoryMapper empHistoryMapper, OrgMapper orgMapper) {
        this.employeeRepository = employeeRepository;
        this.empHistoryMapper = empHistoryMapper;
        this.orgMapper = orgMapper;
    }

    @Override
    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. id=" + id));
    }

    @Override
    @Transactional
    public Employee register(String name, Position position, Long orgUnitId, LocalDate hireDate) {
        OrgUnitVO orgUnit = orgMapper.selectOrgUnit(orgUnitId);
        if (orgUnit == null) {
            throw new BusinessException("존재하지 않는 소속 조직입니다. orgUnitId=" + orgUnitId);
        }
        return employeeRepository.save(new Employee(name, position, orgUnitId, hireDate));
    }

    @Override
    @Transactional
    public void transfer(Long employeeId, Long newOrgUnitId, String changedBy, Position actorPosition) {
        requireLeaderOrAbove(actorPosition, "발령(부서이동)");
        Employee employee = getEmployee(employeeId);
        OrgUnitVO newOrgUnit = orgMapper.selectOrgUnit(newOrgUnitId);
        if (newOrgUnit == null) {
            throw new BusinessException("존재하지 않는 소속 조직입니다. orgUnitId=" + newOrgUnitId);
        }
        Long beforeOrgUnitId = employee.getOrgUnitId();
        employee.changeOrgUnit(newOrgUnitId); // JPA: dirty checking으로 UPDATE
        empHistoryMapper.insertHistory(
                EmpHistoryVO.transfer(employeeId, beforeOrgUnitId, newOrgUnitId, changedBy)); // MyBatis: 명시적 INSERT
        // 두 작업은 같은 JpaTransactionManager 트랜잭션에 참여해 원자적으로 커밋/롤백된다.
    }

    @Override
    @Transactional
    public void promote(Long employeeId, Position newPosition, String changedBy, Position actorPosition) {
        requireLeaderOrAbove(actorPosition, "발령(승진)");
        Employee employee = getEmployee(employeeId);
        Position beforePosition = employee.getPosition();
        employee.promote(newPosition);
        empHistoryMapper.insertHistory(
                EmpHistoryVO.promotion(employeeId, beforePosition, newPosition, changedBy));
    }

    /**
     * 발령 처리 권한 검증 (docs/adr/ADR-019). 팀장/본부장/대표이사만 발령을 처리할 수 있다.
     * Position enum은 이미 결재라인 자동구성(ADR-006)이 전제하는 오름차순 직급 순서를 갖고
     * 있어, 같은 순서를 그대로 재사용한다 — 별도의 역할(Role) 체계를 새로 만들지 않는다.
     */
    private void requireLeaderOrAbove(Position actorPosition, String action) {
        if (actorPosition.ordinal() < Position.TEAM_LEADER.ordinal()) {
            throw new BusinessException(action + "는 팀장급 이상만 처리할 수 있습니다.");
        }
    }

    @Override
    public List<EmpHistoryVO> getHistory(Long employeeId) {
        return empHistoryMapper.selectByEmployeeId(employeeId);
    }
}
