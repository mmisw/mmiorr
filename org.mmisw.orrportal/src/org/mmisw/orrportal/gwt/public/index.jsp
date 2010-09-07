<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@page import="org.mmisw.orrportal.gwt.server.PageUtil"%>
<%
String btop = getServletContext().getRealPath("/_btop.html");
String bbot = getServletContext().getRealPath("/_bbot.html");
%>

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>MMI Ontology Registry and Repository</title>
    <script type="text/javascript" language="javascript" src="org.mmisw.orrportal.gwt.Orr.nocache.js"></script>
@ga_snippet@
  </head>

  <body>
<%=PageUtil.include(btop)%>
  
  <div id="loading" align="center"><br/>
  	<script>
  		document.write("<img src=\"images/loading.gif\"> Loading...");
  	</script>
	<noscript>
		The MMI ORR Portal requires JavaScript enabled in your web browser.<br/> 
		Enable JavaScript and then refresh this page.
	</noscript>
  </div>
  
  <iframe src="javascript:''"  id="__gwt_historyFrame"  style="width:0;height:0;border:0"></iframe>

  <div id="main">
  </div>
   
<%=PageUtil.include(bbot)%>
  </body>
</html>
