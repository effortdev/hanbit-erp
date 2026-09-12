<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인 — 한빛전자 ERP</title>
    <style>
        body { font-family: "Malgun Gothic", sans-serif; background: #f4f5f7; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .card { background: #fff; padding: 32px; border-radius: 6px; width: 320px; box-shadow: 0 1px 4px rgba(0,0,0,.1); }
        h1 { font-size: 18px; margin-top: 0; }
        label { display: block; margin: 12px 0 4px; font-size: 13px; color: #555; }
        input { padding: 8px; width: 100%; box-sizing: border-box; }
        button { margin-top: 16px; padding: 8px 16px; width: 100%; cursor: pointer; }
        .msg { font-size: 13px; padding: 8px; border-radius: 4px; margin-bottom: 8px; }
        .error { background: #fdecea; color: #c0392b; }
        .info { background: #eaf6ea; color: #27632a; }
    </style>
</head>
<body>
<div class="card">
    <h1>한빛전자 ERP 로그인</h1>
    <c:if test="${param.error != null}">
        <div class="msg error">아이디 또는 비밀번호가 올바르지 않습니다.</div>
    </c:if>
    <c:if test="${param.logout != null}">
        <div class="msg info">로그아웃되었습니다.</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>아이디</label>
        <input type="text" name="username" required="required" autofocus="autofocus"/>
        <label>비밀번호</label>
        <input type="password" name="password" required="required"/>
        <button type="submit">로그인</button>
    </form>
</div>
</body>
</html>
