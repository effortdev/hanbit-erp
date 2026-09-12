<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card">
    <h2>전자결재</h2>
    <c:forEach var="t" items="${documentTypes}">
        <a href="${pageContext.request.contextPath}/approval/draft/${t}">+ ${t.label} 기안</a>
    </c:forEach>
</div>

<div class="card">
    <h3>내가 결재할 문서 (${fn:length(myInbox)})</h3>
    <table>
        <thead><tr><th>유형</th><th>제목</th><th>기안자</th><th>기안일</th></tr></thead>
        <tbody>
        <c:forEach var="doc" items="${myInbox}">
            <tr>
                <td>${doc.documentType.label}</td>
                <td><a href="${pageContext.request.contextPath}/approval/${doc.id}">${doc.title}</a></td>
                <td>${doc.drafterName}</td>
                <td>${doc.createdAt}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="card">
    <h3>내가 기안한 문서 (${fn:length(myDrafts)})</h3>
    <table>
        <thead><tr><th>유형</th><th>제목</th><th>기안일</th></tr></thead>
        <tbody>
        <c:forEach var="doc" items="${myDrafts}">
            <tr>
                <td>${doc.documentType.label}</td>
                <td><a href="${pageContext.request.contextPath}/approval/${doc.id}">${doc.title}</a></td>
                <td>${doc.createdAt}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
