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
	
	private static final PortalControl instance = new PortalControl();
	
	static PortalControl getInstance() {
		return instance;
	}
	
	private PortalMainPanel portalMainPanel;
	
	private LoginResult loginResult;
	
	private OntologyInfo ontologyInfo;
	
	private OntologyPanel ontologyPanel;
	
	
	public void launchCreateVocabulary() {
		String url = createLinkVoc2Rdf();
		String features = null;
		Window.open(url, "_blank", features);
	}

	public void launchCreateMapping() {
		String url = createLinkVine();
		String features = null;
		Window.open(url, "_blank", features);
	}

	public void launchCreateUpload() {
		String url = createLinkUpload();
		String features = null;
		Window.open(url, "_blank", features);
	}

	
	
	
	private String createLinkVoc2Rdf() {
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
	
	private String createLinkVine() {
		String link = "http://mmisw.org/vine/";
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	private String createLinkUpload() {
		String link = GWT.getModuleBaseURL()+ "?_edit=y";
		if ( loginResult != null ) {
			link += "&userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	public void editNewVersion() {
		if ( ontologyPanel != null ) {
			portalMainPanel.editNewVersion(ontologyPanel);
		}
	}

	public void cancelEdit() {
		portalMainPanel.cancelEdit(ontologyPanel);
	}
	
	public void showVersions() {
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
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
		
		public String getFormat() {
			return format;
		}
	}
		
	public String getDownloadOptionHtml(DownloadOption dopc) {
		String url = ontologyInfo.getUri() + "?form=" +dopc.getFormat();
		return "<a target=\"_blank\" href=\"" +url+ "\">" +dopc.getName()+ "</a>";
	}

	/**
	 * @param portalMainPanel the portalMainPanel to set
	 */
	public void setPortalMainPanel(PortalMainPanel portalMainPanel) {
		this.portalMainPanel = portalMainPanel;
	}

	/**
	 * @param loginResult the loginResult to set
	 */
	public void setLoginResult(LoginResult loginResult) {
		this.loginResult = loginResult;
	}

	/**
	 * @param ontologyInfo the ontologyInfo to set
	 */
	public void setOntologyInfo(OntologyInfo ontologyInfo) {
		this.ontologyInfo = ontologyInfo;
	}

	public LoginResult getLoginResult() {
		return loginResult;
	}

	public void setOntologyPanel(OntologyPanel ontologyPanel) {
		this.ontologyPanel = ontologyPanel;		
	}
	
	public OntologyPanel getOntologyPanel() {
		return ontologyPanel;
	}


}
