package egovframework.erp.approval.event;

import egovframework.erp.approval.domain.DocumentType;
import org.springframework.context.ApplicationEvent;

/**
 * 문서의 마지막 결재 단계가 승인되었을 때 발행된다. Module 3(근태)가 {@code AttendanceApprovalListener}로
 * FR-2-5(휴가 반영)를 처리한다 (docs/adr/ADR-008). Module 5(재고/구매)는 아직 없어 임시로
 * 로그만 남기는 리스너({@link DocumentApprovedLogListener})가 대신 받고 있다 — 구현 시 전용
 * 리스너로 FR-2-6을 처리한다 (docs/adr/ADR-006).
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
