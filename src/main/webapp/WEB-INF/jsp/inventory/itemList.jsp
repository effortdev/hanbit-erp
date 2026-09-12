<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>재고 현황</h2>
    <a href="${pageContext.request.contextPath}/inventory/items/new">+ 품목 등록</a>
    <a href="${pageContext.request.contextPath}/inventory/purchase">구매요청서 목록</a>
</div>

<div class="card">
    <table>
        <thead><tr><th>품목</th><th>단위</th><th>현재고</th><th>안전재고</th><th></th><th></th></tr></thead>
        <tbody>
        <c:forEach var="item" items="${items}">
            <tr>
                <td>${item.name}</td>
                <td>${item.unit}</td>
                <td>${item.currentStock}</td>
                <td>${item.safetyStock}</td>
                <td>
                    <c:if test="${item.belowSafetyStock}">
                        <span style="color:#c0392b;">⚠ 안전재고 이하</span>
                    </c:if>
                </td>
                <td>
                    <c:if test="${item.belowSafetyStock}">
                        <a href="${pageContext.request.contextPath}/inventory/purchase/new">+ 구매요청서 작성</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty items}">
            <tr><td colspan="6">등록된 품목이 없습니다.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
