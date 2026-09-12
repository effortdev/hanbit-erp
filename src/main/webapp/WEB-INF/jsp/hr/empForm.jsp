<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>사원 등록</h2>
    <form method="post" action="${pageContext.request.contextPath}/hr/emp">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>이름</label>
        <input type="text" name="name" required="required"/>

        <label>직급</label>
        <select name="position">
            <c:forEach var="p" items="${positions}">
                <option value="${p}">${p.label}</option>
            </c:forEach>
        </select>

        <label>소속 조직</label>
        <select name="orgUnitId">
            <c:forEach var="org" items="${orgUnits}">
                <option value="${org.id}">
                    <c:if test="${org.type == 'TEAM'}">└ </c:if>${org.name}
                </option>
            </c:forEach>
        </select>

        <label>입사일</label>
        <input type="date" name="hireDate" required="required"/>

        <button type="submit">등록</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
