package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.img.OntMdImageBundle;
import org.mmisw.ontmd.gwt.client.metadata.MainPanel;
import org.mmisw.ontmd.gwt.client.portal.Portal;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntMdServiceAsync;
import org.mmisw.ontmd.gwt.client.util.Util;
import org.mmisw.ontmd.gwt.client.voc2rdf.Voc2Rdf;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * The entry point.
 * 
 * @author Carlos Rueda
 */
public class Main implements EntryPoint {

	public String footer; 

	static String baseUrl;

	public static OntMdImageBundle images = (OntMdImageBundle) GWT
			.create(OntMdImageBundle.class);
	
	
	static AppInfo appInfo;
	
	public static MetadataBaseInfo metadataBaseInfo;

	// buffer for logging in the client interface; used only if a certain parameter is given
	private static StringBuffer logBuffer;


	public static OntMdServiceAsync ontmdService;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		log("Util.getLocationProtocol() = " + Util.getLocationProtocol());
		log("Util.getLocationHost()     = " + Util.getLocationHost());
		log("GWT.getHostPageBaseURL()   = " + GWT.getHostPageBaseURL());
		log("GWT.getModuleBaseURL()     = " + GWT.getModuleBaseURL());
		log("GWT.getModuleName()        = " + GWT.getModuleName());
		baseUrl = Util.getLocationProtocol() + "//" + Util.getLocationHost();
		baseUrl = baseUrl.replace("/+$", ""); // remove trailing slashes
		log("baseUrl = " + baseUrl);

		// launch portal?
//		boolean launchPortal = GWT.getHostPageBaseURL().endsWith("/portal/");
		// Note: now, always launch the portal
		boolean launchPortal = true;

		//
		// TODO Remove the following mechanisms to launch the old versions of the
		// other tools.
		//
		
		// launch Voc2RDF?
		boolean launchVoc2rdf = GWT.getHostPageBaseURL().endsWith("/voc2rdf/");
		
		Map<String, String> params = Util.getParams();

		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( false ) {
				// test the "include version" option:
				params.put("_xv", "y");
			}
		}
		
		
		if (params != null) {
			String _log = (String) params.get("_log");
			if (_log != null) {
				logBuffer = new StringBuffer();
				params.remove("_log");
			}
			
			// temporary: if param _ontmd=y is passed, then launch old ontmd, not the portal
			String _ontmd = (String) params.get("_ontmd");
			if (_ontmd != null) {
				launchPortal = false;
				params.remove("_ontmd");
			}
			
		}
		
		getOntMdService();
		
		if ( launchPortal ) {
			Portal portal = new Portal();
			portal.launch(this, params);
		}
		else if ( launchVoc2rdf ) {
			Voc2Rdf voc2Rdf = new Voc2Rdf();
			voc2Rdf.launch(this, params);
		}
		else {
			getAppInfo(params);
		}
	}

	public void startGui(final Map<String, String> params, Widget mainPanel) {

		VerticalPanel panel = new VerticalPanel();
//		panel.setBorderWidth(1);
		panel.setWidth("100%");
		RootPanel.get().add(panel);

		HorizontalPanel hpanel = new HorizontalPanel();
		panel.add(hpanel);
		hpanel.setWidth("100%");
		hpanel.add(mainPanel);

		if ( logBuffer != null ) {
			final HTML logLabel = Util.createHtml("", 10);
			ButtonBase buttonLog = Util.createButton("Refresh Log",
					"Refresh log info", new ClickListener() {
						public void onClick(Widget sender) {
							logLabel.setHTML("<pre>" + logBuffer.toString()
									+ "</pre>");
						}
					});
			ButtonBase buttonClear = Util.createButton("Clear Log",
					"Clear log info", new ClickListener() {
						public void onClick(Widget sender) {
							logBuffer.setLength(0);
							logLabel.setHTML("");
						}
					});
			panel.add(buttonLog);
			panel.add(buttonClear);
			panel.add(logLabel);
		}
		
		panel.add(Util.createHtml("<font color=\"gray\">" +footer+ "</font><br/><br/>", 10));

	}

	private static void getOntMdService() {
		String moduleRelativeURL = GWT.getModuleBaseURL() + "ontmdService";
		log("Getting " + moduleRelativeURL + " ...");
		ontmdService = (OntMdServiceAsync) GWT.create(OntMdService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) ontmdService;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		log("   ontmdService " + ontmdService);
	}

	
	private void getAppInfo(final Map<String, String> params) {
		AsyncCallback<AppInfo> callback = new AsyncCallback<AppInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				String error = thr.toString();
				while ( ( thr = thr.getCause()) != null ) {
					error += "\n" + thr.toString();
				}
				RootPanel.get().add(new Label(error));
			}

			public void onSuccess(AppInfo aInfo) {
				appInfo = aInfo;
				footer = appInfo.toString();
				getBaseInfo(params);
			}
		};

		log("Main.getAppInfo: Getting application info ...");
		ontmdService.getAppInfo(callback);
	}

	
	private void getBaseInfo(final Map<String, String> params) {
		AsyncCallback<MetadataBaseInfo> callback = new AsyncCallback<MetadataBaseInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				String error = thr.toString();
				while ( ( thr = thr.getCause()) != null ) {
					error += "\n" + thr.toString();
				}
				RootPanel.get().add(new Label(error));
			}

			public void onSuccess(MetadataBaseInfo bInfo) {
				removeLoadingMessage();
				String error = bInfo.getError();
				if ( error != null ) {
					RootPanel.get().add(new Label(error));
				}
				else {
					metadataBaseInfo = bInfo;
					Widget mainPanel = new MainPanel(params);
					startGui(params, mainPanel);
				}
			}
		};

		log("Main.Getting base info ...");
		ontmdService.getBaseInfo(params, callback);
	}

	public static void refreshOptions(AttrDef attr, AsyncCallback<AttrDef> callback) {
		assert attr.getOptionsVocabulary() != null ;
		// refresh options
		log("Getting base info ...");
		ontmdService.refreshOptions(attr, callback);
	}
    

	public static void log(String msg) {
		if ( logBuffer != null ) {
			logBuffer.append(msg + "\n");
		}
		GWT.log(msg, null);
	}

	private void removeLoadingMessage() {
    	Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
    }

}
