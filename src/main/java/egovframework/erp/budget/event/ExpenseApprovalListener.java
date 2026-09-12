package egovframework.erp.budget.event;

import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.event.DocumentApprovedEvent;
import egovframework.erp.approval.event.DocumentRejectedEvent;
import egovframework.erp.budget.domain.BudgetExpenseStatus;
import egovframework.erp.budget.mapper.BudgetExpenseRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * FR-4-2: 지출결의서 승인/반려 결과를 예산 모듈에 반영한다. Module 3의
 * {@code AttendanceApprovalListener}와 동일한 패턴 — AFTER_COMMIT으로 받아 결재
 * 트랜잭션과 실패 도메인을 분리한다 (docs/adr/ADR-008).
 */
@Component
public class ExpenseApprovalListener {

    private static final Logger log = LoggerFactory.getLogger(ExpenseApprovalListener.class);

    private final BudgetExpenseRequestMapper budgetExpenseRequestMapper;

    @Autowired
    public ExpenseApprovalListener(BudgetExpenseRequestMapper budgetExpenseRequestMapper) {
        this.budgetExpenseRequestMapper = budgetExpenseRequestMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(DocumentApprovedEvent event) {
        if (event.getDocumentType() != DocumentType.EXPENSE) {
            return;
        }
        budgetExpenseRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), BudgetExpenseStatus.APPROVED);
        log.info("지출결의서 승인 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(DocumentRejectedEvent event) {
        if (event.getDocumentType() != DocumentType.EXPENSE) {
            return;
        }
        budgetExpenseRequestMapper.updateStatusByApprovalDocumentId(event.getDocumentId(), BudgetExpenseStatus.REJECTED);
        log.info("지출결의서 반려 반영 완료 - approvalDocumentId={}", event.getDocumentId());
    }
}
