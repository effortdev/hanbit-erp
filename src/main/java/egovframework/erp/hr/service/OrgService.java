package egovframework.erp.hr.service;

import egovframework.erp.hr.domain.OrgUnitVO;

import java.util.List;

public interface OrgService {

    List<OrgUnitVO> getOrgTree();

    OrgUnitVO getOrgUnit(Long id);

    Long registerOrgUnit(OrgUnitVO orgUnit);
}
