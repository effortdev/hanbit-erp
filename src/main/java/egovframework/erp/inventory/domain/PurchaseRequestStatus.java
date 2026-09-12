package egovframework.erp.inventory.domain;

/**
 * 구매요청서 상태 (docs/adr/ADR-015). CONFIRMED는 "구매확정"과 "입고대기"를 함께 나타낸다
 * — 그 사이에 별도로 트리거되는 액션이 없어 컬럼을 나누지 않았다.
 */
public enum PurchaseRequestStatus {
    PENDING,
    CONFIRMED,
    RECEIVED,
    REJECTED
}
