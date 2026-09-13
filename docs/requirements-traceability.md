# 요구사항 추적 매트릭스 (Requirements Traceability Matrix)

`docs/erp_requirements.md`의 FR/NFR 전체 항목을 실제 코드와 대조한 점검 결과다.
점검일: 2026-09-13. 점검 방식: 코드 직접 읽기 + ADR 대조(코드가 요구사항과 다르게
동작하는 경우, 그 결정이 ADR로 문서화되어 있는지까지 확인).

상태 값:
- **구현됨**: 요구사항 문구대로 동작함
- **부분구현**: 요구사항의 일부만 충족하거나, 다른 방식으로 충족함(동등하지 않을 수 있음)
- **미구현**: 코드에 해당 기능/제약이 없음
- **해당없음**: 이번 점검 범위에 해당하지 않음

---

## 모듈 1. 조직/사원관리

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-1-1 | 구현됨 | `OrgServiceImpl.registerOrgUnit()` — HQ는 parent 불가, TEAM은 parent가 HQ여야 함을 검증 (docs/adr/ADR-004) | 2단계 구조가 DB 제약이 아니라 서비스 계층 검증으로 강제됨 |
| FR-1-2 | 부분구현 | `EmpServiceImpl.register/transfer/promote`, `EmpController` | 직급(`promote`)·소속부서(`transfer`) 수정 경로는 있으나, **입사일(hireDate)을 수정하는 API/화면이 전혀 없음** — `Employee` 엔티티에 setter조차 없음 |
| FR-1-3 | 구현됨 | `EmpServiceImpl.transfer()`/`promote()` — `Employee` 변경과 `EmpHistoryMapper.insertHistory()`가 동일 `@Transactional` 안에서 실행 | `EmpHistoryMapper`는 insert/select만 존재 (append-only 확인) |
| FR-1-4 | 구현됨 | approval/attendance/budget/inventory/sales 전 모듈이 `EmployeeRepository`/`OrgMapper`를 직접 참조 (예: `ApprovalServiceImpl`, `ExpenseServiceImpl`) | 어느 모듈도 employee/org_unit을 자체 테이블로 복제하지 않음 (mapper XML은 전부 JOIN, 별도 INSERT 대상 아님) |
| NFR-1-1 | 구현됨 | 위와 동일 — 6개 모듈 전부 동일한 `org_unit`/`employee` 테이블을 단일 소스로 참조 | |

---

## 모듈 2. 전자결재

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-2-1 | 구현됨 | `approval_line_rule` 시드 데이터 + `ApprovalLineResolverImpl.resolve()` | VACATION/GENERAL은 요구사항 표와 정확히 일치. **EXPENSE/PURCHASE는 아래 FR-2-4 참고 — 결재라인 자체는 자동 구성되지만, 원안과 다른 조건(금액 구간)으로 구성됨** |
| FR-2-2 | 구현됨 | `ApprovalServiceImpl.act()`, `ApprovalController.reject()`(`comment` 파라미터 필수) | 사소한 틈: HTML `required` + 컨트롤러 파라미터 필수 처리는 있으나, 서비스 계층에서 공백 문자열(`" "`)까지 막는 검증은 없음 |
| FR-2-3 | 구현됨 | `DocumentStatusCalculator.calculate()` — WAITING/IN_PROGRESS/APPROVED/REJECTED 판정, `ApprovalServiceImpl.getDocument()`로 조회 | |
| FR-2-4 | **미구현** (원안 기준) | `ApprovalLineResolverImpl.isConditionMet()` — `AMOUNT_GTE_5M`(500만원 이상)이면 CEO 단계 추가. 예산 소진율(80%)은 결재라인 구성에 전혀 관여하지 않음 | ADR-011/ADR-012에 의도적 대체로 문서화되어 있음. `BudgetThresholdPolicy`/`BUDGET_80_EXCEEDED`는 코드 감사 중 삭제됨 (docs/adr/ADR-012 "후속 기록 — 폐기됨"). **요구사항 문서 원문과 실제 동작이 다름 — 우선순위 결정 필요** |
| FR-2-5 | 구현됨 | `AttendanceApprovalListener.onApproved()` (`@TransactionalEventListener(AFTER_COMMIT)`) | |
| FR-2-6 | 구현됨 (해석 차이 있음) | `PurchaseApprovalListener.onApproved()` | "구매 내역 생성"이 승인 시점의 신규 INSERT가 아니라, 기안 시점에 이미 만들어진 PENDING 행의 상태를 CONFIRMED로 전환하는 방식. 결과적으로 동일한 정보를 제공하지만 문구상 "생성" 시점이 다름 |
| NFR-2-1 | **미구현** | 코드 전체에 알림/이메일/웹소켓/SSE 관련 구현 없음 (`notification`, `알림`, `websocket`, `email` grep 결과 없음) | "내 결재함(inbox)"을 직접 열람해야만 상태 변화를 알 수 있음 — 준실시간 알림도 없음 |
| NFR-2-2 | 구현됨 | `ApprovalServiceImpl.getDocument()` — 기안자도 결재자도 아니면 `BusinessException` | |
| NFR-2-3 | 구현됨 | `ApprovalActionMapper`(insert/select만), `ApprovalActionMapper.xml`에 update/delete 미정의 | |

