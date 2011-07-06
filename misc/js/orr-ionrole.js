/**
 * @param term The ID of the term (not the full URI)
 * @param anchorId Used to identify the elements to update
 */
function IonRoleTooltip(term, anchorId) {
	var ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";
	var termId = term.replace(/ /g, '_');
	this.uri = ionroleprefix + termId;

	var data = null;
 	var uriElem = document.getElementById("uri" + anchorId);
 	var nameElem = document.getElementById("name" + anchorId);
	var descriptionElem = document.getElementById("description" + anchorId);
	
	var gotData = function(data) {
		array = jQuery.csv(',', '"', '\n')(data);
		nameElem.innerHTML = array[1][0];
		descriptionElem.innerHTML = array[1][1];
	};
	
 	var gotError = function(msg) {
		nameElem.innerHTML = "";
		descriptionElem.innerHTML = '<i>' +msg+ '</i>';
	};
 

	this.show = function() {		
			if ( data != null ) {
				return;
			}

			uriElem.href = this.uri;
			uriElem.innerHTML = this.uri;
			
			query = "prefix ionrole: <" +ionroleprefix+ "> " +
						  "SELECT ?role ?description " +
						  " WHERE { " + 
						  " ionrole:" +termId+ " ionrole:User_Role ?role. " +
						  " ionrole:" +termId+ " ionrole:Description ?description.\n" +
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
						gotError("IE: " +e);    
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
							nameElem.innerHTML = "";                                                
							descriptionElem.innerHTML = "Error. Code returned: " + xmlhttp.status;   
						}
					}
				}
				try {
					xmlhttp.open("GET", url, true);
					xmlhttp.send();
				}
				catch(e) {
					gotError(e+ " (" +navigator.appName+ ")");
				}
			}	
			query = query.replace(/ /g, '+');
			loadInfo("http://mmisw.org/ont?form=csv&sparql=" +query);
		}
}


