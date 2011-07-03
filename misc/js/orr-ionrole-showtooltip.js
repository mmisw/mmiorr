
		function ionrole_showTooltip(tableId, aid) {
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

