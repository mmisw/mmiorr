package org.mmisw.voc2rdf.gwt.client;

import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.img.Voc2RdfImageBundle;
import org.mmisw.voc2rdf.gwt.client.rpc.BaseInfo;
import org.mmisw.voc2rdf.gwt.client.rpc.Voc2RdfService;
import org.mmisw.voc2rdf.gwt.client.rpc.Voc2RdfServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint {

	public static final String APP_NAME = "Voc2RDF";
	public static final String VERSION = "0.1.0";
	public static final String VERSION_COMMENT = "- ";

	static String baseUrl;

	static Voc2RdfImageBundle images = (Voc2RdfImageBundle) GWT
			.create(Voc2RdfImageBundle.class);
	
	static BaseInfo baseInfo;

	private static boolean includeLog;

	static Voc2RdfServiceAsync voc2rdfService;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		log("Util.getLocationProtocol() = " + Util.getLocationProtocol());
		log("Util.getLocationHost()     = " + Util.getLocationHost());
		log("GWT.getHostPageBaseURL()   = " + GWT.getHostPageBaseURL());
		log("GWT.getModuleBaseURL()     = " + GWT.getModuleBaseURL());
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

		getVoc2RdfService();
		getPrimaryConcepts(params);
	}

	private void startGui(final Map<String, String> params) {
		MainPanel mainPanel = new MainPanel(params);

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

	private static void getVoc2RdfService() {
		String moduleRelativeURL = GWT.getModuleBaseURL() + "voc2rdfService";
		log("Getting " + moduleRelativeURL + " ...");
		voc2rdfService = (Voc2RdfServiceAsync) GWT.create(Voc2RdfService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) voc2rdfService;
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		log("   voc2rdfService " + voc2rdfService);
	}

	private void getPrimaryConcepts(final Map<String, String> params) {
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
		voc2rdfService.getBaseInfo(callback);
	}

	// always write to this buffer, but show contents if includeLog is true
	private static final StringBuffer log = new StringBuffer();

	public static void log(String msg) {
		log.append(msg + "\n");
		GWT.log(msg, null);
	}

}
