package egovframework.erp.attendance.event;

import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.event.DocumentApprovedEvent;
import egovframework.erp.approval.event.DocumentRejectedEvent;
import egovframework.erp.attendance.domain.VacationStatus;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * FR-2-5 / FR-3-2: 휴가신청 승인/반려 결과를 근태 모듈에 반영한다. Module 2의 결재 트랜잭션이
 * 커밋된 뒤 별도로 실행되어, 이쪽 처리가 실패해도 이미 확정된 결재 결과를 되돌리지 않는다
 * (docs/adr/ADR-008 — AFTER_COMMIT을 선택한 이유 참고).
 */
@Component
public class AttendanceApprovalListener {

    private static final Logger log = LoggerFactory.getLogger(AttendanceApprovalListener.class);

    private final VacationRequestMapper vacationRequestMapper;

    @Autowired
    public AttendanceApprovalListener(VacationRequestMapper vacationRequestMapper) {
        this.vacationRequestMapper = vacationRequestMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(DocumentApprovedEvent event) {
        if (event.getDocumentType() != DocumentType.VACATION) {
            return;
        }
        vacationRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), VacationStatus.APPROVED);
        log.info("휴가신청 승인 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(DocumentRejectedEvent event) {
        if (event.getDocumentType() != DocumentType.VACATION) {
            return;
        }
        vacationRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), VacationStatus.REJECTED);
        log.info("휴가신청 반려 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }
}
