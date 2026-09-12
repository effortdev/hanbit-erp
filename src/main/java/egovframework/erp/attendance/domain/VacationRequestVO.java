package egovframework.erp.attendance.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 휴가신청. approval_document_id로 전자결재 문서와 1:1 연결된다 (docs/adr/ADR-008).
 * days는 신청 시점에 계산해 저장하는 스냅샷이다 (docs/adr/ADR-009).
 */
public class VacationRequestVO {

    private Long id;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int days;
    private String reason;
    private VacationStatus status;
    private Long approvalDocumentId;
    private LocalDateTime createdAt;

    public VacationRequestVO() {
    }

    public VacationRequestVO(Long employeeId, LocalDate startDate, LocalDate endDate, int days,
                              String reason, Long approvalDocumentId) {
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.reason = reason;
        this.approvalDocumentId = approvalDocumentId;
        this.status = VacationStatus.PENDING;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public VacationStatus getStatus() {
        return status;
    }

    public void setStatus(VacationStatus status) {
        this.status = status;
    }

    public Long getApprovalDocumentId() {
        return approvalDocumentId;
    }

    public void setApprovalDocumentId(Long approvalDocumentId) {
        this.approvalDocumentId = approvalDocumentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
