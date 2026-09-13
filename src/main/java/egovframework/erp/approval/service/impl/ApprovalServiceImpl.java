package egovframework.erp.approval.service.impl;

import egovframework.erp.approval.domain.ActionType;
import egovframework.erp.approval.domain.ApprovalActionVO;
import egovframework.erp.approval.domain.ApprovalDocumentDetail;
import egovframework.erp.approval.domain.ApprovalDocumentVO;
import egovframework.erp.approval.domain.ApprovalStepRules;
import egovframework.erp.approval.domain.ApprovalStepVO;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.event.DocumentApprovedEvent;
import egovframework.erp.approval.event.DocumentRejectedEvent;
import egovframework.erp.approval.mapper.ApprovalActionMapper;
import egovframework.erp.approval.mapper.ApprovalDocumentMapper;
import egovframework.erp.approval.mapper.ApprovalStepMapper;
import egovframework.erp.approval.service.ApprovalLineResolver;
import egovframework.erp.approval.service.ApprovalLineResolver.ResolvedStep;
import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final String LOGISTICS_DIVISION_NAME = "물류본부";

    private final ApprovalDocumentMapper documentMapper;
    private final ApprovalStepMapper stepMapper;
    private final ApprovalActionMapper actionMapper;
    private final ApprovalLineResolver lineResolver;
    private final EmployeeRepository employeeRepository;
    private final OrgMapper orgMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ApprovalServiceImpl(ApprovalDocumentMapper documentMapper,
                                ApprovalStepMapper stepMapper,
                                ApprovalActionMapper actionMapper,
                                ApprovalLineResolver lineResolver,
                                EmployeeRepository employeeRepository,
                                OrgMapper orgMapper,
                                ApplicationEventPublisher eventPublisher) {
        this.documentMapper = documentMapper;
        this.stepMapper = stepMapper;
        this.actionMapper = actionMapper;
        this.lineResolver = lineResolver;
        this.employeeRepository = employeeRepository;
        this.orgMapper = orgMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Long draftVacation(Long drafterId, String title, String content, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BusinessException("휴가 시작일/종료일이 올바르지 않습니다.");
        }
        ApprovalDocumentVO doc = new ApprovalDocumentVO();
        doc.setDocumentType(DocumentType.VACATION);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setStartDate(startDate);
        doc.setEndDate(endDate);
        return draft(drafterId, doc, null);
    }

    @Override
    @Transactional
    public Long draftExpense(Long drafterId, String title, String content, BigDecimal amount) {
        requirePositiveAmount(amount);
        ApprovalDocumentVO doc = new ApprovalDocumentVO();
        doc.setDocumentType(DocumentType.EXPENSE);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setAmount(amount);
        return draft(drafterId, doc, amount);
    }

    @Override
    @Transactional
    public Long draftPurchase(Long drafterId, String title, String content, BigDecimal amount) {
        requirePositiveAmount(amount);
        Employee drafter = requireEmployee(drafterId);
        requireLogisticsDivision(drafter);

        ApprovalDocumentVO doc = new ApprovalDocumentVO();
        doc.setDocumentType(DocumentType.PURCHASE);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setAmount(amount);
        return draft(drafter, doc, amount);
    }

    @Override
    @Transactional
    public Long draftGeneral(Long drafterId, String title, String content) {
        ApprovalDocumentVO doc = new ApprovalDocumentVO();
        doc.setDocumentType(DocumentType.GENERAL);
        doc.setTitle(title);
        doc.setContent(content);
        return draft(drafterId, doc, null);
    }

    private Long draft(Long drafterId, ApprovalDocumentVO doc, BigDecimal amountForLineCheck) {
        return draft(requireEmployee(drafterId), doc, amountForLineCheck);
    }

    private Long draft(Employee drafter, ApprovalDocumentVO doc, BigDecimal amountForLineCheck) {
        doc.setDrafterId(drafter.getId());
        documentMapper.insertDocument(doc);

        List<ResolvedStep> steps = lineResolver.resolve(doc.getDocumentType(), drafter, amountForLineCheck);
        int order = 1;
        for (ResolvedStep step : steps) {
            ApprovalStepVO stepVO = new ApprovalStepVO();
            stepVO.setDocumentId(doc.getId());
            stepVO.setStepOrder(order++);
            stepVO.setApproverLevel(step.approverLevel());
            stepVO.setApproverEmployeeId(step.approverEmployeeId());
            stepMapper.insertStep(stepVO);
        }

        if (steps.isEmpty()) {
            // 모든 단계가 자기결재로 스킵됨 -> 상신 즉시 자동 승인 (docs/adr/ADR-006)
            eventPublisher.publishEvent(new DocumentApprovedEvent(this, doc.getId(), doc.getDocumentType()));
        }
        return doc.getId();
    }

    @Override
    public ApprovalDocumentDetail getDocument(Long documentId, Long viewerId) {
        ApprovalDocumentVO document = requireDocument(documentId);
        List<ApprovalStepVO> steps = stepMapper.selectByDocumentId(documentId);

        boolean isDrafter = document.getDrafterId().equals(viewerId);
        boolean isApprover = steps.stream().anyMatch(s -> s.getApproverEmployeeId().equals(viewerId));
        if (!isDrafter && !isApprover) {
            throw new BusinessException("조회 권한이 없습니다."); // NFR-2-2
        }
        return new ApprovalDocumentDetail(document, steps, viewerId);
    }

    @Override
    public List<ApprovalDocumentVO> getMyDrafts(Long drafterId) {
        return documentMapper.selectByDrafterId(drafterId);
    }

    @Override
    public List<ApprovalDocumentVO> getMyInbox(Long approverId) {
        return documentMapper.selectByApproverCandidate(approverId).stream()
                .filter(doc -> ApprovalStepRules.findActionableStepFor(
                        stepMapper.selectByDocumentId(doc.getId()), approverId).isPresent())
                .toList();
    }

    @Override
    @Transactional
    public void act(Long documentId, Long stepId, Long actorId, ActionType action, String comment) {
        ApprovalDocumentVO document = requireDocument(documentId);
        List<ApprovalStepVO> steps = stepMapper.selectByDocumentId(documentId);

        ApprovalStepVO target = steps.stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("해당 결재 단계를 찾을 수 없습니다."));

        if (!target.getApproverEmployeeId().equals(actorId)) {
            throw new BusinessException("본인에게 배정된 결재 단계만 처리할 수 있습니다.");
        }
        if (!ApprovalStepRules.isActionable(steps, target)) {
            throw new BusinessException("아직 처리할 차례가 아니거나 이미 처리된 단계입니다.");
        }
        if (action == ActionType.REJECT && (comment == null || comment.isBlank())) {
            // FR-2-2: 반려 시 사유 입력은 필수 — 화면단 required만으로는 API 직접 호출을 막지 못해 서비스 계층에서도 강제한다.
            throw new BusinessException("반려 시 사유를 입력해야 합니다.");
        }

        actionMapper.insertAction(ApprovalActionVO.of(stepId, action, comment));

        boolean isLastStep = steps.stream().mapToInt(ApprovalStepVO::getStepOrder).max().orElse(0) == target.getStepOrder();
        if (action == ActionType.APPROVE && isLastStep) {
            eventPublisher.publishEvent(new DocumentApprovedEvent(this, documentId, document.getDocumentType()));
        } else if (action == ActionType.REJECT) {
            // DocumentApprovedEvent와 대칭 (docs/adr/ADR-008 — Module 3 구현 중 반려 이벤트 부재를 발견해 보완)
            eventPublisher.publishEvent(new DocumentRejectedEvent(this, documentId, document.getDocumentType(), comment));
        }
    }

    private void requireLogisticsDivision(Employee drafter) {
        OrgUnitVO team = orgMapper.selectOrgUnit(drafter.getOrgUnitId());
        OrgUnitVO hq = team != null && team.getParentId() != null ? orgMapper.selectOrgUnit(team.getParentId()) : null;
        String hqName = hq != null ? hq.getName() : (team != null ? team.getName() : null);
        if (!LOGISTICS_DIVISION_NAME.equals(hqName)) {
            throw new BusinessException("구매요청서는 " + LOGISTICS_DIVISION_NAME + " 소속만 기안할 수 있습니다.");
        }
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("금액은 0보다 커야 합니다.");
        }
    }

    private Employee requireEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사원입니다. employeeId=" + employeeId));
    }

    private ApprovalDocumentVO requireDocument(Long documentId) {
        ApprovalDocumentVO document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("존재하지 않는 문서입니다. documentId=" + documentId);
        }
        return document;
    }
}
