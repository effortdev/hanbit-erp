package egovframework.erp.budget.service;

import egovframework.erp.budget.domain.AccountCategory;
import egovframework.erp.budget.domain.BudgetExpenseRequestVO;

import java.math.BigDecimal;
import java.util.List;

/** FR-4-2: 지출결의서는 전자결재를 통해 상신되며 승인 시 예산에서 차감된다 (docs/adr/ADR-012). */
public interface ExpenseService {

    /**
     * @param exceptionReason 예산 100% 초과 + 500만원 이상 건에서만 필요 (그 외엔 무시됨)
     * @return 생성된 budget_expense_request의 id
     */
    Long requestExpense(Long employeeId, String title, String content, AccountCategory accountCategory,
                         BigDecimal amount, String exceptionReason);

    List<BudgetExpenseRequestVO> getMyRequests(Long employeeId);
}
