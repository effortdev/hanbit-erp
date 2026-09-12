package egovframework.erp.approval.service;

import egovframework.erp.approval.domain.ActionType;
import egovframework.erp.approval.domain.ApprovalDocumentDetail;
import egovframework.erp.approval.domain.ApprovalDocumentVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ApprovalService {

    Long draftVacation(Long drafterId, String title, String content, LocalDate startDate, LocalDate endDate);

    Long draftExpense(Long drafterId, String title, String content, BigDecimal amount);

    /** FR-2-1 결재라인 표 기준 구매요청서는 물류본부 한정이다 — 물류본부 소속이 아니면 BusinessException. */
    Long draftPurchase(Long drafterId, String title, String content, BigDecimal amount);

    Long draftGeneral(Long drafterId, String title, String content);

    /** NFR-2-2: 기안자 또는 결재라인에 포함된 사람만 조회할 수 있다. */
    ApprovalDocumentDetail getDocument(Long documentId, Long viewerId);

    List<ApprovalDocumentVO> getMyDrafts(Long drafterId);

    /** 결재라인에 포함되어 있고, 실제로 지금 처리할 차례인 문서만 반환한다. */
    List<ApprovalDocumentVO> getMyInbox(Long approverId);

    void act(Long documentId, Long stepId, Long actorId, ActionType action, String comment);
}
