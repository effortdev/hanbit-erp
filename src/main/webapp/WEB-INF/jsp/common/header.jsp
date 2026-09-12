<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>한빛전자 ERP</title>
    <style>
        body { font-family: "Malgun Gothic", sans-serif; margin: 0; background: #f4f5f7; }
        header { background: #1f2d3d; color: #fff; padding: 12px 24px; display: flex; justify-content: space-between; align-items: center; }
        header a { color: #fff; text-decoration: none; margin-right: 16px; font-weight: bold; }
        header .user { font-weight: normal; font-size: 13px; color: #cbd3dc; }
        main { padding: 24px; max-width: 960px; margin: 0 auto; }
        table { border-collapse: collapse; width: 100%; background: #fff; }
        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; font-size: 14px; }
        th { background: #eef1f4; }
        form.inline { display: inline; }
        .card { background: #fff; padding: 16px 20px; border-radius: 4px; margin-bottom: 16px; }
        label { display: block; margin: 8px 0 4px; font-size: 13px; color: #555; }
        input, select { padding: 6px; width: 260px; }
        button { padding: 6px 14px; margin-top: 12px; cursor: pointer; }
    </style>
</head>
<body>
<header>
    <div>
        <a href="${pageContext.request.contextPath}/hr/org">조직도</a>
        <a href="${pageContext.request.contextPath}/hr/emp">사원관리</a>
        <a href="${pageContext.request.contextPath}/approval">전자결재</a>
        <a href="${pageContext.request.contextPath}/attendance">근태관리</a>
    </div>
    <div class="user">
        <sec:authentication property="principal.employeeName"/>님
        (<sec:authentication property="principal.position.label"/>)
        <a href="${pageContext.request.contextPath}/logout">로그아웃</a>
    </div>
</header>
<main>
