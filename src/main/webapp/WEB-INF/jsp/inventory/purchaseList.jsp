<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>구매요청서</h2>
    <a href="${pageContext.request.contextPath}/inventory">재고 현황</a>
    <a href="${pageContext.request.contextPath}/inventory/purchase/new">+ 구매요청서 작성</a>
</div>

<div class="card">
    <table>
        <thead><tr><th>품목</th><th>수량</th><th>금액</th><th>진행상태</th><th>결재문서</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="req" items="${requests}">
            <tr>
                <td>${req.itemName}</td>
                <td>${req.quantity}${req.itemUnit}</td>
                <td>${req.amount}</td>
                <td>
                    <c:choose>
                        <c:when test="${req.status == 'PENDING'}"><span class="badge badge-neutral">상신 (결재 진행중)</span></c:when>
                        <c:when test="${req.status == 'CONFIRMED'}"><span class="badge badge-primary">구매확정 (입고대기)</span></c:when>
                        <c:when test="${req.status == 'RECEIVED'}"><span class="badge badge-success">입고완료</span></c:when>
                        <c:when test="${req.status == 'REJECTED'}"><span class="badge badge-danger">반려</span></c:when>
                    </c:choose>
                </td>
                <td><a href="${pageContext.request.contextPath}/approval/${req.approvalDocumentId}">문서 보기</a></td>
                <td>
                    <c:if test="${req.status == 'CONFIRMED'}">
                        <form method="post" action="${pageContext.request.contextPath}/inventory/purchase/${req.id}/receive">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit">입고 처리</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty requests}">
            <tr><td colspan="6">구매요청서가 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
