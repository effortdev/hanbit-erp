package egovframework.erp.budget.web;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.service.ExpenseService;
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
@RequestMapping("/budget/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", expenseService.getMyRequests(SecurityUtils.currentEmployeeId()));
        return "budget/expenseList";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categories", AccountCategory.values());
        return "budget/expenseForm";
    }

    @PostMapping
    public String create(@RequestParam String title,
                          @RequestParam(required = false) String content,
                          @RequestParam AccountCategory accountCategory,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String exceptionReason) {
        expenseService.requestExpense(SecurityUtils.currentEmployeeId(), title, content, accountCategory, amount, exceptionReason);
        return "redirect:/budget/expense";
    }
}
