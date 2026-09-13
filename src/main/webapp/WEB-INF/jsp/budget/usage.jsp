<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>예산 소진 현황 (올해)</h2>
    <div class="page-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/budget/allocations/new">+ 예산 배정</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/budget/expense">지출결의서 목록</a>
    </div>
</div>

<div class="card">
    <table>
        <thead><tr><th>계정과목</th><th>배정액</th><th>사용액(승인+상신중)</th><th>잔여</th><th>소진율</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="u" items="${usages}">
            <tr>
                <td>${u.accountCategory.label}</td>
                <td><fmt:formatNumber value="${u.allocated}" type="number"/></td>
                <td><fmt:formatNumber value="${u.used}" type="number"/></td>
                <td><fmt:formatNumber value="${u.remaining}" type="number"/></td>
                <td><fmt:formatNumber value="${u.usageRate}" type="percent" maxFractionDigits="1"/></td>
                <td><c:if test="${u.warning}"><span class="badge badge-warning">80% 경고</span></c:if></td>
            </tr>
        </c:forEach>
        <c:if test="${empty usages}">
            <tr><td colspan="6">배정된 예산이 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
