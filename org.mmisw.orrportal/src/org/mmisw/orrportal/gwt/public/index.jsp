<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@page import="org.mmisw.orrportal.gwt.server.PageUtil"%>
<%
PageUtil pageUtil = new PageUtil(getServletContext());
%>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <%=pageUtil.includeHeadTitle()%>
    <script type="text/javascript" language="javascript" src="org.mmisw.orrportal.gwt.Orr.nocache.js"></script>
@ga_snippet@
  </head>

  <body>
<%=pageUtil.includeTop()%>
  
  <div id="loading" align="center"><br/>
  	<script>
  		document.write("<img src=\"images/loading.gif\"> Loading...");
  	</script>
	<noscript>
		This application requires JavaScript enabled in your web browser.<br/>
	</noscript>
  </div>
  
  <iframe src="javascript:''"  id="__gwt_historyFrame"  style="width:0;height:0;border:0"></iframe>

  <div id="main">
  </div>
   
<%=pageUtil.includeBottom()%>
  </body>
</html>
