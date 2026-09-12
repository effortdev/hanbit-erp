package egovframework.erp.sales.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.inventory.domain.InventoryTransactionVO;
import egovframework.erp.inventory.domain.ItemVO;
import egovframework.erp.inventory.mapper.InventoryTransactionMapper;
import egovframework.erp.inventory.mapper.ItemMapper;
import egovframework.erp.sales.domain.SalesOrderStatus;
import egovframework.erp.sales.domain.SalesOrderVO;
import egovframework.erp.sales.domain.SalesSummaryVO;
import egovframework.erp.sales.mapper.CustomerMapper;
import egovframework.erp.sales.mapper.SalesOrderMapper;
import egovframework.erp.sales.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Module 5의 ItemMapper/InventoryTransactionMapper를 직접 재사용한다 — 재고는
 * Module 5가 소유한 도메인이고, Module 6은 그 위에 쌓이는 나중 모듈이라 정방향으로
 * 참조하는 것이 자연스럽다 (docs/adr/ADR-008 패턴과 같은 방향).
 */
@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;
    private final CustomerMapper customerMapper;
    private final ItemMapper itemMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public SalesOrderServiceImpl(SalesOrderMapper salesOrderMapper,
                                  CustomerMapper customerMapper,
                                  ItemMapper itemMapper,
                                  InventoryTransactionMapper inventoryTransactionMapper,
                                  EmployeeRepository employeeRepository) {
        this.salesOrderMapper = salesOrderMapper;
        this.customerMapper = customerMapper;
        this.itemMapper = itemMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Long registerOrder(Long employeeId, Long customerId, Long itemId, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new BusinessException("수량은 0보다 커야 합니다.");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new BusinessException("단가는 0보다 커야 합니다.");
        }
        Employee salesRep = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId));
        if (customerMapper.selectById(customerId) == null) {
            throw new BusinessException("존재하지 않는 거래처입니다. customerId=" + customerId);
        }
        ItemVO item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("존재하지 않는 품목입니다. itemId=" + itemId);
        }
        if (quantity > item.getCurrentStock()) {
            // FR-6-2 + NFR-6-1: 가용 재고보다 많은 수량은 등록 시점에 사전 차단
            throw new BusinessException("가용 재고보다 많은 수량은 수주할 수 없습니다. 가용재고=" + item.getCurrentStock() + item.getUnit());
        }

        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        SalesOrderVO order = new SalesOrderVO(customerId, itemId, salesRep.getOrgUnitId(), employeeId, quantity, unitPrice, amount);
        salesOrderMapper.insertOrder(order);
        return order.getId();
    }

    @Override
    public List<SalesOrderVO> getMyOrders(Long employeeId) {
        return salesOrderMapper.selectByEmployeeId(employeeId);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long employeeId) {
        SalesOrderVO order = salesOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("존재하지 않는 수주입니다. id=" + orderId);
        }
        if (order.getStatus() != SalesOrderStatus.REGISTERED) {
            throw new BusinessException("등록 상태의 수주만 확정할 수 있습니다. 현재 상태: " + order.getStatus());
        }

        // ADR-017: 등록 시점에 확인했더라도, 그 사이 다른 수주가 먼저 확정됐을 수 있어
        // 확정 시점에 DB 갱신 자체를 조건부로 걸어 다시 한번 막는다 (동시성 가드).
        int affected = itemMapper.decreaseStockIfAvailable(order.getItemId(), order.getQuantity());
        if (affected == 0) {
            throw new BusinessException("가용 재고가 부족하여 수주를 확정할 수 없습니다."); // NFR-6-1
        }
        inventoryTransactionMapper.insertTransaction(
                InventoryTransactionVO.issue(order.getItemId(), order.getQuantity(), order.getId(), employeeId));

        // ADR-018: 재고 차감과 매출 생성을 하나의 트랜잭션으로 묶는다 — CONFIRMED 전환 자체가 매출 데이터다.
        salesOrderMapper.confirmOrder(orderId);
    }

    @Override
    public List<SalesSummaryVO> getMonthlySummary() {
        return salesOrderMapper.selectMonthlySummary();
    }
}
