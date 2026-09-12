package egovframework.erp.hr.mapper;

import egovframework.erp.hr.domain.OrgUnitVO;

import java.util.List;

/**
 * 조직 트리 조회/등록용 MyBatis 매퍼 (docs/adr/ADR-003, ADR-004 참고).
 */
public interface OrgMapper {

    List<OrgUnitVO> selectOrgTree();

    OrgUnitVO selectOrgUnit(Long id);

    void insertOrgUnit(OrgUnitVO orgUnit);
}
