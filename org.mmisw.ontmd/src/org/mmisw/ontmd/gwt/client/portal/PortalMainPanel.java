package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.LoginListener;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.UserPanel;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class PortalMainPanel extends VerticalPanel implements LoginListener, HistoryListener {

	public enum InterfaceType { BROWSE, ONTOLOGY_VIEW, ONTOLOGY_EDIT, SEARCH };
	
	
	private final PortalControl pctrl;

	private final LoginControlPanel loginControlPanel = new LoginControlPanel(this);
	
	private final HeaderPanel headerPanel = new HeaderPanel(loginControlPanel); 

	private final MenuBarPanel menuBarPanel = new MenuBarPanel();

	private final VerticalPanel bodyPanel = new VerticalPanel();
	
	private final BrowsePanel browsePanel;


	private InterfaceType interfaceType = InterfaceType.BROWSE;
	
	private UserPanel userPanel;
	
	private MyDialog signInPopup;

	
	static Map<String, Object> historyTokenMap = new HashMap<String, Object>();
	
	
	
	PortalMainPanel(final Map<String, String> params, List<OntologyInfo> ontologyInfos) {
		super();
		
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

	    menuBarPanel.showMenuBar(interfaceType);
	    
	    browsePanel = new BrowsePanel(ontologyInfos, loginResult);
	    this.setWidth("100%");
	    bodyPanel.setWidth("100%");
	    loginControlPanel.update(loginResult);
	    
//	    bodyPanel.setBorderWidth(1);
		
	    
	    this.add(headerPanel);
	    
	    this.add(menuBarPanel);
	    
	    this.add(bodyPanel);

	    bodyPanel.add(browsePanel);

	}
	
	private void userSignedIn() {
		menuBarPanel.showMenuBar(interfaceType);
		browsePanel.setLoginResult(pctrl.getLoginResult());
		loginControlPanel.update(pctrl.getLoginResult());
		if ( signInPopup != null ) {
			signInPopup.hide();
		}
	}
	
	void userSignedOut() {
		if ( userPanel != null ) {
			userPanel.logout();
		}
		pctrl.setLoginResult(null);
		menuBarPanel.showMenuBar(interfaceType);
		browsePanel.ontologyTable.showProgress();
	    browsePanel.setLoginResult(pctrl.getLoginResult());
	    loginControlPanel.update(null);
	    
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
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				userSignedIn();
			}
		});
	    
	}


	public void onHistoryChanged(String historyToken) {
		
		Object obj = historyTokenMap.get(historyToken);
		
		if ( obj == null ) {
			// for simplicity, always handle this by dispatching the main page:
			dispatchMainPanel();
			return;
		}
		
		if ( obj instanceof OntologyInfo ) {
			OntologyInfo ontologyInfo = (OntologyInfo) obj;
			pctrl.setOntologyInfo(ontologyInfo);
			
			Main.log("onHistoryChanged: OntologyUri: " +ontologyInfo.getUri());
			
			dispatchOntologyPanel(ontologyInfo);
			return;
		}
		
	}

	
	private void dispatchMainPanel() {
		interfaceType = InterfaceType.BROWSE;
	    menuBarPanel.showMenuBar(interfaceType);

	    bodyPanel.clear();
		bodyPanel.add(browsePanel);
	}
	
	private void dispatchOntologyPanel(final OntologyInfo ontologyInfo) {
		interfaceType = InterfaceType.ONTOLOGY_VIEW;
	    menuBarPanel.showMenuBar(interfaceType);

		String ontologyUri = ontologyInfo.getUri();

		Main.log("dispatchOntologyPanel:  ontologyUri=" +ontologyUri);
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo);

		bodyPanel.clear();
		bodyPanel.add(ontologyPanel);
		
		pctrl.setOntologyPanel(ontologyPanel);
	}

	public void editNewVersion(OntologyPanel ontologyPanel) {
		interfaceType = InterfaceType.ONTOLOGY_EDIT;
	    menuBarPanel.showMenuBar(interfaceType);
		
		ontologyPanel.setEditingInterface();
	}

	public void cancelEdit(OntologyPanel ontologyPanel) {
		interfaceType = InterfaceType.ONTOLOGY_VIEW;
	    menuBarPanel.showMenuBar(interfaceType);
		
		ontologyPanel.setViewingInterface();
	}


}
