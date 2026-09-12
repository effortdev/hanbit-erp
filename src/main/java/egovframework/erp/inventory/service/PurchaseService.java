package egovframework.erp.inventory.service;

import egovframework.erp.inventory.domain.PurchaseRequestVO;

import java.math.BigDecimal;
import java.util.List;

/** FR-5-2, FR-5-3: 구매요청서 상신과 입고 처리 (docs/adr/ADR-008, ADR-015). */
public interface PurchaseService {

    /** @return 생성된 purchase_request의 id */
    Long requestPurchase(Long employeeId, Long itemId, int quantity, BigDecimal amount, String reason);

    List<PurchaseRequestVO> getMyRequests(Long employeeId);

    /** 구매확정(CONFIRMED) 상태의 구매요청만 입고 처리할 수 있다. */
    void receiveGoods(Long purchaseRequestId, Long employeeId);
}
