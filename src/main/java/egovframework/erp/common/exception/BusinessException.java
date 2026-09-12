package egovframework.erp.common.exception;

/**
 * 도메인 규칙 위반 시 서비스 계층에서 던지는 공통 예외.
 * 모든 모듈이 이 예외를 상속/재사용한다.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
