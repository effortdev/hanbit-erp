package egovframework.erp.hr.web;

import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.service.EmpService;
import egovframework.erp.hr.service.OrgService;
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
 * 로그인/인증은 아직 없다. 인증 방식은 전자결재 모듈(Module 2) ADR에서 다룰 예정이라
 * 발령 처리자(changedBy)는 화면에서 직접 입력받는다 (README 참고).
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
    public String transfer(@PathVariable Long id, @RequestParam Long newOrgUnitId, @RequestParam String changedBy) {
        empService.transfer(id, newOrgUnitId, changedBy);
        return "redirect:/hr/emp/" + id + "/history";
    }

    @PostMapping("/{id}/promote")
    public String promote(@PathVariable Long id, @RequestParam Position newPosition, @RequestParam String changedBy) {
        empService.promote(id, newPosition, changedBy);
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
