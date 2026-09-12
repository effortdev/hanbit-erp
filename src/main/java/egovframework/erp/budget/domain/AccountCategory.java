package egovframework.erp.budget.domain;

/** 사전 정의된 계정과목(비용 항목) — 자유 텍스트 대신 열거형으로 제한한다 (docs/adr/ADR-013). */
public enum AccountCategory {
    LABOR("인건비"),
    TRAVEL("출장비"),
    SUPPLIES("소모품비"),
    ENTERTAINMENT("접대비"),
    OTHER("기타");

    private final String label;

    AccountCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
