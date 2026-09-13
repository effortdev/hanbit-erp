<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>거래처 관리</h2>
    <div class="page-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/sales/customers/new">+ 거래처 등록</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/sales/orders">수주 목록</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/sales/dashboard">매출 대시보드</a>
    </div>
</div>

<div class="card">
    <table>
        <thead><tr><th>거래처명</th><th>담당자</th><th>연락처</th><th>이메일</th><th>주소</th></tr></thead>
        <tbody>
        <c:forEach var="c" items="${customers}">
            <tr>
                <td>${c.name}</td>
                <td>${c.contactPerson}</td>
                <td>${c.phone}</td>
                <td>${c.email}</td>
                <td>${c.address}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty customers}">
            <tr><td colspan="5">등록된 거래처가 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
