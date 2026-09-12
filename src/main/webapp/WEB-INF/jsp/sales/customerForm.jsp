<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>거래처 등록</h2>
    <form method="post" action="${pageContext.request.contextPath}/sales/customers">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>거래처명</label>
        <input type="text" name="name" required="required"/>
        <label>담당자</label>
        <input type="text" name="contactPerson"/>
        <label>연락처</label>
        <input type="text" name="phone"/>
        <label>이메일</label>
        <input type="text" name="email"/>
        <label>주소</label>
        <input type="text" name="address"/>
        <button type="submit">등록</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
