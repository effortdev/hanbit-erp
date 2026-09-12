# ㈜한빛전자 미니 ERP

전자정부프레임워크(eGovFrame) 표준 구조를 따르는 미니 ERP 포트폴리오 프로젝트.

## 스택 요약

| 항목 | 선택 | 비고 |
|---|---|---|
| 아키텍처 | Controller → Service → ServiceImpl → DAO/Mapper | `docs/adr/ADR-001-architecture-baseline.md` |
| 데이터 접근 | MyBatis 기본, 단순 CRUD에 한해 JPA 허용 | 모듈별 근거는 각 모듈 ADR 참고 |
| eGovFrame 런타임 | **RTE jar 미사용, 순수 Spring 5.3.x 구현** | `docs/adr/ADR-002-egovframe-runtime-strategy.md` — 반드시 읽어볼 것 |
| 화면 | Spring MVC + JSP | eGovFrame 전통 방식 |
| 인증 | Spring Security + 세션 기반 폼 로그인 | `docs/adr/ADR-005-authentication.md` |
| 모듈 간 연동 | 정방향은 직접 서비스 호출, 역방향은 Spring 이벤트(`@TransactionalEventListener`) | `docs/adr/ADR-006`, `ADR-008` |
| DB | MySQL 8 (Docker Compose) | `docker-compose.yml` |
| 빌드 | Maven, Java 17 | `pom.xml` |
| 로컬 실행 | Jetty(`jetty-maven-plugin`) | 배포 산출물은 표준 WAR — `docs/troubleshooting.md` 참고 |

> **주의**: 이 프로젝트는 "eGovFrame 표준 구조를 따르는 순수 Spring 구현"입니다. 공식 eGovFrame RTE(`org.egovframe.rte.*`) 유틸리티 jar는 사용하지 않습니다 (이유는 ADR-002 참고).

## 실행 방법

```bash
# 1. MySQL 기동
docker compose up -d

# 2. 스키마 적용 (로컬 3306이 다른 프로젝트에서 이미 쓰이는 경우가 많아 3309로 매핑함)
# --default-character-set=utf8mb4를 꼭 지정할 것 (누락 시 한글 시드 데이터가 깨져서 저장됨 — docs/troubleshooting.md)
mysql --default-character-set=utf8mb4 -h127.0.0.1 -P3309 -uerp -perp1234 hanbit_erp < sql/schema.sql

# 3. 빌드/실행 (JAVA_HOME을 JDK 17로 지정)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
mvn jetty:run
# http://localhost:8081/login  (데모 계정: ceo/hqhead1/leader1/staff1 등, 비밀번호 전부 erp1234! — sql/schema.sql 참고)
```

## 모듈 진행 순서

1. 조직/사원관리 (기반 도메인) — **완료**
2. 전자결재 — **완료**
3. 근태관리 — **완료**
4. 예산/지출관리 — **완료**
5. 재고/구매관리 — **완료**
6. 영업/매출관리

## 문서

- `docs/adr/` — 기술 선택 지점별 Architecture Decision Record
- `docs/troubleshooting.md` — 진행 중 발생한 이슈와 해결 과정
