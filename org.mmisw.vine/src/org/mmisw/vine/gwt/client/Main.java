package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mmisw.vine.gwt.client.img.VineImageBundle;
import org.mmisw.vine.gwt.client.rpc.VineService;
import org.mmisw.vine.gwt.client.rpc.VineServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint {
	
	public static final String APP_NAME = "Vine";
	public static final String VERSION = "0.1.pre1";
	public static final String VERSION_COMMENT = "- Please note: this is just a preliminary, " +
			"non-operational Web user interface. <a href=http://marinemetadata.org/vine>Click here</a> " +
			"for current information about Vine.";
	
	public static final String GET_USERS = "bioportal/rest/users";
	
	
	static String baseUrl;
	
	static VineImageBundle images = (VineImageBundle) GWT.create(VineImageBundle.class);

	private static boolean includeLog;
	
	
	static VineServiceAsync vineService;
	
	// cached list of all ontologies
	static List<String> allUris = new ArrayList<String>();
	
	// selected ontologies to work on:
	static List<String> workingUris = new ArrayList<String>();
	

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
      
      if ( true || 
    		  GWT.isScript() ) {
    	  // ie, actually running on the server.
    	  getVineService();
    	  getAllOntologies(params);
      }
      else { 
    	  // running in hosted mode. Skip the vineService for now.
    	  allUris = new ArrayList<String>();
    	  allUris.add("http://marinemetadata.org/cf");
    	  allUris.add("http://marinemetadata.org/gcmd");
    	  startGui(params); 
      }
  }
  
  
  private void startGui(final Map<String,String> params) {

	  RootPanel.get().add(Util.createHtml(APP_NAME+ " " +VERSION+ " " +VERSION_COMMENT+ "<br/><br/>", 10));
	  MainPanel mainPanel = new MainPanel();
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
      AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
          public void onFailure(Throwable thr) {
              RootPanel.get().add(new HTML(thr.toString()));
          }

		public void onSuccess(List<String> ontUris) {
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
