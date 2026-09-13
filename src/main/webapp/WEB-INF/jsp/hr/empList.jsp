<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>사원 목록</h2>
    <a href="${pageContext.request.contextPath}/hr/emp/new">+ 사원 등록</a>
</div>

<div class="card">
    <table>
        <thead>
        <tr><th>ID</th><th>이름</th><th>직급</th><th>소속</th><th>입사일</th><th>상태</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="emp" items="${employees}">
            <tr>
                <td>${emp.id}</td>
                <td>${emp.name}</td>
                <td>${emp.position}</td>
                <td>
                    <c:forEach var="org" items="${orgUnits}">
                        <c:if test="${org.id == emp.orgUnitId}">${org.name}</c:if>
                    </c:forEach>
                </td>
                <td>${emp.hireDate}</td>
                <td>
                    <c:choose>
                        <c:when test="${emp.status == 'ACTIVE'}"><span class="badge badge-success">재직</span></c:when>
                        <c:when test="${emp.status == 'LEFT'}"><span class="badge badge-neutral">퇴사</span></c:when>
                        <c:otherwise><span class="badge badge-neutral">${emp.status}</span></c:otherwise>
                    </c:choose>
                </td>
                <td><a href="${pageContext.request.contextPath}/hr/emp/${emp.id}/history">발령이력</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
