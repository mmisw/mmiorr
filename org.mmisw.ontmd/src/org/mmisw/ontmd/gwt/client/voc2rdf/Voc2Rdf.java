package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * The Voc2RDF application. 
 * 
 * @author Carlos Rueda
 */
public class Voc2Rdf {

	static String baseUrl;

	public static AppInfo appInfo = new AppInfo("MMI Voc2RDF");
	public static Voc2RdfBaseInfo baseInfo;

//	static Voc2RdfServiceAsync voc2rdfService;


//	private static Voc2RdfServiceAsync getVoc2RdfService() {
//		if ( voc2rdfService == null ) {
//			String moduleRelativeURL = GWT.getModuleBaseURL() + "voc2rdfService";
//			Main.log("Getting " + moduleRelativeURL + " ...");
//			voc2rdfService = (Voc2RdfServiceAsync) GWT.create(Voc2RdfService.class);
//			ServiceDefTarget endpoint = (ServiceDefTarget) voc2rdfService;
//			endpoint.setServiceEntryPoint(moduleRelativeURL);
//			Main.log("   voc2rdfService " + voc2rdfService);
//		}
//		return voc2rdfService;
//	}

	private Main main;


	public void launch(Main main, Map<String, String> params) {
		this.main = main;
//		getVoc2RdfService();
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
				getPrimaryConcepts(params);
			}
		};

		Main.log("Getting application info ...");
//		voc2rdfService.getAppInfo(callback);
		Main.ontmdService.getVoc2RdfAppInfo(callback);
	}

	private void getPrimaryConcepts(final Map<String, String> params) {
		AsyncCallback<Voc2RdfBaseInfo> callback = new AsyncCallback<Voc2RdfBaseInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(Voc2RdfBaseInfo bInfo) {
				removeLoadingMessage();
				baseInfo = bInfo;
				Voc2RdfMainPanel v2rMainPanel = new Voc2RdfMainPanel(params);
				main.startGui(params, v2rMainPanel);
			}
		};

		Main.log("Getting base info ...");
//		voc2rdfService.getBaseInfo(callback);
		Main.ontmdService.getVoc2RdfBaseInfo(callback);
	}


	private void removeLoadingMessage() {
    	Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
    }

}
