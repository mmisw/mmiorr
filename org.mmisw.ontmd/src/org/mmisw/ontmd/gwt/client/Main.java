package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.img.OntMdImageBundle;
import org.mmisw.ontmd.gwt.client.rpc.AppInfo;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntMdServiceAsync;
import org.mmisw.ontmd.gwt.client.voc2rdf.Voc2Rdf;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
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
	
	static BaseInfo baseInfo;

	private static boolean includeLog;

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
				includeLog = true;
				params.remove("_log");
			}
			
			if ( ! launchVoc2rdf ) {
				String voc2rdf = (String) params.get("voc2rdf");
				if (voc2rdf != null) {
					launchVoc2rdf = true;
					params.remove("voc2rdf");
				}
			}
		}
		
		getOntMdService();
		
		if ( launchVoc2rdf ) {
			Voc2Rdf voc2Rdf = new Voc2Rdf();
			voc2Rdf.launch(this, params);
		}
		else {
			getAppInfo(params);
		}
	}

	public void startGui(final Map<String, String> params, Widget mainPanel) {

		Panel panel = new VerticalPanel();
		RootPanel.get().add(panel);
		Grid hpanel = new Grid(1, 1);
		hpanel.getCellFormatter().setAlignment(0,0, HasHorizontalAlignment.ALIGN_CENTER,
				HasVerticalAlignment.ALIGN_MIDDLE);
		panel.add(hpanel);
		
		hpanel.setWidget(0, 0, mainPanel);

		if (includeLog) {
			final HTML logLabel = Util.createHtml("", 10);
			ButtonBase buttonLog = Util.createButton("Refresh Log",
					"Refresh log info", new ClickListener() {
						public void onClick(Widget sender) {
							logLabel.setHTML("<pre>" + log.toString()
									+ "</pre>");
						}
					});
			ButtonBase buttonClear = Util.createButton("Clear Log",
					"Clear log info", new ClickListener() {
						public void onClick(Widget sender) {
							log.setLength(0);
							logLabel.setHTML("");
						}
					});
			panel.add(buttonLog);
			panel.add(buttonClear);
			panel.add(logLabel);
		} else {
			log.setLength(0);
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

		log("Getting application info ...");
		ontmdService.getAppInfo(callback);
	}

	
	private void getBaseInfo(final Map<String, String> params) {
		AsyncCallback<BaseInfo> callback = new AsyncCallback<BaseInfo>() {
			public void onFailure(Throwable thr) {
				removeLoadingMessage();
				String error = thr.toString();
				while ( ( thr = thr.getCause()) != null ) {
					error += "\n" + thr.toString();
				}
				RootPanel.get().add(new Label(error));
			}

			public void onSuccess(BaseInfo bInfo) {
				removeLoadingMessage();
				String error = bInfo.getError();
				if ( error != null ) {
					RootPanel.get().add(new Label(error));
				}
				else {
					baseInfo = bInfo;
					Widget mainPanel = new MainPanel(params);
					startGui(params, mainPanel);
				}
			}
		};

		log("Getting base info ...");
		ontmdService.getBaseInfo(params, callback);
	}

	public static void refreshOptions(AttrDef attr, AsyncCallback<AttrDef> callback) {
		assert attr.getOptionsVocabulary() != null ;
		// refresh options
		log("Getting base info ...");
		ontmdService.refreshOptions(attr, callback);
	}
    

	// always write to this buffer, but show contents if includeLog is true
	private static final StringBuffer log = new StringBuffer();

	public static void log(String msg) {
		log.append(msg + "\n");
		GWT.log(msg, null);
	}

	private void removeLoadingMessage() {
    	Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
    }

}
