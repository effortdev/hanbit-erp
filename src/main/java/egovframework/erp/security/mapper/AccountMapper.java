package egovframework.erp.security.mapper;

import egovframework.erp.security.AccountVO;

/**
 * account + employee 조인 조회. 인증은 단순 CRUD가 아니라 조인/판단 로직이 섞여 있어
 * ADR-001 기본 정책대로 MyBatis를 사용한다.
 */
public interface AccountMapper {

    AccountVO selectByUsername(String username);
}
