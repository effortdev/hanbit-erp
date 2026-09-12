package egovframework.erp.approval.service;

import egovframework.erp.approval.domain.ApproverLevel;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.hr.domain.Employee;

import java.math.BigDecimal;
import java.util.List;

/**
 * 기안 유형별 결재라인을 실시간으로 계산한다 (FR-2-1, docs/adr/ADR-006).
 * "몇 단계인지"는 규칙 테이블, "누가인지"는 Module 1 조직도를 조회해서 채운다.
 */
public interface ApprovalLineResolver {

    /**
     * @param amount EXPENSE/PURCHASE가 아니면 null이어도 된다 (조건부 단계 판단에만 쓰인다)
     * @return 자기결재 스킵까지 반영된 최종 결재라인 (비어 있으면 상신 즉시 자동 승인 대상)
     */
    List<ResolvedStep> resolve(DocumentType documentType, Employee drafter, BigDecimal amount);

    record ResolvedStep(ApproverLevel approverLevel, Long approverEmployeeId) {
    }
}
