package egovframework.erp.sales.web;

import egovframework.erp.sales.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalesDashboardController {

    private final SalesOrderService salesOrderService;

    @Autowired
    public SalesDashboardController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    /** FR-6-4: 월별/부서별 매출 현황. */
    @GetMapping("/sales/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("summary", salesOrderService.getMonthlySummary());
        return "sales/dashboard";
    }
}
