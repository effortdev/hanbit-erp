# ADR-006: 결재라인 자동구성 로직

- 상태: 승인됨
- 범위: Module 2 (전자결재, `egovframework.erp.approval`)

## 상황

FR-2-1: 기안 유형(휴가신청/지출결의서/구매요청서/품의서)별로 결재라인이 자동 구성되어야 한다. 요구사항 문서의 결재라인 표를 보면 두 가지 성격이 섞여 있다.

- **"몇 단계를, 어떤 조건에서" 거치는가**: 기안 유형마다 고정이거나 조건부다 (예: 지출결의서는 금액이 예산의 80%를 넘으면 대표이사 단계가 추가됨 — FR-2-4).
- **"실제로 누가" 결재하는가**: 이건 조직 구조와 발령 이력에 따라 계속 바뀐다 (Module 1의 `emp_history`가 이미 이 변화를 추적하고 있음).

## 후보

1. **완전 동적 산출** (결재라인을 코드/규칙 없이 매번 조직도만 보고 계산) — "몇 단계인지"를 표현할 곳이 없다. 지출결의서의 조건부 대표이사 단계(FR-2-4)처럼 기안 유형·상황에 따라 단계 수 자체가 달라지는 경우를 표현하지 못한다.
2. **완전 정적 테이블** (기안 유형별로 결재자 이름/사번까지 미리 박아둔 테이블) — 발령(부서이동/승진)이 날 때마다 이 테이블을 일일이 갱신해야 한다. Module 1이 발령 이력을 자동 반영하도록 설계한 의미(FR-1-3, FR-1-4)가 완전히 무력화된다.
3. **규칙 테이블 + 조직계층 동적 조회 혼합** *(채택)* — "몇 단계·어떤 조건"은 `approval_line_rule` 규칙 테이블에, "누가"는 Module 1 조직도를 그 시점에 실시간 조회해 채운다.

```sql
CREATE TABLE approval_line_rule (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type    VARCHAR(20) NOT NULL,   -- VACATION | EXPENSE | PURCHASE | GENERAL
    step_order       INT NOT NULL,
    approver_level   VARCHAR(20) NOT NULL,   -- TEAM_LEADER | DIVISION_HEAD | CEO
    condition_expr   VARCHAR(50) NULL        -- NULL=항상 포함, 'BUDGET_80_EXCEEDED'=조건부
);
```

```java
// "몇 단계/어떤 조건"은 규칙 테이블 조회
List<ApprovalLineRuleVO> rules = approvalLineRuleMapper.selectByDocumentType(VACATION);
// "누가"는 매 기안 시점에 Module 1 조직도를 실시간 조회
Long teamLeaderId = orgMapper.selectOrgUnit(drafter.getOrgUnitId()).getLeaderEmployeeId();
```

## 결정

혼합 방식(3번)을 채택하고, 결재라인은 문서 기안 시점에 **1회 계산되어 `approval_step`으로 고정**된다(이후 발령이 나도 이미 상신된 문서의 결재라인은 바뀌지 않음 — 진행 중인 결재의 안정성을 위해 의도적으로 그렇게 설계한다).

### 결재자 판별 로직 (반드시 확인이 필요했던 부분)

Module 1의 현재 스키마(`employee.position` + `employee.org_unit_id`)만으로는 "이 조직의 팀장/본부장이 누구인지"를 **결정적으로(deterministic)** 특정할 수 없다. `position = 'TEAM_LEADER'`이면서 같은 `org_unit_id`를 가진 사원이 유일하다는 것을 DB가 전혀 보장하지 않기 때문이다 — 예를 들어 인사이동 과도기에 팀장이 두 명 겹치거나(구 팀장이 아직 발령 처리 전), 팀장 공석 상태(0명)가 스키마상 얼마든지 가능하다. `position`은 어디까지나 "직급 호칭"이지 "이 조직의 결재권자"라는 기능적 역할을 보장하는 필드가 아니다.

**결론: 스키마 보완이 필요하다.** `org_unit`에 `leader_employee_id`(nullable FK → `employee.id`) 컬럼을 추가한다.

```sql
ALTER TABLE org_unit ADD COLUMN leader_employee_id BIGINT NULL;
ALTER TABLE org_unit ADD CONSTRAINT fk_org_unit_leader FOREIGN KEY (leader_employee_id) REFERENCES employee (id);
```

