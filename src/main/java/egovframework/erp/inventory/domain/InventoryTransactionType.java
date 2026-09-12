package egovframework.erp.inventory.domain;

/**
 * RECEIPT(입고, Module 5) / ISSUE(출고, Module 6 — 수주 확정 시 발생, docs/adr/ADR-017).
 * ISSUE는 Module 5 설계 시점엔 "실제로 필요해지면 추가한다"며 미뤄뒀다가, Module 6에서
 * 실제로 필요해져 지금 추가했다.
 */
public enum InventoryTransactionType {
    RECEIPT,
    ISSUE
}
