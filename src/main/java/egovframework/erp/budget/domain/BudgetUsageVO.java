package egovframework.erp.budget.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 조직 x 계정과목 단위의 소진 현황 (FR-4-3). used는 APPROVED+PENDING 합계다 (docs/adr/ADR-012). */
public class BudgetUsageVO {

    private final AccountCategory accountCategory;
    private final BigDecimal allocated;
    private final BigDecimal used;

    public BudgetUsageVO(AccountCategory accountCategory, BigDecimal allocated, BigDecimal used) {
        this.accountCategory = accountCategory;
        this.allocated = allocated;
        this.used = used;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public BigDecimal getAllocated() {
        return allocated;
    }

    public BigDecimal getUsed() {
        return used;
    }

    public BigDecimal getRemaining() {
        return allocated.subtract(used);
    }

    public BigDecimal getUsageRate() {
        if (allocated.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return used.divide(allocated, 4, RoundingMode.HALF_UP);
    }

    public boolean isWarning() {
        return getUsageRate().compareTo(new BigDecimal("0.8")) >= 0;
    }
}