별도의 `org_role` 매핑 테이블(N:M)은 채택하지 않았다. 현재 요구사항에는 "조직당 역할이 여러 개(부팀장, 예산 담당자 등)"일 필요가 없고, "조직당 결재권자 1명"이라는 1:1 관계만 필요하기 때문이다(ADR-004와 동일한 논리: 지금 없는 요구사항을 위해 매핑 테이블을 미리 두지 않는다). 필요해지면 그때 별도 ADR로 확장한다.

이 컬럼 하나로 팀장/본부장을 **동일한 방식**으로 표현할 수 있다 — `org_unit.type`이 `TEAM`이면 그 팀의 `leader_employee_id`가 팀장, `HQ`이면 그 본부의 `leader_employee_id`가 본부장이다. Module 1의 `OrgUnitVO`/`org_unit` 테이블을 그대로 확장하는 것이므로, "팀장 판별"과 "본부장 판별"은 코드 상 완전히 같은 함수(`OrgMapper.selectOrgUnit(id).getLeaderEmployeeId()`)를 쓴다 — 상위 조직(본부장)이든 하위 조직(팀장)이든 조회 대상 `org_unit`만 다를 뿐이다.

```java
// ApprovalLineResolver 내부 (개념 코드)
OrgUnitVO team = orgMapper.selectOrgUnit(drafter.getOrgUnitId());       // 기안자의 팀
Long teamLeaderId = requireLeader(team, "팀장");                        // 팀장 = team.leaderEmployeeId

OrgUnitVO hq = orgMapper.selectOrgUnit(team.getParentId());             // 팀의 상위 본부
Long divisionHeadId = requireLeader(hq, "본부장");                      // 본부장 = hq.leaderEmployeeId
```

**대표이사는 조직 리더가 아니다.** 대표이사는 어떤 `org_unit`에도 속하지 않는(조직도보다 상위인) 유일 인물이므로, 조직 리더 조회가 아니라 `employee` 중 `position = CEO` 이고 `status = ACTIVE`인 사원을 조회해서 정확히 1명이어야 함을 검증한다(0명 또는 2명 이상이면 설정 오류로 `BusinessException`). 대표이사도 인사 관리 편의상 `employee.org_unit_id`는 경영지원본부로 등록해 두지만(NOT NULL 제약 유지, 스키마 변경 최소화), 결재라인 판별 시에는 이 소속 정보를 쓰지 않는다.

**리더 공석 처리**: `leader_employee_id`가 NULL이면 해당 조직은 결재권자가 지정되지 않은 것이므로, 문서 상신 시점에 `BusinessException`으로 막는다(잘못된 결재선으로 진행되는 것보다 상신 실패가 안전하다).

**자기결재 방지(self-approval skip)**: 기안자 본인이 해당 단계의 결재권자로 계산되면(예: 팀장이 본인 명의로 기안) 그 단계는 결재라인에서 건너뛰고 다음 단계로 넘어간다. 모든 단계가 스킵되어 결재라인이 0단계가 되면 그 문서는 상신 즉시 자동 승인 처리한다(더 상위의 결재자가 정의되어 있지 않은 유형에서, 최고 직급자가 기안한 경우에 해당).

### 결재 이력의 append-only 보장 (NFR-2-3)

Module 1의 `emp_history` 패턴(ADR-003)을 그대로 재사용한다. `approval_step`(문서 기안 시점에 결정되는 결재라인 정의, 이후 불변)과 `approval_action`(승인/반려 행위 로그, insert-only)을 분리해, "결재 상태"를 컬럼 업데이트가 아니라 `approval_action` 로그로부터 매번 파생시킨다. `ApprovalActionMapper`에도 `update`/`delete`를 두지 않는다.

### 예산/지출관리(Module 4)·재고/구매관리(Module 5)·근태관리(Module 3)에 대한 순방향 의존 처리

Module 2는 의존성 순서상 Module 3/4/5보다 먼저 만들어지므로, 그 모듈들의 구체 클래스에 의존할 수 없다. 두 가지 서로 다른 메커니즘으로 분리한다.

