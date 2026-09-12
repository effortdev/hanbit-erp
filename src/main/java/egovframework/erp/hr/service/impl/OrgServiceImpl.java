package egovframework.erp.hr.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.hr.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrgServiceImpl implements OrgService {

    private final OrgMapper orgMapper;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public OrgServiceImpl(OrgMapper orgMapper, EmployeeRepository employeeRepository) {
        this.orgMapper = orgMapper;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<OrgUnitVO> getOrgTree() {
        return orgMapper.selectOrgTree();
    }

    @Override
    public OrgUnitVO getOrgUnit(Long id) {
        return orgMapper.selectOrgUnit(id);
    }

    @Override
    @Transactional
    public Long registerOrgUnit(OrgUnitVO orgUnit) {
        // 본부-팀 2단계 고정 구조 검증 (docs/adr/ADR-004)
        if (orgUnit.getType() == OrgType.HQ && orgUnit.getParentId() != null) {
            throw new BusinessException("본부는 상위 조직을 가질 수 없습니다.");
        }
        if (orgUnit.getType() == OrgType.TEAM) {
            if (orgUnit.getParentId() == null) {
                throw new BusinessException("팀은 소속 본부를 지정해야 합니다.");
            }
            OrgUnitVO parent = orgMapper.selectOrgUnit(orgUnit.getParentId());
            if (parent == null || parent.getType() != OrgType.HQ) {
                throw new BusinessException("팀의 상위 조직은 본부여야 합니다.");
            }
        }
        orgMapper.insertOrgUnit(orgUnit);
        return orgUnit.getId();
    }

    @Override
    @Transactional
    public void assignLeader(Long orgUnitId, Long leaderEmployeeId) {
        OrgUnitVO orgUnit = orgMapper.selectOrgUnit(orgUnitId);
        if (orgUnit == null) {
            throw new BusinessException("존재하지 않는 조직입니다. orgUnitId=" + orgUnitId);
        }
        Employee leader = employeeRepository.findById(leaderEmployeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + leaderEmployeeId));
        if (!leader.getOrgUnitId().equals(orgUnitId)) {
            throw new BusinessException(orgUnit.getName() + "의 결재권자는 그 조직 소속 사원이어야 합니다.");
        }
        orgMapper.updateLeader(orgUnitId, leaderEmployeeId);
    }
}
