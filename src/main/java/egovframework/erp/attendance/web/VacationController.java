package egovframework.erp.attendance.web;

import egovframework.erp.attendance.service.VacationBalanceService;
import egovframework.erp.attendance.service.VacationService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/attendance/vacation")
public class VacationController {

    private final VacationService vacationService;
    private final VacationBalanceService vacationBalanceService;

    @Autowired
    public VacationController(VacationService vacationService, VacationBalanceService vacationBalanceService) {
        this.vacationService = vacationService;
        this.vacationBalanceService = vacationBalanceService;
    }

    @GetMapping
    public String list(Model model) {
        Long me = SecurityUtils.currentEmployeeId();
        model.addAttribute("requests", vacationService.getMyRequests(me));
        model.addAttribute("annualDays", vacationBalanceService.getAnnualDays(me));
        model.addAttribute("remainingDays", vacationBalanceService.getRemainingDays(me));
        return "attendance/vacationList";
    }

    @GetMapping("/new")
    public String newForm() {
        return "attendance/vacationForm";
    }

    @PostMapping
    public String create(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(required = false) String reason) {
        vacationService.requestVacation(SecurityUtils.currentEmployeeId(), startDate, endDate, reason);
        return "redirect:/attendance/vacation";
    }
}
