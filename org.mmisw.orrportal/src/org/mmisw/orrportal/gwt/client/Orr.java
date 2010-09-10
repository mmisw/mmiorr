package org.mmisw.orrportal.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.AppInfo;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrportal.gwt.client.CookieMan.UserInfo;
import org.mmisw.orrportal.gwt.client.img.OrrPortalImageBundle;
import org.mmisw.orrportal.gwt.client.portal.PortalConsts;
import org.mmisw.orrportal.gwt.client.portal.PortalControl;
import org.mmisw.orrportal.gwt.client.portal.PortalMainPanel;
import org.mmisw.orrportal.gwt.client.rpc.OrrService;
import org.mmisw.orrportal.gwt.client.rpc.OrrServiceAsync;
import org.mmisw.orrportal.gwt.client.rpc.PortalBaseInfo;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * Main elements supporting the ORR Portal application.
 * 
 * @author Carlos Rueda
 */
public class Orr {
	
	/** Interface for asynchronous calls to the ORR back-end. */
	public static OrrServiceAsync service;

	public static OrrPortalImageBundle images = (OrrPortalImageBundle) GWT
				.create(OrrPortalImageBundle.class);

	/**
	 * Initialization method.
	 * 
	 * @param withLogging 
	 *                   true to include logging.
	 */
	static void init(boolean withLogging) {
		
		if ( withLogging ) {
			_logger = new OrrLogger();
		}
		
		Orr.log("Util.getLocationProtocol() = " + OrrUtil.getLocationProtocol()
		    + "\nUtil.getLocationHost()     = " + OrrUtil.getLocationHost()
		    + "\nGWT.getHostPageBaseURL()   = " + GWT.getHostPageBaseURL()
		    + "\nGWT.getModuleBaseURL()     = " + GWT.getModuleBaseURL()
		    + "\nGWT.getModuleName()        = " + GWT.getModuleName()
		);
//		String baseUrl = OrrUtil.getLocationProtocol() + "//" + OrrUtil.getLocationHost();
//		baseUrl = baseUrl.replace("/+$", ""); // remove trailing slashes
//		Orr.log("baseUrl = " + baseUrl);

		_getService();
	}

	
	/**
	 * Once initialized ({@link #init(boolean)}), call this method to launch the application.
	 * @param params
	 */
	static void launch(Map<String, String> params) {
		_getAppInfo(params);
	}

	/**
	 * (1)
	 */
	private static void _getAppInfo(final Map<String, String> params) {
		AsyncCallback<AppInfo> callback = new AsyncCallback<AppInfo>() {
			public void onFailure(Throwable thr) {
				OrrUtil.removeLoadingMessage();
				_addToMainPanel(new HTML(thr.toString()));
			}

			public void onSuccess(AppInfo aInfo) {
				appInfo = aInfo;
				footer = appInfo.toString();
				Orr.log("ORR: Got application info: " +appInfo);
				_getPortalBaseInfo(params);
			}
		};

		Orr.log("ORR: Getting application info ...");
		Orr.service.getAppInfo(callback);
	}

	
	/**
	 * (2)
	 */
	private static void _getPortalBaseInfo(final Map<String, String> params) {
		AsyncCallback<PortalBaseInfo> callback = new AsyncCallback<PortalBaseInfo>() {
			public void onFailure(Throwable thr) {
				OrrUtil.removeLoadingMessage();
				_addToMainPanel(new HTML(thr.toString()));
			}

			public void onSuccess(PortalBaseInfo bInfo) {
				portalBaseInfo = bInfo;
				Orr.log("ORR: Got PortalBaseInfo: Ont service = " +portalBaseInfo.getOntServiceUrl());
				_getMetadataBaseInfo(params);
			}
		};

		Orr.log("ORR: Getting PortalBaseInfo ...");
		Orr.service.getPortalBaseInfo(callback);
	}

	
	/**
	 * (3)
	 */
	private static void _getMetadataBaseInfo(final Map<String, String> params) {
		
		AsyncCallback<MetadataBaseInfo> callback = new AsyncCallback<MetadataBaseInfo>() {

			public void onFailure(Throwable thr) {
				OrrUtil.removeLoadingMessage();
				_addToMainPanel(new HTML(thr.toString()));
			}

			public void onSuccess(MetadataBaseInfo result) {
				Orr.log("ORR: Got MetadataBaseInfo");
				metadataBaseInfo  = result;
				
				UserInfo userInfo = CookieMan.getUserInfo();
				if ( userInfo != null && userInfo.getPassword() != null ) {
					_loginRememberedUser(userInfo, params);
				}
				else {
					_startGui(params, null);
				}
			}
		};
		
		boolean includeVersion = params != null && "y".equals(params.get("_xv"));
		Orr.log("ORR: Getting MetadataBaseInfo ... includeVersion= " +includeVersion+ " ...");
		Orr.service.getMetadataBaseInfo(includeVersion, callback);
	}
	
	
	/**
	 * (4)
	 */
	private static void _loginRememberedUser(final UserInfo userInfo, final Map<String, String> params) {

		AsyncCallback<LoginResult> callback = new AsyncCallback<LoginResult>() {

			public void onFailure(Throwable ex) {
				OrrUtil.removeLoadingMessage();
				String error = ex.getMessage();
				Orr.log("ORR: login error: " +error);
				Orr.log("ORR: Continuing with startGui");
				_startGui(params, null);
			}

			public void onSuccess(LoginResult loginResult) {
				if ( loginResult.getError() != null ) {
					Orr.log("ORR: login error: " +loginResult.getError());
					Orr.log("ORR: Continuing with startGui");
					loginResult = null;
				}
				else {
					Orr.log("ORR: login ok: " +loginResult.getUserName());
					CookieMan.setUserInfo(userInfo);
				}
				// now start the GUI:
				_startGui(params, loginResult);
			}
			
		};
		Orr.log("ORR: authenticating remembered user in this computer: " +userInfo.getUsername()+ " ...");
		Orr.service.authenticateUser(userInfo.getUsername(), userInfo.getPassword(), callback);
	}
	
	
	/**
	 * (5)
	 */
	private static void _startGui(final Map<String, String> params, LoginResult loginResult) {
		
		Orr.log("ORR: starting GUI.  loginResult=" +loginResult);
		
		PortalControl.getInstance().setLoginResult(loginResult);
		
		// TODO: remove this parameter once PortalMainPanel does not need it
		List<RegisteredOntologyInfo> ontologyInfos = new ArrayList<RegisteredOntologyInfo>();
		portalMainPanel = new PortalMainPanel(params, ontologyInfos);
		PortalControl.getInstance().setPortalMainPanel(portalMainPanel);

		// set the logingResult but do not trigger any updates:
		portalMainPanel.setLoginResult(loginResult);
		
		
		Widget fullPanel = _createFullPanel(portalMainPanel);
		OrrUtil.removeLoadingMessage();
		_addToMainPanel(fullPanel);
		
		
	    String historyToken = History.getToken();
	    if ( historyToken != null && historyToken.trim().length() > 0 ) {
			Orr.log("history token = " +historyToken);
	    	History.fireCurrentHistoryState();
	    }
	    else {
	    	History.newItem(PortalConsts.T_BROWSE);
	    }
	}

	
	public static void refreshListAllOntologies() {
		
		AsyncCallback<List<RegisteredOntologyInfo>> callback = new AsyncCallback<List<RegisteredOntologyInfo>>() {

			public void onFailure(Throwable thr) {
				_addToMainPanel(new HTML(thr.toString()));
			}

			public void onSuccess(List<RegisteredOntologyInfo> result) {
				ontologyInfos = result;
				Orr.log("ORR: Got list of registered ontologies: " +result.size());
				portalMainPanel.refreshedListAllOntologies(ontologyInfos);
			}
			
		};
		portalMainPanel.showRefreshingMessage();
		Orr.log("ORR: Getting list of registered ontologies ... includePriorVersions= " +includePriorVersions+ " ...");
		Orr.service.getAllOntologies(includePriorVersions, callback);
	}

	
	
	
	
