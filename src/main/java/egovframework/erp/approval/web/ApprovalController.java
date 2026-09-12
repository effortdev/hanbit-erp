package egovframework.erp.approval.web;

import egovframework.erp.approval.domain.ActionType;
import egovframework.erp.approval.domain.DocumentType;
import egovframework.erp.approval.service.ApprovalService;
import egovframework.erp.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    @Autowired
    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public String inbox(Model model) {
        Long me = SecurityUtils.currentEmployeeId();
        model.addAttribute("myDrafts", approvalService.getMyDrafts(me));
        model.addAttribute("myInbox", approvalService.getMyInbox(me));
        model.addAttribute("documentTypes", DocumentType.values());
        return "approval/inbox";
    }

    @GetMapping("/draft/{type}")
    public String draftForm(@PathVariable DocumentType type, Model model) {
        model.addAttribute("documentType", type);
        return "approval/draft";
    }

    @PostMapping("/draft/vacation")
    public String draftVacation(@RequestParam String title,
                                 @RequestParam(required = false) String content,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long id = approvalService.draftVacation(SecurityUtils.currentEmployeeId(), title, content, startDate, endDate);
        return "redirect:/approval/" + id;
    }

    @PostMapping("/draft/expense")
    public String draftExpense(@RequestParam String title,
                                @RequestParam(required = false) String content,
                                @RequestParam BigDecimal amount) {
        Long id = approvalService.draftExpense(SecurityUtils.currentEmployeeId(), title, content, amount);
        return "redirect:/approval/" + id;
    }

    @PostMapping("/draft/purchase")
    public String draftPurchase(@RequestParam String title,
                                 @RequestParam(required = false) String content,
                                 @RequestParam BigDecimal amount) {
        Long id = approvalService.draftPurchase(SecurityUtils.currentEmployeeId(), title, content, amount);
        return "redirect:/approval/" + id;
    }

    @PostMapping("/draft/general")
    public String draftGeneral(@RequestParam String title, @RequestParam(required = false) String content) {
        Long id = approvalService.draftGeneral(SecurityUtils.currentEmployeeId(), title, content);
        return "redirect:/approval/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long me = SecurityUtils.currentEmployeeId();
        model.addAttribute("detail", approvalService.getDocument(id, me));
        model.addAttribute("me", me);
        return "approval/detail";
    }

    @PostMapping("/{id}/steps/{stepId}/approve")
    public String approve(@PathVariable Long id, @PathVariable Long stepId, @RequestParam(required = false) String comment) {
        approvalService.act(id, stepId, SecurityUtils.currentEmployeeId(), ActionType.APPROVE, comment);
        return "redirect:/approval/" + id;
    }

    @PostMapping("/{id}/steps/{stepId}/reject")
    public String reject(@PathVariable Long id, @PathVariable Long stepId, @RequestParam String comment) {
        approvalService.act(id, stepId, SecurityUtils.currentEmployeeId(), ActionType.REJECT, comment);
        return "redirect:/approval/" + id;
    }
}
