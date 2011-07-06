/**
 * @param term The ID of the term (not the full URI)
 * @param tableId Used to identify the elements to update
 */
function IonRoleVocab(tableId) {
	var ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";  

	var data = null;
	var tableElem = document.getElementById(tableId);
	
	var gotData = function(data) {
		alert(data);
		array = jQuery.csv(',', '"', '\n')(data);
		tableElem.innerHTML = array[1][0] + "<br/>" +
		array[1][1];
	};                                                       
	
 	var gotError = function(prefix, e) {
		tableElem.innerHTML = '<i>' +prefix+ " " +
			(e.description ? e.description :
			e.message ? e.message :
			e) +
			" (" +navigator.appName+ ")" +
			'</i>';
	};
 

	this.show = function() {		
			if ( data != null ) {
				return;
			}

			query = 
				"prefix ionrole: <" +ionroleprefix+ "> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"SELECT ?instance ?role ?description " +
				" WHERE { " +      
				" ?instance rdf:type ionrole:Role. " +
				" ?instance ionrole:User_Role ?role. " +
				" ?instance ionrole:Description ?description.\n" +
				"}";
			

			function loadInfo(url) {
				var xmlhttp;
				if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
					xmlhttp = new XMLHttpRequest();
				}
				else if (window.XDomainRequest) { 
					var xdr = new XDomainRequest();
					xdr.onload = function() {
						gotData(xdr.responseText);
					}
					try {
						xdr.open("get", url);
						xdr.send();
					}
					catch(e) {
						gotError("xdr error:", e);    
					}
					return;
				}
				else {// code for IE6, IE5
					xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
					//xmlhttp = new ActiveXObject("MSXML2.XMLHTTP.3.0");
				}
				xmlhttp.onreadystatechange = function() {
					if (xmlhttp.readyState==4 ) {
						if ( xmlhttp.status==200) {
							gotData(xmlhttp.responseText);
						}
						else {
							tableElem.innerHTML = "Error. Status returned: " + xmlhttp.status;   
						}
					}
				}
				try {
					xmlhttp.open("GET", url, true);
					xmlhttp.send();
				}
				catch(e) {
					gotError("xhr error:", e);
				}
			}	
			query = encodeURIComponent(query);
			loadInfo("http://mmisw.org/ont?form=csv&sparql=" +query);
		}
}


