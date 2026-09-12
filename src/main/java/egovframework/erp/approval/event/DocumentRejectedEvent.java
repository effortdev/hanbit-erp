package egovframework.erp.approval.event;

import egovframework.erp.approval.domain.DocumentType;
import org.springframework.context.ApplicationEvent;

/**
 * 결재라인 중 한 단계라도 반려되면 발행된다. {@link DocumentApprovedEvent}와 대칭을 이루며,
 * Module 3(근태)가 실제로 구현되며 필요성이 드러나 추가된 이벤트다 (docs/adr/ADR-008).
 */
public class DocumentRejectedEvent extends ApplicationEvent {

    private final Long documentId;
    private final DocumentType documentType;
    private final String comment;

    public DocumentRejectedEvent(Object source, Long documentId, DocumentType documentType, String comment) {
        super(source);
        this.documentId = documentId;
        this.documentType = documentType;
        this.comment = comment;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getComment() {
        return comment;
    }
}
