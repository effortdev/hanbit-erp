<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>조직도 — ㈜한빛전자</h2>
    <a href="${pageContext.request.contextPath}/hr/org/new">+ 조직 등록</a>
</div>

<c:forEach var="hq" items="${orgUnits}">
    <c:if test="${hq.type == 'HQ'}">
        <div class="card">
            <strong>${hq.name}</strong>
            <ul>
                <c:forEach var="team" items="${orgUnits}">
                    <c:if test="${team.type == 'TEAM' && team.parentId == hq.id}">
                        <li>${team.name}</li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
    </c:if>
</c:forEach>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
