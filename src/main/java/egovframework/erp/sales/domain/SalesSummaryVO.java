package egovframework.erp.sales.domain;

import java.math.BigDecimal;

/** FR-6-4: 월별/부서별 매출 현황 (CONFIRMED 수주 집계). */
public class SalesSummaryVO {

    private String orgUnitName;
    private String yearMonth;
    private BigDecimal totalAmount;

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
