package egovframework.erp.hr.web;

import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.service.EmpService;
import egovframework.erp.hr.service.OrgService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * 발령 처리자(changedBy)는 Module 2에서 도입된 로그인 사용자 정보(SecurityUtils)에서 가져온다
 * — Module 1 README에 남겨두었던 "인증 방식은 Module 2에서 다룬다"는 제약이 여기서 해소된다.
 */
@Controller
@RequestMapping("/hr/emp")
public class EmpController {

    private final EmpService empService;
    private final OrgService orgService;

    @Autowired
    public EmpController(EmpService empService, OrgService orgService) {
        this.empService = empService;
        this.orgService = orgService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", empService.getEmployees());
        model.addAttribute("orgUnits", orgService.getOrgTree());
        return "hr/empList";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("orgUnits", orgService.getOrgTree());
        model.addAttribute("positions", Position.values());
        return "hr/empForm";
    }

    @PostMapping
    public String create(@RequestParam String name,
                          @RequestParam Position position,
                          @RequestParam Long orgUnitId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDate) {
        empService.register(name, position, orgUnitId, hireDate);
        return "redirect:/hr/emp";
    }

    @PostMapping("/{id}/transfer")
    public String transfer(@PathVariable Long id, @RequestParam Long newOrgUnitId) {
        empService.transfer(id, newOrgUnitId, SecurityUtils.currentUser().getEmployeeName());
        return "redirect:/hr/emp/" + id + "/history";
    }

    @PostMapping("/{id}/promote")
    public String promote(@PathVariable Long id, @RequestParam Position newPosition) {
        empService.promote(id, newPosition, SecurityUtils.currentUser().getEmployeeName());
        return "redirect:/hr/emp/" + id + "/history";
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        model.addAttribute("employee", empService.getEmployee(id));
        model.addAttribute("history", empService.getHistory(id));
        model.addAttribute("orgUnits", orgService.getOrgTree());
        return "hr/empHistory";
    }
}
