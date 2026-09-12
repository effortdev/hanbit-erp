package egovframework.erp.approval.domain;

import java.time.LocalDateTime;

/** 승인/반려 행위 로그. append-only — update/delete 매퍼를 두지 않는다 (NFR-2-3, docs/adr/ADR-006). */
public class ApprovalActionVO {

    private Long id;
    private Long stepId;
    private ActionType action;
    private String comment;
    private LocalDateTime actedAt;

    public static ApprovalActionVO of(Long stepId, ActionType action, String comment) {
        ApprovalActionVO vo = new ApprovalActionVO();
        vo.stepId = stepId;
        vo.action = action;
        vo.comment = comment;
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getActedAt() {
        return actedAt;
    }

    public void setActedAt(LocalDateTime actedAt) {
        this.actedAt = actedAt;
    }
}
