package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.CookieMan;
import org.mmisw.ontmd.gwt.client.Orr;
import org.mmisw.ontmd.gwt.client.util.table.IQuickInfo;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	
	private PortalMainPanel portalMainPanel;
	
	private LoginResult loginResult;
	private ControlsPanel controlsPanel;
	
	
	private BaseOntologyInfo ontologyInfo;
	private OntologyPanel ontologyPanel;
	
	private EntityInfo entityInfo;
	private EntityPanel entityPanel;
	
	
	/**
	 * @return <code>{@link #getOntologyPanel()}.getMetadataPanel()</code>, if {@link #getOntologyPanel()} is not null.
	 */
	TempOntologyInfoListener getTempOntologyInfoListener() {
		return ontologyPanel != null ? ontologyPanel.getMetadataPanel() : null;
	}
	
	
	public void createNewMappingOntology() {
		portalMainPanel.createNewMappingOntology();
	}


	public void startRegisterExternal() {		
		portalMainPanel.startRegisterExternal();
	}

	
	public void refreshListAllOntologies() {
		portalMainPanel.refreshListAllOntologies();
	}

	public void searchTerms() {
		portalMainPanel.searchTerms();
	}

	public void createNewVocabulary() {
		portalMainPanel.createNewVocabulary();
	}

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
	
	public void createAccount() {
		portalMainPanel.loginCreateAccount();
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
		
	static class ExternalViewersInfo {
		HorizontalPanel hp = new HorizontalPanel();
		HTML hrefHtml;
		String tooltip;
		
		ExternalViewersInfo(String ontbrowserUrl, RegisteredOntologyInfo oi, boolean includeVersion) {
			hp.setSpacing(3);
			
			// URI of the ontology to be retrieved from the "ont" service:
			String uri = Orr.getPortalBaseInfo().getOntServiceUrl() + "?form=owl&uri=" +oi.getUri();
			if ( includeVersion ) {
				uri += "&version=" +oi.getVersionNumber();
			}

			uri = uri.replaceAll("\\?", "%3F").replaceAll("#", "%23").replaceAll("&", "%26");
			
			// the link for the ontology-browser tool:
			String link = ontbrowserUrl+ "/manage/?action=load&clear=true&uri=" +uri;
//			Main.log("ontology-browser link: " +link);
			
			String target = "_blank";
			String href = "<a target=\"" +target+ "\" href=\"" +link+ "\">Ontology Browser</a>";
			
			hrefHtml = new HTML(href);
			tooltip = "Opens the ontology using the external Ontology Browser tool. " +
						"(See http://code.google.com/p/ontology-browser/.)";
			hrefHtml.setTitle(tooltip);
			hp.add(hrefHtml);
		}
	}
	
	public ExternalViewersInfo getExternalViewersInfo(BaseOntologyInfo oi, boolean includeVersion) {
		String ontbrowserUrl = Orr.getPortalBaseInfo().getOntbrowserServiceUrl();
		if ( ontbrowserUrl == null || ontbrowserUrl.trim().length() == 0 ) {
			return null;
		}
		
		if ( oi == null ) {
			oi = ontologyInfo;
		}
		
		if ( oi instanceof RegisteredOntologyInfo ) {
			return new ExternalViewersInfo(ontbrowserUrl, (RegisteredOntologyInfo) oi, includeVersion);
		}
		return null;
	}

	public String getDownloadOptionHtml(DownloadOption dopc, BaseOntologyInfo oi,
			boolean includeVersion) {
		
		if ( oi == null ) {
			oi = ontologyInfo;
		}
		
		if ( oi instanceof RegisteredOntologyInfo ) {
			RegisteredOntologyInfo roi = (RegisteredOntologyInfo) oi;
			final String ontService = Orr.getPortalBaseInfo().getOntServiceUrl();
			String ontUri = URL.encode(roi.getUri()).replaceAll("#", "%23");
			String url = ontService+ "?form=" +dopc.getFormat()+ "&uri=" +ontUri;
			if ( includeVersion ) {
				url += "&version=" +roi.getVersionNumber();
			}
			return "<a target=\"_blank\" href=\"" +url+ "\">" +dopc.getName()+ "</a>";
		}
		
		return null;
	}

	public List<RegisteredOntologyInfo> getVersions() {
		if ( ontologyInfo instanceof RegisteredOntologyInfo ) {
			return ((RegisteredOntologyInfo) ontologyInfo).getPriorVersions();
		}
		return null;
	}

	/**
	 * @param portalMainPanel the portalMainPanel to set
	 */
	public void setPortalMainPanel(PortalMainPanel portalMainPanel) {
		this.portalMainPanel = portalMainPanel;
	}

	
	public void loginOk(LoginResult loginResult) {
		portalMainPanel.loginOk(loginResult);
	}

	public void userAccountCreatedOrUpdated(boolean created, LoginResult loginResult) {
		portalMainPanel.userAccountCreatedOrUpdated(created, loginResult);
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
	public void setOntologyInfo(BaseOntologyInfo ontologyInfo) {
		this.ontologyInfo = ontologyInfo;
	}

	public void setEntityInfo(EntityInfo entityInfo) {
		this.entityInfo = entityInfo;
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

	public void setEntityPanel(EntityPanel entityPanel) {
		this.entityPanel = entityPanel;		
	}
	
	public EntityPanel getEntityPanel() {
		return entityPanel;		
	}
	
	
	public void completedRegisterOntologyResult(RegisterOntologyResult registerOntologyResult) {
		
		portalMainPanel.completedRegisterOntologyResult(registerOntologyResult);
	}


	public void refreshedListAllOntologies(List<RegisteredOntologyInfo> ontologyInfos) {
		portalMainPanel.refreshedListAllOntologies(ontologyInfos);
	}

	public void userToSignIn() {
		portalMainPanel.userToSignIn();
	}
	
	public void userSignedOut() {
		CookieMan.forgetPassword();
		portalMainPanel.userSignedOut();
		cancelEdit();
	}

	public void setMenuBarPanel(ControlsPanel controlsPanel) {
		this.controlsPanel = controlsPanel;
	}

	public ControlsPanel getMenuBarPanel() {
		return controlsPanel;
	}

	
	public String checkCanEditOntology(BaseOntologyInfo oi) {
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
			else if ( oi == null ) {
				error = NOT_AUTHORIZED;
			}
			else if ( oi instanceof RegisteredOntologyInfo ) {
				RegisteredOntologyInfo roi = (RegisteredOntologyInfo) oi;
				if ( ! loginResult.getUserId().equals(roi.getOntologyUserId()) ) {
					error = NOT_AUTHORIZED;
				}
			}
		}
		return error;
	}
	
	
	private IQuickInfo quickInfo = new IQuickInfo() {
		
		public Widget getWidget(String name, final RegisteredOntologyInfo oi, final boolean includeVersionInLinks,
				final boolean includeVersionsMenu) {
			
			if ( true ) {
				// This fragment creates a menuBar for dispatching the options.
				
				ControlsPanel controlsPanel = PortalControl.getInstance().getMenuBarPanel();
				
				// Note: Edit option not included here.
				MenuBar menu = controlsPanel.createOntologyMenuBar(oi, false, includeVersionInLinks, includeVersionsMenu);
				MenuBar mb = new MenuBar(true);
				
				if ( name != null ) {
					mb.addItem("<font size=\"-2\" color=\"gray\">" +name+ "</font>", true, menu);
				}
				else {
					mb.addItem("", menu);
				}
				return mb;
			}
			else {
				// This fragment uses an icon and a click listener to open a popup with the options.
				
				Image img = Orr.images.triright().createImage();
				img.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						
						ControlsPanel controlsPanel = PortalControl.getInstance().getMenuBarPanel();
						
						MenuBar menu = controlsPanel.createOntologyMenuBar(oi, false, includeVersionInLinks, includeVersionsMenu);
						
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

	public BaseOntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}

	public void notifyActivity(boolean b) {
		if ( controlsPanel != null ) {
			controlsPanel.notifyActivity(b);
		}
	}


	/**
	 * returns a title for the GUI according to the current intergace type.
	 * @return
	 */
	public String getTitle() {
		switch ( portalMainPanel.getInterfaceType() ) {
		case BROWSE:
			return "";

		case ONTOLOGY_VIEW:
		case ONTOLOGY_EDIT_NEW_VERSION:
			if ( ontologyInfo != null ) {
				return "Ontology: " +ontologyInfo.getUri();
			}
			break;
		case ENTITY_VIEW:
			if ( entityInfo != null ) {
				return "Term: " +entityInfo.getUri();
			}
			break;
		case UPLOAD_ONTOLOGY:
			return "Registering your ontology";
			
		case UPLOAD_NEW_VERSION:
			return "Registering new version" +
			  (ontologyInfo != null ? " for " +ontologyInfo.getUri() : "");
		}
		
		return null;
	}


	public void searchOntologies(String searchString, Command doneCmd) {
		portalMainPanel.searchOntologies(searchString, doneCmd);
	}
	
	public void unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		portalMainPanel.unregisterOntology(loginResult, oi);
	}

}
