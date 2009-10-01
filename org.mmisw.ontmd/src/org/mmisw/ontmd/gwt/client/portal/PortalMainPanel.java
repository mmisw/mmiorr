package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ResolveUriResult;
import org.mmisw.ontmd.gwt.client.LoginPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.extont.RegisterExternalOntologyWizard;
import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.vine.VineMain;

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
public class PortalMainPanel extends VerticalPanel implements HistoryListener {

	public enum InterfaceType {
		BROWSE, ONTOLOGY_VIEW, ONTOLOGY_EDIT_NEW_VERSION, ONTOLOGY_EDIT_NEW, SEARCH,
		USER_ACCOUNT,
		ENTITY_VIEW,
		ENTITY_NOT_FOUND,
		UPLOAD_ONTOLOGY,
	};
	
	
	private final PortalControl pctrl;

	private final HeaderPanel headerPanel = new HeaderPanel(); 

	private final ControlsPanel controlsPanel = new ControlsPanel();

	private final VerticalPanel bodyPanel = new VerticalPanel();
	
	private final BrowsePanel browsePanel;


	private InterfaceType interfaceType = InterfaceType.BROWSE;
	
	private LoginPanel loginPanel;
	
	private MyDialog signInPopup;

	private String pendingMessage = null;

	
	/** helps confirm the leave of the current page */
	private void _setupWindowCloseListener() {
		Window.addWindowCloseListener(new WindowCloseListener() {
			public String onWindowClosing() {
				if ( interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW_VERSION
				||   interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW
				) {
					return "If any, all edits will be lost";
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
			
			if ( false ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1001");
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
	    this.add(controlsPanel);
	    this.add(bodyPanel);

	    String historyToken = History.getToken();
	    if ( historyToken != null && historyToken.trim().length() > 0 ) {
			Main.log("history token = " +historyToken);
	    	History.fireCurrentHistoryState();
	    }
	    else {
		    controlsPanel.showMenuBar(interfaceType);
		    
		    bodyPanel.add(browsePanel);
		    
//			History.newItem("", false);

	    }
	}
	
	
	InterfaceType getInterfaceType() {
		return interfaceType;
	}


	private void userSignedIn() {
		controlsPanel.showMenuBar(interfaceType);
		browsePanel.setLoginResult(pctrl.getLoginResult());
		if ( signInPopup != null ) {
			signInPopup.hide();
		}
	}
	
	void userSignedOut() {
		if ( loginPanel != null ) {
			loginPanel.logout();
		}
		pctrl.setLoginResult(null);
		headerPanel.updateLinks(interfaceType);
		controlsPanel.showMenuBar(interfaceType);
		browsePanel.ontologyTable.showProgress();
	    browsePanel.setLoginResult(pctrl.getLoginResult());
	    
	    History.newItem(PortalConsts.T_BROWSE);
	}
	
	void userToSignIn() {
		if ( loginPanel == null ) {
			loginPanel = new LoginPanel();
			signInPopup = new MyDialog(loginPanel.getWidget()) {
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
							loginPanel.getFocus();
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

	public void userAccountCreatedOrUpdated(boolean created, final LoginResult loginResult) {
		if ( created ) {
			// the timer is to let the user account panel to show any final messages
			// for a little while before going to a new page:
			new Timer() {
				public void run() {
					loginOk(loginResult);
				}
			}.schedule(1000);
		}
		// Else: nothing--let the user account panel continue.
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

	
	// LoginListener
	public void loginCreateAccount() {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				if ( signInPopup != null ) {
					signInPopup.hide();
				}
			}
		});
		History.newItem(PortalConsts.T_USER_ACCOUNT);
	}


	public void onHistoryChanged(String historyToken) {
		
		Main.log("onHistoryChanged: historyToken: " +historyToken);
		
		historyToken = historyToken.trim();
		if ( historyToken.length() > 0 ) {
			if ( historyToken.toLowerCase().equals(PortalConsts.T_BROWSE) ) {
				dispatchMainPanel(false);		
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_SEARCH_TERMS) ) {
				dispatchSearchTerms();		
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_USER_ACCOUNT) ) {
				dispatchCreateAccount();		
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_SIGN_IN) ) {
				PortalControl.getInstance().userToSignIn();
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_SIGN_OUT) ) {
				PortalControl.getInstance().userSignedOut();
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_VOC2RDF) ) {
				dispatchNewVocabulary();
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_VINE) ) {
				dispatchNewMappingOntology();
			}
			else if ( historyToken.toLowerCase().equals(PortalConsts.T_UPLOAD) ) {
				dispatchUploadOntology();
			}
			else {
				String uri = historyToken.trim();
				Main.log("onHistoryChanged: URI: " +uri);
				
				// TODO remove unused code getOntologyInfo once resolveUri is tested.
				if ( true ) {
					resolveUri(uri);
				}
				else {  
					getOntologyInfo(uri);
				}
			}
		}
		else {
			dispatchMainPanel(false);
			History.newItem(PortalConsts.T_BROWSE);
		}
			
	}

	
	
