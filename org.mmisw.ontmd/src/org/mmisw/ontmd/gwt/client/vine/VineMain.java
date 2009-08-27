package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.vine.img.VineImageBundle;
import org.mmisw.ontmd.gwt.client.vine.util.Util;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * The entry point for the Web VINE applications.
 * 
 * @author Carlos Rueda
 */
public class VineMain implements EntryPoint {
	
	public String footer;
	
	public static final String VERSION_COMMENT = 
		"NOTE: This is a preliminary, not yet operational Web VINE prototype. " +
		"<a target=_blank href=http://marinemetadata.org/vine>Click here</a> for current " +
		"information about VINE.";
	
	
	private static String baseUrl;
	
	public static VineImageBundle images = (VineImageBundle) GWT.create(VineImageBundle.class);

//	private static AppInfo appInfo;
	
//	private static boolean includeLog;
	
	
//	static VineServiceAsync vineService;
	
	// cached list of all ontologies
	private static List<RegisteredOntologyInfo> allUris = new ArrayList<RegisteredOntologyInfo>();
	
	// selected ontologies to work on:
	// Map: code -> RegisteredOntologyInfo
	private static final Map<String, RegisteredOntologyInfo> workingUris = new LinkedHashMap<String,RegisteredOntologyInfo>();
	
	
	public static List<RegisteredOntologyInfo> getAllUris() {
		return allUris;
	}

	public static void setAllUris(List<RegisteredOntologyInfo> allUris) {
		VineMain.allUris = allUris;
	}

	public static Map<String, RegisteredOntologyInfo> getWorkingUris() {
		return workingUris;
	}

	public static void addWorkingUri(RegisteredOntologyInfo uri) {
		char code = (char) ((int) 'A' + VineMain.workingUris.size());
		uri.setCode(code);
		VineMain.workingUris.put(""+ code, uri);
	}

	public static boolean containsWorkingUri(RegisteredOntologyInfo uri) {
		return workingUris.get("" +uri.getCode()) != null;
	}


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
      Main.log("Util.getLocationProtocol() = " +Util.getLocationProtocol());
      Main.log("Util.getLocationHost()     = " +Util.getLocationHost());
      Main.log("GWT.getHostPageBaseURL()   = " +GWT.getHostPageBaseURL());
      Main.log("GWT.getModuleBaseURL()     = " +GWT.getModuleBaseURL());
      baseUrl = Util.getLocationProtocol() + "//" + Util.getLocationHost();
      baseUrl = baseUrl.replace("/+$", "");   // remove trailing slashes
      Main.log("baseUrl = " +baseUrl);
      
//      Map<String,String> params = Util.getParams();
//	  
//      if ( params != null ) {
//          String _log = (String) params.get("_log");
//          if ( _log != null ) {
//              includeLog = true;
//              params.remove("_log");
//          }
//          
//      }
      
//      getVineService();
//      getAppInfo(params);
  }
  
  
//  private void startGui(final Map<String,String> params) {
//
//	  MainPanel mainPanel = new MainPanel();
//	  HorizontalPanel hp = new HorizontalPanel();
//	  RootPanel.get().add(hp);
//	  hp.add(VineMain.images.vinealpha().createImage());
//	  hp.add(Util.createHtml("<br/>\n" +VERSION_COMMENT, 11));
//	  RootPanel.get().add(mainPanel);
//
//      if ( includeLog ) {
//          final HTML logLabel = Util.createHtml("", 10);
//          ButtonBase buttonLog = Util.createButton("Refresh Log",
//                  "Refresh log info", new ClickListener() {
//                      public void onClick(Widget sender) {
//                          logLabel.setHTML("<pre>" +log.toString()+ "</pre>");
//                      }
//                  });
//          ButtonBase buttonClear = Util.createButton("Clear Log",
//                  "Clear log info", new ClickListener() {
//                      public void onClick(Widget sender) {
//                          log.setLength(0);
//                          logLabel.setHTML("");
//                      }
//                  });
//          RootPanel.get().add(buttonLog);
//          RootPanel.get().add(buttonClear);
//          RootPanel.get().add(logLabel);    
//      }
//      else {
//          log.setLength(0);
//      }
//      RootPanel.get().add(Util.createHtml("<font color=\"gray\">" +footer+ "</font><br/><br/>", 10));
//  }
  
//  private static void getVineService() {
//	  String moduleRelativeURL = GWT.getModuleBaseURL() + "vineService";
//      log("Getting " +moduleRelativeURL+ " ...");
//      vineService = (VineServiceAsync) GWT.create(VineService.class);
//      ServiceDefTarget endpoint = (ServiceDefTarget) vineService;
//      endpoint.setServiceEntryPoint(moduleRelativeURL);
//      log("   vineService " +vineService);
//  }
  
  
  
//	private void getAppInfo(final Map<String, String> params) {
//		AsyncCallback<AppInfo> callback = new AsyncCallback<AppInfo>() {
//			public void onFailure(Throwable thr) {
//				removeLoadingMessage();
//				String error = thr.toString();
//				while ( ( thr = thr.getCause()) != null ) {
//					error += "\n" + thr.toString();
//				}
//				RootPanel.get().add(new Label(error));
//			}
//
//			public void onSuccess(AppInfo aInfo) {
//				appInfo = aInfo;
//				footer = appInfo.toString();
//				getAllOntologies(params);
//			}
//		};
//
//		log("Getting application info ...");
//		vineService.getAppInfo(callback);
//	}

  
  
//  private void getAllOntologies(final Map<String,String> params) {
//      AsyncCallback<List<RegisteredOntologyInfo>> callback = new AsyncCallback<List<RegisteredOntologyInfo>>() {
//          public void onFailure(Throwable thr) {
//        	  removeLoadingMessage();
//              RootPanel.get().add(new HTML(thr.toString()));
//          }
//
//		public void onSuccess(List<RegisteredOntologyInfo> ontUris) {
//			removeLoadingMessage();
//			VineMain.allUris = ontUris;
//			log("getAllOntologies: retrieved " +ontUris.size()+ " ontologies");
//			startGui(params);
//			
//		}
//      };
//
//      log("Getting ontologies ...");
//      vineService.getAllOntologies(callback);
//  }

  
//  // always write to this buffer, but show contents if includeLog is true
//  private static final StringBuffer log = new StringBuffer();
//  public static void log(String msg) {
//      log.append(msg+ "\n");
//      GWT.log(msg, null);
//  }

//	private void removeLoadingMessage() {
//    	Element loadingElement = DOM.getElementById("loading");
//		if ( loadingElement != null ) {
//			DOM.removeChild(RootPanel.getBodyElement(), loadingElement);
//		}
//    }

}
