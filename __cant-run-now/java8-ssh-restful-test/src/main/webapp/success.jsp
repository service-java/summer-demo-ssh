<%--
  Created by IntelliJ IDEA.
  User: Luo_0412
  Date: 2017/4/10
  Time: 19:01
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>HTML5 Page Title</title>

    <link href="https://cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<div class="container" style="margin-top: 20px;">

    <p> 用户: ${user.userName } </p>
    <p> 密码: ${user.password } </p>
    <p> 邮箱: ${user.mail }     </p>
    <p> 手机: ${user.tel }      </p>
    <p> 地址: ${user.address }  </p>
    <p> 兴趣: ${user.favor }    </p>

</div><!-- ./container -->

<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<script src="https://cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>

</body>
</html>
