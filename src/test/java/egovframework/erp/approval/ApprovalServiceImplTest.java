package egovframework.erp.approval;

import egovframework.erp.approval.domain.ActionType;
import egovframework.erp.approval.domain.ApprovalDocumentVO;
import egovframework.erp.approval.domain.ApprovalStepVO;
import egovframework.erp.approval.domain.ApproverLevel;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.mapper.ApprovalActionMapper;
import egovframework.erp.approval.mapper.ApprovalDocumentMapper;
import egovframework.erp.approval.mapper.ApprovalStepMapper;
import egovframework.erp.approval.service.ApprovalLineResolver;
import egovframework.erp.approval.service.impl.ApprovalServiceImpl;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FR-2-2 회귀 테스트: 반려 시 사유 입력은 화면단 required뿐 아니라 서비스 계층에서도
 * 강제되어야 한다 (docs/requirements-traceability.md 격차 #5).
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalDocumentMapper documentMapper;
    @Mock
    private ApprovalStepMapper stepMapper;
    @Mock
    private ApprovalActionMapper actionMapper;
    @Mock
    private ApprovalLineResolver lineResolver;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrgMapper orgMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApprovalServiceImpl service;

    private static final Long DOCUMENT_ID = 100L;
    private static final Long STEP_ID = 200L;
    private static final Long APPROVER_ID = 3L;

    @BeforeEach
    void setUp() {
        service = new ApprovalServiceImpl(documentMapper, stepMapper, actionMapper,
                lineResolver, employeeRepository, orgMapper, eventPublisher);

        ApprovalDocumentVO document = new ApprovalDocumentVO();
        document.setId(DOCUMENT_ID);
        document.setDocumentType(DocumentType.VACATION);
        document.setDrafterId(4L);
        when(documentMapper.selectById(DOCUMENT_ID)).thenReturn(document);

        ApprovalStepVO step = new ApprovalStepVO();
        step.setId(STEP_ID);
        step.setDocumentId(DOCUMENT_ID);
        step.setStepOrder(1);
        step.setApproverLevel(ApproverLevel.TEAM_LEADER);
        step.setApproverEmployeeId(APPROVER_ID);
        when(stepMapper.selectByDocumentId(DOCUMENT_ID)).thenReturn(List.of(step));
    }

    @Test
    void 반려_시_사유가_없으면_예외() {
        assertThrows(BusinessException.class,
                () -> service.act(DOCUMENT_ID, STEP_ID, APPROVER_ID, ActionType.REJECT, null));

        verify(actionMapper, never()).insertAction(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 반려_시_사유가_공백뿐이면_예외() {
        assertThrows(BusinessException.class,
                () -> service.act(DOCUMENT_ID, STEP_ID, APPROVER_ID, ActionType.REJECT, "   "));

        verify(actionMapper, never()).insertAction(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 반려_시_사유가_있으면_정상_처리된다() {
        assertDoesNotThrow(() ->
                service.act(DOCUMENT_ID, STEP_ID, APPROVER_ID, ActionType.REJECT, "예산 초과로 반려합니다."));

        verify(actionMapper).insertAction(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 승인_시에는_사유가_없어도_예외가_아니다() {
        assertDoesNotThrow(() ->
                service.act(DOCUMENT_ID, STEP_ID, APPROVER_ID, ActionType.APPROVE, null));

        verify(actionMapper).insertAction(org.mockito.ArgumentMatchers.any());
    }
}
