package egovframework.erp.attendance.web;

import egovframework.erp.attendance.service.AttendanceService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String home(Model model) {
        Long me = SecurityUtils.currentEmployeeId();
        model.addAttribute("today", attendanceService.getTodayRecord(me));
        model.addAttribute("records", attendanceService.getMyRecords(me));
        return "attendance/home";
    }

    @PostMapping("/check-in")
    public String checkIn() {
        attendanceService.checkIn(SecurityUtils.currentEmployeeId());
        return "redirect:/attendance";
    }

    @PostMapping("/check-out")
    public String checkOut() {
        attendanceService.checkOut(SecurityUtils.currentEmployeeId());
        return "redirect:/attendance";
    }
}
