# ADR-002: eGovFrame 런타임(RTE) 의존성 전략

- 상태: 승인됨
- 범위: 프로젝트 전체 (빌드/의존성)

## 상황

전자정부표준프레임워크(eGovFrame) 4.2는 계층 구조, 패키지 컨벤션, XML 설정 스타일뿐 아니라 자체 런타임 환경(RTE) 유틸리티 jar(`org.egovframe.rte.*`, 예: `fdl.cmmn`, `fdl.excel`, `fdl.property`)를 제공한다. 이 jar들은 Maven Central에 배포되어 있지 않고, 정부 전용 Nexus(`maven.egovframe.go.kr`)를 `pom.xml`의 `<repositories>`에 추가로 등록해야 내려받을 수 있다. 이 저장소는 HTTPS 미지원/접속 불안정 이력이 있는 외부 공공 인프라로, 포트폴리오를 열람하는 면접관이 로컬에서 `git clone` 후 `mvn package`를 직접 실행했을 때 저장소 접속 실패로 빌드가 깨질 위험이 있다.

## 후보

1. **공식 RTE 의존성 직접 사용** — `maven.egovframe.go.kr`을 저장소로 등록하고 `org.egovframe.rte.*` jar에 의존. eGovFrame을 문자 그대로 재현하지만, 외부 저장소 가용성에 빌드 성공 여부가 좌우됨(재현성 리스크를 우리가 통제할 수 없음).
2. **공식 아카이브를 로컬 vendoring(사내 리포지토리/로컬 jar 설치)** — 저장소 이슈는 피하지만, 저장소에 없는 바이너리를 리포에 커밋해야 해서 라이선스/용량 문제가 있고 최신화가 어려움.
3. **순수 Spring 5.3.x + MyBatis + Spring Data JPA로 구현하되 eGovFrame 표준 패키지/계층/XML 설정 스타일은 그대로 준수** *(채택)* — RTE의 FDL 유틸리티(공통 코드/게시판/엑셀 등 부가 기능)는 이 프로젝트 범위(ERP 도메인 로직)에서 직접 요구되지 않으므로, eGovFrame이 실제로 강제하는 핵심인 "계층 구조 + 영속성 정책 + 설정 스타일"만 재현하고 RTE 유틸리티 jar 자체에는 의존하지 않는다.

## 결정

3번(순수 Spring 기반 구현)을 채택한다. `pom.xml`은 Maven Central에 있는 표준 Spring/MyBatis/Hibernate 아티팩트만 사용하며, `org.egovframe.rte.*` 계열 의존성은 추가하지 않는다.

## 근거

- 이 프로젝트가 실제로 증명하려는 역량은 "eGovFrame이 요구하는 계층형 아키텍처와 MyBatis/JPA 정책을 설계·구현할 수 있는가"이지, RTE 유틸리티 jar를 가져다 쓸 수 있는가가 아니다.
- 빌드 재현성(reproducibility)은 포트폴리오의 신뢰도에 직결된다. 면접관의 `mvn package`가 외부 정부 인프라 상태에 좌우되어서는 안 된다.
- README에 이 결정을 명시해 "eGovFrame 표준 구조를 따르는 순수 Spring 구현"임을 투명하게 밝힌다 — 실제 RTE를 썼다고 오해받지 않도록 한다.

## 트레이드오프

- eGovFrame RTE가 제공하는 공통 컴포넌트(예: `EgovAbstractServiceImpl`, 공통 코드 관리, 게시판 유틸)를 그대로 재사용하지 못하고 필요한 만큼만 직접 구현해야 한다.
- "eGovFrame 기반"이라는 문구가 엄밀히는 "RTE 실사용"이 아닌 "표준 아키텍처 패턴 준수"를 의미하므로, 이력서/포트폴리오 설명 시 이 차이를 명확히 전달해야 한다.
