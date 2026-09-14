# ㈜한빛전자 미니 ERP

전자정부표준프레임워크(eGovFrame) 4.2 관례를 따르는 6개 모듈짜리 미니 ERP 포트폴리오 프로젝트. 이 문서 하나로 프로젝트 전체 구조를 5분 안에 파악할 수 있도록 정리했다.

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [아키텍처](#3-아키텍처)
4. [모듈별 핵심 기능](#4-모듈별-핵심-기능)
5. [설계 결정 하이라이트 (ADR)](#5-설계-결정-하이라이트-adr)
6. [트러블슈팅 하이라이트](#6-트러블슈팅-하이라이트)
7. [실행 방법](#7-실행-방법)
8. [테스트 현황](#8-테스트-현황)

---

## 1. 프로젝트 개요

**가상 기업 프로필** (`docs/erp_requirements.md` 기준)

| 항목 | 내용 |
|---|---|
| 회사명(가상) | ㈜한빛전자 (제조·유통업) |
| 연매출 | 약 100억 원 |
| 임직원 수 | 약 65명 |
| 조직 구조 | 본부(3) - 팀(8) 2단계 고정 구조 |
| 결재라인 기준 | 직급 기준 자동 결정 (사원→팀장→본부장, 금액/유형에 따라 대표이사 결재 추가) |

이 전제 위에서 **6개 ERP 모듈**을 갖춘 미니 ERP 시스템을 구현했다.

| # | 모듈 | 한 줄 요약 |
|---|---|---|
| 1 | 조직/사원관리 | 전 모듈이 참조하는 기반 도메인(조직도, 사원, 발령이력) |
| 2 | 전자결재 | 기안 유형별 결재라인 자동구성 + 승인/반려 처리를 담당하는 코어 모듈 |
| 3 | 근태관리 | 출퇴근 기록, 휴가신청(결재 연동), 연차 잔여현황 |
| 4 | 예산/지출관리 | 부서×계정과목 예산 배정, 지출결의서, 소진율 경고/차단 |
| 5 | 재고/구매관리 | 재고 현황, 구매요청서(결재 연동), 입고 처리 |
| 6 | 영업/매출관리 | 거래처, 수주 등록/확정, 재고 차감 + 매출 집계 |

6개 모듈 전부 구현이 완료된 상태다.

---

## 2. 기술 스택

| 영역 | 선택 | 비고 |
|---|---|---|
| 아키텍처 | eGovFrame 표준 4단 계층: `Controller → Service(interface) → ServiceImpl → DAO/Mapper` | [ADR-001](docs/adr/ADR-001-architecture-baseline.md) |
| eGovFrame 런타임 | **RTE jar(`org.egovframe.rte.*`) 미사용, 순수 Spring 5.3.x 구현** | 정부 전용 Nexus의 빌드 재현성 리스크 때문 — [ADR-002](docs/adr/ADR-002-egovframe-runtime-strategy.md) 요약은 [5장](#5-설계-결정-하이라이트-adr) 참고 |
| 데이터 접근 | MyBatis(SqlMap) 기본, 단순 CRUD에 한해 Spring Data JPA 허용 | 모듈별 근거는 각 모듈 ADR (예: [ADR-003](docs/adr/ADR-003-hr-persistence-strategy.md)) |
| 동적 쿼리 | MyBatis 동적 SQL(`<foreach>` 등)만 사용, QueryDSL 등 별도 도구 미도입 | [ADR-007](docs/adr/ADR-007-dynamic-query-processing.md) |
| 인증/인가 | Spring Security + 세션 기반 폼 로그인, `maximumSessions=1` | [ADR-005](docs/adr/ADR-005-authentication.md) |
| 모듈 간 연동 | 정방향(나중 모듈→이전 모듈): 직접 서비스 호출 / 역방향: Spring 애플리케이션 이벤트(`@TransactionalEventListener`) | [ADR-006](docs/adr/ADR-006-approval-line-composition.md), [ADR-008](docs/adr/ADR-008-vacation-approval-integration.md) |
| DB | MySQL 8 (Docker Compose, 로컬 포트 3309) | `docker-compose.yml` |
| 빌드 | Maven, Java 17 | `pom.xml` |
| 로컬 실행 | Jetty (`jetty-maven-plugin`, Servlet 3.1) | 배포 산출물은 표준 WAR — tomcat7-maven-plugin에서 전환한 이유는 [6장](#6-트러블슈팅-하이라이트) 참고 |
| 화면 | Spring MVC + JSP | eGovFrame 전통 방식 |
| 프론트 스타일 | 공통 CSS 디자인 시스템 1개 파일(`src/main/webapp/css/main.css`) | 좌측 고정 사이드바(236px) 레이아웃 + 카드/뱃지 컴포넌트, 상태값 색상 의미 체계(승인·성공=녹색, 반려·재고부족=적색, 중간 경고=주황) |

> **주의**: "eGovFrame 기반"이라는 표현은 "RTE 유틸리티 jar 실사용"이 아니라 "eGovFrame이 요구하는 계층 구조·영속성 정책·설정 스타일을 순수 Spring으로 재현했다"는 뜻이다 ([ADR-002](docs/adr/ADR-002-egovframe-runtime-strategy.md)).

---

## 3. 아키텍처

`docs/erp_requirements.md`가 정의한 모듈 간 연동 관계는 다음과 같다.

```
조직/사원관리 (기반)
      │
      ▼
  전자결재 (코어) ──▶ 근태관리 (휴가승인 반영)
      │
      ├──▶ 예산/지출관리 (지출결의 반영, 초과시 결재단계 추가)
      │
      └──▶ 재고/구매관리 (구매요청 반영) ◀── 영업/매출관리 (수주시 재고차감)
```

**조직/사원관리**가 모든 모듈의 기반 도메인이다 — 조직 계층(`org_unit`)과 사원(`employee`) 정보를 단일 소스로 두고, 다른 모든 모듈이 이를 참조한다(NFR-1-1). **전자결재**는 이 위에서 코어 역할을 한다 — 휴가신청/지출결의서/구매요청서 같은 다른 모듈의 업무가 실제로는 전자결재를 경유해서 상신·승인되기 때문이다. 의존 방향은 항상 "나중에 만들어진 모듈이 먼저 만들어진 모듈을 아는" 한 방향으로 고정했다: 근태·예산·재고 모듈은 전자결재의 `ApprovalService`를 직접 호출해 문서를 기안하지만(정방향, 일반적인 계층 호출), 전자결재는 그 뒤에 붙는 모듈들의 존재를 코드로 알지 못한다.

문제는 "승인 완료"처럼 전자결재 쪽에서 일어난 사건을 근태·예산·재고 모듈에 반대 방향으로 알려야 한다는 점이다. 이 역방향 통지는 `DocumentApprovedEvent`/`DocumentRejectedEvent` 같은 Spring 애플리케이션 이벤트로 처리한다(`@TransactionalEventListener(phase = AFTER_COMMIT)`) — 전자결재는 이벤트를 발행하기만 하고, 각 모듈이 필요한 이벤트를 구독해서 자기 상태를 갱신한다. 그 결과 전자결재 모듈을 수정하지 않고도 새 모듈(근태→예산→재고→영업 순)을 계속 얹을 수 있었고, 실제로 그렇게 순서대로 만들어졌다. 이 설계가 실전에서 어떻게 갱신됐는지는 [5장의 ADR-006/008](#5-설계-결정-하이라이트-adr)에서 다룬다.

---

## 4. 모듈별 핵심 기능

### 1. 조직/사원관리 (`egovframework.erp.hr`)
- 본부-팀 2단계 조직도 등록/조회, 사원 등록/수정/조회 (FR-1-1, FR-1-2)
- 발령(부서이동/승진) 시 변경 이력 자동 기록 — append-only (FR-1-3)
- 조직/사원 정보를 다른 모든 모듈이 참조하는 단일 소스로 제공 (FR-1-4, NFR-1-1)

### 2. 전자결재 (`egovframework.erp.approval`)
- 기안 유형(휴가신청/지출결의서/구매요청서/품의서)별 결재라인 자동구성 (FR-2-1)
- 승인/반려 처리, 반려 시 사유 필수 입력, 기안자의 진행 상태 조회 (FR-2-2, FR-2-3)
- 지출결의서 금액 구간별 결재 단계 자동 확장, 휴가/구매요청 승인 시 근태·재고 모듈에 자동 반영 (FR-2-4, FR-2-5, FR-2-6)

### 3. 근태관리 (`egovframework.erp.attendance`)
- 출퇴근 기록 등록/조회 (FR-3-1)
- 휴가신청은 전자결재로 상신되며 승인 완료 시 자동 반영 (FR-3-2)
- 연차 잔여일수 조회 — 상신 중(PENDING) 건도 임시 차감에 포함 (FR-3-3)

### 4. 예산/지출관리 (`egovframework.erp.budget`)
- 부서×계정과목×연도 단위 예산 배정/조회 (FR-4-1)
- 지출결의서 승인 시 예산 자동 차감 (FR-4-2)
- 예산 소진율 조회, 80% 초과 경고, 100% 초과 시 상신 차단(고액 건은 예외 승인 사유로 허용) (FR-4-3, FR-4-4)

### 5. 재고/구매관리 (`egovframework.erp.inventory`)
- 품목별 재고 현황(입고/출고/현재고) 조회 (FR-5-1)
- 구매요청서 승인 완료 시 구매 내역 자동 생성, 입고 처리 시 재고 자동 증가 (FR-5-2, FR-5-3)
- 영업 모듈의 수주 확정과 연동해 재고 자동 차감 (FR-5-4)

### 6. 영업/매출관리 (`egovframework.erp.sales`)
- 거래처 등록/조회 (FR-6-1)
- 수주 등록 시 가용 재고 확인, 수주 확정 시 재고 차감 + 매출 데이터 생성을 한 트랜잭션으로 처리 (FR-6-2, FR-6-3)
- 월별/부서별 매출 현황 조회 (FR-6-4)

---

## 5. 설계 결정 하이라이트 (ADR)

`docs/adr/`에 ADR-001부터 ADR-018까지 18건이 있다.

| ADR | 제목 | 범위 |
|---|---|---|
| [001](docs/adr/ADR-001-architecture-baseline.md) | 아키텍처 베이스라인 (계층 구조 / 영속성 기본 정책) | 프로젝트 전체 |
| [002](docs/adr/ADR-002-egovframe-runtime-strategy.md) | eGovFrame 런타임(RTE) 의존성 전략 | 프로젝트 전체 |
| [003](docs/adr/ADR-003-hr-persistence-strategy.md) | 조직/사원관리 영속성 전략 (MyBatis/JPA 혼합) | Module 1 |
| [004](docs/adr/ADR-004-org-hierarchy-representation.md) | 조직 계층 표현 방식 (인접 리스트) | Module 1 |
| [005](docs/adr/ADR-005-authentication.md) | 인증/인가 방식 (Spring Security 세션 로그인) | 프로젝트 전체 |
| [006](docs/adr/ADR-006-approval-line-composition.md) | 결재라인 자동구성 로직 | Module 2 |
| [007](docs/adr/ADR-007-dynamic-query-processing.md) | 동적 쿼리 처리 방식 (MyBatis 동적 SQL) | 프로젝트 전체 |
| [008](docs/adr/ADR-008-vacation-approval-integration.md) | 휴가신청-전자결재 연동 방식 | Module 3 |
| [009](docs/adr/ADR-009-vacation-balance-calculation.md) | 연차 잔여현황 계산 방식 | Module 3 |
| [010](docs/adr/ADR-010-attendance-record-source.md) | 출퇴근기록 데이터 원천 (자가입력) | Module 3 |
| [011](docs/adr/ADR-011-expense-approval-line-amount-tiers.md) | 지출결의서 결재라인의 금액 조건 | Module 4 |
| [012](docs/adr/ADR-012-budget-overrun-handling.md) | 예산 초과 처리 방식 | Module 4 |
| [013](docs/adr/ADR-013-budget-allocation-unit-and-period.md) | 예산 배정 단위와 기간 | Module 4 |
| [014](docs/adr/ADR-014-purchase-approval-line-and-item-conditions.md) | 구매요청서 결재라인 및 품목 조건 | Module 5 |
| [015](docs/adr/ADR-015-inventory-receipt-timing.md) | 재고 반영 시점 (승인과 입고의 분리) | Module 5 |
| [016](docs/adr/ADR-016-safety-stock-warning.md) | 안전재고 경고 및 발주 트리거 | Module 5 |
| [017](docs/adr/ADR-017-sales-inventory-deduction.md) | 재고 차감 방식 (Module 5와의 일관성) | Module 6 |
| [018](docs/adr/ADR-018-sales-revenue-timing.md) | 수주-매출 연동 시점 | Module 6 |

아래 네 건은 별도로 짚어둘 만하다.

**[ADR-002] eGovFrame RTE 비의존 결정.** eGovFrame RTE(`org.egovframe.rte.*`) jar는 정부 전용 Nexus(`maven.egovframe.go.kr`)에만 배포되고, 이 저장소는 HTTPS 미지원·접속 불안정 이력이 있다. 포트폴리오를 열람하는 사람이 `git clone` 후 `mvn package`를 실행했을 때 외부 정부 인프라 상태에 따라 빌드가 깨지는 것은 용납할 수 없는 리스크였다. 그래서 RTE 유틸리티 jar 자체에는 의존하지 않고, eGovFrame이 실제로 강제하는 핵심(계층 구조, 영속성 정책, 설정 스타일)만 순수 Spring 5.3.x로 재현했다 — "eGovFrame 표준 구조를 따르는 순수 Spring 구현"이라는 문구는 이 결정을 정직하게 알리기 위한 것이다.

**[ADR-006 / ADR-011] 결재라인 자동구성과, 구현 중 요구사항 문서 자체를 갱신한 사례.** ADR-006은 결재라인의 "몇 단계인지/어떤 조건인지"는 `approval_line_rule` 규칙 테이블에 고정하고, "실제로 누가"는 조직도(`org_unit.leader_employee_id`)를 기안 시점에 동적으로 조회해 채우는 혼합 방식을 세웠다. 그런데 Module 4(예산)를 실제로 설계해보니, 요구사항이 원래 원했던 것은 "부서 예산 80% 초과 시 대표이사 결재 추가"가 아니라 **지출 금액 구간 자체**(100만/500만원)에 따라 단계가 늘어나는 것이었다(ADR-011). 예산 소진율은 부서·시점마다 바뀌는 값이라, 상신 시점에 고정돼야 하는 결재라인과 결합하면 조직 개편·예산 재배정 때 이미 상신된 문서의 해석이 흔들릴 위험이 있었기 때문이다. 이 금액 구간 기준은 이후 Module 5의 구매요청서(ADR-014)에도 그대로 재사용됐다 — "구매요청서는 물류본부 한정 고정 3단계"였던 초안을 EXPENSE와 동일한 금액 구간으로 교체했다. 문제는 이 변경 이후에도 `docs/erp_requirements.md`가 여전히 옛 규칙("80% 초과 시 대표이사 추가", "구매요청서는 항상 3단계")을 적고 있었다는 점이다. 코드와 요구사항 문서가 서로 다른 이야기를 하고 있다는 걸 나중에 발견하고, **코드는 이미 합리적인 결정이었다고 판단해 그대로 두고 요구사항 문서 쪽을 실제 구현에 맞춰 갱신**했다(각 FR 항목에 "변경 이력"을 남겨 최초안과 달라진 이유를 추적 가능하게 함).

**[ADR-012] `BudgetThresholdPolicy`를 만들었다가 정직하게 폐기한 사례.** ADR-006 시점에는 "예산 80% 초과" 조건을 판정할 `BudgetThresholdPolicy` 인터페이스를 만들어뒀고, ADR-012에서 Module 4의 실제 구현체(`BudgetThresholdPolicyImpl`)로 채웠다. 하지만 ADR-011로 EXPENSE의 대표이사 조건이 이미 금액 기준으로 바뀌어서, 이 구현체를 실제로 소비하는 결재 규칙이 하나도 없었다. 처음엔 "계약을 지키는 것 자체가 의미 있다", "다른 문서 유형에 향후 재사용될 수 있다"는 근거로 존치했지만, 코드 감사 시점에 `erp_requirements.md`의 Module 5·6 FR/NFR을 전부 다시 확인한 결과 예산 소진율 기반 결재 단계를 요구하는 문장이 어디에도 없다는 걸 확인했다. "향후 재사용"이 근거 없는 추측이었음을 인정하고, 인터페이스·구현체·관련 조건 상수·테스트 2건을 전부 삭제했다(전체 테스트 재실행으로 회귀 없음 확인). **설계를 만든 뒤 그 근거를 사후 검증하고, 근거가 무너지면 정직하게 되돌린 사례**다.

**[ADR-008] Module 3에서 발견한 Module 2의 이벤트 설계 갭.** ADR-006은 결재 승인 시 근태/재고 모듈에 통지하는 `DocumentApprovedEvent`를 정의해뒀지만, Module 3(근태관리)를 실제로 구현하다 보니 **반려 이벤트가 아예 없다**는 게 드러났다. 승인만 이벤트로 알리고 반려는 조용히 로그만 남기고 있었던 것이다 — 근태 모듈 입장에서는 반려도 똑같이 알아야 하는데 이 비대칭을 그대로 둘 수 없었다. ADR-006의 원래 설계 의도(결합도 저감)를 지키면서 대칭을 맞추기 위해 `DocumentRejectedEvent`를 추가하고, 기존 리스너를 포함해 전부 `@TransactionalEventListener(phase = AFTER_COMMIT)`로 전환했다(동기 `@EventListener`였다면 근태 쪽 처리 실패가 결재 트랜잭션 자체를 롤백시킬 수 있었기 때문). 설계 시점에는 보이지 않던 갭이 다음 모듈을 실제로 만들면서 드러나고, 그 자리에서 이전 모듈의 계약을 소급 보완한 사례다.

---

## 6. 트러블슈팅 하이라이트

`docs/troubleshooting.md`에 시간순으로 기록된 이슈 중 대표적인 것만 추렸다. 전체 목록은 [docs/troubleshooting.md](docs/troubleshooting.md) 참고.

- **servlet-api/JSTL 전이 의존성 충돌 (`LinkageError: loader constraint violation`)**: JSTL 의존성(`javax.servlet.jsp.jstl`)이 아주 오래된 `servlet-api:2.5`를 전이적으로 끌어와, 컨테이너가 이미 로드한 `ServletContext`와 이름은 같지만 다른 클래스로더가 로드한 별개 클래스로 취급되며 충돌했다. `<exclusions>`로 배제해 해결 — 서블릿 API는 컨테이너가 제공한다는 원칙을 재확인.
- **`tomcat7-maven-plugin` → `jetty-maven-plugin` 전환**: Spring Security의 `HttpSessionEventPublisher`가 요구하는 `HttpSessionIdListener`(Servlet 3.1+)를 내장 Tomcat 7.0.47(Servlet 3.0, 2013년산)이 제공하지 못해 `NoClassDefFoundError`로 기동이 깨졌다. Servlet 3.1을 지원하는 Jetty 9.4.x로 로컬 실행 도구만 교체했다(배포용 WAR 산출물 자체는 변경 없음).
- **MyBatis 매퍼 XML의 `<=` 이스케이프 누락**: `VacationPolicyMapper.xml`에 `WHERE min_years <= #{yearsOfService}`를 그대로 써서 `<`가 XML 태그 시작 문자로 오인되어 `SAXParseException`이 발생했다. Mockito 기반 단위 테스트는 이 XML 자체를 로드하지 않아 테스트는 다 통과했는데도 버그가 남아있었고, 실제 `mvn jetty:run` 기동 시점에야 드러났다 — 단위 테스트가 매퍼 XML 파싱까지는 검증하지 못한다는 한계를 보여준 사례.
- **한글 시드 데이터 mojibake**: JDBC URL의 `characterEncoding=UTF-8`이나 JSP `pageEncoding`은 문제가 아니었고, `mysql` 클라이언트로 시드를 적재하는 시점에 클라이언트 문자셋이 UTF-8이 아니어서 이미 깨진 채로 저장된 것이 원인이었다. `mysql --default-character-set=utf8mb4 ...` 옵션 명시로 해결.
- **요구사항 정의서와 실제 구현의 불일치 발견**: 코드는 ADR-011/ADR-014에 따라 이미 금액 구간 기준으로 바뀌어 있는데 `erp_requirements.md`는 옛 규칙(예산 80% 초과 기준)을 그대로 적고 있었다. 코드가 더 합리적인 결정이었다고 판단해 문서 쪽을 실제 구현에 맞춰 갱신했다 (5장의 ADR-006/011 스토리와 동일 사건).

---

## 7. 실행 방법

### 사전 요구사항
- JDK 17
- Maven
- Docker (Docker Compose)

### 실행 순서

```bash
# 0. 환경변수 파일 준비 (최초 1회) — DB 비밀번호를 코드/설정 파일에 하드코딩하지 않는다
cp .env.example .env
# .env를 열어 MYSQL_ROOT_PASSWORD / MYSQL_PASSWORD / JDBC_PASSWORD 값을 채운다.
# (JDBC_PASSWORD는 MYSQL_PASSWORD와 같은 값으로 맞춘다)

# 1. MySQL 기동 (로컬 3306 포트 충돌을 피해 3309로 매핑, docker compose가 .env를 자동으로 읽는다)
docker compose up -d

# 2. 스키마 + 시드 데이터 적재
# --default-character-set=utf8mb4 필수 (누락 시 한글 시드 데이터가 깨져서 저장됨 — 6장 참고)
# -p 뒤에 비밀번호를 바로 붙이면 .env에 넣은 값을 그대로 사용한다 (예: -pMYSQL_PASSWORD값)
mysql --default-character-set=utf8mb4 -h127.0.0.1 -P3309 -uerp -p<.env의 MYSQL_PASSWORD> hanbit_erp < sql/schema.sql

# 3. 빌드/실행 (JAVA_HOME을 JDK 17로 지정, .env의 값을 환경변수로 불러온 뒤 실행)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
set -a && source .env && set +a
mvn jetty:run
```

브라우저에서 http://localhost:8081/login 접속. 데모 계정은 전부 비밀번호가 `erp1234!`로 동일하다 (`sql/schema.sql` 참고).

| 계정 | 이름 | 직급 | 소속 |
|---|---|---|---|
| `ceo` | 김대표 | 대표이사 | (조직도 상위 없음) |
| `hqhead1` | 박본부 | 본부장 | 경영지원본부 |
| `hqhead2` | 정영업 | 본부장 | 영업본부 |
| `hqhead3` | 윤물류 | 본부장 | 물류본부 |
| `leader1` | 이인사 | 팀장 | 인사팀 |
| `leader2` | 강국내 | 팀장 | 국내영업팀 |
| `leader3` | 한구매 | 팀장 | 구매팀 |
| `staff1` | 최사원 | 사원 | 인사팀 |

---

## 8. 테스트 현황

- 단위 테스트 12개 클래스, **총 48건 — 전부 통과** (`mvn test`).
- 결재/근태/예산/재고/영업 각 모듈의 서비스 로직(결재라인 판별, 연차 계산, 예산 소진율 판정, 재고 차감 동시성 가드, 수주 확정 트랜잭션 등)을 Mockito 기반으로 검증한다.
- 단위 테스트와 별도로, **각 모듈은 실제 Jetty 서버에 로그인 세션을 만들어 curl로 화면 응답을 직접 확인**했다 — HTTP 200 여부, 좌측 사이드바/카드/뱃지 마크업 존재, 사이드바 활성 메뉴 하이라이트, 상태값별 뱃지 색상(승인=녹색 등) 렌더링까지 실서버 기준으로 재확인한 뒤 반영했다. 단위 테스트가 잡지 못하는 문제(예: 매퍼 XML 파싱 오류, 화면 마크업 누락)가 실제로 있었기 때문이다(6장 참고).
