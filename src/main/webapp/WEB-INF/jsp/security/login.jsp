<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인 — 한빛전자 ERP</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --color-bg: #f4f5f7;
            --color-text: #1a1d23;
            --color-text-secondary: #6b7280;
            --color-border: #e2e5ea;
            --color-border-strong: #d7dbe0;
            --color-primary: #2f6fed;
            --color-primary-dark: #1d4ed8;
            --color-primary-tint: #e8f0fe;
            --color-danger: #b91c1c;
            --color-danger-tint: #fdeaea;
            --color-success: #15803d;
            --color-success-tint: #e6f6ec;
        }
        * { box-sizing: border-box; }
        body {
            font-family: 'Noto Sans KR', -apple-system, sans-serif;
            background: var(--color-bg);
            display: flex; justify-content: center; align-items: center;
            height: 100vh; margin: 0;
        }
        .card {
            background: #fff; padding: 32px; border-radius: 10px; width: 320px;
            border: 1px solid var(--color-border);
            box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
        }
        .brand-title { color: var(--color-text); font-weight: 700; font-size: 15px; margin-bottom: 2px; }
        .brand-subtitle { color: var(--color-text-secondary); font-size: 11.5px; margin-bottom: 20px; }
        label { display: block; margin: 14px 0 5px; font-size: 11.5px; font-weight: 500; color: var(--color-text-secondary); }
        input {
            padding: 8px 10px; width: 100%;
            border: 1px solid var(--color-border-strong); border-radius: 6px;
            font-family: inherit; font-size: 12.5px;
        }
        input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-tint); }
        button {
            margin-top: 18px; padding: 9px 16px; width: 100%; cursor: pointer;
            background: var(--color-primary); color: #fff; border: none; border-radius: 6px;
            font-family: inherit; font-size: 12.5px; font-weight: 500;
        }
        button:hover { background: var(--color-primary-dark); }
        .msg { font-size: 12px; padding: 10px 12px; border-radius: 6px; margin-bottom: 8px; }
        .error { background: var(--color-danger-tint); color: var(--color-danger); }
        .info { background: var(--color-success-tint); color: var(--color-success); }
    </style>
</head>
<body>
<div class="card">
    <div class="brand-title">㈜한빛전자</div>
    <div class="brand-subtitle">사내 ERP 시스템</div>
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
