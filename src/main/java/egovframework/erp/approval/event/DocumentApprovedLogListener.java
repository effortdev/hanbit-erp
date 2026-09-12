package egovframework.erp.approval.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 임시 리스너. Module 3/5가 구현되면 각자 전용 리스너로 대체/추가된다 (docs/adr/ADR-006). */
@Component
public class DocumentApprovedLogListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentApprovedLogListener.class);

    @EventListener
    public void onDocumentApproved(DocumentApprovedEvent event) {
        log.info("문서 최종 승인됨 - documentId={}, documentType={} (Module 3/5 리스너 미구현: FR-2-5/FR-2-6 반영 대기)",
                event.getDocumentId(), event.getDocumentType());
    }
}
