<%@ page import="java.math.*" %>
<%@ page import="java.servlet.*" %>
<%@ page import="java.servlet.http.*" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html lang="en">
<head>
<title>The BodgeIt Store</title>
<link href="style.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="./js/util.js"></script>
</head>
<body>
<%
	String username = (String) session.getAttribute("username");
	String usertype = (String) session.getAttribute("usertype");
%>
<div style="text-align: center;">
<table style="width: 80%" class="border">
<tr style="background-color: #C3D9FF">
<td style="text-align: center" colspan="6">
<H1>The BodgeIt Store</H1>
<table style="width: 100%" class="noborder">
<tr style="background-color: #C3D9FF">
<td style="text-align: center; width: 30%">&nbsp;</td>
<td style="text-align: center; width: 40%">We bodge it, so you dont have to!</td>
<td style="text-align: center; width: 30%" style="text-align: right" >
<%
	if (username != null) {
		out.println("User: <a href=\"password.jsp\">" + username + "</a>");
	} else {
		out.println("Guest user");
	}
%>
</tr>
</table>
</td>
</tr>
<tr>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="home.jsp">Home</a></td>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="about.jsp">About Us</a></td>
<%
	if (usertype != null && usertype.equals("ADMIN")) {
%>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="contact.jsp">Comments</a></td>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="admin.jsp">Admin</a></td>
<%
	} else {
%>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="contact.jsp">Contact Us</a></td>
<%
	}
%>
<td style="background-color: #EEEEEE; width: 16%; text-align: center">
<%
	if (usertype == null) {
%>
		<a href="login.jsp">Login</a>
<%
	} else {
%>
		<a href="logout.jsp">Logout</a>
<%
	}
%>
</td>
<%
	if (usertype == null || ! usertype.equals("ADMIN")) {
%>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="basket.jsp">Your Basket</a></td>
<%
	}
%>
<td style="background-color: #EEEEEE; width: 16%; text-align: center"><a href="search.jsp">Search</a></td>
</tr>
<tr>
<td colspan="6" style="text-align: center">
<table style="width: 100%" class="border">
<tr>
<td style="width: 25%; text-align: left; vertical-align: top">
<%
	Connection c = null;
	PreparedStatement stmt = null;
	ResultSet rs = null;
	try {
		// Get hold of the JDBC driver
		Class.forName("org.hsqldb.jdbcDriver" );
		// Establish a connection to an in memory db
		c = DriverManager.getConnection("jdbc:hsqldb:mem:SQL", "sa", "");
		stmt = c.prepareStatement("SELECT * FROM ProductTypes ORDER BY type");
		rs = stmt.executeQuery();
		while (rs.next()) {
			String type = rs.getString("type");
			out.println("<a href=\"product.jsp?typeid=" + rs.getInt("typeid") + "\">" + type + "</a><br/>");
		}
	} catch (SQLException e) {
		if ("true".equals(request.getParameter("debug"))) {
			c.createStatement().execute("UPDATE Score SET status = 1 WHERE task = 'HIDDEN_DEBUG'");
			out.println("DEBUG System error: " + e + "<br/><br/>");
		} else {
			out.println("System error.");
		}
	} finally {
		stmt.close();
		rs.close();
		c.close();
	}
%>
<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
</td>
<td style="vertical-align: top; width: 70%">
