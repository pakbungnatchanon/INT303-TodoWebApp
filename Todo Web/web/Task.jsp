<%-- 
    Document   : Task
    Created on : Sep 21, 2018, 12:54:06 AM
    Author     : pakbung
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <table> 
            <tr>
                <th> <h2>
                        USER :: ${sessionScope.username.username} 
                    </h2> </th>
            </tr>
        </table>
        <table>
            <from action = "add" medthod="post">
                <tr> 
                    <td> New Task :: </td>
                    <td> <input type="text"/></td>
                </tr>
                <tr> 
                    <td></td>
                    <td> <input type="submit" value="add"/></td>
                </tr>
            </from>
        </table>
        
            <table> 
                <tr> 
                    <th> Date:: </th>
                    <th> Task:: </th>
                    <th> Status:: </th>
                </tr>
                <c:forEach items="${Task}" var="t">  
                <tr> 
                    <td> ${t.createdate} </td>
                    <td> ${t.task} </td>
                    <td> ${t.status} </td> 
                </tr>
                </c:forEach>
            </table>
    </body>
</html>
