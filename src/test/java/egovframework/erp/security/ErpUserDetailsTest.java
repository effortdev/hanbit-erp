package egovframework.erp.security;

import egovframework.erp.hr.domain.Position;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * ADR-005 후속 기록: maximumSessions(1) 동시 로그인 제어가 실제로는 아무 세션도 만료시키지
 * 못했던 원인 재현/회귀 방지. ErpUserDetailsService.loadUserByUsername()은 로그인마다 DB를
 * 새로 조회해 매번 새 ErpUserDetails 인스턴스를 만드는데, equals/hashCode를 재정의하지
 * 않으면(기본 Object 동일성) Spring Security의 SessionRegistry가 "같은 계정의 재로그인"을
 * 서로 다른 사용자로 취급해 만료 대상 세션을 하나도 찾지 못한다.
 */
class ErpUserDetailsTest {

    private AccountVO accountFor(String username) {
        AccountVO account = new AccountVO();
        account.setEmployeeId(1L);
        account.setUsername(username);
        account.setPasswordHash("hash");
        account.setEnabled(true);
        account.setEmployeeName("테스트");
        account.setPosition(Position.TEAM_LEADER);
        account.setOrgUnitId(1L);
        return account;
    }

    @Test
    void loadingSameUsernameTwiceProducesEqualPrincipals() {
        ErpUserDetails firstLogin = new ErpUserDetails(accountFor("leader1"));
        ErpUserDetails secondLogin = new ErpUserDetails(accountFor("leader1"));

        assertEquals(firstLogin, secondLogin);
        assertEquals(firstLogin.hashCode(), secondLogin.hashCode());
    }

    @Test
    void differentUsernamesProduceUnequalPrincipals() {
        ErpUserDetails leader = new ErpUserDetails(accountFor("leader1"));
        ErpUserDetails staff = new ErpUserDetails(accountFor("staff1"));

        assertNotEquals(leader, staff);
    }

    @Test
    void sessionRegistryFindsExistingSessionOnReLogin() {
        // ConcurrentSessionControlAuthenticationStrategy가 재로그인 시 실제로 호출하는 것과
        // 동일한 조회 — 이게 빈 리스트를 반환하면 maximumSessions는 아무 것도 만료시키지 못한다.
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();

        ErpUserDetails firstLogin = new ErpUserDetails(accountFor("leader1"));
        sessionRegistry.registerNewSession("session-A", firstLogin);

        ErpUserDetails secondLogin = new ErpUserDetails(accountFor("leader1"));
        List<SessionInformation> existingSessions = sessionRegistry.getAllSessions(secondLogin, false);

        assertEquals(1, existingSessions.size(),
                "재로그인 시점에 같은 계정의 기존 세션을 찾아야 maximumSessions가 동작한다");
        assertEquals("session-A", existingSessions.get(0).getSessionId());
    }
}