---

## 모듈 3. 근태관리

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-3-1 | 구현됨 | `AttendanceServiceImpl.checkIn()/checkOut()/getMyRecords()` | 중복 출근/퇴근 없이 처리 전 방어 |
| FR-3-2 | 구현됨 | `VacationServiceImpl.requestVacation()` → `ApprovalService.draftVacation()` 직접 호출, `AttendanceApprovalListener`가 승인 이벤트 구독 | |
| FR-3-3 | 구현됨 | `VacationBalanceServiceImpl.getRemainingDays()` = 연차일수 - APPROVED합 - PENDING합 | |
| NFR-3-1 | 구현됨 (트레이드오프 문서화됨) | `AttendanceApprovalListener`가 `AFTER_COMMIT`으로 상태 반영 | 결재 트랜잭션과 근태 반영은 별도 트랜잭션(최终적 일관성). 리스너 실패 시 어긋날 수 있음이 ADR-008에 트레이드오프로 이미 기술되어 있음 |

---

## 모듈 4. 예산/지출관리

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-4-1 | 구현됨 | `BudgetAllocationServiceImpl.allocate()`/`getUsage()` | 조직/계정과목/연도 단위 배정, 중복 배정 방지 |
| FR-4-2 | 구현됨 (파생 계산 방식) | `ExpenseApprovalListener.onApproved()` → 상태를 APPROVED로 전환. 소진율은 `sumAmountByStatuses(APPROVED+PENDING)`로 매번 계산 | "차감"을 별도 잔액 컬럼 갱신이 아니라 원천 데이터 합산으로 구현 (다른 모듈과 동일한 설계 원칙) |
| FR-4-3 | 구현됨 | `BudgetAllocationServiceImpl.getUsage()` → `BudgetUsageVO`, `budget/usage.jsp` | |
| FR-4-4 | **미구현** (원안 기준) | `ExpenseServiceImpl.requestExpense()` — 80~100%는 경고만 표시하고 결재 단계를 추가하지 않음. 결재 단계 추가는 오직 금액(500만원)으로만 결정됨(`ApprovalLineResolverImpl`) | FR-2-4와 동일한 원인. ADR-011/012에 의도적 결정으로 문서화됨 |
| NFR-4-1 | 부분구현 | `ExpenseServiceImpl.requestExpense()`(기안 시점 동기 검증) + `ExpenseApprovalListener`(승인 후 `AFTER_COMMIT` 비동기 상태 전환) | 문구상 "지출결의 승인 트랜잭션과 함께 원자적으로"는 아님(다른 트랜잭션) — 다만 PENDING 상태도 소진율 합산에 포함되므로 실제 과다지출 방지 효과는 기안 시점에 이미 동기적으로 보장됨. 순수 문구 기준으로는 미충족 |

---

