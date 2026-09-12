package egovframework.erp.sales.web;

import egovframework.erp.inventory.service.ItemService;
import egovframework.erp.sales.service.CustomerService;
import egovframework.erp.sales.service.SalesOrderService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/sales/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final CustomerService customerService;
    private final ItemService itemService;

    @Autowired
    public SalesOrderController(SalesOrderService salesOrderService, CustomerService customerService, ItemService itemService) {
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.itemService = itemService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", salesOrderService.getMyOrders(SecurityUtils.currentEmployeeId()));
        return "sales/orderList";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("items", itemService.getAllItems()); // FR-6-2: 가용재고 표시
        return "sales/orderForm";
    }

    @PostMapping
    public String create(@RequestParam Long customerId, @RequestParam Long itemId,
                          @RequestParam int quantity, @RequestParam BigDecimal unitPrice) {
        salesOrderService.registerOrder(SecurityUtils.currentEmployeeId(), customerId, itemId, quantity, unitPrice);
        return "redirect:/sales/orders";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id) {
        salesOrderService.confirmOrder(id, SecurityUtils.currentEmployeeId());
        return "redirect:/sales/orders";
    }
}
