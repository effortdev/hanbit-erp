package egovframework.erp.approval.event;

import egovframework.erp.approval.domain.DocumentType;
import org.springframework.context.ApplicationEvent;

/**
 * 문서의 마지막 결재 단계가 승인되었을 때 발행된다. Module 3(근태)/Module 5(재고/구매)가
 * 아직 없어 이 이벤트를 직접 구독하는 리스너가 없으므로, 지금은 로그만 남기는 리스너
 * ({@link DocumentApprovedLogListener})만 등록되어 있다 — 향후 두 모듈이 각자
 * {@code @EventListener}를 등록해 FR-2-5/FR-2-6을 처리한다 (docs/adr/ADR-006).
 */
public class DocumentApprovedEvent extends ApplicationEvent {

    private final Long documentId;
    private final DocumentType documentType;

    public DocumentApprovedEvent(Object source, Long documentId, DocumentType documentType) {
        super(source);
        this.documentId = documentId;
        this.documentType = documentType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }
}
