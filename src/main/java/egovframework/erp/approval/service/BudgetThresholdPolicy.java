package egovframework.erp.approval.service;

import java.math.BigDecimal;

/**
 * FR-2-4: 지출 금액이 부서 예산의 80%를 초과하는지 판단하는 훅.
 * Module 4(예산/지출관리)가 {@code BudgetThresholdPolicyImpl}로 실제 구현했다
 * (docs/adr/ADR-006, docs/adr/ADR-012). EXPENSE의 대표이사 단계 조건이 ADR-011에서
 * 금액 기준으로 바뀌어 지금은 이 정책을 소비하는 결재 규칙이 없지만, 계약은 유지한다.
 */
public interface BudgetThresholdPolicy {

    boolean isExceeded(Long orgUnitId, BigDecimal amount);
}
