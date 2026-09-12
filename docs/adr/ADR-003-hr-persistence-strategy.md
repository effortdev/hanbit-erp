# ADR-003: 조직/사원관리 모듈의 영속성 전략 (MyBatis/JPA 혼합 + 트랜잭션 통합)

- 상태: 승인됨
- 범위: Module 1 (조직/사원관리, `egovframework.erp.hr`)

## 상황

ADR-001에 따라 데이터 접근은 기본적으로 MyBatis를 쓰되, 단순 CRUD에 한해 JPA를 허용하고 그 근거를 남겨야 한다. 조직/사원관리 모듈에는 성격이 다른 세 가지 데이터 접근이 섞여 있다.

- 사원 등록/조회/수정: 단건 기준 전형적인 CRUD (FR-1-2)
- 조직도 조회: 본부-팀 계층을 트리 형태로 정렬해 내려줘야 함 (FR-1-1)
- 발령 이력: 수정 불가(append-only)로 쌓여야 하고, 조회 시 사원/조직 정보와 조인이 필요함 (FR-1-3)

이 셋을 모두 같은 기술로 강제하면 부자연스럽다. 또한 "발령 처리"는 사원의 현재 소속을 바꾸는 동시에 발령 이력을 남기는 **하나의 트랜잭션**이어야 하므로 (NFR: 정합성), 두 기술을 섞으면서도 트랜잭션 경계를 하나로 묶을 방법이 필요하다.

## 후보

1. **전체 MyBatis 통일** — 가장 일관되지만, 사원 등록처럼 정말 단순한 CRUD에도 매퍼 XML을 매번 작성해야 해 보일러플레이트가 늘고, ADR-001이 허용한 "단순 CRUD의 JPA 예외"를 실제로 시연하지 못한다.
2. **전체 JPA 통일** — 조직 트리 조회처럼 정렬·계층 표현이 필요한 조회를 JPQL/Criteria로 표현하면 오히려 SQL보다 가독성이 떨어지고, "결재 이력은 append-only여야 한다"는 제약을 엔티티 매핑만으로 강제하기 어렵다(JPA는 기본적으로 update 가능한 영속성 컨텍스트를 가정).
3. **혼합: 사원 CRUD = Spring Data JPA, 조직 트리·발령이력 = MyBatis** *(채택)*

```java
// 사원 기본 CRUD — Spring Data JPA (단순 CRUD 예외 적용, ADR-001 근거)
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByOrgUnitId(Long orgUnitId);
}

// 조직 트리 조회 — MyBatis (정렬된 계층 구조는 SQL 제어가 명확함)
public interface OrgMapper {
    List<OrgUnitVO> selectOrgTree();
}
<!-- OrgMapper.xml -->
<select id="selectOrgTree" resultType="OrgUnitVO">
  SELECT id, name, type, parent_id
  FROM org_unit
  ORDER BY COALESCE(parent_id, id), type ASC, id
</select>

// 발령 이력 — MyBatis insert만 노출 (update/delete 매핑 자체를 만들지 않아 append-only를 코드 구조로 강제)
public interface EmpHistoryMapper {
    void insertHistory(EmpHistoryVO history);
    List<EmpHistoryVO> selectByEmployeeId(Long employeeId);
}
```

## 결정

혼합 전략(3번)을 채택한다. 그리고 발령 처리처럼 JPA 엔티티 변경과 MyBatis insert가 한 트랜잭션에 묶여야 하는 경우를 위해, **`JpaTransactionManager`를 애플리케이션 전역의 유일한 `PlatformTransactionManager`로 등록**한다.

```xml
<!-- context-jpa.xml -->
<bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
  <property name="entityManagerFactory" ref="entityManagerFactory"/>
</bean>
<tx:annotation-driven transaction-manager="transactionManager"/>
```

```xml
<!-- context-mybatis.xml: MyBatis는 같은 DataSource를 참조할 뿐, 별도 트랜잭션 매니저를 두지 않는다 -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <property name="dataSource" ref="dataSource"/>
  <property name="mapperLocations" value="classpath:egovframework/mapper/**/*.xml"/>
</bean>
```

```java
@Service
public class EmpServiceImpl implements EmpService {
    private final EmployeeRepository employeeRepository; // JPA
    private final EmpHistoryMapper empHistoryMapper;      // MyBatis

    @Transactional
    public void transfer(Long employeeId, Long newOrgUnitId, String changedBy) {
        Employee emp = employeeRepository.findById(employeeId).orElseThrow();
        Long beforeOrgUnitId = emp.getOrgUnitId();
        emp.changeOrgUnit(newOrgUnitId);           // JPA: 영속성 컨텍스트 dirty checking으로 UPDATE
        empHistoryMapper.insertHistory(EmpHistoryVO.transfer(
            employeeId, beforeOrgUnitId, newOrgUnitId, changedBy)); // MyBatis: 명시적 INSERT
        // 두 작업 모두 같은 JpaTransactionManager가 연 JDBC Connection 위에서 커밋/롤백된다.
    }
}
```

## 근거

- `JpaTransactionManager`는 내부적으로 `DataSourceTransactionManager`와 동일하게 `DataSource`에서 얻은 `Connection`을 트랜잭션 동기화 저장소(`TransactionSynchronizationManager`)에 바인딩한다. MyBatis-Spring의 `SqlSessionTemplate`은 트랜잭션 동기화 저장소에 바인딩된 커넥션이 있으면 그것을 재사용하도록 설계되어 있어, 별도 설정 없이 JPA가 연 트랜잭션에 MyBatis 호출이 자동으로 참여한다. 즉 트랜잭션 매니저를 두 개 두지 않고도 "JPA 변경 + MyBatis insert"가 원자적으로 처리된다.
- 발령 이력 Mapper에 `update`/`delete` 메서드를 아예 만들지 않음으로써, "append-only여야 한다"는 요구를 코드 리뷰 시점에 시각적으로 드러나게 한다(런타임 트리거보다 감사 추적이 쉬움).
- 사원 CRUD만 JPA로 남겨 ADR-001이 요구한 "단순 CRUD 예외 + 근거 기록"을 실제 코드로 시연한다.

## 트레이드오프

- 같은 모듈 안에 두 가지 영속성 기술이 공존해 처음 보는 개발자는 어디에 무엇을 추가해야 하는지 학습 비용이 든다 (본 ADR과 패키지 내 주석으로 완화).
- `JpaTransactionManager` 단일화는 "MyBatis만 쓰는 모듈"에도 동일한 트랜잭션 매니저를 강제하므로, 향후 모듈(전자결재 등)에서 순수 MyBatis만 쓰더라도 JPA 의존성(`spring-orm`, `EntityManagerFactory` 설정)이 클래스패스에 남아있게 된다.
