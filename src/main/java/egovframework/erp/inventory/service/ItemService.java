package egovframework.erp.inventory.service;

import egovframework.erp.inventory.domain.ItemVO;

import java.util.List;

/** FR-5-1: 품목별 재고 현황 조회 (docs/adr/ADR-016). */
public interface ItemService {

    Long registerItem(String name, String unit, int initialStock, int safetyStock);

    List<ItemVO> getAllItems();
}
