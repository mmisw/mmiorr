package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.img.OntMdImageBundle;
import org.mmisw.ontmd.gwt.client.rpc.BaseInfo;
import org.mmisw.ontmd.gwt.client.rpc.OntMdService;
import org.mmisw.ontmd.gwt.client.rpc.OntMdServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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

	public static final String APP_NAME = "ontmd";
	public static final String VERSION = "0.1.alpha3";
	public static final String VERSION_COMMENT = "";

	static String baseUrl;

	static OntMdImageBundle images = (OntMdImageBundle) GWT
			.create(OntMdImageBundle.class);
	
	static BaseInfo baseInfo;

	private static boolean includeLog;

	static OntMdServiceAsync ontmdService;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		if ( false ) { // disabled for now.
			prepareHistory(); // TODO: proper history handling
		}
		
		log("Util.getLocationProtocol() = " + Util.getLocationProtocol());
		log("Util.getLocationHost()     = " + Util.getLocationHost());
		log("GWT.getHostPageBaseURL()   = " + GWT.getHostPageBaseURL());
		log("GWT.getModuleBaseURL()     = " + GWT.getModuleBaseURL());
		log("GWT.getModuleName()        = " + GWT.getModuleName());
		baseUrl = Util.getLocationProtocol() + "//" + Util.getLocationHost();
		baseUrl = baseUrl.replace("/+$", ""); // remove trailing slashes
		log("baseUrl = " + baseUrl);

		Map<String, String> params = Util.getParams();

		if (params != null) {
			String _log = (String) params.get("_log");
			if (_log != null) {
				includeLog = true;
				params.remove("_log");
			}
		}

		getOntMdService();
		getBaseInfo(params);
	}

	private void prepareHistory() {
		History.newItem("");
		History.newItem("app");
		History.addHistoryListener(new HistoryListener() {
			public void onHistoryChanged(String historyToken) {
				// get to the initial token?
				if ( "".equals(historyToken) ) {
					if ( Window.confirm("Do you want to leave this OntMd session?") ) {
						History.back();
					}
					else {
						History.forward();				
					}
				}
				log("onHistoryChanged: " +historyToken);
			}
		});

	}

	private void startGui(final Map<String, String> params) {
		MainPanel mainPanel = new MainPanel(params);

		Element loadingElement = DOM.getElementById("loading");
		if ( loadingElement != null ) {
			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
		}
		
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
		
		panel.add(
				Util.createHtml(APP_NAME + " " + VERSION + " "
						+ VERSION_COMMENT + "<br/><br/>", 10));

	}

	private static void getOntMdService() {
		String moduleRelativeURL = GWT.getModuleBaseURL() + "ontmdService";
		log("Getting " + moduleRelativeURL + " ...");
		ontmdService = (OntMdServiceAsync) GWT.create(OntMdService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) ontmdService;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		log("   ontmdService " + ontmdService);
	}

	
	private void getBaseInfo(final Map<String, String> params) {
		AsyncCallback<BaseInfo> callback = new AsyncCallback<BaseInfo>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(BaseInfo bInfo) {
				baseInfo = bInfo;
				startGui(params);
			}
		};

		log("Getting base info ...");
		ontmdService.getBaseInfo(callback);
	}

	// always write to this buffer, but show contents if includeLog is true
	private static final StringBuffer log = new StringBuffer();

	public static void log(String msg) {
		log.append(msg + "\n");
		GWT.log(msg, null);
	}

}