	private static Widget _createFullPanel(PortalMainPanel portalMainPanel) {
		VerticalPanel panel = new VerticalPanel();
//		panel.setBorderWidth(1);
		panel.setWidth("100%");
		_addToMainPanel(panel);

		HorizontalPanel hpanel = new HorizontalPanel();
		panel.add(hpanel);
		hpanel.setWidth("100%");
		hpanel.add(portalMainPanel);

		Widget logWidget = Orr.getLogWidget();
		if ( logWidget != null ) {
			panel.add(logWidget);
		}
		
		panel.add(OrrUtil.createHtml("<font color=\"gray\">" +footer+ "</font><br/><br/>", 10));
		return panel;
	}


	/**
	 * Logs a message. This always calls GWT.log(msg, null) but also handles an ad hoc logging
	 * widget specially useful when the app is deployed.
	 * @param msg
	 */
	public static void log(String msg) {
		if ( _logger != null ) {
			_logger.log(msg);
		}
		GWT.log(msg, null);
	}

	/**
	 * Gets a widget with controls for the logging information.
	 * Returns null if {@link #init(boolean)} was called with no logging.
	 */
	static Widget getLogWidget() {
		return _logger == null ? null : _logger.getWidget();
	}
	
	public static void refreshOptions(AttrDef attr, AsyncCallback<AttrDef> callback) {
		assert attr.getOptionsVocabulary() != null ;
		// refresh options
		Orr.log("ORR: refreshing options of " +attr.getUri());
		Orr.service.refreshOptions(attr, callback);
	}
    
	
	public static MetadataBaseInfo getMetadataBaseInfo() {
		return metadataBaseInfo;
	}

	public static PortalBaseInfo getPortalBaseInfo() {
		return portalBaseInfo;
	}
	

	private static void _getService() {
		String moduleRelativeURL = GWT.getModuleBaseURL() + "orrService";
		Orr.log("ORR: Getting " + moduleRelativeURL + " ...");
		service = (OrrServiceAsync) GWT.create(OrrService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		Orr.log("ORR:    orrService " + service);
	}

	/**
	 * Adds a widget to the "main" element in the document.
	 */
	private static void _addToMainPanel(Widget w) {
		RootPanel.get("main").add(w);
		// OLD RootPanel.get().add(w);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	// private variables
	///////////////////////////////////////////////////////////////////////////////
	
	// buffer for logging in the client interface; used only if a certain parameter is given
	private static OrrLogger _logger;
	
	private static AppInfo appInfo = new AppInfo("MMI Portal");
	private static PortalBaseInfo portalBaseInfo;

	private static String footer; 
	
	private static MetadataBaseInfo metadataBaseInfo;
	
	private static PortalMainPanel portalMainPanel;
	
	
	// for getAllOntologies
	private static final boolean includePriorVersions = true;

	private static List<RegisteredOntologyInfo> ontologyInfos;

	
	// non-instanceable
	private Orr() {}
}
