# ADR-010: 출퇴근기록 데이터 원천

- 상태: 승인됨
- 범위: Module 3 (근태관리)

> **참고**: 이 ADR의 지시 문장이 "사원이 직접 "출근/퇴근" 버\_" 부분에서 끊긴 채 전달되었다. 문맥과 FR-3-1("사원은 출퇴근 기록을 등록/조회할 수 있어야 한다" — 등록 주체가 사원 본인)로 미루어 "사원이 직접 화면에서 출근/퇴근 버튼을 눌러 기록한다"는 자가입력 방식으로 이해하고 작성했다. 의도와 다르면 알려주면 다시 정리하겠다.

## 상황

FR-3-1은 사원이 출퇴근 기록을 등록/조회할 수 있어야 한다고만 요구하고, 그 기록이 어디서 오는지는 명시하지 않는다. 실물 출입 기기 연동부터 순수 자가입력까지 여러 선택지가 있다.

## 후보

1. **생체인식/사원증 리더 등 실물 기기 연동** — 가장 신뢰도 높은 원천이지만, 하드웨어·전용 프로토콜(예: Wiegand, 제조사 SDK) 연동이 필요해 포트폴리오 범위를 크게 벗어난다.
2. **사내망 IP/사내 Wi-Fi 접속 기반 자동 감지** — 별도 기기는 없지만 "사내망 접속 = 출근"이라는 전제가 재택/외근 환경에서 성립하지 않고, 감지 인프라(사내망 IP 대역 관리 등)가 필요하다.
3. **사원 자가입력(화면에서 출근/퇴근 버튼 클릭)** *(채택)* — FR-3-1이 요구하는 "등록"을 그대로 만족하고, 서버 시각을 신뢰 기준으로 삼아 구현이 단순하다.

## 결정

사원이 화면에서 "출근"/"퇴근" 버튼을 누르면 그 시각의 **서버 시각**(`LocalDateTime.now()`)을 `attendance_record`에 기록한다. 사원이 직접 시각을 입력하게 하지 않는다(클라이언트 시각 위변조 방지의 최소한).

```java
@Transactional
public void checkIn(Long employeeId) {
    LocalDate today = LocalDate.now();
    if (attendanceRecordMapper.selectByEmployeeAndDate(employeeId, today) != null) {
        throw new BusinessException("이미 출근 처리되었습니다.");
    }
    attendanceRecordMapper.insertCheckIn(employeeId, today, LocalDateTime.now());
}

@Transactional
public void checkOut(Long employeeId) {
    AttendanceRecordVO record = attendanceRecordMapper.selectByEmployeeAndDate(employeeId, LocalDate.now());
    if (record == null || record.getCheckInTime() == null) {
        throw new BusinessException("출근 기록이 없어 퇴근 처리를 할 수 없습니다.");
    }
    if (record.getCheckOutTime() != null) {
        throw new BusinessException("이미 퇴근 처리되었습니다.");
    }
    attendanceRecordMapper.updateCheckOut(record.getId(), LocalDateTime.now());
}
```

## 근거

- 실물 기기 연동은 이 프로젝트가 검증하려는 역량(계층 구조·MyBatis/JPA 정책·모듈 연동 설계)과 무관한 하드웨어 통합 작업이라 배제한다.
- 자가입력 방식은 FR-3-1의 문면("사원은 ... 등록")과 정확히 일치한다.
- 서버 시각을 신뢰 기준으로 삼는 것은 최소한의 무결성 장치이며, 추가 인프라 없이 구현 가능하다.

## 트레이드오프

- 대리 출퇴근(동료가 대신 버튼을 눌러주는 행위) 같은 부정 입력을 시스템적으로 막을 수 없다. 실제 조직이라면 생체인식·사원증 리더와 결합해 보완하지만, 이번 범위에서는 "누가 로그인해서 눌렀는지"(Module 2에서 도입한 로그인 세션) 이상의 신원 검증은 하지 않는다.
- 재택근무·외근처럼 사무실 밖에서의 근무를 구분하는 개념이 없다 — 버튼을 누른 위치와 무관하게 동일하게 기록된다.
