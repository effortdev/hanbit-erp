<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>[${detail.document.documentType.label}] ${detail.document.title}</h2>
    <p>기안자: ${detail.document.drafterName} / 기안일: ${detail.document.createdAt}</p>
    <p>상태: <strong>${detail.status.label}</strong></p>
    <c:if test="${not empty detail.document.content}"><p>내용: ${detail.document.content}</p></c:if>
    <c:if test="${not empty detail.document.amount}"><p>금액: ${detail.document.amount}원</p></c:if>
    <c:if test="${not empty detail.document.startDate}"><p>기간: ${detail.document.startDate} ~ ${detail.document.endDate}</p></c:if>
</div>

<div class="card">
    <h3>결재라인</h3>
    <table>
        <thead><tr><th>순서</th><th>레벨</th><th>결재자</th><th>결과</th><th>의견</th></tr></thead>
        <tbody>
        <c:forEach var="step" items="${detail.steps}">
            <tr>
                <td>${step.stepOrder}</td>
                <td>${step.approverLevel}</td>
                <td>${step.approverName}</td>
                <td>${empty step.actionResult ? '대기' : step.actionResult}</td>
                <td>${step.actionComment}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty detail.steps}">
            <tr><td colspan="5">결재라인 없음 (기안자 본인이 최종 결재권자라 자동 승인됨)</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<c:if test="${not empty detail.actionableStepId}">
    <div class="card">
        <h3>결재 처리</h3>
        <form method="post" action="${pageContext.request.contextPath}/approval/${detail.document.id}/steps/${detail.actionableStepId}/approve">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <label>의견 (선택)</label>
            <input type="text" name="comment"/>
            <button type="submit">승인</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/approval/${detail.document.id}/steps/${detail.actionableStepId}/reject" style="margin-top:12px;">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <label>반려 사유 (필수)</label>
            <input type="text" name="comment" required="required"/>
            <button type="submit">반려</button>
        </form>
    </div>
</c:if>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
