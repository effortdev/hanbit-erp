# 트러블슈팅 로그

## 2026-09-12 — `VacationPolicyMapper.xml`의 `<=` 때문에 MyBatis 매퍼 파싱 실패

- **증상**: Module 3 추가 후 `mvn jetty:run`이 `SAXParseException: The content of elements must consist of well-formed character data or markup`로 `sqlSessionFactory` 빈 생성 실패.
- **원인**: `VacationPolicyMapper.xml`의 SQL에 `WHERE min_years <= #{yearsOfService}`를 그대로 썼는데, `<`는 XML에서 태그 시작 문자라 SQL이 아니라 잘못된 XML로 파싱됨. Mockito 기반 단위 테스트는 이 XML을 전혀 로드하지 않기 때문에(인터페이스만 목킹) 테스트가 다 통과한 뒤에도 이 버그가 남아 있었고, 실제 Spring 컨텍스트 기동(`mvn jetty:run`) 시점에야 드러났다.
- **해결**: `<`를 `&lt;`로 이스케이프. MyBatis where절에서 `<`, `<=`, `&`가 들어가면 XML 이스케이프(`&lt;`, `&lt;=`, `&amp;`)나 `<![CDATA[...]]>`가 필요하다는 점을 다시 확인.


## 2026-09-12 — 전자결재 기안 링크에서 `DocumentType` enum 변환 실패

- **증상**: `/approval` 목록 화면의 "기안" 링크를 누르면 `Failed to convert value of type 'java.lang.String' to required type 'DocumentType'` 오류.
- **원인**: `GET /approval/draft/{type}`는 Spring의 기본 `@PathVariable` enum 컨버터를 쓰므로 enum 상수 이름(대문자, 예: `VACATION`)이 정확히 와야 하는데, `inbox.jsp`에서 링크를 만들 때 `fn:toLowerCase(t)`로 소문자화해서 넘겼음. 반면 각 문서 유형의 `POST /approval/draft/{vacation|expense|purchase|general}`는 컨트롤러에 소문자로 하드코딩되어 있어, 같은 화면 안에 대문자/소문자 두 관례가 섞여 있었던 게 근본 원인.
- **해결**: GET 링크(`inbox.jsp`)는 enum 그대로(`${t}`, 대문자)를 쓰도록 수정. `draft.jsp`가 렌더링하는 POST 폼의 action은 `fn:toLowerCase(documentType)`으로 소문자화해 하드코딩된 POST 매핑과 맞춘다. 즉 "GET은 enum 이름 그대로, POST는 소문자"로 규칙을 명확히 분리.

## 2026-09-12 — Spring Security 도입 후 `tomcat7-maven-plugin`이 `NoClassDefFoundError`로 기동 실패

- **증상**: Module 2에서 Spring Security(`maximumSessions` + `HttpSessionEventPublisher`)를 추가한 뒤 `mvn tomcat7:run`이 `NoClassDefFoundError: javax/servlet/http/HttpSessionIdListener`로 기동 실패.
- **원인**: `HttpSessionIdListener`는 Servlet 3.1에서 추가된 인터페이스인데, `tomcat7-maven-plugin`이 내장하는 Tomcat은 7.0.47(2013년, Servlet 3.0)이라 아예 존재하지 않음. Spring Security 5.8.x의 `HttpSessionEventPublisher`는 이 인터페이스를 구현하므로 클래스 로딩 자체가 불가능.
- **해결**: 로컬 실행 도구를 `tomcat7-maven-plugin`에서 `jetty-maven-plugin`(Jetty 9.4.x, Servlet 3.1 지원)으로 교체 (`pom.xml`). `mvn jetty:run`으로 실행. 배포용 산출물은 여전히 표준 WAR(`mvn package`)이므로 실제 운영 배포 방식(Tomcat 8.5/9)에는 영향 없음 — 이건 어디까지나 "로컬 개발 중 즉시 확인용" 도구 교체다.


모듈 진행 중 발생한 이슈와 원인/해결 과정을 시간순으로 기록한다. (포트폴리오 자료 겸용)

---

## 2026-09-12 — eGovFrame 4.2 공식 RTE jar를 Maven Central에서 찾을 수 없음

- **증상**: `org.egovframe.rte.*` / `egovframework.rte.*` 아티팩트가 Maven Central(`search.maven.org`)에 존재하지 않음.
- **원인**: eGovFrame RTE는 정부 전용 Nexus(`maven.egovframe.go.kr`)에만 배포되며, 이 저장소는 HTTPS 미지원/접속 불안정 이력이 있어 외부 빌드 재현성을 해칠 수 있음.
- **해결**: RTE 유틸리티 jar에 의존하지 않고, eGovFrame이 실제로 강제하는 계층 구조·영속성 정책·설정 스타일만 순수 Spring 5.3.x 기반으로 재현하기로 결정 (`docs/adr/ADR-002-egovframe-runtime-strategy.md`). README에 이 결정을 명시해 포트폴리오 열람 시 오해가 없도록 함.

