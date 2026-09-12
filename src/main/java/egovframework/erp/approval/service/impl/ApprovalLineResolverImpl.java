package egovframework.erp.approval.service.impl;

import egovframework.erp.approval.domain.ApprovalCondition;
import egovframework.erp.approval.domain.ApprovalLineRuleVO;
import egovframework.erp.approval.domain.ApproverLevel;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.mapper.ApprovalLineRuleMapper;
import egovframework.erp.approval.service.ApprovalLineResolver;
import egovframework.erp.approval.service.BudgetThresholdPolicy;
import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.EmployeeStatus;
import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 결재라인 판별 로직의 실제 구현체 (docs/adr/ADR-006).
 */
@Service
public class ApprovalLineResolverImpl implements ApprovalLineResolver {

    private final ApprovalLineRuleMapper ruleMapper;
    private final OrgMapper orgMapper;
    private final EmployeeRepository employeeRepository;
    private final BudgetThresholdPolicy budgetThresholdPolicy;

    @Autowired
    public ApprovalLineResolverImpl(ApprovalLineRuleMapper ruleMapper,
                                     OrgMapper orgMapper,
                                     EmployeeRepository employeeRepository,
                                     BudgetThresholdPolicy budgetThresholdPolicy) {
        this.ruleMapper = ruleMapper;
        this.orgMapper = orgMapper;
        this.employeeRepository = employeeRepository;
        this.budgetThresholdPolicy = budgetThresholdPolicy;
    }

    @Override
    public List<ResolvedStep> resolve(DocumentType documentType, Employee drafter, BigDecimal amount) {
        List<ApprovalLineRuleVO> rules = ruleMapper.selectByDocumentType(documentType);
        List<ResolvedStep> resolved = new ArrayList<>();

        for (ApprovalLineRuleVO rule : rules) {
            if (rule.getConditionExpr() == ApprovalCondition.BUDGET_80_EXCEEDED
                    && !budgetThresholdPolicy.isExceeded(drafter.getOrgUnitId(), amount)) {
                continue; // 조건 미충족 - 이 단계는 결재라인에서 제외 (FR-2-4)
            }

            Long approverId = resolveApprover(rule.getApproverLevel(), drafter);

            if (approverId.equals(drafter.getId())) {
                continue; // 자기결재 스킵 (docs/adr/ADR-006)
            }
            resolved.add(new ResolvedStep(rule.getApproverLevel(), approverId));
        }
        return resolved;
    }

    private Long resolveApprover(ApproverLevel level, Employee drafter) {
        switch (level) {
            case TEAM_LEADER:
                return requireLeader(drafterTeam(drafter), "팀장");
            case DIVISION_HEAD:
                return requireLeader(divisionOf(drafterTeam(drafter)), "본부장");
            case CEO:
                return requireSingleCeo();
            default:
                throw new IllegalStateException("알 수 없는 결재자 레벨: " + level);
        }
    }

    private OrgUnitVO drafterTeam(Employee drafter) {
        OrgUnitVO unit = orgMapper.selectOrgUnit(drafter.getOrgUnitId());
        if (unit == null) {
            throw new BusinessException("기안자의 소속 조직을 찾을 수 없습니다.");
        }
        if (unit.getType() != OrgType.TEAM) {
            // 본부장/대표이사 등 본부 직속 사원의 결재라인은 이번 범위에서 다루지 않는다 (docs/adr/ADR-006 한계).
            throw new BusinessException("본부 직속 사원의 결재라인 자동구성은 아직 지원하지 않습니다: " + unit.getName());
        }
        return unit;
    }

    private OrgUnitVO divisionOf(OrgUnitVO team) {
        OrgUnitVO hq = orgMapper.selectOrgUnit(team.getParentId());
        if (hq == null) {
            throw new BusinessException(team.getName() + "의 상위 본부를 찾을 수 없습니다.");
        }
        return hq;
    }

    private Long requireLeader(OrgUnitVO orgUnit, String roleLabel) {
        Long leaderId = orgUnit.getLeaderEmployeeId();
        if (leaderId == null) {
            throw new BusinessException(orgUnit.getName() + "의 " + roleLabel + "이(가) 지정되지 않아 결재라인을 구성할 수 없습니다.");
        }
        return leaderId;
    }

    private Long requireSingleCeo() {
        List<Employee> ceos = employeeRepository.findByPositionAndStatus(Position.CEO, EmployeeStatus.ACTIVE);
        if (ceos.size() != 1) {
            throw new BusinessException("대표이사가 정확히 1명 지정되어 있지 않습니다 (현재 " + ceos.size() + "명). 설정을 확인하세요.");
        }
        return ceos.get(0).getId();
    }
}
