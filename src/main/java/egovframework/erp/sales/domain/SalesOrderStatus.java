package egovframework.erp.sales.domain;

/** 수주 상태. 전자결재를 거치지 않는 2단계 흐름이다 (docs/adr/ADR-018). */
public enum SalesOrderStatus {
    REGISTERED,
    CONFIRMED
}
