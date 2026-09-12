package egovframework.erp.budget.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 지출결의서. approval_document_id로 전자결재 문서와 1:1 연결된다 (Module 3의
 * VacationRequestVO와 동일한 패턴 — docs/adr/ADR-008 참고).
 */
public class BudgetExpenseRequestVO {

    private Long id;
    private Long employeeId;
    private Long orgUnitId;
    private AccountCategory accountCategory;
    private BigDecimal amount;
    private String reason;
    private String exceptionReason;
    private BudgetExpenseStatus status;
    private Long approvalDocumentId;
    private LocalDateTime createdAt;

    public BudgetExpenseRequestVO() {
    }

    public BudgetExpenseRequestVO(Long employeeId, Long orgUnitId, AccountCategory accountCategory,
                                   BigDecimal amount, String reason, String exceptionReason, Long approvalDocumentId) {
        this.employeeId = employeeId;
        this.orgUnitId = orgUnitId;
        this.accountCategory = accountCategory;
        this.amount = amount;
        this.reason = reason;
        this.exceptionReason = exceptionReason;
        this.approvalDocumentId = approvalDocumentId;
        this.status = BudgetExpenseStatus.PENDING;
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

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public BudgetExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(BudgetExpenseStatus status) {
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
