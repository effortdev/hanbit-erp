# ADR-008: 휴가신청-전자결재 연동 방식

- 상태: 승인됨
- 범위: Module 3 (근태관리, `egovframework.erp.attendance`), Module 2 소폭 확장

## 상황

FR-3-2: 휴가신청은 전자결재 모듈을 통해 상신되며, 승인 완료 시 근태 모듈에 자동 반영되어야 한다. Module 2는 이미 "몰라도 되는 방향"의 결합만 허용하도록 설계되어 있다 — Module 2는 Module 3를 알 수 없지만(의존성 순서상 먼저 만들어졌으므로), Module 3는 Module 2를 알아도 된다(나중에 만들어졌고 그 위에 쌓이므로). 이 비대칭을 실제 연동에서 어떻게 코드로 구현할지 정해야 한다.

## 결정

### 정방향(근태 → 결재): 직접 서비스 호출

휴가신청 등록 시점에 `attendance_vacation_request`를 만들면서, Module 2의 `ApprovalService.draftVacation(...)`을 그대로 호출해 `approval_document`/`approval_step`을 즉시 생성한다. `attendance_vacation_request.approval_document_id`(FK)로 두 문서를 1:1 연결한다.

```java
@Transactional
public Long requestVacation(Long employeeId, LocalDate start, LocalDate end, String reason) {
    int days = BusinessDayCounter.count(start, end);
    // ... 잔여 연차 검증 (docs/adr/ADR-009) ...
    Long documentId = approvalService.draftVacation(employeeId, "휴가신청(" + start + "~" + end + ")", reason, start, end);
    VacationRequestVO request = new VacationRequestVO(employeeId, start, end, days, reason, documentId);
    vacationRequestMapper.insertRequest(request);
    return request.getId();
}
```

이는 이미 확립된 "나중에 만들어진 모듈이 먼저 만들어진 모듈의 공개 서비스를 직접 호출한다"는 일반적인 계층 의존 방향과 같아서 별도 장치가 필요 없다.

### 역방향(결재 → 근태): Module 2가 이미 정의해둔 이벤트 구독

승인/반려 결과는 Module 3가 `DocumentApprovedEvent`(ADR-006에서 이미 정의됨)를 구독하는 `AttendanceApprovalListener`로 받는다. 다만 구현 중 확인한 바로는, **Module 2는 지금까지 승인 이벤트만 발행했고 반려 이벤트가 없었다.** 반려 시 `act()`는 `approval_action`에 REJECT를 기록할 뿐 아무 이벤트도 쏘지 않았다(승인/반려 결과를 "받기만" 하면 되는 Module 3 입장에서는 반려도 알아야 하므로 이 비대칭을 그대로 둘 수 없었다). ADR-006의 이벤트 기반 설계 의도(순방향 의존 차단)를 지키면서 대칭을 맞추기 위해, **`DocumentRejectedEvent`를 대칭으로 추가**했다(Module 2의 소폭 확장 — Module 1에 `leader_employee_id`를 추가했던 것과 같은 성격의 후속 보완).

```java
// egovframework.erp.approval.event (Module 2)
public class DocumentRejectedEvent extends ApplicationEvent {
    private final Long documentId;
    private final DocumentType documentType;
    private final String comment;
    // ...
}
```

`ApprovalServiceImpl.act()`에서 `action == REJECT`이고 그 반려가 실제로 처리 가능한 상태였을 때(= `isActionable` 통과) 이 이벤트를 발행하도록 한 줄 추가했다.

```java
@Component
public class AttendanceApprovalListener {  // Module 3
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApproved(DocumentApprovedEvent event) {
        if (event.getDocumentType() != DocumentType.VACATION) return;
        vacationRequestMapper.updateStatusByDocumentId(event.getDocumentId(), VacationStatus.APPROVED);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejected(DocumentRejectedEvent event) {
        if (event.getDocumentType() != DocumentType.VACATION) return;
        vacationRequestMapper.updateStatusByDocumentId(event.getDocumentId(), VacationStatus.REJECTED);
    }
}
```

### 이벤트 전달 시점: `@EventListener`가 아니라 `@TransactionalEventListener(AFTER_COMMIT)`

Module 2의 기존 `DocumentApprovedLogListener`는 평범한 `@EventListener`였다. Spring의 기본 이벤트 발행은 **동기 호출**이라, `@EventListener`는 이벤트를 발행한 메서드와 같은 스레드·같은 트랜잭션 안에서 즉시 실행된다. 즉 `AttendanceApprovalListener`가 `@EventListener`였다면, 근태 쪽 상태 갱신이 예외를 던질 경우 그 예외가 `ApprovalServiceImpl.act()`의 트랜잭션까지 타고 올라가 **결재 승인/반려 자체가 롤백**된다 — "근태 모듈이 결재 내부를 몰라도 된다"는 결합도 저감 의도와 어긋난다(컴파일 타임 의존은 끊었지만 실패 전파는 여전히 강결합).

그래서 `AttendanceApprovalListener`, 그리고 기존 `DocumentApprovedLogListener`도 함께 **`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`** 로 바꿨다. 결재 트랜잭션이 커밋된 "이후" 별도로 실행되므로, 근태 쪽 갱신이 실패해도 이미 커밋된 결재 결과에는 영향을 주지 않는다(다만 근태 상태가 결재 상태와 잠시 어긋날 수 있다는 트레이드오프는 남는다 — 아래 참고).

## 근거

- 정방향은 일반적인 계층 호출이라 특별한 장치가 필요 없고, 역방향만 결합도 저감 장치(이벤트)가 필요하다는 ADR-006의 원래 분석이 맞았음을 재확인했다.
- 반려 이벤트 부재는 설계 시점에는 드러나지 않던 실제 구현 갭이었다 — Module 3를 실제로 만들어보고 나서야 발견됐다. ADR을 사후에 갱신해 이 발견 과정을 남긴다.
- `AFTER_COMMIT` 전환은 "한 모듈의 실패가 다른 모듈이 이미 끝낸 트랜잭션을 되돌리면 안 된다"는 일반 원칙을 반영한다.

## 트레이드오프

- `AFTER_COMMIT` 리스너가 실패하면(예: DB 순단) 결재는 승인/반려로 확정됐는데 근태 쪽 상태는 갱신되지 않는 정합성 어긋남이 생길 수 있다. 지금은 재시도/보정 배치 없이 로그만 남긴다 — 실제 서비스라면 아웃박스 패턴이나 배치 정합성 점검이 필요하지만 이번 포트폴리오 범위 밖으로 둔다.
- 이벤트 발행 시점에 활성 트랜잭션이 없으면(`fallbackExecution=false` 기본값) 리스너가 아예 실행되지 않는다. `ApprovalServiceImpl`의 모든 상태 변경 메서드가 `@Transactional`이라 항상 트랜잭션 안에서 발행되지만, 이 전제가 깨지면(예: 트랜잭션 밖에서 호출) 이벤트가 조용히 사라진다는 점을 인지하고 있어야 한다.
