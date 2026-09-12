package egovframework.erp.budget.web;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.service.BudgetAllocationService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetAllocationService budgetAllocationService;

    @Autowired
    public BudgetController(BudgetAllocationService budgetAllocationService) {
        this.budgetAllocationService = budgetAllocationService;
    }

    @GetMapping
    public String usage(Model model) {
        Long orgUnitId = SecurityUtils.currentUser().getOrgUnitId();
        model.addAttribute("usages", budgetAllocationService.getUsage(orgUnitId));
        return "budget/usage";
    }

    @GetMapping("/allocations/new")
    public String newAllocationForm(Model model) {
        model.addAttribute("categories", AccountCategory.values());
        return "budget/allocationForm";
    }

    @PostMapping("/allocations")
    public String createAllocation(@RequestParam AccountCategory accountCategory,
                                    @RequestParam int fiscalYear,
                                    @RequestParam BigDecimal amount) {
        Long orgUnitId = SecurityUtils.currentUser().getOrgUnitId();
        budgetAllocationService.allocate(orgUnitId, accountCategory, fiscalYear, amount);
        return "redirect:/budget";
    }
}
