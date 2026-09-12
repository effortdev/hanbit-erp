package egovframework.erp.inventory.domain;

import java.time.LocalDateTime;

/** 입출고 이력. append-only — update/delete 매퍼를 두지 않는다 (docs/adr/ADR-015). */
public class InventoryTransactionVO {

    private Long id;
    private Long itemId;
    private InventoryTransactionType transactionType;
    private int quantity;
    private Long purchaseRequestId;
    private Long salesOrderId;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static InventoryTransactionVO receipt(Long itemId, int quantity, Long purchaseRequestId, Long createdBy) {
        InventoryTransactionVO vo = new InventoryTransactionVO();
        vo.itemId = itemId;
        vo.transactionType = InventoryTransactionType.RECEIPT;
        vo.quantity = quantity;
        vo.purchaseRequestId = purchaseRequestId;
        vo.createdBy = createdBy;
        return vo;
    }

    /** docs/adr/ADR-017: 수주 확정 시 출고 이력. */
    public static InventoryTransactionVO issue(Long itemId, int quantity, Long salesOrderId, Long createdBy) {
        InventoryTransactionVO vo = new InventoryTransactionVO();
        vo.itemId = itemId;
        vo.transactionType = InventoryTransactionType.ISSUE;
        vo.quantity = quantity;
        vo.salesOrderId = salesOrderId;
        vo.createdBy = createdBy;
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public InventoryTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(InventoryTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getPurchaseRequestId() {
        return purchaseRequestId;
    }

    public void setPurchaseRequestId(Long purchaseRequestId) {
        this.purchaseRequestId = purchaseRequestId;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
