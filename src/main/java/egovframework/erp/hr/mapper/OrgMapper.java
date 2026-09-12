package egovframework.erp.hr.mapper;

import egovframework.erp.hr.domain.OrgUnitVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 조직 트리 조회/등록용 MyBatis 매퍼 (docs/adr/ADR-003, ADR-004 참고).
 */
public interface OrgMapper {

    List<OrgUnitVO> selectOrgTree();

    OrgUnitVO selectOrgUnit(Long id);

    void insertOrgUnit(OrgUnitVO orgUnit);

    /** 이 조직의 결재권자(팀장/본부장)를 지정한다 — Module 2 결재라인 판별의 전제 (docs/adr/ADR-006). */
    void updateLeader(@Param("orgUnitId") Long orgUnitId, @Param("leaderEmployeeId") Long leaderEmployeeId);
}
