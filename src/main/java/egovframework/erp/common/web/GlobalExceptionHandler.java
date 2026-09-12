package egovframework.erp.common.web;

import egovframework.erp.common.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전 모듈 공통 예외 처리. 도메인 규칙 위반(BusinessException)은 오류 메시지를
 * 화면에 표시하고, 그 외 예상치 못한 예외는 공통 오류 화면으로 안내한다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "common/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Model model) {
        model.addAttribute("errorMessage", "처리 중 오류가 발생했습니다: " + ex.getMessage());
        return "common/error";
    }
}
