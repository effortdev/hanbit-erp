package egovframework.erp.approval.service;

import java.math.BigDecimal;

/**
 * FR-2-4: 지출 금액이 부서 예산의 80%를 초과하는지 판단하는 훅.
 * Module 4(예산/지출관리)가 아직 없으므로, 그 전까지는 {@code NoBudgetModuleYetPolicy}가
 * 항상 false를 반환한다. Module 4 구현 시 실제 예산 조회 구현체로 교체한다 (docs/adr/ADR-006).
 */
public interface BudgetThresholdPolicy {

    boolean isExceeded(Long orgUnitId, BigDecimal amount);
}
