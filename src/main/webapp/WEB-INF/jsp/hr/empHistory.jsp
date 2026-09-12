<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>${employee.name} (${employee.position}) — 발령이력</h2>
</div>

<div class="card">
    <h3>부서이동</h3>
    <form method="post" action="${pageContext.request.contextPath}/hr/emp/${employee.id}/transfer">
        <label>새 소속 조직</label>
        <select name="newOrgUnitId">
            <c:forEach var="org" items="${orgUnits}">
                <c:if test="${org.type == 'TEAM'}">
                    <option value="${org.id}">${org.name}</option>
                </c:if>
            </c:forEach>
        </select>
        <label>처리자</label>
        <input type="text" name="changedBy" required="required" placeholder="예: 인사팀 홍길동"/>
        <button type="submit">발령 처리</button>
    </form>
</div>

<div class="card">
    <h3>승진</h3>
    <form method="post" action="${pageContext.request.contextPath}/hr/emp/${employee.id}/promote">
        <label>새 직급</label>
        <select name="newPosition">
            <option value="STAFF">사원</option>
            <option value="ASSISTANT_MANAGER">대리</option>
            <option value="MANAGER">과장</option>
            <option value="DEPUTY_GENERAL_MANAGER">차장</option>
            <option value="TEAM_LEADER">팀장</option>
            <option value="DIVISION_HEAD">본부장</option>
            <option value="CEO">대표이사</option>
        </select>
        <label>처리자</label>
        <input type="text" name="changedBy" required="required" placeholder="예: 인사팀 홍길동"/>
        <button type="submit">승진 처리</button>
    </form>
</div>

<div class="card">
    <h3>이력</h3>
    <table>
        <thead>
        <tr><th>일시</th><th>구분</th><th>변경 전</th><th>변경 후</th><th>처리자</th></tr>
        </thead>
        <tbody>
        <c:forEach var="h" items="${history}">
            <tr>
                <td>${h.changedAt}</td>
                <td>${h.changeType}</td>
                <td>
                    <c:if test="${h.changeType == 'TRANSFER'}">org#${h.beforeOrgUnitId}</c:if>
                    <c:if test="${h.changeType == 'PROMOTION'}">${h.beforePosition}</c:if>
                </td>
                <td>
                    <c:if test="${h.changeType == 'TRANSFER'}">org#${h.afterOrgUnitId}</c:if>
                    <c:if test="${h.changeType == 'PROMOTION'}">${h.afterPosition}</c:if>
                </td>
                <td>${h.changedBy}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
