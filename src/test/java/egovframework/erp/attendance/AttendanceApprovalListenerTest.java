package egovframework.erp.attendance;

import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.event.DocumentApprovedEvent;
import egovframework.erp.approval.event.DocumentRejectedEvent;
import egovframework.erp.attendance.domain.VacationStatus;
import egovframework.erp.attendance.event.AttendanceApprovalListener;
import egovframework.erp.attendance.mapper.VacationRequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** docs/adr/ADR-008: VACATION 문서만 반영하고, 다른 유형(예: EXPENSE)은 무시해야 한다. */
@ExtendWith(MockitoExtension.class)
class AttendanceApprovalListenerTest {

    @Mock
    private VacationRequestMapper vacationRequestMapper;

    @Test
    void 휴가신청_승인_이벤트는_상태를_승인으로_갱신한다() {
        AttendanceApprovalListener listener = new AttendanceApprovalListener(vacationRequestMapper);
        listener.onApproved(new DocumentApprovedEvent(this, 10L, DocumentType.VACATION));
        verify(vacationRequestMapper).updateStatusByApprovalDocumentId(10L, VacationStatus.APPROVED);
    }

    @Test
    void 휴가신청_반려_이벤트는_상태를_반려로_갱신한다() {
        AttendanceApprovalListener listener = new AttendanceApprovalListener(vacationRequestMapper);
        listener.onRejected(new DocumentRejectedEvent(this, 10L, DocumentType.VACATION, "사유 불충분"));
        verify(vacationRequestMapper).updateStatusByApprovalDocumentId(10L, VacationStatus.REJECTED);
    }

    @Test
    void 휴가신청이_아닌_문서의_승인_이벤트는_무시한다() {
        AttendanceApprovalListener listener = new AttendanceApprovalListener(vacationRequestMapper);
        listener.onApproved(new DocumentApprovedEvent(this, 20L, DocumentType.EXPENSE));
        verify(vacationRequestMapper, never()).updateStatusByApprovalDocumentId(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
