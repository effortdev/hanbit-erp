package egovframework.erp.sales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 수주. CONFIRMED 상태인 행 자체가 매출 데이터다 — 별도 매출 테이블을 두지 않는다
 * (docs/adr/ADR-018).
 */
public class SalesOrderVO {

    private Long id;
    private Long customerId;
    private Long itemId;
    private Long orgUnitId;
    private Long employeeId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private SalesOrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    // 화면 표시용 (조인 결과)
    private String customerName;
    private String itemName;
    private String itemUnit;
    private String orgUnitName;

    public SalesOrderVO() {
    }

    public SalesOrderVO(Long customerId, Long itemId, Long orgUnitId, Long employeeId,
                         int quantity, BigDecimal unitPrice, BigDecimal amount) {
        this.customerId = customerId;
        this.itemId = itemId;
        this.orgUnitId = orgUnitId;
        this.employeeId = employeeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.status = SalesOrderStatus.REGISTERED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public void setStatus(SalesOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }
}
