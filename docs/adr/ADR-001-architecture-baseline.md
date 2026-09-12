# ADR-001: 아키텍처 베이스라인 (계층 구조 / 영속성 기본 정책)

- 상태: 승인됨
- 범위: 프로젝트 전체

## 상황

㈜한빛전자 미니 ERP는 SI/공공기관 채용 대응 포트폴리오로 구축한다. eGovFrame(전자정부표준프레임워크)이 요구하는 표준 개발 관행을 그대로 재현해야 하며, 6개 모듈(조직/사원, 전자결재, 근태, 예산/지출, 재고/구매, 영업/매출)이 공통된 계층 구조와 데이터 접근 정책을 공유해야 한다. 정책을 프로젝트 시작 시점에 고정해 두지 않으면 모듈마다 계층 이름이나 책임 범위가 달라져 유지보수성과 채용 포트폴리오로서의 설득력이 떨어진다.

## 후보

1. **Controller가 DAO/Mapper를 직접 호출** — 계층이 얇아 초기 개발은 빠르지만, 비즈니스 로직이 Controller에 누적되고 트랜잭션 경계를 서비스 계층에 둘 수 없어 eGovFrame 표준과 어긋남.
2. **Controller → Service(구현체 겸용) → Mapper** — Service 인터페이스와 구현체를 분리하지 않는 방식. 테스트 시 목(mock) 대체가 어렵고, eGovFrame 표준 산출물 템플릿(인터페이스/구현체 분리)과 불일치.
3. **Controller → Service(interface) → ServiceImpl → DAO/Mapper** *(채택)* — eGovFrame 표준 개발환경이 강제하는 계층. 인터페이스와 구현을 분리해 테스트 용이성을 확보하고, `@Transactional` 경계를 ServiceImpl에 명확히 둘 수 있음.

```java
// 후보 3 (채택) 예시
public interface EmpService {
    Employee register(Employee emp);
}

@Service
public class EmpServiceImpl implements EmpService {
    private final EmployeeRepository employeeRepository;
    @Transactional
    public Employee register(Employee emp) { return employeeRepository.save(emp); }
}
```

## 결정

`Controller → Service(interface) → ServiceImpl → DAO/Mapper` 4단 계층을 전 모듈 공통 표준으로 채택한다. 데이터 접근은 기본적으로 **MyBatis(SqlMap)** 를 사용하고, 조회/등록/수정이 단건 위주인 **단순 CRUD에 한해 JPA(Spring Data JPA)** 사용을 허용한다. JPA를 적용하는 모듈/클래스에는 반드시 "왜 이 모듈만 JPA인지" 근거를 코드 주석 또는 별도 ADR로 남긴다 (예: Module 1의 `docs/adr/ADR-003-hr-persistence-strategy.md`).

## 근거

- eGovFrame 실무 채용 공고와 SI 현장 관행이 요구하는 계층 구조를 그대로 재현해 포트폴리오의 신뢰도를 높인다.
- 인터페이스/구현 분리는 서비스 계층 단위 테스트(Mockito 등)를 쉽게 하고, 계층 간 책임을 코드 리뷰로 검증 가능하게 한다.
- MyBatis를 기본으로 하되 JPA를 예외적으로 허용함으로써, "왜 이 기술을 썼는가"를 매번 설명해야 하는 규율을 강제해 기술 선택의 자의성을 줄인다.

## 트레이드오프

- 계층이 늘어나 단순 CRUD 하나에도 인터페이스/구현체/매퍼 세 파일이 필요해 초기 보일러플레이트가 늘어난다.
- MyBatis/JPA를 혼용하는 모듈은 트랜잭션 매니저를 하나로 통합해야 하는 추가 설계 부담이 있다 (Module 1 ADR-004에서 다룸).
