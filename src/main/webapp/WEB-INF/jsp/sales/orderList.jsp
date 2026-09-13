<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>수주 목록</h2>
    <div class="page-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/sales/orders/new">+ 수주 등록</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/sales/customers">거래처 관리</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/sales/dashboard">매출 대시보드</a>
    </div>
</div>

<div class="card">
    <table>
        <thead><tr><th>거래처</th><th>품목</th><th>수량</th><th>단가</th><th>금액</th><th>상태</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="o" items="${orders}">
            <tr>
                <td>${o.customerName}</td>
                <td>${o.itemName}</td>
                <td>${o.quantity}${o.itemUnit}</td>
                <td>${o.unitPrice}</td>
                <td>${o.amount}</td>
                <td>
                    <c:choose>
                        <c:when test="${o.status == 'REGISTERED'}"><span class="badge badge-neutral">등록됨</span></c:when>
                        <c:when test="${o.status == 'CONFIRMED'}"><span class="badge badge-success">확정(매출 반영됨)</span></c:when>
                    </c:choose>
                </td>
                <td>
                    <c:if test="${o.status == 'REGISTERED'}">
                        <form method="post" action="${pageContext.request.contextPath}/sales/orders/${o.id}/confirm">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit">수주 확정</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty orders}">
            <tr><td colspan="7">수주 내역이 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
