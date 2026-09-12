package egovframework.erp.hr.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrgServiceImpl implements OrgService {

    private final OrgMapper orgMapper;

    @Autowired
    public OrgServiceImpl(OrgMapper orgMapper) {
        this.orgMapper = orgMapper;
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
}
