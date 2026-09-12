package egovframework.erp.inventory.service.impl;

import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.inventory.domain.InventoryTransactionVO;
import egovframework.erp.inventory.domain.ItemVO;
import egovframework.erp.inventory.domain.PurchaseRequestStatus;
import egovframework.erp.inventory.domain.PurchaseRequestVO;
import egovframework.erp.inventory.mapper.InventoryTransactionMapper;
import egovframework.erp.inventory.mapper.ItemMapper;
import egovframework.erp.inventory.mapper.PurchaseRequestMapper;
import egovframework.erp.inventory.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 근태(Module 3)/예산(Module 4)과 같은 구조: 자체 검증 후 Module 2의 공개 서비스를
 * 직접 호출하는 정방향 의존 (docs/adr/ADR-008 패턴). 물류본부 소속 검증은 Module 2의
 * {@code ApprovalService.draftPurchase(...)}가 이미 하므로 여기서 다시 하지 않는다
 * (docs/adr/ADR-014).
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRequestMapper purchaseRequestMapper;
    private final ItemMapper itemMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final ApprovalService approvalService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRequestMapper purchaseRequestMapper,
                                ItemMapper itemMapper,
                                InventoryTransactionMapper inventoryTransactionMapper,
                                ApprovalService approvalService,
                                EmployeeRepository employeeRepository) {
        this.purchaseRequestMapper = purchaseRequestMapper;
        this.itemMapper = itemMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.approvalService = approvalService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Long requestPurchase(Long employeeId, Long itemId, int quantity, BigDecimal amount, String reason) {
        if (quantity <= 0) {
            throw new BusinessException("수량은 0보다 커야 합니다.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("금액은 0보다 커야 합니다.");
        }
        Employee drafter = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId));
        ItemVO item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("존재하지 않는 품목입니다. itemId=" + itemId);
        }

        String title = "구매요청(" + item.getName() + " " + quantity + item.getUnit() + ")";
        // 물류본부 소속 검증은 Module 2가 이미 한다 (docs/adr/ADR-014)
        Long documentId = approvalService.draftPurchase(employeeId, title, reason, amount);

        PurchaseRequestVO request = new PurchaseRequestVO(
                employeeId, drafter.getOrgUnitId(), itemId, quantity, amount, reason, documentId);
        purchaseRequestMapper.insertRequest(request);
        return request.getId();
    }

    @Override
    public List<PurchaseRequestVO> getMyRequests(Long employeeId) {
        return purchaseRequestMapper.selectByEmployeeId(employeeId);
    }

    @Override
    @Transactional
    public void receiveGoods(Long purchaseRequestId, Long employeeId) {
        PurchaseRequestVO request = purchaseRequestMapper.selectById(purchaseRequestId);
        if (request == null) {
            throw new BusinessException("존재하지 않는 구매요청서입니다. id=" + purchaseRequestId);
        }
        if (request.getStatus() != PurchaseRequestStatus.CONFIRMED) {
            throw new BusinessException("구매확정(입고대기) 상태가 아니면 입고 처리를 할 수 없습니다. 현재 상태: " + request.getStatus());
        }

        itemMapper.increaseStock(request.getItemId(), request.getQuantity());
        inventoryTransactionMapper.insertTransaction(
                InventoryTransactionVO.receipt(request.getItemId(), request.getQuantity(), request.getId(), employeeId));
        purchaseRequestMapper.updateStatus(purchaseRequestId, PurchaseRequestStatus.RECEIVED);
    }
}