- **FR-2-4(예산 80% 초과 시 대표이사 단계 추가)**: 문서 상신 "그 순간"의 판단이 필요하므로 이벤트가 아니라 **동기 호출용 인터페이스**를 둔다.
  ```java
  public interface BudgetThresholdPolicy {
      boolean isExceeded(Long orgUnitId, BigDecimal amount);
  }
  ```
  Module 4가 만들어지기 전까지는 `NoBudgetModuleYetPolicy`(항상 `false` 반환 + 경고 로그)를 기본 빈으로 등록한다. Module 4 구현 시 실제 예산 조회 구현체로 교체하고 이 ADR에 후속 각주를 남긴다.
  > **후속 각주 (Module 4 구현 시점)**: `NoBudgetModuleYetPolicy`를 제거하고 `BudgetThresholdPolicyImpl`(Module 4)로 교체했다. 다만 EXPENSE의 대표이사 단계 조건은 ADR-011에서 금액 기준(`AMOUNT_GTE_5M`)으로 바뀌어, 지금은 이 정책을 실제로 소비하는 결재 규칙이 없다 — 인터페이스 계약 자체는 정직하게 구현해뒀다(docs/adr/ADR-012 참고).
  > **추가 후속 각주 (코드 감사 시점)**: 위 판단을 다시 확인한 결과 실제로 재사용될 계획이 없는 죽은 코드였다. `BudgetThresholdPolicy` 인터페이스·구현체·`BUDGET_80_EXCEEDED` 조건을 전부 삭제했다 (docs/adr/ADR-012의 "후속 기록 — 폐기됨" 참고).
- **FR-2-5(휴가 승인 시 근태 반영), FR-2-6(구매요청 승인 시 구매내역 생성)**: 결재가 "완전히 끝난 후" 다른 모듈이 반응하면 되는 사후 통지이므로 **Spring 애플리케이션 이벤트**로 분리한다.
  ```java
  public class DocumentApprovedEvent extends ApplicationEvent {
      private final Long documentId;
      private final DocumentType documentType;
  }
  ```
  마지막 결재 단계가 승인되면 `ApplicationEventPublisher.publishEvent(new DocumentApprovedEvent(...))`를 호출한다. 지금은 로그만 남기는 리스너 하나를 두고, Module 3/5 구현 시 각자 `@EventListener`를 등록해 소비한다. 이렇게 하면 Module 2가 Module 3/5를 몰라도 되고(의존 방향이 항상 상위 모듈 → 하위 모듈 한 방향), Module 3/5가 나중에 붙을 때 Module 2 코드를 다시 열 필요가 없다.

## 근거

- 규칙 테이블/동적 조회 분리는 "정책"과 "사실(현재 조직도)"을 분리하는 일반적인 설계 원칙이며, 발령이 나도 결재라인 규칙 자체(몇 단계인지)는 바뀌지 않는다는 실제 업무 규칙과 맞아떨어진다.
- `leader_employee_id` 컬럼은 최소한의 스키마 변경으로 "결재권자"라는 기능적 개념과 "직급 호칭"이라는 표시적 개념을 분리해, 팀장이 공석이거나 중복 지정된 데이터 이상 상태를 상신 시점에 명시적 오류로 드러낸다(조용히 잘못된 사람에게 결재가 가는 것보다 안전).
- 이벤트/인터페이스로 순방향 의존을 끊는 것은 지금까지 지켜온 모듈 의존성 순서(조직→결재→근태→예산→재고→영업)를 코드 레벨에서도 실제로 지키기 위함이다.

## 트레이드오프

- 이미 상신된 문서는 결재라인이 고정되므로, 상신 직후 조직 개편이 있어도 그 문서는 옛 결재라인으로 진행된다(의도된 동작이지만 운영 시 혼란의 소지가 있어 문서 상세 화면에 "결재라인은 상신 시점 기준"이라는 안내가 필요).
- `NoBudgetModuleYetPolicy`가 항상 `false`를 반환하는 동안은 FR-2-4가 실질적으로 비활성 상태다 — Module 4 구현 전까지는 예산 초과로 인한 대표이사 결재 추가가 실제로 발생하지 않는다는 점을 포트폴리오 설명 시 명시해야 한다.
- **알려진 한계(구현 중 확인됨)**: 결재라인 판별은 기안자가 TEAM 소속인 경우만 지원한다. 본부장/대표이사처럼 본부(HQ)에 직속으로 등록된 사원이 기안하면 `ApprovalLineResolverImpl`이 `BusinessException`을 던진다(조용히 잘못된 결재라인을 만드는 대신 명시적으로 막음). 요구사항의 결재 시나리오는 팀원 기준으로 서술되어 있어 이번 범위에서는 이 제약을 그대로 두었고, 본부 직속 사원의 기안 플로우가 실제로 필요해지면 별도 ADR로 다룬다.
