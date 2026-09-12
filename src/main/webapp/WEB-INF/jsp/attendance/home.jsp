<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>근태관리</h2>
    <a href="${pageContext.request.contextPath}/attendance/vacation">휴가신청/연차현황</a>
</div>

<div class="card">
    <h3>오늘 (${today.workDate})</h3>
    <c:choose>
        <c:when test="${empty today}">
            <form method="post" action="${pageContext.request.contextPath}/attendance/check-in">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit">출근</button>
            </form>
        </c:when>
        <c:otherwise>
            <p>출근: ${today.checkInTime}</p>
            <c:choose>
                <c:when test="${empty today.checkOutTime}">
                    <form method="post" action="${pageContext.request.contextPath}/attendance/check-out">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit">퇴근</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p>퇴근: ${today.checkOutTime}</p>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
</div>

<div class="card">
    <h3>최근 기록</h3>
    <table>
        <thead><tr><th>날짜</th><th>출근</th><th>퇴근</th></tr></thead>
        <tbody>
        <c:forEach var="r" items="${records}">
            <tr>
                <td>${r.workDate}</td>
                <td>${r.checkInTime}</td>
                <td>${r.checkOutTime}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