## 2026-09-12 — 로컬 JDK 21이 eGovFrame 4.2(Spring 5.3.27) 공식 지원 범위 밖

- **증상**: 로컬 기본 JDK가 Corretto 21인데, eGovFrame 4.2가 목표하는 Spring Boot 2.7.12 / Spring Framework 5.3.27 조합은 공식적으로 Java 17까지만 검증됨.
- **해결**: 빌드를 Java 17로 고정 (`pom.xml`의 `maven.compiler.release=17`), 로컬에 이미 설치된 `jdk-17.jdk`를 사용하도록 빌드 시 `JAVA_HOME`을 명시.

## 2026-09-12 — `tomcat7:run` 실행 시 `LinkageError: loader constraint violation` (ServletContext)

- **증상**: `mvn tomcat7:run`으로 내장 Tomcat 7.0.47을 띄우면 `WebappClassLoader wants to load interface javax.servlet.ServletContext. A different interface with the same name was previously loaded by ... ClassRealm`로 컨텍스트 시작이 실패.
- **원인**: `mvn dependency:tree -Dincludes=javax.servlet:servlet-api`로 추적한 결과, JSTL 의존성 체인(`org.glassfish.web:javax.servlet.jsp.jstl:1.2.5` → `javax.servlet.jsp.jstl:jstl-api:1.2`)이 전이적으로 아주 오래된 `javax.servlet:servlet-api:2.5`를 `compile` 스코프로 끌어옴. `tomcat7-maven-plugin`의 `run` 목표는 프로젝트 클래스패스를 웹앱 클래스로더에 그대로 태우는데, Tomcat 7 부트스트랩(Plexus ClassRealm)이 이미 로드한 `ServletContext`와 우리 웹앱이 끌어온 servlet-api 2.5의 `ServletContext`가 이름은 같지만 서로 다른 클래스로더가 로드한 별개 클래스로 취급되어 링크 에러가 남.
- **해결**: `javax.servlet.jsp.jstl` 의존성에서 `javax.servlet:servlet-api`를 `<exclusions>`로 배제 (`pom.xml`). Servlet API는 컨테이너가 제공하므로 애플리케이션이 별도로 끌고 올 필요가 없다는 원칙(ADR-002와 같은 결의 연장선)을 여기서도 적용.
- 참고로 `javax.servlet-api`(우리가 직접 선언했던 4.0.1)도 같은 이유로 pom에서 제거하고, `web.xml`도 Tomcat 7.0.47이 실제 지원하는 Servlet 3.0 스펙(`web-app_3_0.xsd`)에 맞춰 다운그레이드함.

## 2026-09-12 — 화면의 한글 데이터가 깨져서 보임 (mojibake)

- **증상**: JSP 정적 텍스트(제목, 메뉴 등)는 정상 출력되는데, MySQL에서 조회한 조직명("경영지원본부" 등)만 깨진 문자로 표시됨.
- **원인 추적**: `HEX(name)`으로 저장된 바이트를 직접 확인해보니 DB에 저장된 시점부터 이미 깨져 있었음(이중 인코딩). `docker exec -i ... mysql -uroot -p... < schema.sql`로 시드 데이터를 넣을 때 `mysql` 클라이언트가 컨테이너의 기본 로케일(비 UTF-8)을 클라이언트 문자셋으로 사용해, UTF-8로 작성된 `schema.sql`의 한글 리터럴을 다른 문자셋으로 잘못 해석해 저장한 것이 원인. (JDBC URL의 `characterEncoding=UTF-8`이나 JSP `pageEncoding`은 애초에 문제가 아니었음 — 그쪽은 정상이었고, DB에 적재되는 시점이 문제였음.)
- **해결**: 스키마/시드 데이터 적재 시 `mysql --default-character-set=utf8mb4 ...` 옵션을 명시. README의 적재 명령에도 반영.
- **부수적으로 함께 정리한 것**: JSP 쪽 인코딩도 방어적으로 명시해 둠 — `header.jsp`의 `<%@ page %>` 지시어에 `pageEncoding="UTF-8"` 추가, `web.xml`에 `<jsp-config>`로 `*.jsp` 전체에 대해 `page-encoding=UTF-8`을 전역 지정.

## 2026-09-12 — 로컬에 Maven 미설치

- **증상**: `mvn` 명령이 없어 빌드 검증 불가.
- **해결**: Homebrew로 Maven 설치(`brew install maven`). 설치 과정에서 Homebrew가 더 이상 참조되지 않는 orphaned kegs(예: 오래된 `openjdk` keg, 각종 이미지 라이브러리)와 캐시(Flutter 설치 zip 캐시 등)를 함께 정리(autoremove/cleanup)했음 — 실제 설치된 애플리케이션이 아니라 Homebrew Cellar/Cache 항목만 제거된 것으로 확인.
