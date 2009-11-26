package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.CookieMan;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.CookieMan.UserInfo;
import org.mmisw.ontmd.gwt.client.rpc.PortalBaseInfo;
import org.mmisw.ontmd.gwt.client.vine.VineMain;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * The Portal application. 
 * 
 * @author Carlos Rueda
 */
public class Portal {

	// for getAllOntologies
	boolean includePriorVersions = true;


	static String baseUrl;

	public static AppInfo appInfo = new AppInfo("MMI Portal");
	public static PortalBaseInfo portalBaseInfo;

	private Main main;

	
	private List<RegisteredOntologyInfo> ontologyInfos;
	

	public Portal() {
		PortalControl.getInstance().setPortal(this);
	}
	
	public void launch(Main main, Map<String, String> params) {
		this.main = main;
		getAppInfo(params);
	}

	private void getAppInfo(final Map<String, String> params) {
		AsyncCallback<AppInfo> callback = new AsyncCallback<AppInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(AppInfo aInfo) {
				appInfo = aInfo;
				Main.log("Getting application info: " +appInfo);
				main.footer = appInfo.toString();
				getMetadataBaseInfo(params);
			}
		};

		Main.log("Portal.getAppInfo: Getting application info ...");
		Main.ontmdService.getPortalAppInfo(callback);
	}
	
	private void getMetadataBaseInfo(final Map<String, String> params) {
		boolean includeVersion = params != null && "y".equals(params.get("_xv"));
		
		Main.log("Portal.getMetadataBaseInfo: includeVersion= " +includeVersion+ " ...");
		
		AsyncCallback<MetadataBaseInfo> callback = new AsyncCallback<MetadataBaseInfo>() {

			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(MetadataBaseInfo result) {
				Main.setMetadataBaseInfo(result);
				getAllOntologies(params);
			}
			
		};
		Main.ontmdService.getMetadataBaseInfo(includeVersion, callback);
	}
	
	private void getAllOntologies(final Map<String, String> params) {
		
		Main.log("Portal.getAllOntologies, includePriorVersions= " +includePriorVersions+ " ...");
		AsyncCallback<List<RegisteredOntologyInfo>> callback = new AsyncCallback<List<RegisteredOntologyInfo>>() {

			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(List<RegisteredOntologyInfo> result) {
				ontologyInfos = result;
				VineMain.setAllUris(ontologyInfos);
				startApplication(params);
			}
			
		};
		Main.ontmdService.getAllOntologies(includePriorVersions, callback );
	}
	
	private void startApplication(final Map<String, String> params) {
		AsyncCallback<PortalBaseInfo> callback = new AsyncCallback<PortalBaseInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(PortalBaseInfo bInfo) {
				removeLoadingMessage();
				portalBaseInfo = bInfo;
				Main.log("PortalBaseInfo: Ont service = " +portalBaseInfo.getOntServiceUrl());
				PortalMainPanel portalMainPanel = new PortalMainPanel(params, ontologyInfos);
				
				UserInfo userInfo = CookieMan.getUserInfo();
				if ( userInfo != null && userInfo.getPassword() != null ) {
					loginAndStartGui(userInfo, params, portalMainPanel);
				}
				else {
					main.startGui(params, portalMainPanel);
					portalMainPanel.dispatch();
				}
			}
		};

		Main.log("Portal.getPortalBaseInfo ...");
		Main.ontmdService.getPortalBaseInfo(callback);
	}


	/**
	 * Attempts to login the user stored in the cookie and then starts the GUI.
	 */
	private void loginAndStartGui(final UserInfo userInfo, final Map<String, String> params, final PortalMainPanel portalMainPanel) {

		AsyncCallback<LoginResult> callback = new AsyncCallback<LoginResult>() {

			public void onFailure(Throwable ex) {
				String error = ex.getMessage();
				Main.log("login error: " +error);
				Main.log("Continuing with startGui");
				main.startGui(params, portalMainPanel);
				portalMainPanel.dispatch();
			}

			public void onSuccess(LoginResult loginResult) {
				if ( loginResult.getError() != null ) {
					Main.log("login error: " +loginResult.getError());
					Main.log("Continuing with startGui");
					loginResult = null;
				}
				else {
					Main.log("login ok: " +userInfo.getUsername());
					CookieMan.setUserInfo(userInfo);
				}
				PortalControl.getInstance().loginOk(loginResult);
				main.startGui(params, portalMainPanel);
				portalMainPanel.dispatch();
			}
			
		};
		Main.log("authenticating remembered user in this computer...");
		Main.ontmdService.authenticateUser(userInfo.getUsername(), userInfo.getPassword(), callback);
	}

	void refreshListAllOntologies() {
		
		Main.log("Portal.refreshListAllOntologies, includePriorVersions= " +includePriorVersions+ " ...");
		AsyncCallback<List<RegisteredOntologyInfo>> callback = new AsyncCallback<List<RegisteredOntologyInfo>>() {

			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(List<RegisteredOntologyInfo> result) {
				ontologyInfos = result;
				PortalControl.getInstance().refreshedListAllOntologies(ontologyInfos);
			}
			
		};
		Main.ontmdService.getAllOntologies(includePriorVersions, callback );
	}
	

	private void removeLoadingMessage() {
    	Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
    }

}
