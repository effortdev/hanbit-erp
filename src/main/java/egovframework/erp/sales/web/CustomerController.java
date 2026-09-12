package egovframework.erp.sales.web;

import egovframework.erp.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sales/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "sales/customerList";
    }

    @GetMapping("/new")
    public String newForm() {
        return "sales/customerForm";
    }

    @PostMapping
    public String create(@RequestParam String name,
                          @RequestParam(required = false) String contactPerson,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String address) {
        customerService.registerCustomer(name, contactPerson, phone, email, address);
        return "redirect:/sales/customers";
    }
}
