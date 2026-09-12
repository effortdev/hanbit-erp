package egovframework.erp.sales;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.inventory.domain.ItemVO;
import egovframework.erp.inventory.mapper.InventoryTransactionMapper;
import egovframework.erp.inventory.mapper.ItemMapper;
import egovframework.erp.sales.domain.SalesOrderStatus;
import egovframework.erp.sales.domain.SalesOrderVO;
import egovframework.erp.sales.mapper.CustomerMapper;
import egovframework.erp.sales.mapper.SalesOrderMapper;
import egovframework.erp.sales.service.impl.SalesOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** docs/adr/ADR-017/ADR-018: 재고 부족 사전 차단, 확정 시 즉시 재고차감+매출생성 검증. */
@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock
    private SalesOrderMapper salesOrderMapper;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock
    private EmployeeRepository employeeRepository;

    private SalesOrderServiceImpl service() {
        return new SalesOrderServiceImpl(salesOrderMapper, customerMapper, itemMapper, inventoryTransactionMapper, employeeRepository);
    }

    @Test
    void 가용재고보다_많은_수량은_등록_자체가_차단된다() {
        when(employeeRepository.findById(1L)).thenReturn(java.util.Optional.of(employee(21L)));
        when(customerMapper.selectById(10L)).thenReturn(new egovframework.erp.sales.domain.CustomerVO());
        when(itemMapper.selectById(100L)).thenReturn(item(100L, 5));

        assertThrows(BusinessException.class, () ->
                service().registerOrder(1L, 10L, 100L, 10, new BigDecimal("1000")));

        verify(salesOrderMapper, never()).insertOrder(any());
    }

    @Test
    void 등록상태가_아니면_확정할_수_없다() {
        SalesOrderVO order = order(SalesOrderStatus.CONFIRMED);
        when(salesOrderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> service().confirmOrder(1L, 9L));
        verify(itemMapper, never()).decreaseStockIfAvailable(any(), anyInt());
    }

    @Test
    void 확정시점에_재고가_부족하면_차단된다() {
        SalesOrderVO order = order(SalesOrderStatus.REGISTERED);
        when(salesOrderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.decreaseStockIfAvailable(order.getItemId(), order.getQuantity())).thenReturn(0); // 경합으로 재고 소진

        assertThrows(BusinessException.class, () -> service().confirmOrder(1L, 9L));
        verify(salesOrderMapper, never()).confirmOrder(any());
    }

    @Test
    void 확정되면_재고차감과_매출생성이_함께_일어난다() {
        SalesOrderVO order = order(SalesOrderStatus.REGISTERED);
        when(salesOrderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.decreaseStockIfAvailable(order.getItemId(), order.getQuantity())).thenReturn(1);

        service().confirmOrder(1L, 9L);

        verify(itemMapper).decreaseStockIfAvailable(order.getItemId(), order.getQuantity());
        verify(inventoryTransactionMapper).insertTransaction(any());
        verify(salesOrderMapper).confirmOrder(1L);
    }

    private static SalesOrderVO order(SalesOrderStatus status) {
        SalesOrderVO order = new SalesOrderVO(10L, 100L, 21L, 1L, 3, new BigDecimal("1000"), new BigDecimal("3000"));
        order.setId(1L);
        order.setStatus(status);
        return order;
    }

    private static ItemVO item(Long id, int currentStock) {
        ItemVO item = new ItemVO();
        item.setId(id);
        item.setName("테스트품목");
        item.setUnit("개");
        item.setCurrentStock(currentStock);
        item.setSafetyStock(0);
        return item;
    }

    private static Employee employee(Long orgUnitId) {
        Employee employee = new Employee("테스트", Position.STAFF, orgUnitId, LocalDate.of(2020, 1, 1));
        try {
            Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, 1L);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return employee;
    }
}
