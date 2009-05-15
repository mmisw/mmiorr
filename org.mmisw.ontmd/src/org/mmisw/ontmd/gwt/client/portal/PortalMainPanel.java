package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.LoginListener;
import org.mmisw.ontmd.gwt.client.UserPanel;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class PortalMainPanel extends VerticalPanel implements LoginListener {

	private final LoginControlPanel loginControlPanel = new LoginControlPanel(this);
	private final HeaderPanel headerPanel = new HeaderPanel(loginControlPanel); 

	private final MenuBarPanel menuBarPanel = new MenuBarPanel();
	private final SelectionTree selTree = new SelectionTree(this);
	private final OntologyTable ontologyTable = new OntologyTable();


	private UserPanel userPanel;
	private LoginResult loginResult;
	
	private MyDialog signInPopup;

	
	// all the ontologies from the registry
	private List<OntologyInfo> ontologyInfos;
	
	// the current displayed elements
	private final List<OntologyInfo> selectedOntologyInfos = new ArrayList<OntologyInfo>();

	
	private HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
	
	
	PortalMainPanel(final Map<String, String> params, List<OntologyInfo> ontologyInfos) {
		super();
		
		this.ontologyInfos = ontologyInfos;
		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( true ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1002");
				loginResult.setUserName("carueda");
			}
		}
		
		
	    if ( loginResult == null && params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    }

		

	    
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(this.ontologyInfos, loginResult);
	    
	    
	    this.add(headerPanel);
	    
	    
	    this.add(menuBarPanel);
	    
	    menuBarPanel.showMenuBar(loginResult != null);
	    loginControlPanel.update(loginResult);
	    	
	    
	    hSplit.setLeftWidget(selTree);
	    hSplit.setRightWidget(ontologyTable);
		
	    hSplit.setSplitPosition("200px");
	    hSplit.setHeight("500px");
	    
	    
		DecoratorPanel decPanel = new DecoratorPanel();
		this.add(decPanel);
	    decPanel.setWidget(hSplit);

	    hSplit.setWidth("1200px");

	}
	
	
	void allSelected() {
		ontologyTable.showProgress();
		ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
	}
		
	void authorSelected(final String userId) {
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( OntologyInfo oi : ontologyInfos ) {
					if ( userId.equalsIgnoreCase(oi.getUserId()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
	}

	void authoritySelected(final String auth) {
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( OntologyInfo oi : ontologyInfos ) {
					if ( auth.equalsIgnoreCase(oi.getAuthority()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
	}

	void typeSelected(final String type) {
		ontologyTable.showProgress();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				selectedOntologyInfos.clear();
				for ( OntologyInfo oi : ontologyInfos ) {
					if ( type.equalsIgnoreCase(oi.getType()) ) {
						selectedOntologyInfos.add(oi);
					}
				}
				ontologyTable.setOntologyInfos(selectedOntologyInfos, loginResult);
			}
		});
		
	}


	void userSignedOut() {
		ontologyTable.showProgress();
	    loginResult = null;
	    loginControlPanel.update(null);
	    menuBarPanel.showMenuBar(false);
	    selTree.update(this.ontologyInfos, loginResult);
	    ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
	}
	
	void userToSignIn() {
		if ( userPanel == null ) {
			userPanel = new UserPanel(this);
			signInPopup = new MyDialog(userPanel) {
				public boolean onKeyUpPreview(char key, int modifiers) {
					// avoid ENTER close the popup
					if ( key == KeyboardListener.KEY_ESCAPE  ) {
						hide();
						return false;
					}
					return true;
				}
			};
		}
		signInPopup.setText("Sign in");
		signInPopup.show();
	}

	public void loginOk(final LoginResult loginResult) {
		this.loginResult = loginResult;
		ontologyTable.showProgress();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				menuBarPanel.showMenuBar(loginResult != null);
				loginControlPanel.update(loginResult);
				if ( signInPopup != null ) {
					signInPopup.hide();
				}
				selTree.update(ontologyInfos, loginResult);
				ontologyTable.setOntologyInfos(ontologyInfos, loginResult);
			}
		});
	    
	}


}
