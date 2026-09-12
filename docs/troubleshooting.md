# 트러블슈팅 로그

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
