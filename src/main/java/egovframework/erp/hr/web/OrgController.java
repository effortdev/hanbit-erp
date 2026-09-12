package egovframework.erp.hr.web;

import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr/org")
public class OrgController {

    private final OrgService orgService;

    @Autowired
    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping
    public String tree(Model model) {
        model.addAttribute("orgUnits", orgService.getOrgTree());
        return "hr/orgTree";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("orgUnits", orgService.getOrgTree());
        model.addAttribute("orgUnit", new OrgUnitVO());
        model.addAttribute("orgTypes", OrgType.values());
        return "hr/orgForm";
    }

    @PostMapping
    public String create(@ModelAttribute OrgUnitVO orgUnit) {
        orgService.registerOrgUnit(orgUnit);
        return "redirect:/hr/org";
    }
}
