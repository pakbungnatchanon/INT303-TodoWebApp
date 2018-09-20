<%-- 
    Document   : Login
    Created on : Sep 20, 2018, 10:22:04 PM
    Author     : pakbu
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Page ::</title>
    </head>
    <body>
        <h1>Login Page ::</h1>
        <table class="table"> 
            <form action="Login" method="post">
                <tr>
                    <td> Username ::</td>
                    <td> <input type="text" name="username" required/> </td>
                </tr>
                <tr>
                    <td> Password :: </td>             
                    <td> <input type="password" name="password" required/> </td> 
                </tr>
                <tr>
                    <td> <input type = "submit" value = "Login"/></td>
                </tr>
                <tr>
                    <td colspan="2"> <p style="color: red"> ${message} </p> </td>
                </tr>
            </form>
        </table>
    </body>
</html>
