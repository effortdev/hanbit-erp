<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>조직 등록</h2>
    <form method="post" action="${pageContext.request.contextPath}/hr/org">
        <label>유형</label>
        <select name="type">
            <c:forEach var="t" items="${orgTypes}">
                <option value="${t}">${t}</option>
            </c:forEach>
        </select>

        <label>조직명</label>
        <input type="text" name="name" required="required"/>

        <label>상위 본부 (팀인 경우만 선택)</label>
        <select name="parentId">
            <option value="">-- 없음(본부) --</option>
            <c:forEach var="hq" items="${orgUnits}">
                <c:if test="${hq.type == 'HQ'}">
                    <option value="${hq.id}">${hq.name}</option>
                </c:if>
            </c:forEach>
        </select>

        <button type="submit">등록</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
