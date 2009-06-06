package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.ontmd.gwt.client.LoginListener;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.UserPanel;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class PortalMainPanel extends VerticalPanel implements LoginListener, HistoryListener {

	public enum InterfaceType { BROWSE, ONTOLOGY_VIEW, ONTOLOGY_EDIT_NEW_VERSION, ONTOLOGY_EDIT_NEW, SEARCH };
	
	
	private final PortalControl pctrl;

	private final HeaderPanel headerPanel = new HeaderPanel(); 

	private final MenuBarPanel menuBarPanel = new MenuBarPanel();

	private final VerticalPanel bodyPanel = new VerticalPanel();
	
	private final BrowsePanel browsePanel;


	private InterfaceType interfaceType = InterfaceType.BROWSE;
	
	private UserPanel userPanel;
	
	private MyDialog signInPopup;

	
	static Map<String, Object> historyTokenMap = new HashMap<String, Object>();
	
	
	/** helps confirm the leave of the current page */
	private void _setupWindowCloseListener() {
		Window.addWindowCloseListener(new WindowCloseListener() {
			public String onWindowClosing() {
				if ( interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW_VERSION ) {
					return "If any, all edit will be lost";
				}
				return null;
			}
			
			public void onWindowClosed() { /* ignore */  }
		});
	}
	
	
	PortalMainPanel(final Map<String, String> params, List<RegisteredOntologyInfo> ontologyInfos) {
		super();
	
		_setupWindowCloseListener();

		pctrl = PortalControl.getInstance();
		pctrl.setPortalMainPanel(this);
		
		History.addHistoryListener(this);
		
		LoginResult loginResult = null;
		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( true ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1002");
				loginResult.setUserName("carueda");
				loginResult.setUserRole("ROLE_ADMINISTRATOR");
			}
		}
		
		
	    if ( loginResult == null && params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    }
	    
	    pctrl.setLoginResult(loginResult);

	    browsePanel = new BrowsePanel(ontologyInfos, loginResult);
	    this.setWidth("100%");
	    bodyPanel.setWidth("100%");

	    interfaceType = InterfaceType.BROWSE;
	    headerPanel.updateLinks(interfaceType);

	    this.add(headerPanel);
	    this.add(menuBarPanel);
	    this.add(bodyPanel);

	    String historyToken = History.getToken();
	    if ( historyToken != null && historyToken.trim().length() > 0 ) {
			Main.log("history token = " +historyToken);
	    	History.fireCurrentHistoryState();
	    }
	    else {
		    menuBarPanel.showMenuBar(interfaceType);
		    
		    bodyPanel.add(browsePanel);
		    
//			History.newItem("", false);

	    }
	}
	
	private void userSignedIn() {
		menuBarPanel.showMenuBar(interfaceType);
		browsePanel.setLoginResult(pctrl.getLoginResult());
		if ( signInPopup != null ) {
			signInPopup.hide();
		}
	}
	
	void userSignedOut() {
		if ( userPanel != null ) {
			userPanel.logout();
		}
		pctrl.setLoginResult(null);
		headerPanel.updateLinks(interfaceType);
		menuBarPanel.showMenuBar(interfaceType);
		browsePanel.ontologyTable.showProgress();
	    browsePanel.setLoginResult(pctrl.getLoginResult());
	    
	}
	
	void userToSignIn() {
		if ( userPanel == null ) {
			userPanel = new UserPanel(this);
			signInPopup = new MyDialog(userPanel.getWidget()) {
				public boolean onKeyUpPreview(char key, int modifiers) {
					// avoid ENTER close the popup
					if ( key == KeyboardListener.KEY_ESCAPE  ) {
						hide();
						return false;
					}
					return true;
				}
				
				public void show() {
					// use a timer to make the userPanel focused (there must be a better way)
					new Timer() {
						public void run() {
							userPanel.getFocus();
						}
					}.schedule(700);
					super.show();
				}
			};
		}
		signInPopup.setText("Sign in");
		signInPopup.center();
		signInPopup.show();
	}

	public void loginOk(final LoginResult loginResult) {
		pctrl.setLoginResult(loginResult);
		browsePanel.ontologyTable.showProgress();
		headerPanel.updateLinks(interfaceType);
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				userSignedIn();
			}
		});
	    
	}


	public void onHistoryChanged(String historyToken) {
		
		Main.log("onHistoryChanged: historyToken: " +historyToken);
		
		Object obj = historyTokenMap.get(historyToken);
		
		if ( obj == null ) {

			if ( historyToken.trim().length() > 0 ) {
				if ( historyToken.trim().toLowerCase().equals("browse") ) {
					dispatchMainPanel(false);		
				}
				else {
					String ontologyUri = historyToken.trim();
					Main.log("onHistoryChanged: URI: " +ontologyUri);
					getOntologyInfo(ontologyUri);
				}
			}
			else {
				dispatchMainPanel(false);
			}
			
			return;
		}
		
		if ( obj instanceof RegisteredOntologyInfo ) {
			RegisteredOntologyInfo ontologyInfo = (RegisteredOntologyInfo) obj;
			pctrl.setOntologyInfo(ontologyInfo);
			Main.log("onHistoryChanged: OntologyInfo: " +ontologyInfo.getUri());
			dispatchOntologyPanel(ontologyInfo);
			return;
		}
		
		
		// else:
		dispatchMainPanel(false);
	}

	
	private void dispatchMainPanel(boolean reloadList) {
		
		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}
		
		interfaceType = InterfaceType.BROWSE;
	    menuBarPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);

	    bodyPanel.clear();

	    if ( reloadList ) {
	    	bodyPanel.add(new HTML("<i>Refreshing...</i>"));
	    	Portal portal = pctrl.getPortal();
	    	portal.refreshListAllOntologies();
	    }
	    else {
	    	bodyPanel.add(browsePanel);
	    }
	}
	
	private void dispatchOntologyPanel(final RegisteredOntologyInfo ontologyInfo) {
		String ontologyUri = ontologyInfo.getUri();
		Main.log("dispatchOntologyPanel:  ontologyUri=" +ontologyUri);

		interfaceType = InterfaceType.ONTOLOGY_VIEW;

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading ontology...</i>"));
	    
	    DeferredCommand.addCommand(new Command() {
			public void execute() {
				OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, true);
				pctrl.setOntologyInfo(ontologyInfo);
				pctrl.setOntologyPanel(ontologyPanel);
				menuBarPanel.showMenuBar(interfaceType);
				headerPanel.updateLinks(interfaceType);
				
			    bodyPanel.clear();
				bodyPanel.add(ontologyPanel);
			}
	    });
	}

	
	public void createNewFromFile() {
		RegisteredOntologyInfo ontologyInfo = new RegisteredOntologyInfo();
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, false);

		pctrl.setOntologyInfo(ontologyInfo);
		pctrl.setOntologyPanel(ontologyPanel);
		
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW;
	    menuBarPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.createNewFromFile();
		
	    bodyPanel.clear();
		bodyPanel.add(ontologyPanel);
	}

	
	
	public void createNewVocabulary() {
		RegisteredOntologyInfo ontologyInfo = new RegisteredOntologyInfo();
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, false);

		pctrl.setOntologyInfo(ontologyInfo);
		pctrl.setOntologyPanel(ontologyPanel);
		
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW;
	    menuBarPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.createNewVocabulary();
		
	    bodyPanel.clear();
		bodyPanel.add(ontologyPanel);
	}


	
	
	public void editNewVersion(OntologyPanel ontologyPanel) {
		RegisteredOntologyInfo ontologyInfo = ontologyPanel.getOntologyInfo();
		String error = pctrl.checkCanEditOntology(ontologyInfo);
		
		if ( error != null ) {
			Window.alert(error);
			return;
		}
			
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW_VERSION;
	    menuBarPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.updateInterface(interfaceType);
	}

	public void cancelEdit(OntologyPanel ontologyPanel) {
		if ( ! Window.confirm("Any edits will be lost") ) {
			return;
		}
		switch ( interfaceType ) {
			case ONTOLOGY_EDIT_NEW_VERSION:
				interfaceType = InterfaceType.ONTOLOGY_VIEW;
			    menuBarPanel.showMenuBar(interfaceType);
			    headerPanel.updateLinks(interfaceType);
			    if ( ontologyPanel != null ) {
			    	ontologyPanel.updateInterface(interfaceType);
			    }
				break;
			case ONTOLOGY_EDIT_NEW:
				interfaceType = InterfaceType.BROWSE;
			    menuBarPanel.showMenuBar(interfaceType);
			    headerPanel.updateLinks(interfaceType);
			    bodyPanel.clear();
			    bodyPanel.add(browsePanel);
				break;
			default:
				// shouldn't happen. just return;
				return;
		}
	}

	public void completedRegisterOntologyResult(RegisterOntologyResult registerOntologyResult) {
		
		dispatchMainPanel(true);
	}

	public void refreshedListAllOntologies(List<RegisteredOntologyInfo> ontologyInfos) {
		bodyPanel.clear();
		bodyPanel.add(browsePanel);
		browsePanel.setAllOntologyInfos(ontologyInfos);
	}

	
	/**
	 * Requests an ontology to the back-end and dispatches a corresponding
	 * ontology panel.
	 * @param ontologyUri
	 */
	private void getOntologyInfo(final String ontologyUri) {
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				String error = null;
				if ( ontologyInfo == null ) {
					error = "<b>" +ontologyUri+ "</b>: " +
							"<font color=\"red\">" +"Ontology not found by this URI"+ "</font>";
				}
				else if ( ontologyInfo.getError() != null ) {
					error = "<font color=\"red\">" +ontologyInfo.getError()+ "</font>";
				}
				
				if ( error != null ) {
					VerticalPanel vp = new VerticalPanel();
					vp.setSpacing(14);
					vp.add(new HTML(error));
					vp.add(new Hyperlink("Go to main page", "browse"));
					bodyPanel.clear();
				    bodyPanel.add(vp);
				}
				else {
					dispatchOntologyPanel(ontologyInfo);
				}
			}
		};

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading ontology...</i>"));

		Main.log("getOntologyInfo: ontologyUri = " +ontologyUri);
		Main.ontmdService.getOntologyInfo(ontologyUri, callback);
	}


}
