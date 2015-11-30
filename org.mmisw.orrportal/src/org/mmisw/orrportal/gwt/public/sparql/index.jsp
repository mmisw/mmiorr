<%@page import="org.mmisw.orrportal.gwt.server.PageUtil"%>
<%
PageUtil pageUtil = new PageUtil(getServletContext());
%>
<html>
<head>
<%=pageUtil.includeHeadTitle()%>
<link href='//cdn.jsdelivr.net/yasgui/2.1.0/yasgui.min.css' rel='stylesheet' type='text/css'/>
<script src='//cdn.jsdelivr.net/yasgui/2.1.0/yasgui.min.js'></script>
@ga_snippet@
</head>

<body>
<%=pageUtil.includeTop()%>

<a href="..">
<img src="../images/logo.png" />
</a>

<div style="margin: 5px; font-family: Arial, sans-serif">
    Edit, submit, and view the results of <a target="_blank" href="http://en.wikipedia.org/wiki/SPARQL">SPARQL</a>
    queries against the repository.
    <span style="color:gray;font-size:x-small">
        (Powered by
        <a target="_blank" href="http://doc.yasgui.org/" style="color:gray">YASGUI</a>.)
    </span>
</div>

<div id="editor"></div>

<script>
    YASGUI.YASQE.defaults.sparql.endpoint = "<%=pageUtil.getSparqlEndpoint()%>";
    YASGUI.YASQE.defaults.sparql.acceptHeaderGraph = "application/rdf+xml";
    YASGUI.defaults.catalogueEndpoints = [];
    YASGUI(document.getElementById("editor"));
</script>

<%=pageUtil.includeBottom()%>
</body>
</html>
