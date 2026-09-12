package egovframework.erp.inventory.web;

import egovframework.erp.inventory.service.ItemService;
import egovframework.erp.inventory.service.PurchaseService;
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
@RequestMapping("/inventory/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ItemService itemService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService, ItemService itemService) {
        this.purchaseService = purchaseService;
        this.itemService = itemService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", purchaseService.getMyRequests(SecurityUtils.currentEmployeeId()));
        return "inventory/purchaseList";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        return "inventory/purchaseForm";
    }

    @PostMapping
    public String create(@RequestParam Long itemId, @RequestParam int quantity,
                          @RequestParam BigDecimal amount, @RequestParam(required = false) String reason) {
        purchaseService.requestPurchase(SecurityUtils.currentEmployeeId(), itemId, quantity, amount, reason);
        return "redirect:/inventory/purchase";
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id) {
        purchaseService.receiveGoods(id, SecurityUtils.currentEmployeeId());
        return "redirect:/inventory/purchase";
    }
}
