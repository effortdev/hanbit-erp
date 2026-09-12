<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>수주 등록</h2>
    <p>가용 재고보다 많은 수량은 등록할 수 없습니다.</p>
    <form method="post" action="${pageContext.request.contextPath}/sales/orders">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>거래처</label>
        <select name="customerId">
            <c:forEach var="cust" items="${customers}">
                <option value="${cust.id}">${cust.name}</option>
            </c:forEach>
        </select>
        <label>품목</label>
        <select name="itemId">
            <c:forEach var="item" items="${items}">
                <option value="${item.id}">${item.name} (가용재고: ${item.currentStock}${item.unit})</option>
            </c:forEach>
        </select>
        <label>수량</label>
        <input type="number" name="quantity" min="1" required="required"/>
        <label>단가</label>
        <input type="number" name="unitPrice" min="1" required="required"/>
        <button type="submit">등록</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
