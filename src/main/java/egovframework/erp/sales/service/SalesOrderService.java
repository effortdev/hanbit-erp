package egovframework.erp.sales.service;

import egovframework.erp.sales.domain.SalesOrderVO;
import egovframework.erp.sales.domain.SalesSummaryVO;

import java.math.BigDecimal;
import java.util.List;

/** FR-6-2~6-4: 수주 등록/확정과 매출 현황 (docs/adr/ADR-017, ADR-018). */
public interface SalesOrderService {

    /** 가용 재고보다 많으면 등록 자체를 막는다 (NFR-6-1). */
    Long registerOrder(Long employeeId, Long customerId, Long itemId, int quantity, BigDecimal unitPrice);

    List<SalesOrderVO> getMyOrders(Long employeeId);

    /** 재고 차감 + 매출 생성을 하나의 트랜잭션으로 처리한다. */
    void confirmOrder(Long orderId, Long employeeId);

    List<SalesSummaryVO> getMonthlySummary();
}
