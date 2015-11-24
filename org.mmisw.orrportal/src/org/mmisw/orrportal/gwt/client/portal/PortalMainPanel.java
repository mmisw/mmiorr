package org.mmisw.orrportal.gwt.client.portal;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.HostingType;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OtherOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ResolveUriResult;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.orrportal.gwt.client.LoginPanel;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.portal.admin.AdminPanel;
import org.mmisw.orrportal.gwt.client.portal.extont.RegisterNewWizard;
import org.mmisw.orrportal.gwt.client.portal.extont.RegisterVersionWizard;
import org.mmisw.orrportal.gwt.client.util.GaUtil;
import org.mmisw.orrportal.gwt.client.util.MyDialog;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;
import org.mmisw.orrportal.gwt.client.vine.VineMain;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
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
		UPLOAD_NEW_VERSION,
		ADMIN,
	};


	private static final String ABANDON_CONFIRMATION =
			"If any, all edits will be lost.\n" +
			"Proceed?";

	/** #315: upon change in the browser history, these two variables help with
	 * prompting user for confirmation prior to abandoning edits or ongoing
	 * upload operation, ang returning to the associated history token.
	 */
	private String currentHistoryToken;      // token to return to if canceling abandon
	private boolean returningToOngoingEdit;  // to handle the 2nd associated history change

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
				||   interfaceType == InterfaceType.UPLOAD_NEW_VERSION
				||   interfaceType == InterfaceType.UPLOAD_ONTOLOGY
				) {
					return "If any, all edits will be lost";
				}
				return null;
			}

			public void onWindowClosed() { /* ignore */  }
		});
	}


	public PortalMainPanel(final Map<String, String> params, List<RegisteredOntologyInfo> ontologyInfos) {
		super();

		_setupWindowCloseListener();

		pctrl = PortalControl.getInstance();
		pctrl.setPortalMainPanel(this);

		currentHistoryToken = History.getToken();
		History.addHistoryListener(this);

		LoginResult loginResult = pctrl.getLoginResult();

	    browsePanel = new BrowsePanel(ontologyInfos, loginResult);
	    this.setWidth("100%");
	    bodyPanel.setWidth("100%");

	    interfaceType = InterfaceType.BROWSE;
	    headerPanel.updateLinks(interfaceType);

	    this.add(headerPanel);
	    this.add(controlsPanel);
	    this.add(bodyPanel);

	}

	void dispatch() {
	    String historyToken = History.getToken();
	    if ( historyToken != null && historyToken.trim().length() > 0 ) {
			Orr.log("history token = " +historyToken);
	    	History.fireCurrentHistoryState();
	    }
	    else {
		    controlsPanel.showMenuBar(interfaceType);
		    bodyPanel.add(browsePanel);
	    }
	}


	InterfaceType getInterfaceType() {
		return interfaceType;
	}


	private void userSignedIn() {
		Orr.log("userSignedIn: interfaceType=" +interfaceType);
		controlsPanel.showMenuBar(interfaceType);
		browsePanel.setLoginResult(pctrl.getLoginResult(), true);
	}

	void userSignedOut() {
		if ( loginPanel != null ) {
			loginPanel.logout();
		}
		pctrl.setLoginResult(null);
		headerPanel.updateLinks(interfaceType);
		controlsPanel.showMenuBar(interfaceType);
		browsePanel.ontologyTable.showProgress();
	    browsePanel.setLoginResult(pctrl.getLoginResult(), true);

//	    History.newItem(PortalConsts.T_BROWSE);
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
			userAccountCreated(loginResult);
		}
		// Else: nothing--let the user account panel continue.
	}

	private void userAccountCreated(final LoginResult loginResult) {
		// do the loginOk thing:
		loginOk(loginResult);
		// and refresh the user account in update mode:
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				dispatchUserAccount(true);
			}
		});
	}

	/**
	 * Sets the login result associated with this panel.
	 * Does not trigger any GUI updates.
	 * @param loginResult
	 */
	public void setLoginResult(LoginResult loginResult) {
		pctrl.setLoginResult(loginResult);
		browsePanel.setLoginResult(loginResult, false);
	}

	public void loginOk(final LoginResult loginResult) {
		if ( signInPopup != null ) {
			signInPopup.hide();
		}

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				pctrl.setLoginResult(loginResult);
				browsePanel.ontologyTable.showProgress();
				headerPanel.updateLinks(interfaceType);
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


	public void dispatchMainPage(String searchString) {
		// in general, do not reload the list, except if the current known list is empty:
		boolean reload = browsePanel.getNumberOfOntologies() == 0;
		dispatchMainPanel(reload, searchString);
	}

	public void onHistoryChanged(String historyToken) {

		Orr.log("onHistoryChanged: historyToken: [" +historyToken+ "]");
		historyToken = historyToken.trim();

		if (returnToOngoingEdit(historyToken)) {
			return;
		}

		currentHistoryToken = historyToken;

		// TODO trackPageview or trackEvent?
		GaUtil.trackPageview(historyToken);

		final String lowercase = historyToken.toLowerCase();
		boolean dispatched = false;

		if ( historyToken.length() == 0 || lowercase.equals(PortalConsts.T_BROWSE) ) {
			dispatchMainPage("");
			dispatched = true;
            // TODO replace #b token with nothing
		}

		else if ( lowercase.startsWith(PortalConsts.T_SEARCH_ONTS) ) {
			// remove first leading slash if any:
			String searchString = historyToken.substring(PortalConsts.T_SEARCH_ONTS.length()).replaceAll("^/", "");
//			searchString = URL.decode(searchString.replace("%2F", "/"));
            // TODO if searchString is empty, go to main page while removing the #so token
			dispatchMainPage(searchString);
			dispatched = true;
		}

		else if ( lowercase.startsWith(PortalConsts.T_SEARCH_TERMS) ) {
			// remove first leading slash if any:
			String searchString = historyToken.substring(PortalConsts.T_SEARCH_TERMS.length()).replaceAll("^/", "");
//			searchString = URL.decode(searchString.replace("%2F", "/"));
			dispatchSearchTerms(searchString);
			dispatched = true;
		}

		else if ( lowercase.equals(PortalConsts.T_USER_ACCOUNT) ) {
			dispatchUserAccount(false);
			dispatched = true;
		}

		else if ( lowercase.startsWith(PortalConsts.T_REGISTERED_BY_USER) ) {
			// remove first leading slash if any:
			String user = historyToken.substring(PortalConsts.T_REGISTERED_BY_USER.length()).replaceAll("^/", "");
			dispatchRegisteredByUser(user);
			dispatched = true;
            // TODO select corresponding element in the tree
		}

		else if ( lowercase.startsWith(PortalConsts.T_REGISTERED_BY_AUTHORITY) ) {
			// remove first leading slash if any:
			String authority = historyToken.substring(PortalConsts.T_REGISTERED_BY_AUTHORITY.length()).replaceAll("^/", "");
			dispatchRegisteredByAuthority(authority);
			dispatched = true;
            // TODO select corresponding element in the tree
		}

		else if ( lowercase.equals(PortalConsts.T_SIGN_IN)
		||        lowercase.equals(PortalConsts.T_SIGN_OUT) ) {
			// #220 "on browse page but tag remains in #login after signing in"
			// history token mechanism not used anymore.
			// Note: we could just let these tags be handle as URIs below.
			// But better force the default which is open the main browse page
			dispatched = false;
		}

		else if ( lowercase.equals(PortalConsts.T_VOC2RDF) ) {
			dispatchNewVocabulary();
			dispatched = true;
		}

		else if ( lowercase.equals(PortalConsts.T_VINE) ) {
			dispatchNewMappingOntology();
			dispatched = true;
		}

		else if ( lowercase.equals(PortalConsts.T_REGISTER_EXTERNAL) ) {
			dispatchUploadOntology();
			dispatched = true;
		}

		else if ( lowercase.equals(PortalConsts.T_ADMIN) ) {
			dispatchAdmin();
			dispatched = true;
		}

		else {
			String uri = historyToken.trim();
			dispatched = true;
			resolveUri(uri);
		}


		if ( ! dispatched ) {
			// just go the "home" page, ie., the main browse page:
			History.newItem(null);
		}
	}

	/**
	 * Handles the confirmation about abandoning ongoing edits or
	 * upload operation upon change in browser history.
	 *
	 * @param historyToken
	 * 					the history token the browser has just arrived to.
   *
	 * @return true to indicate that the user wants to continue where she was.
	 *         false to indicate to just proceed processing the new history token.
	 */
	private boolean returnToOngoingEdit(String historyToken) {
		Orr.log("#315 returnToOngoingEdit: " +
				"historyToken: [" +historyToken+ "]  " +
				"previous=[" +currentHistoryToken+ "]  " +
				"returningToOngoingEdit=" + returningToOngoingEdit
		);

		if (returningToOngoingEdit) {
			// this is the 2nd call associated with the actual token we should return
			// to when the user decided to continue on the ongoing change action.
			returningToOngoingEdit = false;
			return true;
		}

		if (historyToken.equals(currentHistoryToken)) {
			// should not happen because there's no history change, but just in case.
			return false;
		}

		Orr.log("returnToOngoingEdit: interfaceType=" +interfaceType);
		if ( interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW_VERSION
		||   interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW
		||   interfaceType == InterfaceType.UPLOAD_NEW_VERSION
		||   interfaceType == InterfaceType.UPLOAD_ONTOLOGY
		) {
			if (Window.confirm(ABANDON_CONFIRMATION)) {
				return false;
			}

			// set this flag to also return true in next call
			returningToOngoingEdit = true;
			History.newItem(currentHistoryToken);
			return true;
		}

		return false;
	}

	private void dispatchUserAccount(boolean accountJustCreated) {
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
		userAccountPanel.dispatch(accountJustCreated);
	}


	public void refreshListAllOntologies() {
		dispatchMainPanel(true, "");
	}

	public void searchTerms() {
		History.newItem(PortalConsts.T_SEARCH_TERMS);
	}



	private void dispatchSearchTerms(String searchString) {

		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}

		SearchTermsPanel searchTermsPanel = new SearchTermsPanel(searchString);

		interfaceType = InterfaceType.SEARCH;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);

	    bodyPanel.clear();
		bodyPanel.add(searchTermsPanel);
	}

	private void dispatchRegisteredByUser(String user) {
		browsePanel.authorSelected(user, true);
	}

	private void dispatchRegisteredByAuthority(String authority) {
		browsePanel.authoritySelected(authority);
	}


	public void showRefreshingMessage() {
	    bodyPanel.clear();
	    bodyPanel.add(new HTML("<i>Refreshing...</i>"));
	}

	private void dispatchMainPanel(boolean reloadList, final String searchString) {
		Orr.log("__dispatchMainPanel: reloadList=" + reloadList);
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
	    	Orr.refreshListAllOntologies(null);
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
		DeferredCommand.addCommand(new Command() {
            public void execute() {
                controlsPanel.dispatchSearchOntologies(searchString);
            }
        });
	}

    /**
     * similar to dispatchMainPanel(boolean, String) but allowing a more general post-action, which
     * would be a preferred mechanism in general, but for now we are just focusing on fixing
     * #321 ""more graceful handling of browser navigation including history"
     * @param run
     */
    public void dispatchMainPanel2(Runnable run) {
        Orr.log("__dispatchMainPanel2");
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

        bodyPanel.add(new HTML("<i>Refreshing...</i>"));
        Orr.refreshListAllOntologies(run);
    }

    private void dispatchOntologyPanel(final RegisteredOntologyInfo ontologyInfo, final boolean versionExplicit) {
		String ontologyUri = ontologyInfo.getUri();
		Orr.log("dispatchOntologyPanel:  ontologyUri=" +ontologyUri);

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
		Orr.log("dispatchTermPanel:  entityUri=" +entityUri);

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

	/**
	 * Dispatchs interface to register an external ontology.
	 */
	public void startRegisterExternal() {
		History.newItem(PortalConsts.T_REGISTER_EXTERNAL);
	}

	/** Starts the sequence to register an external ontology.
	 */
	private void dispatchUploadOntology() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || loginResult.getError() != null ) {
			pendingMessage = "Please, sign in and then select \"Upload\"" +
				" to register an external ontology."
			;
			History.newItem(PortalConsts.T_BROWSE);
			return;
		}

		// TODO actual parameters
		RegisterNewWizard wizard = new RegisterNewWizard(this);

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
		BaseOntologyInfo ontologyInfo = ontologyPanel.getOntologyInfo();

		if ( ontologyInfo instanceof RegisteredOntologyInfo ) {
			// this should be the normal case.

			RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
			OntologyData ontologyData = roi.getOntologyData();
			if ( ontologyData instanceof OtherOntologyData ) {
				dispatchUploadNewVersionOntology(roi);
				return;
			}

			// #203: Allow to upload file for new version instead of in-place editing
			if ( ! Window.confirm(
					"Do you want to do in-place editing of the contents of the ontology?\n" +
					"\n" +
					"Click OK to proceed with in-place editing of the contents and the metadata.\n" +
					"\n" +
					"Click Cancel to proceed with file upload and metadata editing (file upload is optional)")
			) {
				dispatchUploadNewVersionOntology(roi);
				return;
			}
		}

		// TODO: check if we ever get to this point in this method.

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


	/**
	 * Starts the sequence to register a new version of an ontology.
	 */
	private void dispatchUploadNewVersionOntology(RegisteredOntologyInfo roi) {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || loginResult.getError() != null ) {
			pendingMessage = "Please, sign in, browse to the desired ontology, " +
				"and then select \"Edit new version\" to register a new version."
			;
			History.newItem(PortalConsts.T_BROWSE);
			return;
		}

		HostingType hostingType = roi.getHostingType();

		Orr.log("PortalMainPanel.editNewVersion: Dispatching wizard to capture new version. " +
				"hostingType = " +hostingType);

		RegisterVersionWizard wizard = new RegisterVersionWizard(this, roi, hostingType);

		pctrl.setOntologyInfo(roi);
		pctrl.setOntologyPanel(null);

		interfaceType = InterfaceType.UPLOAD_NEW_VERSION;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);

	    bodyPanel.clear();
		bodyPanel.add(wizard.getWidget());

	}


	public  void reviewAndRegister(OntologyPanel ontologyPanel) {
		if ( ontologyPanel != null ) {
			ontologyPanel.reviewAndRegister();
		}
	}


	public void cancelEdit(OntologyPanel ontologyPanel) {
		Orr.log("#315 cancelEdit: returningToOngoingEdit=" +returningToOngoingEdit);

		// this is a "cancel" request *not* via the browser history, so
		// unset this flag to avoid double confirmation
		returningToOngoingEdit = false;

		switch ( interfaceType ) {
			case ONTOLOGY_EDIT_NEW_VERSION:
			case ONTOLOGY_EDIT_NEW:
			case UPLOAD_ONTOLOGY:
			case UPLOAD_NEW_VERSION:
				if ( ! Window.confirm(ABANDON_CONFIRMATION) ) {
					return;
				}
				break;
		}

		switch ( interfaceType ) {
			case ONTOLOGY_EDIT_NEW_VERSION:
				interfaceType = InterfaceType.ONTOLOGY_VIEW;
			    controlsPanel.showMenuBar(interfaceType);
			    headerPanel.updateLinks(interfaceType);
			    if ( ontologyPanel != null ) {
			    	ontologyPanel.cancel();
			    	ontologyPanel.updateInterface(interfaceType);
			    }
				break;

			case ONTOLOGY_EDIT_NEW:
			case UPLOAD_ONTOLOGY:
				interfaceType = InterfaceType.BROWSE;
				History.newItem(PortalConsts.T_BROWSE);
				// TODO remove the following
//			    controlsPanel.showMenuBar(interfaceType);
//			    headerPanel.updateLinks(interfaceType);
//			    bodyPanel.clear();
//			    bodyPanel.add(browsePanel);
				break;

			case UPLOAD_NEW_VERSION:
				if ( pctrl.getOntologyInfo() instanceof RegisteredOntologyInfo ) {
					RegisteredOntologyInfo roi = (RegisteredOntologyInfo) pctrl.getOntologyInfo();
					String uri = roi.getUri();
					if ( uri.equals(History.getToken()) ){
						resolveUri(uri);
					}
					else {
						History.newItem(uri);
					}
				}
				else {
					History.newItem(PortalConsts.T_BROWSE);
				}
				break;

			default:
				// shouldn't happen. just return;
				Orr.log("#315 cancelEdit: shouldn't happen interfaceType=" +interfaceType);
		}
	}

	public void completedRegisterOntologyResult(RegisterOntologyResult registerOntologyResult) {
		dispatchMainPanel(true, "");
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
	 * panel, ontology or term.
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
				Orr.log("resolveUri <" +uri+ ">: call completed.");

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
	    bodyPanel.add(new HTML("<i>Resolving " +uri+ " ...</i>"));

		Orr.log("resolveUri: " +uri);
		Orr.service.resolveUri(uri, callback);
	}


	void searchOntologies(String searchString, Command doneCmd) {
		browsePanel.searchOntologies(searchString, doneCmd);
	}


	private void dispatchAdmin() {

		OntologyPanel ontologyPanel = pctrl.getOntologyPanel();
		if ( ontologyPanel != null ) {
			ontologyPanel.cancel();
			pctrl.setOntologyInfo(null);
			pctrl.setOntologyPanel(null);
		}

		AdminPanel adminPanel = new AdminPanel();

		interfaceType = InterfaceType.ADMIN;
	    controlsPanel.showMenuBar(interfaceType);
	    headerPanel.updateLinks(interfaceType);

	    bodyPanel.clear();
		bodyPanel.add(adminPanel);
	}


	public void unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo roi) {
		if ( Window.confirm(
				"Are you sure you want to unregister this ontology?\n" +
				"\n" +
				"Ontology URI: " +roi.getUri()+ "\n" +
				"Version: " +roi.getVersionNumber()+ "\n" +
				"\n" +
				"Click OK to proceed with the removal (this cannot be undone!)\n" +
		"\n")
		) {
			_doUnregisterOntology(loginResult, roi);
		}
	}

	private void _doUnregisterOntology(final LoginResult loginResult, final RegisteredOntologyInfo oi) {

		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setSize("600", "150");
		popup.getTextArea().setText("please wait ...");
		PortalControl.getInstance().notifyActivity(true);
		popup.setText("Unregistering ontology ...");
		popup.center();
		popup.show();

		AsyncCallback<UnregisterOntologyResult> callback = new AsyncCallback<UnregisterOntologyResult>() {
			public void onFailure(Throwable thr) {
				PortalControl.getInstance().notifyActivity(false);
				Window.alert(thr.toString());
			}

			public void onSuccess(UnregisterOntologyResult result) {
				Orr.log("UnregisterOntologyResult obtained: " +result);
				PortalControl.getInstance().notifyActivity(false);
				unregistrationCompleted(popup, result);
			}
		};

		Orr.log("Calling service unregisterOntology ...");
		Orr.service.unregisterOntology(loginResult, oi, callback);
	}

	private void unregistrationCompleted(MyDialog registrationPopup, final UnregisterOntologyResult unregisterOntologyResult) {

		registrationPopup.hide();

		String error = unregisterOntologyResult.getError();

		StringBuffer sb = new StringBuffer();

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(6);

		if ( error == null ) {

			String uri = unregisterOntologyResult.getUri();
			String versionNumber = unregisterOntologyResult.getVersionNumber();

			vp.add(new HTML("The ontology has been unregistered. <br/>\n" +
					"<br/>\n" +
					"Ontology URI: " +uri+ "<br/>\n" +
					"Version: " +versionNumber+ "<br/>\n" +
					"\n"
			));

			vp.add(new HTML("<br/>For diagnostics, this is the response from the back-end server:"));

			sb.append(unregisterOntologyResult.getInfo());
		}
		else {
			sb.append(error);
		}

		String msg = sb.toString();
		Orr.log("Unregistration result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setCloseButtonText("Return to ontology list");
		popup.setText(error == null ? "Unregistration completed" : "Error");
		popup.addTextArea(null).setText(msg);
		popup.getTextArea().setSize("600", "150");

		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.center();

		popup.addPopupListener(new PopupListener() {
			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				_completedUnregisterOntology(unregisterOntologyResult);
			}
		});
		popup.show();
	}

	private void _completedUnregisterOntology(UnregisterOntologyResult unregisterOntologyResult) {
		dispatchMainPanel(true, "");
		History.newItem(PortalConsts.T_BROWSE);
	}

    public void markTestingOntology(LoginResult loginResult, RegisteredOntologyInfo roi) {
        boolean isTesting = OrrUtil.isTestingOntology(roi);
        String msg = isTesting
            ? "remove the 'testing' mark for this ontology?"
            : "mark this ontology as 'testing'?";

        if (Window.confirm(
            "Are you sure you want to " + msg + "\n" +
                "\n" +
                "Ontology URI: " + roi.getUri() + "\n" +
                "Version: " + roi.getVersionNumber() + "\n" +
                "\n")
            ) {
            _doMarkTestingOntology(loginResult, roi, !isTesting);

        }

    }

    private void _doMarkTestingOntology(final LoginResult loginResult, final RegisteredOntologyInfo oi,
                                        final boolean markTesting) {

        PortalControl.getInstance().notifyActivity(true);
        AsyncCallback<String> callback = new AsyncCallback<String>() {
            public void onFailure(Throwable thr) {
                PortalControl.getInstance().notifyActivity(false);
                Window.alert(thr.toString());
            }

            public void onSuccess(String result) {
                Orr.log("markTestingOntology returned: " + result);
                PortalControl.getInstance().notifyActivity(false);
                Window.alert(result);
                _completedMarkTestingOntology();
            }
        };

        Orr.log("Calling service markTestingOntology ...");
        Orr.service.markTestingOntology(loginResult, oi, markTesting, callback);

    }

    private void _completedMarkTestingOntology() {
        dispatchMainPanel(true, "");
        History.newItem(PortalConsts.T_BROWSE);
    }

}
