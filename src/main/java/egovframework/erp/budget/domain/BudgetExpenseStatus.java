package egovframework.erp.budget.domain;

/** budget_expense_request의 현재 상태. Module 2 이벤트 수신으로 갱신된다 (Module 3의 VacationStatus와 동일 패턴). */
public enum BudgetExpenseStatus {
    PENDING,
    APPROVED,
    REJECTED
}
