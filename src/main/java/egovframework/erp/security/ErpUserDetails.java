package egovframework.erp.security;

import egovframework.erp.hr.domain.Position;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 인증된 사용자의 사번/소속 조직/직급을 SecurityContext에 실어 두어, 결재라인 판별(ADR-006)과
 * 문서 조회 권한 검사(NFR-2-2)에서 DB를 다시 조회하지 않고 재사용한다 (docs/adr/ADR-005).
 */
public class ErpUserDetails implements UserDetails {

    private final Long employeeId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final String employeeName;
    private final Position position;
    private final Long orgUnitId;

    public ErpUserDetails(AccountVO account) {
        this.employeeId = account.getEmployeeId();
        this.username = account.getUsername();
        this.passwordHash = account.getPasswordHash();
        this.enabled = account.isEnabled();
        this.employeeName = account.getEmployeeName();
        this.position = account.getPosition();
        this.orgUnitId = account.getOrgUnitId();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public Position getPosition() {
        return position;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Spring Security의 동시 세션 제어(ConcurrencyControlAuthenticationStrategy,
     * docs/adr/ADR-005)는 SessionRegistry에 세션을 이 principal 객체를 키로 등록/조회한다.
     * loadUserByUsername()이 로그인마다 DB를 새로 조회해 매번 새 ErpUserDetails 인스턴스를
     * 만들기 때문에, equals/hashCode를 재정의하지 않으면(기본 Object 동일성 비교) 같은
     * 계정의 서로 다른 로그인 시도가 SessionRegistry에서 "다른 사용자"로 취급되어
     * maximumSessions 제한이 조용히 무력화된다 — username 기준으로 동등성을 정의해 이를 막는다.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErpUserDetails)) return false;
        ErpUserDetails that = (ErpUserDetails) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
