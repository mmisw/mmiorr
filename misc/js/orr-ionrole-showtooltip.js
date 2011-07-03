## @param0 : Some string to differentiate multiple occurrences of the same term in the same confluence page

#if ($param0)
	#set ($anchor = $param0)
#else
	#set ($anchor = "")
#end
#set ($IID = $body + $anchor)

<script src="http://cdn.jquerytools.org/1.2.5/full/jquery.tools.min.js"></script>

<script src="https://mmisw.googlecode.com/svn/trunk/misc/js/orr-jquery-csv.js"></script>

<link rel="stylesheet" type="text/css" href="https://mmisw.googlecode.com/svn/trunk/misc/css/orr-tooltip.css"/>


<script>
var ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";
if (! ionroledict) {
	var ionroledict = {};
}
ionroledict['$IID'] = {};
ionroledict['$IID']['term'] = "$body".trim();
ionroledict['$IID']['termId'] = ionroledict['$IID']['term'].replace(/ /g, '_');
ionroledict['$IID']['uri'] = ionroleprefix + ionroledict['$IID']['termId'];

</script>


<a id="mytable$IID" href="">$body</a>
<div id="tooltip$IID" class="tooltip">
	<table border="0" width="500px">
		<tr>
			<td class="label">URI</td>
			<td class="value">
			   <a id="uri$IID" target="_blank" href="">$body</a>
			</td>
		</tr>
		<tr>
			<td class="label">Name</td>
			<td class="value" id="name$IID"> <i>loading...</i> </td>
		</tr>
		<tr>
			<td class="label">Description</td>
			<td class="value" id="description$IID"> <i>loading...</i></td>

		</tr>
	</table>
</div>

<script>

		function showTooltip(tableId, aid) {
			var anchorId = aid;
			resultElem = document.getElementById(tableId);
			if ( resultElem.data != null ) {
				return;
			}
			var uriElem = document.getElementById("uri" + anchorId);
			uriElem.href = ionroledict[anchorId]['uri'];
			uriElem.innerHTML = ionroledict[anchorId]['uri'];
			
			query = "prefix ionrole: <" +ionroleprefix+ "> " +
						  "SELECT ?role ?description " +
						  " WHERE { " + 
						  " ionrole:" +ionroledict[anchorId]['termId']+ " ionrole:User_Role ?role. " +
						  " ionrole:" +ionroledict[anchorId]['termId']+ " ionrole:Description ?description.\n" +
						  "}";
			

			function loadInfo(url) {
				var xmlhttp;
				if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
					xmlhttp=new XMLHttpRequest();
				}
				else {// code for IE6, IE5
					xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
				}
				xmlhttp.onreadystatechange=function() {
					var nameElem = document.getElementById("name" + anchorId);
					var descriptionElem = document.getElementById("description" + anchorId);
					if (xmlhttp.readyState==4 ) {
						if ( xmlhttp.status==200) {
							data = xmlhttp.responseText;
							resultElem.data = data;
							array = jQuery.csv(',', '"', '\n')(data);
							nameElem.innerHTML = array[1][0];
							descriptionElem.innerHTML = array[1][1];
						}
						else {
							nameElem.innerHTML = "";
							descriptionElem.innerHTML = "Error. Code returned: " + xmlhttp.status;
						}
					}
				}
				xmlhttp.open("GET", url, true);
				xmlhttp.send();
			}	
			query = query.replace(/ /g, '+');
			loadInfo("http://mmisw.org/ont?form=csv&sparql=" +query);
		}


// execute your scripts when the DOM is ready. this is a good habit
$(function() {
		
	$("#mytable$IID").tooltip({
		//tip: '#tooltip$IID',
		position: 'bottom right',
		delay: 200,
		effect: 'fade',
		relative: 'true',
		
		onShow: function() {
			showTooltip("mytable$IID", "$IID");
		}
		
	});
});
	
</script>

