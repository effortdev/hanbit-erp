<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  뷰가 JSP forward로 렌더링되는 동안 request.getServletPath()는 JSP 자체의 경로를
  반환하고, 컨트롤러가 실제로 매핑된 원래 경로는 forward 속성에만 남아있다
  (Servlet 스펙의 forward 시맨틱스) — 그래서 그 속성을 우선 사용하고, 없으면
  (forward가 아닌 직접 접근 등) servletPath로 대체한다.
--%>
<c:set var="navFwdPath" value="${pageContext.request.getAttribute('javax.servlet.forward.servlet_path')}"/>
<c:set var="navCurPath" value="${empty navFwdPath ? pageContext.request.servletPath : navFwdPath}"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>한빛전자 ERP</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
<div class="app-shell">
<aside class="sidebar">
    <div class="sidebar-brand">
        <div class="brand-title">㈜한빛전자</div>
        <div class="brand-subtitle">사내 ERP 시스템</div>
    </div>
    <nav class="sidebar-nav">
        <div class="nav-group">
            <div class="nav-group-title">조직/사원관리</div>
            <a class="nav-item ${fn:startsWith(navCurPath, '/hr/org') ? 'active' : ''}" href="${pageContext.request.contextPath}/hr/org">조직도</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/hr/emp') ? 'active' : ''}" href="${pageContext.request.contextPath}/hr/emp">사원목록</a>
        </div>
        <div class="nav-group">
            <div class="nav-group-title">전자결재</div>
            <a class="nav-item ${fn:startsWith(navCurPath, '/approval') ? 'active' : ''}" href="${pageContext.request.contextPath}/approval">결재함</a>
        </div>
        <div class="nav-group">
            <div class="nav-group-title">근태관리</div>
            <a class="nav-item ${navCurPath == '/attendance' ? 'active' : ''}" href="${pageContext.request.contextPath}/attendance">출퇴근</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/attendance/vacation') ? 'active' : ''}" href="${pageContext.request.contextPath}/attendance/vacation">휴가/연차</a>
        </div>
        <div class="nav-group">
            <div class="nav-group-title">예산/지출관리</div>
            <a class="nav-item ${(navCurPath == '/budget' || fn:startsWith(navCurPath, '/budget/allocations')) ? 'active' : ''}" href="${pageContext.request.contextPath}/budget">예산현황</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/budget/expense') ? 'active' : ''}" href="${pageContext.request.contextPath}/budget/expense">지출결의서</a>
        </div>
        <div class="nav-group">
            <div class="nav-group-title">재고/구매관리</div>
            <a class="nav-item ${(navCurPath == '/inventory' || fn:startsWith(navCurPath, '/inventory/items')) ? 'active' : ''}" href="${pageContext.request.contextPath}/inventory">재고현황</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/inventory/purchase') ? 'active' : ''}" href="${pageContext.request.contextPath}/inventory/purchase">구매요청서</a>
        </div>
        <div class="nav-group">
            <div class="nav-group-title">영업/매출관리</div>
            <a class="nav-item ${fn:startsWith(navCurPath, '/sales/customers') ? 'active' : ''}" href="${pageContext.request.contextPath}/sales/customers">거래처</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/sales/orders') ? 'active' : ''}" href="${pageContext.request.contextPath}/sales/orders">수주관리</a>
            <a class="nav-item ${fn:startsWith(navCurPath, '/sales/dashboard') ? 'active' : ''}" href="${pageContext.request.contextPath}/sales/dashboard">매출대시보드</a>
        </div>
    </nav>
    <div class="sidebar-user">
        <div class="user-name"><sec:authentication property="principal.employeeName"/>님</div>
        <div class="user-position"><sec:authentication property="principal.position.label"/></div>
        <form class="user-logout-form" method="post" action="${pageContext.request.contextPath}/logout">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="user-logout">로그아웃</button>
        </form>
    </div>
</aside>
<main class="content">
