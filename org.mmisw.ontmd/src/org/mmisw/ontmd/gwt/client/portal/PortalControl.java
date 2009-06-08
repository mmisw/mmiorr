package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.OntologyTable.IQuickInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Carlos Rueda
 */
public class PortalControl {
	private PortalControl() {}
	
	private static final PortalControl instance = new PortalControl();
	
	public static PortalControl getInstance() {
		return instance;
	}
	
	private Portal portal;
	private PortalMainPanel portalMainPanel;
	
	private LoginResult loginResult;
	
	private RegisteredOntologyInfo ontologyInfo;
	
	private OntologyPanel ontologyPanel;
	private ControlsPanel controlsPanel;
	
	
	public void launchCreateMapping() {
		String url = createLinkVine();
		String features = null;
		Window.open(url, "_blank", features);
	}

	public void createNewFromFile() {
		// Old:
//		String url = createLinkUpload();
//		String features = null;
//		Window.open(url, "_blank", features);
		
		
		portalMainPanel.createNewFromFile();
	}

	
	
	
	private String createLinkVine() {
		String link = "http://mmisw.org/vine/";
		if ( loginResult != null ) {
			link += "?userId=" +loginResult.getUserId();
			link += "&sessionid=" +loginResult.getSessionId();
		}
		return link;
	}

	// Old
//	private String createLinkUpload() {
////		String link = GWT.getModuleBaseURL()+ "?_ontmd=y&_edit=y";
//		String link = "http://mmisw.org/ontmd/?_edit=y";
//		if ( loginResult != null ) {
//			link += "&userId=" +loginResult.getUserId();
//			link += "&sessionId=" +loginResult.getSessionId();
//			link += "&userName=" +loginResult.getUserName();
//		}
//		return link;
//	}

	public void createNewVocabulary() {
		// Old:
//		String url = createLinkVoc2Rdf();
//		String features = null;
//		Window.open(url, "_blank", features);

		portalMainPanel.createNewVocabulary();
	}

	// Old:
//	private String createLinkVoc2Rdf() {
//		String link = GWT.getModuleBaseURL()+ "voc2rdf/";
//		if ( GWT.isClient() ) {
//			link += "index.html";
//		}
//		if ( loginResult != null ) {
//			link += "?userId=" +loginResult.getUserId();
//			link += "&sessionid=" +loginResult.getSessionId();
//		}
//		return link;
//	}
//	


	public void editNewVersion() {
		if ( ontologyPanel != null ) {
			portalMainPanel.editNewVersion(ontologyPanel);
		}
	}

	public void reviewAndRegister() {
		portalMainPanel.reviewAndRegister(ontologyPanel);
	}
	
	public void cancelEdit() {
		portalMainPanel.cancelEdit(ontologyPanel);
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
		
	public String getDownloadOptionHtml(DownloadOption dopc, RegisteredOntologyInfo oi) {
		if ( oi == null ) {
			oi = ontologyInfo;
		}
		
		if ( oi != null ) {
			String url = oi.getUri() + "?form=" +dopc.getFormat();
			return "<a target=\"_blank\" href=\"" +url+ "\">" +dopc.getName()+ "</a>";
		}
		return null;
	}

	public List<RegisteredOntologyInfo> getVersions() {
		if ( ontologyInfo != null ) {
			return ontologyInfo.getPriorVersions();
		}
		return null;
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
	public void setOntologyInfo(RegisteredOntologyInfo ontologyInfo) {
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

	public void completedRegisterOntologyResult(RegisterOntologyResult registerOntologyResult) {
		
		portalMainPanel.completedRegisterOntologyResult(registerOntologyResult);
	}

	public void refreshedListAllOntologies(List<RegisteredOntologyInfo> ontologyInfos) {
		portalMainPanel.refreshedListAllOntologies(ontologyInfos);
	}

	/**
	 * @return the portal
	 */
	public Portal getPortal() {
		return portal;
	}

	/**
	 * @param portal the portal to set
	 */
	public void setPortal(Portal portal) {
		this.portal = portal;
	}

	public void userToSignIn() {
		portalMainPanel.userToSignIn();
	}
	
	public void userSignedOut() {
		portalMainPanel.userSignedOut();
		cancelEdit();
	}

	public void setMenuBarPanel(ControlsPanel controlsPanel) {
		this.controlsPanel = controlsPanel;
	}

	public ControlsPanel getMenuBarPanel() {
		return controlsPanel;
	}

	
	public String checkCanEditOntology(RegisteredOntologyInfo oi) {
		final String NOT_AUTHORIZED = "You are not authorized to edit this ontology";

		if ( oi == null ) {
			oi = ontologyInfo;
		}
		String error = null;
		
		if ( loginResult == null ) {
			error = NOT_AUTHORIZED;
		}
		else {
			if ( loginResult.isAdministrator() ) {
				// OK.
			}
			else if ( oi == null || ! loginResult.getUserId().equals(oi.getOntologyUserId()) ) {
				error = NOT_AUTHORIZED;
			}
		}
		return error;
	}
	
	
	private IQuickInfo quickInfo = new IQuickInfo() {
		
		public Widget getWidget(final RegisteredOntologyInfo oi) {
			
			if ( true ) {
				ControlsPanel controlsPanel = PortalControl.getInstance().getMenuBarPanel();
				
				// TODO do not include Edit option yet
				MenuBar menu = controlsPanel.createOntologyMenuBar(oi, false);
				MenuBar mb = new MenuBar(true);
//				mb.addItem("<font color=\"blue\">i</font>", true, menu);
				mb.addItem("", menu);
				return mb;
			}
			else {
				Image img = Main.images.triright().createImage();
				img.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						
						ControlsPanel controlsPanel = PortalControl.getInstance().getMenuBarPanel();
						
						MenuBar menu = controlsPanel.createOntologyMenuBar(oi, false);
						
						final PopupPanel menuPopup = new PopupPanel(true);
					    menuPopup.setWidget(menu);
					    menuPopup.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
						menuPopup.show();
					}
				});
				return img;
			}
		}
	
	};

	/**
	 * @return the quickInfo
	 */
	public IQuickInfo getQuickInfo() {
		return quickInfo;
	}

	public RegisteredOntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}

	public void notifyActivity(boolean b) {
		if ( controlsPanel != null ) {
			controlsPanel.notifyActivity(b);
		}
	}
	

}
