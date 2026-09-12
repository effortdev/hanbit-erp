<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>지출결의서</h2>
    <a href="${pageContext.request.contextPath}/budget">예산 현황</a>
    <a href="${pageContext.request.contextPath}/budget/expense/new">+ 지출결의서 상신</a>
</div>

<div class="card">
    <table>
        <thead><tr><th>계정과목</th><th>금액</th><th>사유</th><th>상태</th><th>결재문서</th></tr></thead>
        <tbody>
        <c:forEach var="req" items="${requests}">
            <tr>
                <td>${req.accountCategory.label}</td>
                <td>${req.amount}</td>
                <td>${req.reason}</td>
                <td>${req.status}</td>
                <td><a href="${pageContext.request.contextPath}/approval/${req.approvalDocumentId}">문서 보기</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
