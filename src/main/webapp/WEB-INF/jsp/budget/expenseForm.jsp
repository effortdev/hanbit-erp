<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>지출결의서 상신</h2>
    <p style="font-size:13px;color:#555;">
        100만원 미만: 팀장 단독 승인 / 100만~500만원: 팀장→본부장 / 500만원 이상: 팀장→본부장→대표이사<br/>
        예산 소진율이 100%를 넘는 상태에서 500만원 이상 지출을 상신하려면 예외 승인 사유가 필요합니다.
    </p>
    <form method="post" action="${pageContext.request.contextPath}/budget/expense">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>제목</label>
        <input type="text" name="title" required="required"/>
        <label>계정과목</label>
        <select name="accountCategory">
            <c:forEach var="c" items="${categories}">
                <option value="${c}">${c.label}</option>
            </c:forEach>
        </select>
        <label>금액</label>
        <input type="number" name="amount" min="1" required="required"/>
        <label>사유</label>
        <input type="text" name="content"/>
        <label>예외 승인 사유 (예산 초과 + 500만원 이상일 때만 필요)</label>
        <input type="text" name="exceptionReason"/>
        <button type="submit">상신</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
