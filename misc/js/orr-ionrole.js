function ionrole(term, tableId, anchorId) {
	this.ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";
	this.term = term;
	this.termId = term.replace(/ /g, '_');
	this.uri = this.ionroleprefix + this.termId;


	this.tableId = tableId;
	this.anchorId = anchorId;

	this.showTooltip = function() {
			resultElem = document.getElementById(this.tableId);
			if ( resultElem.data != null ) {
				return;
			}
			var uriElem = document.getElementById("uri" + this.anchorId);
			uriElem.href = this.uri;
			uriElem.innerHTML = this.uri;
			
			query = "prefix ionrole: <" +this.ionroleprefix+ "> " +
						  "SELECT ?role ?description " +
						  " WHERE { " + 
						  " ionrole:" +this.termId+ " ionrole:User_Role ?role. " +
						  " ionrole:" +this.termId+ " ionrole:Description ?description.\n" +
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
					var nameElem = document.getElementById("name" + this.anchorId);
					var descriptionElem = document.getElementById("description" + this.anchorId);
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
