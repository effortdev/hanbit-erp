package egovframework.erp.approval.domain;

/** 기안 유형 (FR-2-1). */
public enum DocumentType {
    VACATION("휴가신청"),
    EXPENSE("지출결의서"),
    PURCHASE("구매요청서"),
    GENERAL("품의서");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
