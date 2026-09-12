# ADR-005: 인증/인가 방식

- 상태: 승인됨
- 범위: 프로젝트 전체 (Module 2 전자결재부터 실제로 사용)

## 상황

Module 1까지는 로그인 없이 내부 관리 화면으로만 구현했다 (README에 명시). Module 2(전자결재)부터는 "누가 기안했는가", "누가 결재해야 하는가", "본인 문서만 조회 가능한가"(NFR-2-2)를 판단해야 하므로 더 이상 인증을 미룰 수 없다. eGovFrame 전통 방식(JSP + 세션 기반 화면)과 어울리는 인증 방식을 정해야 한다.

## 후보

1. **JWT + 무상태(stateless) 인증** — SPA/REST API에는 적합하지만, 서버 사이드 세션으로 로그인 상태를 유지하는 JSP 화면(ADR-002에서 이미 JSP 방식을 확정)과는 궁합이 맞지 않는다. 매 요청 토큰 검증/갱신 로직을 추가로 설계해야 해 배보다 배꼽이 커진다.
2. **직접 구현한 세션 필터 + 커스텀 로그인** — 가장 가볍지만, 비밀번호 인코딩·세션 고정 공격 방어·동시 로그인 제어 같은 보안 요구를 전부 직접 챙겨야 해 실수 여지가 크다.
3. **Spring Security + 세션 기반 폼 로그인(formLogin)** *(채택)* — eGovFrame 실무에서 가장 흔히 쓰는 조합. `BCryptPasswordEncoder`, 세션 고정 보호, 동시 세션 제어(`maximumSessions`)를 프레임워크가 검증된 형태로 제공한다.

```xml
<!-- context-security.xml -->
<security:http auto-config="false" use-expressions="true">
    <security:form-login login-page="/login" login-processing-url="/login"
                          default-target-url="/" authentication-failure-url="/login?error"/>
    <security:logout logout-url="/logout" logout-success-url="/login?logout"/>
    <security:session-management>
        <security:concurrency-control max-sessions="1" error-if-maximum-exceeded="false"/>
    </security:session-management>
    <security:intercept-url pattern="/login" access="permitAll()"/>
    <security:intercept-url pattern="/**" access="isAuthenticated()"/>
</security:http>
```

## 결정

Spring Security + 세션 기반 폼 로그인을 채택한다.

- 로그인 자격증명은 `employee`와 분리된 `account(employee_id, username, password_hash)` 테이블에 둔다. 사원 마스터(Module 1)에 인증 컬럼을 섞지 않기 위함이다.
- 인증 성공 시 `SecurityContextHolder`에 커스텀 `ErpUserDetails`(사번=employeeId, 소속 조직 id, 직급 포함)를 담아, 이후 결재라인 판별·문서 조회 권한 검사에서 DB를 다시 조회하지 않고 재사용한다.

```java
public class ErpUserDetails implements UserDetails {
    private final Long employeeId;
    private final Long orgUnitId;
    private final Position position;
    // username/password/authorities + getEmployeeId()/getOrgUnitId()/getPosition()
}
```

- `maximumSessions(1)`로 동일 계정 중복 로그인을 차단한다. 이를 위해 `web.xml`에 `HttpSessionEventPublisher`를 리스너로 등록한다(등록하지 않으면 세션 만료가 Spring Security에 통지되지 않아 `maximumSessions`가 무력화됨 — 자주 놓치는 부분이라 명시).

## 근거

- ADR-002(순수 Spring 구현)와 충돌하지 않는다. Spring Security는 eGovFrame RTE의 일부가 아니라 Spring 생태계의 표준 라이브러리이며, Maven Central에서 정상적으로 받을 수 있다.
- eGovFrame 기반 공공기관 SI 프로젝트의 실제 관례(세션 기반 폼 로그인)에 부합해 포트폴리오 설득력이 높다.
- Module 1이 이미 관리하는 조직 계층 정보(소속 팀/직급)를 `ErpUserDetails`로 그대로 흘려보내, Module 2의 결재라인 자동구성(ADR-006)에서 재조회 없이 재사용할 수 있다.

## 트레이드오프

- 세션 기반이므로 서버를 여러 대로 수평 확장하려면 세션 클러스터링/스티키 세션이 필요하다 (현재 단일 인스턴스 포트폴리오 범위에서는 해당 없음, 향후 확장 시 별도 ADR 필요).
- `account`와 `employee`가 분리되어 있어, 사원 등록 시 로그인 계정을 별도로 발급하는 절차가 하나 더 생긴다(초기 비밀번호 발급/재설정 플로우는 이번 범위에서 다루지 않고 시드 데이터로 대체).
