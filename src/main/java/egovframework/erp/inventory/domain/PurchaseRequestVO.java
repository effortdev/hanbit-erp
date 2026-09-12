package egovframework.erp.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 구매요청서. approval_document_id로 전자결재 문서와 1:1 연결된다 (docs/adr/ADR-008 패턴). */
public class PurchaseRequestVO {

    private Long id;
    private Long employeeId;
    private Long orgUnitId;
    private Long itemId;
    private int quantity;
    private BigDecimal amount;
    private String reason;
    private PurchaseRequestStatus status;
    private Long approvalDocumentId;
    private LocalDateTime createdAt;

    // 화면 표시용 (조인 결과)
    private String itemName;
    private String itemUnit;

    public PurchaseRequestVO() {
    }

    public PurchaseRequestVO(Long employeeId, Long orgUnitId, Long itemId, int quantity,
                              BigDecimal amount, String reason, Long approvalDocumentId) {
        this.employeeId = employeeId;
        this.orgUnitId = orgUnitId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
        this.reason = reason;
        this.approvalDocumentId = approvalDocumentId;
        this.status = PurchaseRequestStatus.PENDING;
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

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public PurchaseRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequestStatus status) {
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemUnit() {
        return itemUnit;
    }

    public void setItemUnit(String itemUnit) {
        this.itemUnit = itemUnit;
    }
}
