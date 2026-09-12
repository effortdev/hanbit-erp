# ADR-015: 재고 반영 시점 (승인과 입고의 분리)

- 상태: 승인됨
- 범위: Module 5 (재고/구매관리)

## 상황

FR-5-2(구매요청서 승인 완료 시 구매 내역 생성), FR-5-3(입고 처리 시 재고 증가)은 서로 다른 시점의 이벤트다. 구매요청서가 결재 승인됐다고 해서 실물 품목이 곧바로 창고에 들어온 것은 아니므로, "언제" 재고를 늘릴지 명확히 정해야 한다.

## 결정

**구매요청서 승인 완료 ≠ 재고 증가.** 재고는 별도의 "입고 처리" 액션에서만 늘어난다.

### 상태 흐름

```
상신 → (전자결재 승인/반려) → 구매확정(=입고대기) → 입고완료(재고 반영)
```

`purchase_request.status`는 4가지 실제 값으로 관리한다: `PENDING`(상신, 결재 진행 중) → `CONFIRMED`(결재 승인 완료 = 구매확정 = 입고대기) → `RECEIVED`(입고완료) / `REJECTED`(반려).

> **구현 노트**: "구매확정"과 "입고대기"는 화면(진행상태 트래킹)에는 두 단계로 보여주지만, 그 사이에 별도로 트리거되는 시스템 액션이 없어(발주 전송 같은 중간 단계가 요구사항에 없음) 실제 컬럼값은 `CONFIRMED` 하나로 합쳤다. Module 1의 `DocumentStatusCalculator`(승인 로그에서 상태를 파생)와 같은 원칙 — 트리거가 없는 상태를 별도 컬럼으로 만들지 않고, 화면 표시 단계에서만 "구매확정(입고대기 중)"으로 풀어서 보여준다.

승인/반려는 Module 2의 `DocumentApprovedEvent`/`DocumentRejectedEvent`를 구독하는 `PurchaseApprovalListener`(Module 3의 `AttendanceApprovalListener`, Module 4의 `ExpenseApprovalListener`와 동일한 패턴, `@TransactionalEventListener(AFTER_COMMIT)`)가 받아 `PENDING → CONFIRMED/REJECTED`로 갱신한다.

### 입고 처리

```java
@Transactional
public void receiveGoods(Long purchaseRequestId, Long employeeId) {
    PurchaseRequestVO request = requireConfirmed(purchaseRequestId); // CONFIRMED가 아니면 BusinessException
    itemMapper.increaseStock(request.getItemId(), request.getQuantity());
    inventoryTransactionMapper.insertTransaction(
        InventoryTransactionVO.receipt(request.getItemId(), request.getQuantity(), request.getId(), employeeId));
    purchaseRequestMapper.updateStatus(purchaseRequestId, PurchaseRequestStatus.RECEIVED);
}
```

`inventory_transaction`은 append-only 이력이다(Module 1 `emp_history`, Module 2 `approval_action`과 동일한 패턴 — update/delete 매퍼를 두지 않는다). `item.current_stock`은 이 이력과 별개로 존재하는 "현재 값" 컬럼이며(Module 1의 `employee.position`처럼 현재 상태), 매 조회마다 이력을 합산해 파생시키지 않고 트랜잭션 처리 시점에 함께 갱신한다 — 재고 수량은 조회가 매우 잦고 이력 합산 비용이 아까운 대표적인 케이스라, 다른 모듈(연차 잔여, 예산 소진율)과 달리 이번에는 파생 계산 대신 즉시 갱신 방식을 택했다(트레이드오프 참고).

## 근거

- 영업/매출관리(Module 6, FR-6-3)의 "수주 확정 시 재고 차감"과 대칭을 이루는 흐름이다 — 양쪽 다 "결재/확정"이라는 서류상의 사건과 "실물이 실제로 움직이는" 사건을 분리해서, 재고가 항상 실물 이벤트에만 반응하도록 만든다.
- 결재 승인 시점에 바로 재고를 늘리면, 발주는 났지만 아직 도착하지 않은 물량까지 재고로 잡혀 실물과 장부가 어긋난다 — As-Is 문제("실물 재고와 장부 재고 간 불일치 발생")를 그대로 재현하게 된다.

## 트레이드오프

- `item.current_stock`을 이력과 별개로 즉시 갱신하기 때문에, 이력 테이블과 `current_stock`이 어긋나는 버그가 생기면(예: 트랜잭션 일부만 커밋) 서로 다른 값을 보게 될 위험이 있다. 다른 모듈처럼 완전히 파생시키지 않은 대가다 — 이번 범위에서는 `receiveGoods`를 단일 트랜잭션으로 묶는 것으로 충분하다고 보고 별도 정합성 배치는 두지 않는다.
- 입고 처리에 별도 권한 제한을 두지 않았다(재고관리팀만 가능하도록 제한하지 않음) — 이 프로젝트 전체에 아직 역할 기반 접근 제어(RBAC)가 없어서, 로그인한 누구나 `CONFIRMED` 상태의 구매요청을 입고 처리할 수 있다. 실제 운영에서는 재고관리팀으로 제한해야 한다.
