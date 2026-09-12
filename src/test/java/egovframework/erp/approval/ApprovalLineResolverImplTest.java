package egovframework.erp.approval;

import egovframework.erp.approval.domain.ApprovalCondition;
import egovframework.erp.approval.domain.ApprovalLineRuleVO;
import egovframework.erp.approval.domain.ApproverLevel;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.mapper.ApprovalLineRuleMapper;
import egovframework.erp.approval.service.ApprovalLineResolver.ResolvedStep;
import egovframework.erp.approval.service.BudgetThresholdPolicy;
import egovframework.erp.approval.service.impl.ApprovalLineResolverImpl;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.EmployeeStatus;
import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * docs/adr/ADR-006 핵심 시나리오 검증: 규칙 테이블 + 조직도 혼합 판별, 리더 공석,
 * 대표이사 단수성, 자기결재 스킵, 예산 조건부 단계.
 */
@ExtendWith(MockitoExtension.class)
class ApprovalLineResolverImplTest {

    @Mock
    private ApprovalLineRuleMapper ruleMapper;
    @Mock
    private OrgMapper orgMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BudgetThresholdPolicy budgetThresholdPolicy;

    private ApprovalLineResolverImpl resolver;

    private OrgUnitVO team;
    private OrgUnitVO hq;

    @BeforeEach
    void setUp() {
        resolver = new ApprovalLineResolverImpl(ruleMapper, orgMapper, employeeRepository, budgetThresholdPolicy);

        team = unit(11L, "인사팀", OrgType.TEAM, 1L, 3L); // 팀장 = employeeId 3
        hq = unit(1L, "경영지원본부", OrgType.HQ, null, 2L); // 본부장 = employeeId 2
        lenient().when(orgMapper.selectOrgUnit(11L)).thenReturn(team);
        lenient().when(orgMapper.selectOrgUnit(1L)).thenReturn(hq);
    }

    @Test
    void 휴가신청은_팀장_한단계다() {
        when(ruleMapper.selectByDocumentType(DocumentType.VACATION)).thenReturn(List.of(
                rule(DocumentType.VACATION, 1, ApproverLevel.TEAM_LEADER, null)));
        Employee staff = employee(4L, "최사원", Position.STAFF, 11L);

        List<ResolvedStep> steps = resolver.resolve(DocumentType.VACATION, staff, null);

        assertEquals(1, steps.size());
        assertEquals(ApproverLevel.TEAM_LEADER, steps.get(0).approverLevel());
        assertEquals(3L, steps.get(0).approverEmployeeId());
    }

    @Test
    void 팀장이_공석이면_결재라인을_구성할_수_없다() {
        team.setLeaderEmployeeId(null);
        when(ruleMapper.selectByDocumentType(DocumentType.VACATION)).thenReturn(List.of(
                rule(DocumentType.VACATION, 1, ApproverLevel.TEAM_LEADER, null)));
        Employee staff = employee(4L, "최사원", Position.STAFF, 11L);

        assertThrows(BusinessException.class, () -> resolver.resolve(DocumentType.VACATION, staff, null));
    }

    @Test
    void 팀장_본인이_기안하면_팀장_단계는_스킵되고_본부장_단계로_넘어간다() {
        when(ruleMapper.selectByDocumentType(DocumentType.GENERAL)).thenReturn(List.of(
                rule(DocumentType.GENERAL, 1, ApproverLevel.TEAM_LEADER, null),
                rule(DocumentType.GENERAL, 2, ApproverLevel.DIVISION_HEAD, null)));
        Employee teamLeader = employee(3L, "이인사", Position.TEAM_LEADER, 11L); // team의 leaderEmployeeId와 동일

        List<ResolvedStep> steps = resolver.resolve(DocumentType.GENERAL, teamLeader, null);

        assertEquals(1, steps.size());
        assertEquals(ApproverLevel.DIVISION_HEAD, steps.get(0).approverLevel());
        assertEquals(2L, steps.get(0).approverEmployeeId());
    }

