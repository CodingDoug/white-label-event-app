<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
</head>
<body>
<h1>Populate results</h1>

<c:choose>
  <c:when test="${exception != null}">
    <pre><c:out value="${exception}"/></pre>
  </c:when>
  <c:otherwise>
    <p>Populate completed successfully.</p>
  </c:otherwise>
</c:choose>

</body>
</html>
