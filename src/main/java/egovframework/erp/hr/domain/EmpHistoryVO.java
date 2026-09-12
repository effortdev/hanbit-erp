package egovframework.erp.hr.domain;

import java.time.LocalDateTime;

/**
 * 발령 이력. append-only — 이 VO를 갱신하는 MyBatis update/delete 매퍼는 의도적으로 두지 않는다
 * (docs/adr/ADR-003 참고).
 */
public class EmpHistoryVO {

    private Long id;
    private Long employeeId;
    private ChangeType changeType;
    private Long beforeOrgUnitId;
    private Long afterOrgUnitId;
    private Position beforePosition;
    private Position afterPosition;
    private LocalDateTime changedAt;
    private String changedBy;
    private String memo;

    public static EmpHistoryVO transfer(Long employeeId, Long beforeOrgUnitId, Long afterOrgUnitId, String changedBy) {
        EmpHistoryVO vo = new EmpHistoryVO();
        vo.employeeId = employeeId;
        vo.changeType = ChangeType.TRANSFER;
        vo.beforeOrgUnitId = beforeOrgUnitId;
        vo.afterOrgUnitId = afterOrgUnitId;
        vo.changedBy = changedBy;
        return vo;
    }

    public static EmpHistoryVO promotion(Long employeeId, Position beforePosition, Position afterPosition, String changedBy) {
        EmpHistoryVO vo = new EmpHistoryVO();
        vo.employeeId = employeeId;
        vo.changeType = ChangeType.PROMOTION;
        vo.beforePosition = beforePosition;
        vo.afterPosition = afterPosition;
        vo.changedBy = changedBy;
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public Long getBeforeOrgUnitId() {
        return beforeOrgUnitId;
    }

    public void setBeforeOrgUnitId(Long beforeOrgUnitId) {
        this.beforeOrgUnitId = beforeOrgUnitId;
    }

    public Long getAfterOrgUnitId() {
        return afterOrgUnitId;
    }

    public void setAfterOrgUnitId(Long afterOrgUnitId) {
        this.afterOrgUnitId = afterOrgUnitId;
    }

    public Position getBeforePosition() {
        return beforePosition;
    }

    public void setBeforePosition(Position beforePosition) {
        this.beforePosition = beforePosition;
    }

    public Position getAfterPosition() {
        return afterPosition;
    }

    public void setAfterPosition(Position afterPosition) {
        this.afterPosition = afterPosition;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
