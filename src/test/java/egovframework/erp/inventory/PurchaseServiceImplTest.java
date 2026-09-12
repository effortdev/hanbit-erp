package egovframework.erp.inventory;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.inventory.domain.ItemVO;
import egovframework.erp.inventory.domain.PurchaseRequestStatus;
import egovframework.erp.inventory.domain.PurchaseRequestVO;
import egovframework.erp.inventory.mapper.InventoryTransactionMapper;
import egovframework.erp.inventory.mapper.ItemMapper;
import egovframework.erp.inventory.mapper.PurchaseRequestMapper;
import egovframework.erp.inventory.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** docs/adr/ADR-015: 승인(CONFIRMED)과 입고(RECEIVED)의 분리를 검증한다. */
@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRequestMapper purchaseRequestMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private EmployeeRepository employeeRepository;

    private PurchaseServiceImpl service() {
        return new PurchaseServiceImpl(purchaseRequestMapper, itemMapper, inventoryTransactionMapper,
                approvalService, employeeRepository);
    }

    @Test
    void 상신만으로는_재고가_늘지_않는다() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(31L)));
        when(itemMapper.selectById(100L)).thenReturn(item(100L, 5));
        when(approvalService.draftPurchase(any(), any(), any(), any())).thenReturn(999L);

        service().requestPurchase(1L, 100L, 3, new BigDecimal("500000"), "포장재 보충");

        verify(itemMapper, never()).increaseStock(any(), anyInt());
        verify(purchaseRequestMapper).insertRequest(any());
    }

    @Test
    void 대기중인_구매요청은_입고처리할_수_없다() {
        PurchaseRequestVO request = purchaseRequest(PurchaseRequestStatus.PENDING);
        when(purchaseRequestMapper.selectById(1L)).thenReturn(request);

        assertThrows(BusinessException.class, () -> service().receiveGoods(1L, 9L));
        verify(itemMapper, never()).increaseStock(any(), anyInt());
    }

    @Test
    void 이미_입고완료된_구매요청은_다시_입고처리할_수_없다() {
        PurchaseRequestVO request = purchaseRequest(PurchaseRequestStatus.RECEIVED);
        when(purchaseRequestMapper.selectById(1L)).thenReturn(request);

        assertThrows(BusinessException.class, () -> service().receiveGoods(1L, 9L));
    }

    @Test
    void 구매확정_상태는_입고처리하면_재고가_늘고_이력이_남는다() {
        PurchaseRequestVO request = purchaseRequest(PurchaseRequestStatus.CONFIRMED);
        when(purchaseRequestMapper.selectById(1L)).thenReturn(request);

        service().receiveGoods(1L, 9L);

        verify(itemMapper).increaseStock(request.getItemId(), request.getQuantity());
        verify(inventoryTransactionMapper).insertTransaction(any());
        verify(purchaseRequestMapper).updateStatus(1L, PurchaseRequestStatus.RECEIVED);
    }

    private static PurchaseRequestVO purchaseRequest(PurchaseRequestStatus status) {
        PurchaseRequestVO request = new PurchaseRequestVO(
                1L, 31L, 100L, 3, new BigDecimal("500000"), "사유", 999L);
        request.setId(1L);
        request.setStatus(status);
        return request;
    }

    private static ItemVO item(Long id, int currentStock) {
        ItemVO item = new ItemVO();
        item.setId(id);
        item.setName("포장 박스");
        item.setUnit("개");
        item.setCurrentStock(currentStock);
        item.setSafetyStock(50);
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
