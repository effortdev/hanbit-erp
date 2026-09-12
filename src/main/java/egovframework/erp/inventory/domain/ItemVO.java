package egovframework.erp.inventory.domain;

/** 품목 마스터. 품목별 결재 조건 등은 두지 않는다 (docs/adr/ADR-014). */
public class ItemVO {

    private Long id;
    private String name;
    private String unit;
    private int currentStock;
    private int safetyStock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public int getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(int safetyStock) {
        this.safetyStock = safetyStock;
    }

    /** 안전재고 이하로 떨어졌는지 (docs/adr/ADR-016). */
    public boolean isBelowSafetyStock() {
        return currentStock <= safetyStock;
    }
}
