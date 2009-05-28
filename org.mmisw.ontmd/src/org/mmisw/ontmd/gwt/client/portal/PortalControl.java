package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * 
 * @author Carlos Rueda
 */
public class PortalControl {
	private PortalControl() {}
	
	
	static LoginResult loginResult;
	
	static OntologyInfo ontologyInfo;
	
	
	public static void launchCreateVocabulary() {
		String url = createLinkVoc2Rdf();
		String features = null;
		Window.open(url, "_blank", features);
	}

	public static void launchCreateMapping() {
		String url = createLinkVine();
		String features = null;
		Window.open(url, "_blank", features);
	}

	public static void launchCreateUpload() {
		String url = createLinkUpload();
		String features = null;
		Window.open(url, "_blank", features);
	}

	
	
	
	private static String createLinkVoc2Rdf() {
		String link = GWT.getModuleBaseURL()+ "voc2rdf/";
		if ( GWT.isClient() ) {
			link += "index.html";
		}
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}
	
	private static String createLinkVine() {
		String link = "http://mmisw.org/vine/";
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	private static String createLinkUpload() {
		String link = GWT.getModuleBaseURL()+ "?_edit=y";
		if ( loginResult != null ) {
			link += "&userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	public static void editNewVersion() {
		Window.alert("sorry, not implemented yet");		
	}

	public static void editShowVersions() {
		Window.alert("sorry, not implemented yet");		
	}

	public static enum DownloadOption {
		RDFXML("RDF/XML", "rdf"),
		N3("N3", "n3"),
		;
		
		private String name;
		private String format;
		
		private DownloadOption(String name, String format) {
			this.name = name;
			this.format = format;
		}
		
		public String toString() {
			return name;
		}
		
		public String getFormat() {
			return format;
		}
		
		public String getHtml() {
			String url = ontologyInfo.getUri() + "?form=" +getFormat();
			return "<a target=\"_blank\" href=\"" +url+ "\">" +name+ "</a>";
		}
	}
	

}
