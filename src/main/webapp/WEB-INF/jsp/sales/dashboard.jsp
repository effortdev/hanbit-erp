<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>매출 대시보드 (월별/부서별)</h2>
    <a href="${pageContext.request.contextPath}/sales/orders">수주 목록</a>
</div>

<div class="card">
    <table>
        <thead><tr><th>연월</th><th>소속 부서</th><th>매출액</th></tr></thead>
        <tbody>
        <c:forEach var="s" items="${summary}">
            <tr>
                <td>${s.yearMonth}</td>
                <td>${s.orgUnitName}</td>
                <td>${s.totalAmount}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty summary}">
            <tr><td colspan="3">확정된 매출이 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
