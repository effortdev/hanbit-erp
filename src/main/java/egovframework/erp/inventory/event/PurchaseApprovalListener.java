package egovframework.erp.inventory.event;

import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.event.DocumentApprovedEvent;
import egovframework.erp.approval.event.DocumentRejectedEvent;
import egovframework.erp.inventory.domain.PurchaseRequestStatus;
import egovframework.erp.inventory.mapper.PurchaseRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * FR-2-6: 구매요청서 승인 결과를 재고/구매 모듈에 반영한다. Module 3의
 * {@code AttendanceApprovalListener}, Module 4의 {@code ExpenseApprovalListener}와
 * 동일한 패턴 — AFTER_COMMIT으로 받아 결재 트랜잭션과 실패 도메인을 분리한다
 * (docs/adr/ADR-008). 승인 시 CONFIRMED(구매확정=입고대기)로만 바뀌고, 재고 반영은
 * 별도의 입고 처리에서 일어난다(docs/adr/ADR-015) — 재고를 여기서 늘리지 않는다.
 */
@Component
public class PurchaseApprovalListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseApprovalListener.class);

    private final PurchaseRequestMapper purchaseRequestMapper;

    @Autowired
    public PurchaseApprovalListener(PurchaseRequestMapper purchaseRequestMapper) {
        this.purchaseRequestMapper = purchaseRequestMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(DocumentApprovedEvent event) {
        if (event.getDocumentType() != DocumentType.PURCHASE) {
            return;
        }
        purchaseRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), PurchaseRequestStatus.CONFIRMED);
        log.info("구매요청서 구매확정(입고대기) 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(DocumentRejectedEvent event) {
        if (event.getDocumentType() != DocumentType.PURCHASE) {
            return;
        }
        purchaseRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), PurchaseRequestStatus.REJECTED);
        log.info("구매요청서 반려 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }
}
