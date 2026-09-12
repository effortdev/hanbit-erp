<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>휴가신청 / 연차현황</h2>
    <p>올해 부여일수: <strong>${annualDays}</strong>일 / 잔여일수: <strong>${remainingDays}</strong>일</p>
    <a href="${pageContext.request.contextPath}/attendance/vacation/new">+ 휴가 신청</a>
</div>

<div class="card">
    <h3>내 휴가신청 목록</h3>
    <table>
        <thead><tr><th>기간</th><th>일수</th><th>사유</th><th>상태</th><th>결재문서</th></tr></thead>
        <tbody>
        <c:forEach var="req" items="${requests}">
            <tr>
                <td>${req.startDate} ~ ${req.endDate}</td>
                <td>${req.days}</td>
                <td>${req.reason}</td>
                <td>${req.status}</td>
                <td><a href="${pageContext.request.contextPath}/approval/${req.approvalDocumentId}">문서 보기</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
