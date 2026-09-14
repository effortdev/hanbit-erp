# ADR-019: 발령(부서이동/승진) 처리 권한 검증

- 상태: 승인됨
- 범위: Module 1 (조직/사원관리, `egovframework.erp.hr`)

## 상황

일반 사원(STAFF) 계정으로 로그인해도 `/hr/emp/{id}/transfer`, `/hr/emp/{id}/promote`를 호출해 **임의의 사원**(자기 자신 포함)의 소속 조직과 직급을 바꿀 수 있다는 것이 발견됐다. `EmpController.transfer()`/`.promote()`는 `@PathVariable Long id`를 그대로 받아 `EmpService`에 넘길 뿐, 호출자가 그 사원에 대해 발령을 처리할 권한이 있는지 전혀 확인하지 않았다. 이 프로젝트 전체가 아직 역할 기반 접근 제어(RBAC)를 갖추고 있지 않다는 점은 ADR-015의 트레이드오프에도 이미 적혀 있었지만("로그인한 누구나 CONFIRMED 상태의 구매요청을 입고 처리할 수 있다"), 발령 처리는 단순 편의 기능이 아니라 **다른 모듈의 핵심 불변조건을 깨는 진입점**이라는 점에서 심각도가 다르다.

### 확인된 악용 경로

1. **자기 자신을 포함한 임의 사원의 직급 변경(자기 승진)**: `promote(id, newPosition, ...)`는 `id`가 호출자 본인인지 검사하지 않는다. 사원이 본인을 `TEAM_LEADER`/`DIVISION_HEAD`/`CEO`로 직접 승진시킬 수 있다.
2. **대표이사 유일성 불변조건 파괴 (가장 심각)**: `ApprovalLineResolverImpl.requireSingleCeo()`는 `position = CEO`인 사원이 정확히 1명이어야 한다고 가정하고, 아니면 `BusinessException`을 던진다(ADR-006). 어떤 사원이든 자기 자신을 `CEO`로 승진시키는 순간 CEO가 2명이 되어, 대표이사 결재가 필요한 모든 지출결의서·구매요청서(500만원 이상)의 기안 자체가 **회사 전체에서** 즉시 막힌다 — 데이터를 훔치거나 위조하는 공격이 아니라, 단 한 번의 요청으로 핵심 업무 흐름을 마비시키는 가용성 공격(DoS)이다.
3. **자기 자신을 포함한 임의 사원의 소속 조직 변경**: `transfer(id, newOrgUnitId, ...)`도 동일하게 대상 제한이 없다. 팀장/본부장이 아직 지정되지 않은(`leader_employee_id`가 NULL인) 조직으로 스스로 옮겨가는 것 자체는 결재라인 판별에 영향을 주지 않지만(아래 참고), 조직 배치를 조작해 다른 화면(예: 물류본부 소속만 구매요청 가능)의 소속 기반 검증을 우회할 수 있는지는 이번 조사에서 별도로 재확인했다 — `ApprovalServiceImpl.draftPurchase()`의 `requireLogisticsDivision()`은 기안 시점의 실제 `org_unit_id`를 그때그때 조회하므로, 발령 자체가 막히면 이 경로도 함께 막힌다.

### 확인했지만 현재는 도달 불가능한 경로

`OrgServiceImpl.assignLeader(orgUnitId, leaderEmployeeId)`가 존재해 "임의 사원을 특정 조직의 `leader_employee_id`로 지정"하는 기능 자체는 서비스 계층에 이미 있다. 하지만 전수 조사 결과 **이 메서드를 호출하는 컨트롤러/엔드포인트가 프로젝트 어디에도 없다** — `OrgController`는 조직 등록(`create`)과 조회(`tree`)만 제공한다. 즉 "사원이 스스로를 다른 조직의 리더로 지정"하는 시나리오는 코드상 가능성만 있고 **현재는 실제로 도달할 수 있는 경로가 없다**(HTTP로 노출된 적이 없음). 다만 향후 이 메서드를 컨트롤러에 연결할 때는 이번 ADR과 동일한 권한 검증을 반드시 함께 추가해야 한다 — 그때 가서 잊지 않도록 이 사실을 여기 기록해 둔다.

## 후보

1. **`@PreAuthorize` + Spring Security 메서드 보안** — `ErpUserDetails.getAuthorities()`가 지금은 전 사용자에게 `ROLE_USER` 하나만 부여하고 있어(ADR-005), 이 방식을 쓰려면 (a) 직급 기반 역할(`ROLE_TEAM_LEADER` 등)을 `ErpUserDetails`에 새로 설계하고, (b) `context-security.xml`에 `<global-method-security pre-post-annotations="enabled"/>`를 새로 추가해야 한다. 이 프로젝트에 지금까지 전혀 없던 인프라를 이번 수정 하나를 위해 새로 들이는 셈이라, ADR-012/ADR-014/ADR-016에서 반복해온 "근거 없이 미리 만들지 않는다" 원칙과 어긋난다.
2. **서비스 계층의 명시적 권한 검증** *(채택)* — 이 프로젝트가 이미 전 모듈에서 일관되게 써온 패턴이다: `ApprovalLineResolverImpl.requireLeader()`/`requireSingleCeo()`, `ApprovalServiceImpl`의 `requireLogisticsDivision()`, `PurchaseServiceImpl.requireConfirmed()`처럼 서비스 메서드 맨 앞에서 조건을 확인하고 위반 시 `BusinessException`을 던지는 방식. 새 인프라가 필요 없고, 기존 코드 읽는 사람이 바로 이해할 수 있는 동일한 모양이다.

