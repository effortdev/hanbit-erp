package egovframework.erp.security;

import egovframework.erp.hr.domain.Position;

/**
 * 로그인 자격증명 + 화면/결재라인 판별에 필요한 최소 사원 정보를 담은 조회 전용 VO.
 * account와 employee를 조인해서 채운다 (docs/adr/ADR-005).
 */
public class AccountVO {

    private Long employeeId;
    private String username;
    private String passwordHash;
    private boolean enabled;
    private String employeeName;
    private Position position;
    private Long orgUnitId;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }
}
