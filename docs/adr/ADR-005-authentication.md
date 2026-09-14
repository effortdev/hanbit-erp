# ADR-005: 인증/인가 방식

- 상태: 승인됨 (단, `maximumSessions` 관련 결함과 수정 내용은 아래 "후속 기록 — `maximumSessions(1)`가 실제로는 동작하지 않던 결함" 참고)
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

## 후속 기록 — `maximumSessions(1)`가 실제로는 동작하지 않던 결함

- **발견 경위**: WAR+Tomcat 배포 검증(별도 세션) 중 우연히, 같은 계정으로 두 번째 로그인을 해도 첫 번째 세션이 만료되지 않고 계속 유효한 것을 curl로 재현했다. Jetty와 Tomcat 양쪽에서 동일하게 재현되어 컨테이너 차이가 아니라 앱 설정 자체의 결함으로 확인됐다.
- **먼저 배제한 후보**: 이 ADR이 원래 명시한 전제조건들 — `web.xml`의 `HttpSessionEventPublisher` 리스너 등록, `context-security.xml`의 `<concurrency-control max-sessions="1" .../>` 선언 — 은 둘 다 정상적으로 되어 있었다. `springSecurityFilterChain`의 필터 체인 로그에도 `ConcurrentSessionFilter`가 정상적으로 포함돼 있었다. 즉 "설정을 빠뜨렸다"는 가장 흔한 원인은 아니었다.
- **실제 원인**: `ErpUserDetailsService.loadUserByUsername()`은 로그인할 때마다 DB를 새로 조회해 매번 새로운 `ErpUserDetails` 인스턴스를 만든다. 그런데 `ErpUserDetails`가 `equals()`/`hashCode()`를 재정의하지 않고 있었다 — 기본 `Object` 동일성(참조 비교)을 그대로 썼다는 뜻이다. Spring Security의 `SessionRegistryImpl`은 내부적으로 `principal` 객체를 키로 세션 목록을 관리하는데(`getAllSessions(Object principal, ...)`가 이 키로 조회한다), 재로그인 시 만들어지는 새 `ErpUserDetails` 인스턴스는 이전 로그인 때 등록해둔 인스턴스와 **참조가 다르므로 `equals()`가 거짓**이 되어, "이 사용자의 기존 세션"을 하나도 찾지 못한다. 그 결과 `ConcurrentSessionControlAuthenticationStrategy`는 만료시킬 세션이 "0개"라고 판단해 아무것도 하지 않았고, `maximumSessions(1)`이 조용히 무력화됐다(예외도, 로그도 없음 — 원인 파악이 어려웠던 이유).
- **수정**: `ErpUserDetails`에 `username` 기준 `equals()`/`hashCode()`를 추가했다. 이제 서로 다른 로그인 시도에서 만들어진 인스턴스라도 같은 계정이면 `SessionRegistry`가 같은 사용자로 인식한다.
  ```java
  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ErpUserDetails)) return false;
      return username.equals(((ErpUserDetails) o).username);
  }

  @Override
  public int hashCode() {
      return username.hashCode();
  }
  ```
- **검증**: curl로 재현 — 계정 A로 로그인 후 `/hr/org` 정상 응답(200) 확인 → 같은 계정으로 재로그인(세션 B) → 세션 A로 재요청하면 첫 응답은 Spring Security 기본 만료 메시지("This session has been expired ...")를 반환하고, 그다음 요청부터는 `/login`으로 302 리다이렉트되어 재인증이 요구됨을 확인했다. 세션 B는 계속 정상 동작했다. 회귀 테스트로 `ErpUserDetailsTest`(`SessionRegistryImpl.getAllSessions()`가 재로그인 시 기존 세션을 실제로 찾는지 검증)를 추가했고, 수정 전 코드로 되돌려 이 테스트가 실패하는 것도 확인했다.
- **교훈**: `UserDetailsService`가 로그인마다 새 `UserDetails` 인스턴스를 반환하는 구현(대부분의 DB 기반 구현이 그렇다)에서는, 커스텀 `UserDetails`가 `equals()`/`hashCode()`를 재정의하지 않으면 동시 세션 제어가 예외 없이 조용히 무력화된다 — Spring Security 커뮤니티에 잘 알려진 함정이지만 코드 리뷰만으로는 놓치기 쉽고, 실제로 두 번째 로그인을 시도해보는 e2e 검증 없이는 드러나지 않는다.