## 결정

2번(서비스 계층 명시적 검증)을 채택한다. `EmpServiceImpl`에 `requireLeaderOrAbove(Position actorPosition, String action)` 가드를 추가하고, `transfer()`/`promote()` 맨 앞에서 호출한다.

```java
private void requireLeaderOrAbove(Position actorPosition, String action) {
    if (actorPosition.ordinal() < Position.TEAM_LEADER.ordinal()) {
        throw new BusinessException(action + "는 팀장급 이상만 처리할 수 있습니다.");
    }
}
```

`Position` enum은 이미 "오름차순 직급 순서"를 전제로 결재라인 자동구성(ADR-006)에 쓰이고 있으므로, 새 역할 체계를 만드는 대신 그 순서(`ordinal()`)를 그대로 재사용했다 — `STAFF < ASSISTANT_MANAGER < MANAGER < DEPUTY_GENERAL_MANAGER < TEAM_LEADER < DIVISION_HEAD < CEO`. "팀장급 이상"은 `TEAM_LEADER`부터이므로 `TEAM_LEADER.ordinal()`을 기준선으로 삼았다.

호출자의 직급(`actorPosition`)은 서비스가 `SecurityContextHolder`를 직접 참조하지 않고, 컨트롤러가 `SecurityUtils.currentUser().getPosition()`으로 꺼내 파라미터로 넘긴다 — `SecurityUtils`는 지금까지 프로젝트 전체에서 오직 `*Controller` 클래스에서만 쓰여왔고(`*ServiceImpl`에서 쓰인 적이 없음), 서비스 계층을 웹/보안 계층과 분리해 `EmpServiceImplTest`처럼 순수 Mockito 단위 테스트로 남겨두기 위함이다.

```java
// EmpController
empService.transfer(id, newOrgUnitId, SecurityUtils.currentUser().getEmployeeName(),
        SecurityUtils.currentUser().getPosition());
```

**"인사팀 소속이면 허용"은 채택하지 않았다.** 이 문제를 처음 재현한 계정(`staff1`, 최사원)이 실제로 인사팀 소속 사원이다 — 조직 소속만으로 허용하면 이번에 막으려는 바로 그 계정을 다시 통과시키게 된다. 그래서 소속이 아니라 **직급**을 기준선으로 삼았다("팀장급 이상"은 인사팀장(`leader1`)을 포함하면서 인사팀 소속 사원(`staff1`)은 배제한다).

## 근거

- `requireSingleCeo()`의 불변조건(대표이사 정확히 1명)이 깨지면 결재 시스템 전체가 마비된다는 것이 이번 조사에서 확인된 가장 심각한 리스크였다 — 우선순위를 높여 즉시 막아야 한다는 판단이 맞았다.
- 서비스 계층 검증은 진입점이 늘어나도(예: 향후 배치 작업, 다른 컨트롤러) 보호가 유지된다 — 컨트롤러에서만 막으면 서비스 메서드를 직접 호출하는 새 경로가 생겼을 때 다시 뚫릴 수 있다.
- 기존 `Position` 순서를 재사용해 새 개념(역할)을 도입하지 않았다 — ADR-006이 이미 검증한 전제를 그대로 확장한 것이라 일관성이 높다.

## 트레이드오프

- `EmpService.transfer()`/`.promote()`의 메서드 시그니처에 `Position actorPosition` 파라미터가 추가되어, 기존 호출부(`EmpController`, `EmpServiceImplTest`)를 함께 수정해야 했다.
- "팀장급 이상이면 회사의 어느 사원이든 발령시킬 수 있다"는 여전히 넓은 권한이다 — 예를 들어 인사팀장이 아닌 다른 팀의 팀장도 임의 사원을 발령시킬 수 있다. "인사팀 팀장만" 같은 더 좁은 제약은 이번 요구사항(사원의 자기 발령·자기 승진 악용 차단)을 넘어서는 범위라 이번에는 다루지 않았다 — 필요해지면 별도 ADR로 좁히면 된다.
- 여전히 이 프로젝트 전체에 RBAC 인프라(역할/권한 테이블, `@PreAuthorize`)가 없다는 근본적인 한계는 남아있다. ADR-015가 이미 지적한 "입고 처리 권한 제한 없음" 같은 다른 항목들도 이번과 같은 패턴(서비스 계층 명시적 검증)으로 하나씩 메워야 한다.