    @Test
    void 대표이사가_유일하지_않으면_예외() {
        when(ruleMapper.selectByDocumentType(DocumentType.PURCHASE)).thenReturn(List.of(
                rule(DocumentType.PURCHASE, 1, ApproverLevel.CEO, null)));
        when(employeeRepository.findByPositionAndStatus(Position.CEO, EmployeeStatus.ACTIVE)).thenReturn(List.of());
        Employee staff = employee(4L, "최사원", Position.STAFF, 11L);

        assertThrows(BusinessException.class, () -> resolver.resolve(DocumentType.PURCHASE, staff, null));
    }

    @Test
    void 예산초과_조건이_미충족이면_대표이사_단계가_빠진다() {
        when(ruleMapper.selectByDocumentType(DocumentType.EXPENSE)).thenReturn(List.of(
                rule(DocumentType.EXPENSE, 1, ApproverLevel.TEAM_LEADER, null),
                rule(DocumentType.EXPENSE, 2, ApproverLevel.CEO, ApprovalCondition.BUDGET_80_EXCEEDED)));
        when(budgetThresholdPolicy.isExceeded(any(), any())).thenReturn(false);
        Employee staff = employee(4L, "최사원", Position.STAFF, 11L);

        List<ResolvedStep> steps = resolver.resolve(DocumentType.EXPENSE, staff, new BigDecimal("1000"));

        assertEquals(1, steps.size());
        assertTrue(steps.stream().noneMatch(s -> s.approverLevel() == ApproverLevel.CEO));
    }

    @Test
    void 예산초과_조건이_충족되면_대표이사_단계가_포함된다() {
        when(ruleMapper.selectByDocumentType(DocumentType.EXPENSE)).thenReturn(List.of(
                rule(DocumentType.EXPENSE, 1, ApproverLevel.TEAM_LEADER, null),
                rule(DocumentType.EXPENSE, 2, ApproverLevel.CEO, ApprovalCondition.BUDGET_80_EXCEEDED)));
        when(budgetThresholdPolicy.isExceeded(any(), any())).thenReturn(true);
        when(employeeRepository.findByPositionAndStatus(Position.CEO, EmployeeStatus.ACTIVE))
                .thenReturn(List.of(employee(1L, "김대표", Position.CEO, 1L)));
        Employee staff = employee(4L, "최사원", Position.STAFF, 11L);

        List<ResolvedStep> steps = resolver.resolve(DocumentType.EXPENSE, staff, new BigDecimal("999999"));

        assertEquals(2, steps.size());
        assertEquals(ApproverLevel.CEO, steps.get(1).approverLevel());
        assertEquals(1L, steps.get(1).approverEmployeeId());
    }

    private static ApprovalLineRuleVO rule(DocumentType type, int order, ApproverLevel level, ApprovalCondition cond) {
        ApprovalLineRuleVO vo = new ApprovalLineRuleVO();
        vo.setDocumentType(type);
        vo.setStepOrder(order);
        vo.setApproverLevel(level);
        vo.setConditionExpr(cond);
        return vo;
    }

    private static OrgUnitVO unit(Long id, String name, OrgType type, Long parentId, Long leaderId) {
        OrgUnitVO vo = new OrgUnitVO();
        vo.setId(id);
        vo.setName(name);
        vo.setType(type);
        vo.setParentId(parentId);
        vo.setLeaderEmployeeId(leaderId);
        return vo;
    }

    /** Employee 생성자는 id를 받지 않으므로(자동채번 대상) 리플렉션으로 테스트용 id를 주입한다. */
    private static Employee employee(Long id, String name, Position position, Long orgUnitId) {
        Employee employee = new Employee(name, position, orgUnitId, LocalDate.of(2020, 1, 1));
        try {
            Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return employee;
    }
}
