package egovframework.erp.hr.domain;

/**
 * 조직 단위(본부/팀) VO. 조직 트리는 MyBatis로 조회한다 (docs/adr/ADR-003, ADR-004 참고).
 */
public class OrgUnitVO {

    private Long id;
    private String name;
    private OrgType type;
    private Long parentId;
    private Long leaderEmployeeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrgType getType() {
        return type;
    }

    public void setType(OrgType type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /** 이 조직의 결재권자(팀장/본부장). Module 2 결재라인 판별에 사용 (docs/adr/ADR-006). */
    public Long getLeaderEmployeeId() {
        return leaderEmployeeId;
    }

    public void setLeaderEmployeeId(Long leaderEmployeeId) {
        this.leaderEmployeeId = leaderEmployeeId;
    }
}
