package egovframework.erp.inventory.mapper;

import egovframework.erp.inventory.domain.InventoryTransactionVO;

/** append-only — insert/select만 두고 update/delete는 두지 않는다 (docs/adr/ADR-015). */
public interface InventoryTransactionMapper {

    void insertTransaction(InventoryTransactionVO transaction);
}