	private void dispatchCreateAccount() {
		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}
		
		UserAccountPanel userAccountPanel = new UserAccountPanel();

		interfaceType = InterfaceType.USER_ACCOUNT;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		
	    bodyPanel.clear();
		bodyPanel.add(userAccountPanel.getWidget());
		userAccountPanel.dispatch();
	}


	public void refreshListAllOntologies() {
		dispatchMainPanel(true);
	}
	
	public void searchTerms() {
		History.newItem(PortalConsts.T_SEARCH_TERMS);
	}
	
	
	
	private void dispatchSearchTerms() {
		
		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}
		
		SearchTermsPanel searchTermsPanel = new SearchTermsPanel();

		interfaceType = InterfaceType.SEARCH;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		
	    bodyPanel.clear();
		bodyPanel.add(searchTermsPanel);
	}
	

	
	private void dispatchMainPanel(boolean reloadList) {
		
		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}
		
		interfaceType = InterfaceType.BROWSE;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);

	    bodyPanel.clear();

	    if ( reloadList ) {
	    	bodyPanel.add(new HTML("<i>Refreshing...</i>"));
	    	Portal portal = pctrl.getPortal();
	    	portal.refreshListAllOntologies();
	    }
	    else {
	    	bodyPanel.add(browsePanel);
	    	
	    	if ( pendingMessage  != null ) {
	    		DeferredCommand.addCommand(new Command() {
					public void execute() {
		    			Window.alert(pendingMessage);
		    			pendingMessage = null;
					}
	    		});
	    	}
	    }
	}
	
	private void dispatchOntologyPanel(final RegisteredOntologyInfo ontologyInfo, final boolean versionExplicit) {
		String ontologyUri = ontologyInfo.getUri();
		Main.log("dispatchOntologyPanel:  ontologyUri=" +ontologyUri);

		interfaceType = InterfaceType.ONTOLOGY_VIEW;

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading ontology...</i>"));
	    
	    DeferredCommand.addCommand(new Command() {
			public void execute() {
				OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, true, versionExplicit);
				pctrl.setOntologyInfo(ontologyInfo);
				pctrl.setOntologyPanel(ontologyPanel);
				controlsPanel.showMenuBar(interfaceType);
				headerPanel.updateLinks(interfaceType);
				
			    bodyPanel.clear();
				bodyPanel.add(ontologyPanel);
			}
	    });
	}

	private void dispatchEntityPanel(final EntityInfo entityInfo) {
		String entityUri = entityInfo.getUri();
		Main.log("dispatchTermPanel:  entityUri=" +entityUri);

		interfaceType = InterfaceType.ENTITY_VIEW;

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading term...</i>"));
	    
	    DeferredCommand.addCommand(new Command() {
			public void execute() {
				
				pctrl.setOntologyInfo(null);
				pctrl.setOntologyPanel(null);

				EntityPanel entityPanel = new EntityPanel();
//				entityPanel.setSize("400", "100");

				pctrl.setEntityInfo(entityInfo);
				pctrl.setEntityPanel(entityPanel);


				
//				pctrl.setOntologyInfo(entityInfo);
//				pctrl.setOntologyPanel(ontologyPanel);
				controlsPanel.showMenuBar(interfaceType);
				headerPanel.updateLinks(interfaceType);
				
			    bodyPanel.clear();
				bodyPanel.add(entityPanel);
				entityPanel.update(entityInfo);
			}
	    });
	}

	
	private void dispatchUriNotFound(ResolveUriResult resolveUriResult) {
		final NotFoundUriPanel nfup = new NotFoundUriPanel(resolveUriResult.getUri(), resolveUriResult.isUrl());
		
		interfaceType = InterfaceType.ENTITY_NOT_FOUND;

	    bodyPanel.clear();
	    
	    DeferredCommand.addCommand(new Command() {
			public void execute() {
				
				pctrl.setOntologyInfo(null);
				pctrl.setOntologyPanel(null);
				pctrl.setEntityInfo(null);
				pctrl.setEntityPanel(null);

				controlsPanel.showMenuBar(interfaceType);
				headerPanel.updateLinks(interfaceType);
				
				VerticalPanel pan = new VerticalPanel();
				pan.setSpacing(20);
				pan.add(nfup);
				bodyPanel.add(pan);
			}
	    });


	}
	
	public void createNewFromFile() {
		RegisteredOntologyInfo ontologyInfo = new RegisteredOntologyInfo();
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, false, false);

		pctrl.setOntologyInfo(ontologyInfo);
		pctrl.setOntologyPanel(ontologyPanel);
		
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.createNewFromFile();
		
	    bodyPanel.clear();
		bodyPanel.add(ontologyPanel);
	}

	
	
	/** Starts the sequence to register an external ontology.
	 * This will eventually replace {@link #createNewFromFile()}.
	 */
	private void dispatchUploadOntology() {
		// TODO actual parameters
		RegisterExternalOntologyWizard wizard = new RegisterExternalOntologyWizard();

		pctrl.setOntologyInfo(null);
		pctrl.setOntologyPanel(null);
		
		interfaceType = InterfaceType.UPLOAD_ONTOLOGY;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		
	    bodyPanel.clear();
		bodyPanel.add(wizard.getWidget());

	}

	public void createNewMappingOntology() {
		History.newItem(PortalConsts.T_VINE);
	}
	
	/** If no user is logged in, then it simply triggers the main browse page
	 * TODO prompt for Sign in, and then launch the "new mapping" page.
	 * or
	 * TODO Allow the VINE interface, even if no user logged in.
	 */
	public void dispatchNewMappingOntology() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || loginResult.getError() != null ) {
			pendingMessage = "Please, sign in and then select \"Create mapping\"" +
				" to use the integrated VINE tool."
			;
			History.newItem(PortalConsts.T_BROWSE);
			return;
		}
		
		RegisteredOntologyInfo ontologyInfo = new RegisteredOntologyInfo();
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, false, false);

		pctrl.setOntologyInfo(ontologyInfo);
		pctrl.setOntologyPanel(ontologyPanel);
		
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.createNewMappingOntology();
		
	    bodyPanel.clear();
		bodyPanel.add(ontologyPanel);
	}

	void createNewVocabulary() {
		History.newItem(PortalConsts.T_VOC2RDF);
	}
	
	/** If no user is logged in, then it simply triggers the main browse page
	 * TODO prompt for Sign in, and then launch the "new vocabulary" page.
	 * or
	 * TODO Allow the VOC2RDF interface, even if no user logged in.
	 */
	private void dispatchNewVocabulary() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || loginResult.getError() != null ) {
			pendingMessage = "Please, sign in and then select \"Create vocabulary\"" +
					" to use the integrated Voc2RDF tool."
			;
			History.newItem(PortalConsts.T_BROWSE);
			return;
		}
		
		RegisteredOntologyInfo ontologyInfo = new RegisteredOntologyInfo();
		OntologyPanel ontologyPanel = new OntologyPanel(ontologyInfo, false, false);

		pctrl.setOntologyInfo(ontologyInfo);
		pctrl.setOntologyPanel(ontologyPanel);
		
		interfaceType = InterfaceType.ONTOLOGY_EDIT_NEW;
	    controlsPanel.showMenuBar(interfaceType);
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
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);
		ontologyPanel.updateInterface(interfaceType);
	}

	public  void reviewAndRegister(OntologyPanel ontologyPanel) {
		if ( ontologyPanel != null ) {
			ontologyPanel.reviewAndRegister();
		}
	}
	
	
	public void cancelEdit(OntologyPanel ontologyPanel) {
		if ( ontologyPanel != null ) {
			if ( ! Window.confirm("Any edits will be lost") ) {
				return;
			}
		}
		
		switch ( interfaceType ) {
			case ONTOLOGY_EDIT_NEW_VERSION:
				interfaceType = InterfaceType.ONTOLOGY_VIEW;
			    controlsPanel.showMenuBar(interfaceType);
			    headerPanel.updateLinks(interfaceType);
			    if ( ontologyPanel != null ) {
			    	ontologyPanel.updateInterface(interfaceType);
			    }
				break;
			case ONTOLOGY_EDIT_NEW:
				interfaceType = InterfaceType.BROWSE;
			    controlsPanel.showMenuBar(interfaceType);
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
		History.newItem(PortalConsts.T_BROWSE);
	}

	public void refreshedListAllOntologies(List<RegisteredOntologyInfo> ontologyInfos) {
		bodyPanel.clear();
		bodyPanel.add(browsePanel);
		browsePanel.setAllOntologyInfos(ontologyInfos);
		
		VineMain.setAllUris(ontologyInfos);
	}

	
	/**
	 * Requests an ontology or term to the back-end and dispatches a corresponding
	 * panel, ontology or term (TODO: term)
	 * @param uri
	 */
	private void resolveUri(final String uri) {
		AsyncCallback<ResolveUriResult> callback = new AsyncCallback<ResolveUriResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(ResolveUriResult resolveUriResult) {
				Main.log("resolveUri <" +uri+ ">: call completed.");
				
				String error = null;
				if ( resolveUriResult == null ) {
					error = "<b>" +uri+ "</b>: " +
							"<font color=\"red\">" +"URI not found"+ "</font>";
				}
				else if ( resolveUriResult.getError() != null ) {
					error = "<font color=\"red\">" +resolveUriResult.getError()+ "</font>";
				}
				
				if ( error != null ) {
					VerticalPanel vp = new VerticalPanel();
					vp.setSpacing(14);
					vp.add(new HTML(error));
					vp.add(new Hyperlink("Go to main page", PortalConsts.T_BROWSE));
					bodyPanel.clear();
				    bodyPanel.add(vp);
				    return;
				}
				
				RegisteredOntologyInfo registeredOntologyInfo = resolveUriResult.getRegisteredOntologyInfo();
				if ( registeredOntologyInfo != null ) {
					boolean versionExplicit = uri.indexOf("version=") >= 0;
					dispatchOntologyPanel(registeredOntologyInfo, versionExplicit);
					return;
				}
				
				EntityInfo entityInfo = resolveUriResult.getEntityInfo();
				if ( entityInfo != null ) {
					dispatchEntityPanel(entityInfo);
					return;
				}
				
				dispatchUriNotFound(resolveUriResult);
			}
		};

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading ontology...</i>"));

		Main.log("getOntologyInfo: ontologyUri = " +uri);
		Main.ontmdService.resolveUri(uri, callback);
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
				Main.log("getOntologyInfo: ontologyUri = " +ontologyUri+ ". success.");
				
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
					vp.add(new Hyperlink("Go to main page", PortalConsts.T_BROWSE));
					bodyPanel.clear();
				    bodyPanel.add(vp);
				}
				else {
					boolean versionExplicit = ontologyUri.indexOf("version=") >= 0;
					dispatchOntologyPanel(ontologyInfo, versionExplicit);
				}
			}
		};

	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Loading ontology...</i>"));

		Main.log("getOntologyInfo: ontologyUri = " +ontologyUri);
		Main.ontmdService.getOntologyInfo(ontologyUri, callback);
	}


	void searchOntologies(String searchString, Command doneCmd) {
		browsePanel.searchOntologies(searchString, doneCmd);
	}


}
