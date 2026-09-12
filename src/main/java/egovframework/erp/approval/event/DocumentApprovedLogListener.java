package egovframework.erp.approval.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 임시 리스너. Module 5(재고/구매)가 구현되면 전용 리스너로 대체/추가된다 (docs/adr/ADR-006).
 * {@code AFTER_COMMIT}으로 받는 이유는 docs/adr/ADR-008 참고 — 결재 트랜잭션이 다른 모듈의
 * 후처리 실패로 롤백되지 않도록 하기 위함이다.
 */
@Component
public class DocumentApprovedLogListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentApprovedLogListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentApproved(DocumentApprovedEvent event) {
        log.info("문서 최종 승인됨 - documentId={}, documentType={} (Module 5 리스너 미구현: FR-2-6 반영 대기)",
                event.getDocumentId(), event.getDocumentType());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentRejected(DocumentRejectedEvent event) {
        log.info("문서 반려됨 - documentId={}, documentType={}, comment={}",
                event.getDocumentId(), event.getDocumentType(), event.getComment());
    }
}