## 모듈 5. 재고/구매관리

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-5-1 | **부분구현** | `ItemController.list()` → `item.currentStock`만 조회 가능. `InventoryTransactionMapper`에는 **select 메서드 자체가 없음**(insert만 존재) | 입고/출고 트랜잭션은 `inventory_transaction` 테이블에 쌓이고 있지만, 이를 조회하는 서비스/컨트롤러/화면이 전혀 없어 "현재고"만 확인 가능하고 "입고/출고" 내역은 조회 불가 |
| FR-5-2 | 구현됨 | `PurchaseApprovalListener.onApproved()` → CONFIRMED 전환 (FR-2-6과 동일 근거/동일 해석차이) | |
| FR-5-3 | 구현됨 | `PurchaseServiceImpl.receiveGoods()` → `itemMapper.increaseStock()` + `InventoryTransactionVO.receipt()` | |
| FR-5-4 | 구현됨 | `SalesOrderServiceImpl.confirmOrder()` → `itemMapper.decreaseStockIfAvailable()` (Module 6 쪽 구현, Module 5 자원 재사용) | |
| NFR-5-1 | 구현됨 | `ItemMapper.xml` `decreaseStockIfAvailable`: `UPDATE ... WHERE current_stock >= #{quantity}` (조건부 원자적 UPDATE) | DB 레벨에서 음수 재고 원천 차단 |

---

## 모듈 6. 영업/매출관리

| ID | 상태 | 근거 | 비고 |
|---|---|---|---|
| FR-6-1 | 구현됨 | `CustomerServiceImpl.registerCustomer()`/`getAllCustomers()` | |
| FR-6-2 | 구현됨 | `SalesOrderServiceImpl.registerOrder()` — `quantity > item.getCurrentStock()`이면 등록 시점에 차단 | |
| FR-6-3 | 구현됨 | `SalesOrderServiceImpl.confirmOrder()` — 재고 차감 + `salesOrderMapper.confirmOrder()`(상태를 CONFIRMED로) 같은 트랜잭션 | "매출 데이터 생성"은 별도 테이블이 아니라 CONFIRMED 상태의 `sales_order` 행 자체 (ADR-018, 원천 데이터 파생 원칙) |
| FR-6-4 | 구현됨 | `SalesOrderMapper.xml` `selectMonthlySummary()` — `org_unit`별 + `DATE_FORMAT(confirmed_at,'%Y-%m')`별 집계 | |
| NFR-6-1 | 부분구현 | 등록 시점 사전 차단(FR-6-2) + 확정 시점 `decreaseStockIfAvailable` 동시성 가드(ADR-017) | 이중 방어(사전 체크 + DB 조건부 UPDATE)이나, 등록 시점 체크가 동시 등록된 다른 미확정 수주를 반영 못함 — 아래 격차 8번 참고 |

---

## 발견된 주요 격차 (우선순위 논의 필요)

아래는 사소한 문구 차이가 아니라 **실제 동작이 요구사항 원문과 다르거나, 요구사항이 요구하는 기능 자체가 없는** 항목이다. 심각도 순으로 정리했다.

### 1. [High] FR-2-4 / FR-4-4 — "예산 80% 초과 시 대표이사 결재 추가"가 구현되지 않음
- **요구사항 원문**: 지출결의서 금액이 **부서 예산의 80%를 초과**하면 대표이사 결재 단계 추가 (기업 프로필 표에도 동일하게 명시)
- **실제 동작**: 결재 단계는 오직 **지출 금액 자체**(500만원 이상)로만 결정됨. 예산 소진율은 80~100% 구간에서 "경고 표시"만 하고, 100% 이상은 상신을 차단(500만원 이상 고액 건은 사유 입력 시 예외 허용)할 뿐, 결재라인에는 전혀 관여하지 않음
- **근거**: ADR-011, ADR-012 (의도적 결정, 이미 문서화됨). 다만 `erp_requirements.md` 자체는 갱신되지 않아 "문서상 요구사항"과 "실제 구현"이 서로 다른 이야기를 하는 상태
- **영향**: 포트폴리오 심사 시 요구사항 정의서와 실제 화면/코드를 나란히 볼 경우, "왜 요구사항과 다르게 만들었는지" 설명이 필요한 지점. 이미 ADR로 근거는 있으나, 요구사항 문서 자체에 실제 채택안이 반영되어 있지 않음

