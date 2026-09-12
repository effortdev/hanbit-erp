package egovframework.erp.inventory.web;

import egovframework.erp.inventory.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inventory")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        return "inventory/itemList";
    }

    @GetMapping("/items/new")
    public String newForm() {
        return "inventory/itemForm";
    }

    @PostMapping("/items")
    public String create(@RequestParam String name, @RequestParam String unit,
                          @RequestParam int initialStock, @RequestParam int safetyStock) {
        itemService.registerItem(name, unit, initialStock, safetyStock);
        return "redirect:/inventory";
    }
}
