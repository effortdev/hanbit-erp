<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>예산 배정 (본인 소속 조직 기준)</h2>
    <form method="post" action="${pageContext.request.contextPath}/budget/allocations">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>계정과목</label>
        <select name="accountCategory">
            <c:forEach var="c" items="${categories}">
                <option value="${c}">${c.label}</option>
            </c:forEach>
        </select>
        <label>회계연도</label>
        <input type="number" name="fiscalYear" value="2026" required="required"/>
        <label>배정액</label>
        <input type="number" name="amount" min="1" required="required"/>
        <button type="submit">배정</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
