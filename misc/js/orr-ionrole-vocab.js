/**
 * @param tableId Id of the DOM element where the table will be inserted
 */
function IonRoleVocab(tableId) {
	var ionroleprefix = "http://mmisw.org/ont/ooi/ionrole/";  

	var data = null;
	var tableElem = document.getElementById(tableId);
	
	var gotData = function(data) {
		array = jQuery.csv(',', '"', '\n')(data);
		var sb = '<table>\n';
		sb = sb.concat(
			'<tr>',
			'<th>' +'Role'+ '</th>',
			'<th>' +'Description' + '</th>',
			'</tr>'
		);
		// omit first row, which is the header
		for(var i = 1; i < array.length; i++) {
			sb = sb.concat('<tr>');
			var instance = array[i][0];
			if (!instance || instance.length == 0 ) {
				continue;
			}
			var role = array[i][1].replace(/\[\,\]/g, ',');
			var description = array[i][2].replace(/\[\,\]/g, ',');
			sb = sb.concat(
				'<td><a target="_blank" href="' +instance+ '">' +role + '</a></td>',
				'<td>' +description + '</td>'
			);
			sb = sb.concat('</tr>\n');
		}
		sb = sb.concat('</table>\n');
		alert(sb);
		
		tableElem.innerHTML = sb;
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
				"SELECT DISTINCT ?instance ?role ?description " +
				" WHERE { " +      
				" ?instance rdf:type ionrole:Role. " +
				" ?instance ionrole:User_Role ?role. " +
				" ?instance ionrole:Description ?description.\n" +
				"} " +
				"ORDER BY ?instance";
			

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