### 2. [High] 구매요청서 결재라인이 "물류본부는 항상 3단계"에서 "금액에 따라 1~3단계"로 축소됨
- **요구사항 원문**: 기업 프로필 표 — "구매요청서 | 팀장 → 본부장 → 대표이사 | 물류본부 한정" (금액 조건 없음, 항상 3단계)
- **실제 동작**: `ApprovalLineResolverImpl` + `approval_line_rule` 시드 — EXPENSE와 동일한 금액 구간(100만/500만원)을 그대로 재사용. 100만원 미만 구매요청서는 팀장 단독 승인으로 끝남
- **근거**: ADR-014 (의도적 결정, EXPENSE 패턴 재사용이 근거). 다만 이 ADR도 "품목별 조건 미도입"에 대한 트레이드오프만 논의하고, 프로필 표의 "항상 3단계" 문구와의 직접적 충돌은 명시적으로 짚지 않음
- **영향**: 위와 동일한 성격 — 요구사항 문서와 실제 구현이 다른데, 문서 쪽이 갱신되지 않음

### 3. [Medium] FR-5-1 — 재고 입고/출고 이력 조회 기능이 전혀 없음
- **요구사항 원문**: "품목별 재고 현황(**입고/출고**/현재고)을 조회할 수 있어야 한다"
- **실제 동작**: `/inventory` 목록 화면은 현재고만 보여줌. `InventoryTransactionMapper`에 select 메서드 자체가 없어 입고/출고 이력을 조회하는 코드가 아예 존재하지 않음(데이터는 `inventory_transaction` 테이블에 쌓이고 있음 — 조회 기능만 빠짐)
- **영향**: 순수 누락으로 보임(의도적 축소를 뒷받침하는 ADR 없음). 구현 비용이 낮은 편(select 메서드 + 화면 한 개 추가 수준)

### 4. [Medium] NFR-2-1 — 결재 상태 변경 알림(실시간/준실시간)이 전혀 없음
- **요구사항 원문**: "결재 상태 변경 시 관련자에게 실시간(또는 준실시간)으로 알림이 전달되어야 한다"
- **실제 동작**: 알림 메커니즘 자체가 없음(이메일/웹소켓/폴링 배지 등 전무). 사용자가 결재함 화면에 직접 들어와야만 상태를 확인할 수 있음
- **영향**: 포트폴리오 범위에서 흔히 생략되는 항목이지만, 명시적 NFR이므로 "왜 안 했는지"에 대한 답이 필요(예: 범위 외 처리 ADR 작성)

### 5. [Medium] FR-2-2 — 반려 사유 필수 입력이 서버단에서 강제되지 않음
- **요구사항 원문**: "결재자는 승인/반려 처리를 할 수 있고, **반려 시 사유를 입력해야 한다**"
- **실제 동작**: `approval/detail.jsp`의 HTML `required` 속성과 `ApprovalController.reject()`의 `@RequestParam String comment` 파라미터 필수 처리만 있고, 서비스 계층(`ApprovalServiceImpl.act()`)과 DB(`approval_action.comment VARCHAR(500) NULL`, NOT NULL 아님) 어디에도 공백/빈 문자열 검증이 없음. curl 등으로 `comment=`(빈 값)를 직접 POST하면 사유 없이 반려 처리가 그대로 성공함
- **근거**: `approval/web/ApprovalController.java`(reject 메서드), `approval/service/impl/ApprovalServiceImpl.java`(act 메서드), `sql/schema.sql`(approval_action 테이블 정의)
- **영향**: 브라우저 UI로만 쓰면 드러나지 않지만, 요구사항이 명시한 "필수" 제약이 서버단에서 전혀 보장되지 않는 실질적 검증 공백. 구현 비용이 낮은 편(서비스 계층에 `StringUtils.hasText()` 체크 한 줄 + DB NOT NULL 제약 추가)

