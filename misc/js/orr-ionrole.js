function IonRoleTooltip(term, tableId, anchorId) {
	var ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";
	var termId = term.replace(/ /g, '_');
	var uri = ionroleprefix + termId;

	var resultElem = document.getElementById(tableId);
 	var uriElem = document.getElementById("uri" + anchorId);
 	var nameElem = document.getElementById("name" + anchorId);
	var descriptionElem = document.getElementById("description" + anchorId);
 
	this.show = function() {		
			if ( resultElem.data != null ) {
				return;
			}

			uriElem.href = uri;
			uriElem.innerHTML = uri;
			
			query = "prefix ionrole: <" +ionroleprefix+ "> " +
						  "SELECT ?role ?description " +
						  " WHERE { " + 
						  " ionrole:" +termId+ " ionrole:User_Role ?role. " +
						  " ionrole:" +termId+ " ionrole:Description ?description.\n" +
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
}

