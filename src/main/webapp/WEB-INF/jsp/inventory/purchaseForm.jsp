<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>구매요청서 작성</h2>
    <p>
        물류본부 소속만 상신할 수 있습니다.<br/>
        100만원 미만: 팀장 단독 승인 / 100만~500만원: 팀장→본부장 / 500만원 이상: 팀장→본부장→대표이사
    </p>
    <form method="post" action="${pageContext.request.contextPath}/inventory/purchase">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>품목</label>
        <select name="itemId">
            <c:forEach var="item" items="${items}">
                <option value="${item.id}">${item.name} (현재고 ${item.currentStock}${item.unit})</option>
            </c:forEach>
        </select>
        <label>수량</label>
        <input type="number" name="quantity" min="1" required="required"/>
        <label>금액</label>
        <input type="number" name="amount" min="1" required="required"/>
        <label>사유</label>
        <input type="text" name="reason"/>
        <button type="submit">상신</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
