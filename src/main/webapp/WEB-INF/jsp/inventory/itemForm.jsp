<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>품목 등록</h2>
    <form method="post" action="${pageContext.request.contextPath}/inventory/items">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>품목명</label>
        <input type="text" name="name" required="required"/>
        <label>단위</label>
        <input type="text" name="unit" placeholder="개/박스/세트 등" required="required"/>
        <label>초기 재고</label>
        <input type="number" name="initialStock" min="0" value="0" required="required"/>
        <label>안전재고</label>
        <input type="number" name="safetyStock" min="0" value="0" required="required"/>
        <button type="submit">등록</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
