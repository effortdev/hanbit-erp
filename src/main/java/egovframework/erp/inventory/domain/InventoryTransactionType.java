package egovframework.erp.inventory.domain;

/**
 * 지금은 RECEIPT(입고)만 쓴다. ISSUE(출고)는 Module 6이 실제로 필요해지는 시점에
 * 추가한다 — FR-5-4/FR-6-3에 근거는 있지만, 아직 아무도 호출하지 않는 코드를
 * 미리 만들어두지 않는다 (docs/adr/ADR-014, Module 4 BudgetThresholdPolicy 폐기 교훈).
 */
public enum InventoryTransactionType {
    RECEIPT
}
