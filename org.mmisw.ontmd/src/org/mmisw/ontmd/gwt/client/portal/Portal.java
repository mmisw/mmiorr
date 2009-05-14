package org.mmisw.ontmd.gwt.client.portal;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.rpc.PortalBaseInfo;

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

	static String baseUrl;

	public static AppInfo appInfo = new AppInfo("MMI Portal");
	public static PortalBaseInfo baseInfo;


	private Main main;


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
				startApplication(params);
			}
		};

		Main.log("Getting application info ...");
		Main.ontmdService.getPortalAppInfo(callback);
	}

	private void startApplication(final Map<String, String> params) {
		AsyncCallback<PortalBaseInfo> callback = new AsyncCallback<PortalBaseInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(PortalBaseInfo bInfo) {
				removeLoadingMessage();
				baseInfo = bInfo;
				PortalMainPanel portalMainPanel = new PortalMainPanel(params);
				main.startGui(params, portalMainPanel);
			}
		};

		Main.log("Getting base info ...");
		Main.ontmdService.getPortalBaseInfo(callback);
	}


	private void removeLoadingMessage() {
    	Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
    }

}
