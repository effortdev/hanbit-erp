<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>${documentType.label} 기안</h2>
    <form method="post" action="${pageContext.request.contextPath}/approval/draft/${fn:toLowerCase(documentType)}">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <label>제목</label>
        <input type="text" name="title" required="required"/>

        <c:if test="${documentType == 'VACATION'}">
            <label>시작일</label>
            <input type="date" name="startDate" required="required"/>
            <label>종료일</label>
            <input type="date" name="endDate" required="required"/>
        </c:if>

        <c:if test="${documentType == 'EXPENSE' || documentType == 'PURCHASE'}">
            <label>금액</label>
            <input type="number" name="amount" min="1" step="1" required="required"/>
        </c:if>

        <label>내용/사유</label>
        <input type="text" name="content"/>

        <button type="submit">상신</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
