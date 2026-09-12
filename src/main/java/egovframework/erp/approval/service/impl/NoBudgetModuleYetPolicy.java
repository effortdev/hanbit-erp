package egovframework.erp.approval.service.impl;

import egovframework.erp.approval.service.BudgetThresholdPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Module 4(예산/지출관리) 구현 전까지 사용하는 기본 구현. 항상 false를 반환하므로 이 기간 동안
 * FR-2-4(예산 80% 초과 시 대표이사 결재 추가)는 실질적으로 비활성 상태다 (docs/adr/ADR-006).
 */
@Component
public class NoBudgetModuleYetPolicy implements BudgetThresholdPolicy {

    private static final Logger log = LoggerFactory.getLogger(NoBudgetModuleYetPolicy.class);

    @Override
    public boolean isExceeded(Long orgUnitId, BigDecimal amount) {
        log.warn("Module 4(예산/지출관리) 미구현 - 예산 소진율 검사를 건너뛰고 false로 처리합니다. orgUnitId={}, amount={}",
                orgUnitId, amount);
        return false;
    }
}
