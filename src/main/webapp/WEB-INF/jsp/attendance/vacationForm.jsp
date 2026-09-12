<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>휴가 신청</h2>
    <form method="post" action="${pageContext.request.contextPath}/attendance/vacation">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>시작일</label>
        <input type="date" name="startDate" required="required"/>
        <label>종료일</label>
        <input type="date" name="endDate" required="required"/>
        <label>사유</label>
        <input type="text" name="reason"/>
        <button type="submit">상신</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
