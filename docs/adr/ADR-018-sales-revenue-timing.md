# ADR-018: 수주-매출 연동 시점

- 상태: 승인됨
- 범위: Module 6 (영업/매출관리)

## 상황

FR-6-3은 "수주 확정 시 재고가 자동 차감되고, 매출 데이터가 생성되어야 한다"고 요구한다. 재고 차감과 매출 생성이 같은 시점에 함께 일어나야 하는지, 아니면 세금계산서 발행 등 별도 확정 단계를 거쳐야 하는지 정해야 한다.

## 결정

**수주 확정(재고 차감) 시점에 매출도 함께, 하나의 트랜잭션으로 즉시 생성한다.** 세금계산서 발행 같은 별도 회계 확정 단계는 이번 범위에서 다루지 않는다.

```java
@Transactional
public void confirmOrder(Long orderId, Long employeeId) {
    SalesOrderVO order = requireRegistered(orderId); // REGISTERED 상태가 아니면 BusinessException
    int affected = itemMapper.decreaseStockIfAvailable(order.getItemId(), order.getQuantity()); // ADR-017
    if (affected == 0) {
        throw new BusinessException("가용 재고가 부족하여 수주를 확정할 수 없습니다."); // NFR-6-1
    }
    inventoryTransactionMapper.insertTransaction(
        InventoryTransactionVO.issue(order.getItemId(), order.getQuantity(), order.getId(), employeeId));
    salesOrderMapper.confirmOrder(orderId); // status=CONFIRMED, confirmed_at=now — 이 자체가 매출 데이터다
}
```

별도의 "매출" 테이블을 새로 만들지 않는다. `sales_order`에 `amount`(수량×단가)를 이미 들고 있고, `status=CONFIRMED`인 행들의 집합이 곧 매출 데이터다 — Module 4가 별도 잔액 컬럼 없이 `budget_expense_request`에서 소진율을 파생시킨 것과 같은 방식이다. FR-6-4(월별/부서별 매출 현황)는 `CONFIRMED` 상태의 `sales_order`를 `org_unit_id`와 `confirmed_at`의 연-월로 묶어 집계하는 조회로 구현한다.

## 근거

- 요구사항 문서(목업 기준)에 "수주확정 시 재고차감+매출생성"이 하나의 사건으로 안내되어 있어 문면 그대로 구현한다. 재고 차감과 매출 생성을 분리하면 "재고는 줄었는데 매출은 아직 안 잡힌" 어중간한 상태가 생길 수 있는데, 요구사항이 그런 중간 상태를 요구하지 않는다.
- 세금계산서 발행, 매출 확정 승인 같은 회계 프로세스를 넣지 않은 이유는 Module 4(`BudgetThresholdPolicy`)·Module 5(품목별 결재 조건)에서 반복 확인한 원칙과 같다 — `erp_requirements.md`의 Module 6 FR/NFR 어디에도 그런 별도 확정 단계를 요구하는 문장이 없다.
- 하나의 트랜잭션으로 묶은 것은 NFR 관점에서 "재고는 줄었는데 매출 기록이 실패해 사라지는" 정합성 문제를 막기 위함이다 — Module 1의 발령 처리(사원 정보 변경 + 이력 기록)와 같은 원자성 요구다.

## 근거 (재고 부족 차단)

가용 재고보다 많은 수주는 등록 시점(FR-6-2)과 확정 시점(ADR-017의 동시성 가드) 두 곳에서 막는다 — 이미 프론트에서 검증된 로직(목업의 NFR-6-1)을 백엔드 검증으로 그대로 옮긴 것이다. 등록 시점 검증은 사용자에게 즉각적인 피드백을 주기 위함이고, 확정 시점 검증은 그 사이 다른 수주가 먼저 확정되며 재고가 줄어드는 경합을 막기 위한 최종 방어선이다.

## 트레이드오프

- 매출 데이터가 별도 테이블 없이 `sales_order`에 얹혀 있어, 향후 "매출 취소/환불" 같은 요구가 생기면 `sales_order`의 상태 모델을 더 확장해야 한다(REGISTERED/CONFIRMED 외에 CANCELLED 등). 지금은 그런 요구가 없어 다루지 않는다.
- 세금계산서·회계 마감 같은 후속 프로세스가 전혀 없으므로, 이 모듈만으로는 실제 회계 시스템과 연동할 수 없다 — 포트폴리오 범위임을 감안한 의도적 단순화다.
