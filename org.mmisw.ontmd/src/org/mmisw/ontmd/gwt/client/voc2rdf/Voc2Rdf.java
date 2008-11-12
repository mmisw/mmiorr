package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfBaseInfo;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfService;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.Voc2RdfServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * The Voc2RDF application. 
 * 
 * @author Carlos Rueda
 */
public class Voc2Rdf {

	public static final String VOC2RDF_APP_NAME = "Voc2RDF";
	public static final String VOC2RDF_VERSION = "2.0.0.beta2";
	public static final String VOC2RDF_VERSION_COMMENT = "";

	public static final String footer = 
		VOC2RDF_APP_NAME + " " + VOC2RDF_VERSION + " " + VOC2RDF_VERSION_COMMENT;

	static String baseUrl;

	static Voc2RdfBaseInfo baseInfo;

	static Voc2RdfServiceAsync voc2rdfService;


	public static Voc2RdfServiceAsync getVoc2RdfService() {
		if ( voc2rdfService == null ) {
			String moduleRelativeURL = GWT.getModuleBaseURL() + "voc2rdfService";
			Main.log("Getting " + moduleRelativeURL + " ...");
			voc2rdfService = (Voc2RdfServiceAsync) GWT.create(Voc2RdfService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) voc2rdfService;
			endpoint.setServiceEntryPoint(moduleRelativeURL);
			Main.log("   voc2rdfService " + voc2rdfService);
		}
		return voc2rdfService;
	}

	private Main main;


	public void launch(Main main, Map<String, String> params) {
		this.main = main;
		getVoc2RdfService();
		getPrimaryConcepts(params);
	}

	private void getPrimaryConcepts(final Map<String, String> params) {
		AsyncCallback<Voc2RdfBaseInfo> callback = new AsyncCallback<Voc2RdfBaseInfo>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(Voc2RdfBaseInfo bInfo) {
				baseInfo = bInfo;
				Voc2RdfMainPanel v2rMainPanel = new Voc2RdfMainPanel(params);
				main.startGui(params, v2rMainPanel);
			}
		};

		Main.log("Getting base info ...");
		voc2rdfService.getBaseInfo(callback);
	}


	
}
