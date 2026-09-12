package egovframework.erp.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** 현재 로그인 사용자 정보를 서비스/컨트롤러 어디서든 꺼내 쓰기 위한 헬퍼. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static ErpUserDetails currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof ErpUserDetails)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        return (ErpUserDetails) principal;
    }

    public static Long currentEmployeeId() {
        return currentUser().getEmployeeId();
    }
}
