package egovframework.erp.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/hr/org";
    }

    /**
     * Chrome DevTools가 개발자도구를 열 때마다 자동으로 찔러보는 경로 (앱 로직과 무관).
     * 매핑이 없으면 mvc:default-servlet-handler를 거쳐 컨테이너 기본 서블릿의 404
     * 에러 페이지로 떨어져 콘솔에 노이즈가 남는다 — 조용히 204로 받아넘긴다.
     */
    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void chromeDevtoolsProbe() {
    }
}
