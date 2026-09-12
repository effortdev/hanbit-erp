package egovframework.erp.hr.service;

import egovframework.erp.hr.domain.OrgUnitVO;

import java.util.List;

public interface OrgService {

    List<OrgUnitVO> getOrgTree();

    OrgUnitVO getOrgUnit(Long id);

    Long registerOrgUnit(OrgUnitVO orgUnit);

    /** 이 조직의 결재권자(팀장/본부장)를 지정한다. Module 2 결재라인 판별의 전제 (docs/adr/ADR-006). */
    void assignLeader(Long orgUnitId, Long leaderEmployeeId);
}