### 6. [Low] NFR-4-1 — 예산 차감이 "지출결의 승인 트랜잭션과 원자적"이지 않음
- **요구사항 원문**: "예산 차감은 지출결의 승인 트랜잭션과 함께 원자적으로 처리되어야 한다(정합성 보장)"
- **실제 동작**: 승인 트랜잭션 커밋 후 `AFTER_COMMIT` 이벤트로 별도 트랜잭션에서 상태를 전환(다른 모든 모듈과 동일한 아키텍처 패턴, ADR-008). 다만 PENDING 상태도 소진율 계산에 포함되므로 과다지출 자체는 기안 시점에 동기적으로 이미 막혀 있어 **기능적 결과는 크게 다르지 않음**
- **영향**: 문구상으로는 미충족이지만, 이 프로젝트 전체가 채택한 "실패 도메인 분리" 아키텍처 원칙과 직접 상충되는 항목이라 NFR 문구를 수정하거나, 이 프로젝트의 아키텍처 원칙을 예외적으로 깨야 함 — 어느 쪽이든 의도적 판단이 필요

### 7. [Low] FR-1-2 — 사원의 입사일(hireDate)을 수정하는 기능이 없음
- **요구사항 원문**: "사원 정보(직급, 소속 부서, 입사일)를 등록/수정/**조회**할 수 있어야 한다"
- **실제 동작**: 직급/소속부서는 발령(승진/전보) 형태로 수정 가능하지만, 입사일은 등록 시점 이후 수정할 방법이 없음(`Employee` 엔티티에 setter 없음, 서비스/컨트롤러에 관련 메서드 없음)
- **영향**: 실무상 드문 케이스(오탈자 정정 정도)라 우선순위는 낮음

### 8. [Low] NFR-6-1 — 수주 등록 시점 재고 체크가 "동시 등록된 미확정 수주"를 반영하지 못함
- **요구사항 원문**: "가용 재고보다 많은 수량의 수주는 시스템이 **사전에** 차단해야 한다"
- **실제 동작**: `SalesOrderServiceImpl.registerOrder()`는 등록 시점에 품목의 실물 현재고(`item.getCurrentStock()`)만 비교하고, 아직 CONFIRMED되지 않은 다른 REGISTERED 수주가 이미 그 재고를 사실상 예약해둔 상태는 고려하지 않는다. 같은 품목에 대해 재고보다 많은 수량의 수주가 여러 건 동시에 REGISTERED로 등록될 수 있음(각 건은 개별적으로는 재고 이내라 통과)
- **영향**: 실제 음수 재고나 이중 차감은 발생하지 않음 — 확정(`confirmOrder()`) 시점의 `decreaseStockIfAvailable()` 조건부 UPDATE가 최종 안전장치로 작동해 물리적 초과 판매는 100% 방지됨(NFR-5-1과 동일 메커니즘). 다만 "등록은 됐는데 나중에 확정이 막히는" UX상의 사전 차단 실패 케이스는 발생 가능 — 데이터 무결성 문제가 아니라 사용자 경험 개선 항목에 가까워 우선순위는 낮음

---

## 요구사항에 없는데 추가된 것 (참고, 차단 요소 아님)

- **재고 안전재고 경고**(`inventory/itemList.jsp`의 "⚠ 안전재고 이하" 배지, `item.safetyStock` 필드): FR/NFR 어디에도 안전재고 임계값 경고가 명시되어 있지 않음. 다만 사용자 행동을 막는 제약이 아니라 순수 정보성 표시라 리스크는 없음
- **조직 리더 지정(`OrgServiceImpl.assignLeader`)**: 요구사항에 직접 명시되지 않았지만, FR-2-1(결재라인 자동구성)이 "팀장/본부장이 누구인지" 알아야 하므로 사실상 필수적인 지원 기능. 문제 없음

---

*이 문서는 코드가 바뀔 때마다 갱신되는 문서가 아니라, 2026-09-13 시점의 스냅샷이다. 이후 요구사항이나 코드가 바뀌면 재점검이 필요하다.*
