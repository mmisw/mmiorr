package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.vine.gwt.client.img.VineImageBundle;
import org.mmisw.vine.gwt.client.rpc.OntologyInfo;
import org.mmisw.vine.gwt.client.rpc.VineService;
import org.mmisw.vine.gwt.client.rpc.VineServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint {
	
	public static final String APP_NAME = "VINE";
	public static final String VERSION = "0.1.pre1";
	public static final String VERSION_COMMENT = 
		"NOTE: This is preliminary, not yet operational prototype of a Web version for VINE. " +
		"<a target=_blank href=http://marinemetadata.org/vine>Click here</a> for current " +
		"information about VINE.";
	
	public static final String GET_USERS = "bioportal/rest/users";
	
	
	static String baseUrl;
	
	static VineImageBundle images = (VineImageBundle) GWT.create(VineImageBundle.class);

	private static boolean includeLog;
	
	
	static VineServiceAsync vineService;
	
	// cached list of all ontologies
	private static List<OntologyInfo> allUris = new ArrayList<OntologyInfo>();
	
	// selected ontologies to work on:
	// Map: code -> OntologyInfo
	private static final Map<String, OntologyInfo> workingUris = new LinkedHashMap<String,OntologyInfo>();
	
	
	public static List<OntologyInfo> getAllUris() {
		return allUris;
	}

	public static void setAllUris(List<OntologyInfo> allUris) {
		Main.allUris = allUris;
	}

	public static Map<String, OntologyInfo> getWorkingUris() {
		return workingUris;
	}

	public static void addWorkingUri(OntologyInfo uri) {
		char code = (char) ((int) 'A' + Main.workingUris.size());
		uri.setCode(code);
		Main.workingUris.put(""+ code, uri);
	}

	public static boolean containsWorkingUri(OntologyInfo uri) {
		return workingUris.get("" +uri.getCode()) != null;
	}


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
      log("Util.getLocationProtocol() = " +Util.getLocationProtocol());
      log("Util.getLocationHost()     = " +Util.getLocationHost());
      log("GWT.getHostPageBaseURL()   = " +GWT.getHostPageBaseURL());
      log("GWT.getModuleBaseURL()     = " +GWT.getModuleBaseURL());
      baseUrl = Util.getLocationProtocol() + "//" + Util.getLocationHost();
      baseUrl = baseUrl.replace("/+$", "");   // remove trailing slashes
      log("baseUrl = " +baseUrl);
      
      Map<String,String> params = Util.getParams();
	  
      if ( params != null ) {
          String _log = (String) params.get("_log");
          if ( _log != null ) {
              includeLog = true;
              params.remove("_log");
          }
          
      }
      
      getVineService();
      getAllOntologies(params);
  }
  
  
  private void startGui(final Map<String,String> params) {

	  MainPanel mainPanel = new MainPanel();
	  HorizontalPanel hp = new HorizontalPanel();
	  RootPanel.get().add(hp);
	  hp.add(Main.images.vine().createImage());
	  hp.add(Util.createHtml(APP_NAME+ " " +VERSION+ "<br/>\n" +VERSION_COMMENT, 10));
	  RootPanel.get().add(mainPanel);

      if ( includeLog ) {
          final HTML logLabel = Util.createHtml("", 10);
          ButtonBase buttonLog = Util.createButton("Refresh Log",
                  "Refresh log info", new ClickListener() {
                      public void onClick(Widget sender) {
                          logLabel.setHTML("<pre>" +log.toString()+ "</pre>");
                      }
                  });
          ButtonBase buttonClear = Util.createButton("Clear Log",
                  "Clear log info", new ClickListener() {
                      public void onClick(Widget sender) {
                          log.setLength(0);
                          logLabel.setHTML("");
                      }
                  });
          RootPanel.get().add(buttonLog);
          RootPanel.get().add(buttonClear);
          RootPanel.get().add(logLabel);    
      }
      else {
          log.setLength(0);
      }
  }
  
  private static void getVineService() {
	  String moduleRelativeURL = GWT.getModuleBaseURL() + "vineService";
      log("Getting " +moduleRelativeURL+ " ...");
      vineService = (VineServiceAsync) GWT.create(VineService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) vineService;
      endpoint.setServiceEntryPoint(moduleRelativeURL);
      log("   vineService " +vineService);
  }
  
  private void getAllOntologies(final Map<String,String> params) {
      AsyncCallback<List<OntologyInfo>> callback = new AsyncCallback<List<OntologyInfo>>() {
          public void onFailure(Throwable thr) {
              RootPanel.get().add(new HTML(thr.toString()));
          }

		public void onSuccess(List<OntologyInfo> ontUris) {
			Main.allUris = ontUris;
			log("getAllOntologies: retrieved " +ontUris.size()+ " ontologies");
			startGui(params);
			
		}
      };

      log("Getting ontologies ...");
      vineService.getAllOntologies(callback);
  }

  
  // always write to this buffer, but show contents if includeLog is true
  private static final StringBuffer log = new StringBuffer();
  public static void log(String msg) {
      log.append(msg+ "\n");
      GWT.log(msg, null);
  }

}
