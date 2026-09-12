# ADR-017: 재고 차감 방식 (Module 5와의 일관성)

- 상태: 승인됨
- 범위: Module 6 (영업/매출관리), Module 5 `inventory_transaction` 확장

## 상황

Module 5(ADR-015)는 입고 시 `item.current_stock`을 파생 계산이 아니라 그 시점에 즉시 갱신하는 방식을 택했다("재고는 조회가 매우 잦아 이력 합산 비용이 아깝다"는 트레이드오프를 감수). Module 6의 수주 확정 시 재고 차감도 같은 컬럼(`item.current_stock`)을 다루므로, 입고와 출고가 서로 다른 방식(하나는 즉시 갱신, 하나는 파생 계산)을 쓰면 안 된다.

## 결정

**출고도 입고와 동일하게 즉시 갱신 방식을 쓴다.** 수주 확정 시점에 `item.current_stock`을 직접 감소시킨다.

```sql
UPDATE item SET current_stock = current_stock - #{quantity}
WHERE id = #{itemId} AND current_stock >= #{quantity}
```

`WHERE current_stock >= quantity` 가드를 붙여, 영향받은 행이 0건이면 재고 부족으로 판단해 `BusinessException`을 던진다 — 등록 시점에 이미 가용 재고를 확인했더라도(FR-6-2), 확정 시점 사이에 다른 수주가 먼저 확정되며 재고가 줄었을 수 있어 마지막 순간에 한 번 더, 이번엔 DB 갱신 자체를 조건부로 걸어 동시성까지 방어한다.

**`inventory_transaction` 테이블을 그대로 재사용**하고, 새 테이블을 만들지 않는다. `transaction_type`에 `ISSUE`(출고)를 추가하고, 이 트랜잭션이 어느 수주 때문에 발생했는지 가리키는 `sales_order_id`(nullable) 컬럼을 추가한다 — 기존 `purchase_request_id`와 같은 자리에, 방향만 다른 컬럼으로 둔다.

```sql
ALTER TABLE inventory_transaction ADD COLUMN sales_order_id BIGINT NULL;
ALTER TABLE inventory_transaction ADD CONSTRAINT fk_inventory_transaction_sales_order
    FOREIGN KEY (sales_order_id) REFERENCES sales_order (id);
```

```java
public enum InventoryTransactionType {
    RECEIPT,  // Module 5
    ISSUE     // Module 6 — 실제로 필요해진 지금 추가한다 (Module 5 ADR-014/ADR-016에서 예고한 대로)
}
```

`purchase_request_id`/`sales_order_id` 둘 다 nullable이고, `RECEIPT` 트랜잭션은 `purchase_request_id`만, `ISSUE` 트랜잭션은 `sales_order_id`만 채운다 — 하나의 범용 `reference_id`(폴리모픽 참조)로 합치지 않고 각각 명시적인 FK로 둔다. 참조 무결성을 DB가 보장하게 하려는 것이며, Module 5 설계 당시에도 폴리모픽 참조 대신 직접 FK를 택한 것과 같은 판단이다.

## 근거

- 같은 컬럼(`item.current_stock`)을 두 가지 다른 갱신 방식으로 다루면, 입고 트랜잭션과 출고 트랜잭션이 동시에 몰릴 때 어느 쪽 값이 "맞는" 값인지 알 수 없게 된다 — 파생 계산으로 통일하거나 즉시 갱신으로 통일해야 하고, Module 5가 이미 후자를 선택하고 그 트레이드오프를 감수했으므로 그대로 승계한다.
- 이력 테이블을 방향(입고/출고)에 따라 나누지 않고 하나로 유지하면, "이 품목의 전체 입출고 내역"을 한 쿼리로 볼 수 있다 — 실제 재고 관리 시스템의 원장(ledger)과 같은 모양이다.
- Module 5 설계 시점에 `ISSUE`를 미리 만들지 않고 "실제로 필요해지면 추가한다"고 명시해뒀던 것(ADR-014의 교훈 적용)이 지금 실현됐다 — 이것이 그 근거가 투기가 아니라 실제 계획이었음을 보여주는 사례다.

## 트레이드오프

- `item.current_stock`이 이력과 별개로 즉시 갱신되는 방식을 그대로 이어받았으므로, Module 5 ADR-015에서 이미 인정한 트레이드오프(이력과 어긋날 위험, 별도 정합성 배치 없음)가 Module 6에도 동일하게 적용된다.
- 동시성 가드(`WHERE current_stock >= quantity`)는 단일 행 단위 원자적 갱신에는 충분하지만, "여러 수주를 한꺼번에 확정"하는 배치 처리 같은 것은 다루지 않는다(이번 범위에 그런 기능이 없다).
