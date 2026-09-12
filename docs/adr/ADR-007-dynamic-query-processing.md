# ADR-007: 동적 쿼리 처리 방식

- 상태: 승인됨 (소급 작성 — "후속 기록" 참고)
- 범위: 프로젝트 전체

## 상황

프로젝트 킥오프 시점에 "기술 선택이 필요한 지점(예: 동적 쿼리 처리, 인증 방식, 트랜잭션 처리 등)마다 ADR을 작성한다"는 원칙을 정했다. 인증 방식은 ADR-005, 트랜잭션 처리는 ADR-003(및 ADR-008의 AFTER_COMMIT 관련 후속 각주)에서 다뤘지만, 동적 쿼리 처리는 전용 ADR 없이 넘어갔다. ADR-001이 이미 "데이터 접근은 기본적으로 MyBatis(SqlMap)"라는 기본 정책을 정해뒀는데, 조건에 따라 WHERE 절이나 IN 절이 달라지는 "동적 쿼리"를 그 안에서 어떻게 처리할지(MyBatis 동적 SQL만으로 충분한지, QueryDSL 같은 별도 도구가 필요한지)를 명확히 해야 한다.

## 결정

**동적 쿼리는 MyBatis 동적 SQL(`<if>`, `<choose>`, `<foreach>`, `<where>` 등)로 처리하고, QueryDSL이나 JPA Criteria 같은 별도 동적 쿼리 빌더는 도입하지 않는다.** 이는 ADR-001이 정한 "MyBatis 기본" 정책의 연장선이다 — 데이터 접근 기술을 모듈마다 다르게 가져가지 않듯, 쿼리를 정적으로 짤지 동적으로 짤지도 같은 매퍼 XML 안에서 MyBatis의 표준 기능만으로 해결한다.

### 전수 조사 결과

프로젝트 전체 매퍼 XML 17개 파일을 전수 조사한 결과, 동적 SQL 태그가 실제로 쓰인 곳은 다음 한 곳뿐이었다.

```bash
grep -rn "<if \|<if>\|<choose>\|<foreach\|<where>\|<set>\|<trim" src/main/resources/egovframework/mapper/
# -> src/main/resources/egovframework/mapper/budget/BudgetExpenseRequestMapper.xml:41
```

```xml
<!-- Module 4: BudgetExpenseRequestMapper.xml -->
<select id="sumAmountByStatuses" resultType="java.math.BigDecimal">
    SELECT COALESCE(SUM(amount), 0)
    FROM budget_expense_request
    WHERE org_unit_id = #{orgUnitId}
      AND account_category = #{accountCategory}
      AND YEAR(created_at) = #{fiscalYear}
      AND status IN
      <foreach collection="statuses" item="s" open="(" separator="," close=")">
          #{s}
      </foreach>
</select>
```

호출측(`BudgetAllocationServiceImpl`, `ExpenseServiceImpl`)에서 "승인완료+상신중" 두 상태를 한 번에 합산하거나(`List.of(APPROVED, PENDING)`), 상황에 따라 상태 목록의 개수가 달라질 수 있어 `<foreach>`로 IN 절을 동적으로 구성했다. 그 외 모든 조회(Module 1의 조직/사원 조회, Module 2의 결재라인/문서 조회, Module 3의 휴가/근태 조회, Module 5의 재고/구매 조회, Module 6의 거래처/수주 조회 등)는 조건 분기 없이 고정된 WHERE 절만으로 충분해 정적 쿼리로 작성했다.

## 근거

- ADR-001이 이미 "MyBatis 기본, JPA는 단순 CRUD 예외"라는 정책을 정해뒀다. 동적 쿼리 때문에 QueryDSL 같은 제3의 기술을 추가하면, "데이터 접근은 MyBatis(+ 예외적 JPA) 하나로 통일한다"는 원래 결정과 충돌한다 — 기술 스택을 필요 이상으로 늘리지 않는다.
- 전수 조사로 확인했듯 이 프로젝트의 동적 쿼리 요구는 매우 얕다(IN 절 하나). MyBatis의 `<foreach>`만으로 완전히 해결되는 문제에 QueryDSL의 타입-세이프 쿼리 빌더, JPAQueryFactory 설정, Q클래스 생성 빌드 스텝까지 들여오는 것은 Module 4의 `BudgetThresholdPolicy`(요구사항 근거 없이 미리 만들었다가 폐기한 사례, ADR-012 참고)와 같은 종류의 과설계다.
- 조회 조건이 복잡해질수록 동적 SQL이 매퍼 XML 안에서 문자열처럼 흩어져 가독성이 떨어진다는 것이 QueryDSL 도입의 흔한 논거이지만, 이 프로젝트는 그 정도 복잡도에 도달한 조회가 하나도 없다 — 근거 없는 조건을 미리 도입하지 않는다는 원칙(ADR-011, ADR-014, ADR-016에서 반복 확인)을 여기에도 그대로 적용한다.

## 후속 기록 — 왜 이 ADR이 처음엔 없었는가

애초에 이 결정을 별도 ADR로 남기지 않은 이유는, ADR-001이 이미 MyBatis 기본 정책을 정해뒀고 동적 쿼리도 그 연장선이라 "새로운 결정"이라기보다 당연한 귀결로 판단했기 때문이다. 그런데 Module 6 완료 후 ADR 번호 목록을 점검하는 과정에서 ADR-007이 언제 봐도 빈 번호였다는 게 확인됐다 — Module 3 착수 시점에 다음 ADR 번호를 008로 지정하면서 007이 통째로 비었고, 아무도(사용자도, 나도) 그 시점에 "동적 쿼리 처리"를 명시적으로 다루지 않았다는 사실을 뒤늦게 짚었다. 이 문서는 그 빈 번호를 프로젝트 킥오프 시점에 실제로 내려져 있던 결정(MyBatis 동적 SQL 사용, QueryDSL 미도입)을 근거와 함께 소급 기록한 것이다 — 이번에 새로 내린 결정이 아니라, 처음부터 있었지만 문서화되지 않았던 결정을 지금 문서화한 것이다.

## 트레이드오프

- MyBatis 동적 SQL은 QueryDSL/JPA Criteria 대비 컴파일 타임 타입 안전성이 없다(오탈자나 잘못된 컬럼명이 런타임에야 드러남). 이 프로젝트 규모(동적 쿼리 지점 1곳)에서는 이 리스크가 크지 않다고 판단했다.
- 향후 조회 조건이 훨씬 복잡해지는 화면(예: 다중 필터 검색)이 생기면 MyBatis의 `<if>`/`<where>` 조합만으로는 매퍼 XML이 급격히 지저분해질 수 있다 — 그 시점이 오면 이 ADR을 재검토해야 한다.
