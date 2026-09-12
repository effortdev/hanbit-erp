<%@ include file="header.jsp" %>
<div class="card">
    <h2>오류가 발생했습니다</h2>
    <div class="alert alert-danger">${errorMessage}</div>
    <a href="${pageContext.request.contextPath}/">홈으로</a>
</div>
<%@ include file="footer.jsp" %>
