package org.mmisw.ontmd.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.vine.img.VineImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The entry point for the Web VINE applications.
 * 
 * @author Carlos Rueda
 */
public class VineMain {
	
	public String footer;
	
	public static final String VERSION_COMMENT = 
		"NOTE: This is a preliminary, not yet operational Web VINE prototype. " +
		"<a target=_blank href=http://marinemetadata.org/vine>Click here</a> for current " +
		"information about VINE.";
	
	
	public static VineImageBundle images = (VineImageBundle) GWT.create(VineImageBundle.class);

	
	// cached list of all ontologies
	private static List<RegisteredOntologyInfo> allUris = new ArrayList<RegisteredOntologyInfo>();

	// Map: URI -> RegisteredOntologyInfo
	private static final Map<String, RegisteredOntologyInfo> allUrisMap = new LinkedHashMap<String,RegisteredOntologyInfo>();
	
	// selected ontologies to work on:
	// Map: code -> RegisteredOntologyInfo
	private static final Map<String, RegisteredOntologyInfo> workingUris = new LinkedHashMap<String,RegisteredOntologyInfo>();
	
	
	private static List<RelationInfo> relInfos;
	private static final Map<String, RelationInfo> relInfoMap = new LinkedHashMap<String,RelationInfo>();
	
	
	
	public static List<RegisteredOntologyInfo> getAllUris() {
		return allUris;
	}

	public static void setAllUris(List<RegisteredOntologyInfo> allUris) {
		VineMain.allUris = allUris;
		allUrisMap.clear();
		for ( RegisteredOntologyInfo roi : allUris ) {
			allUrisMap.put(roi.getUri(), roi);
		}
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

	
	
	public static Map<String, RelationInfo> getRelInfoMap() {
		return relInfoMap;
	}

	/**
	 * Gets the list of relations.
	 * @param callback
	 */
	public static void getRelationInfos(final AsyncCallback<List<RelationInfo>> callback) {
		if ( relInfos == null ) {
			AsyncCallback<List<RelationInfo>> myCallback = new AsyncCallback<List<RelationInfo>>() {
				public void onFailure(Throwable thr) {
					Main.log("getRelationInfos: ERROR: " +thr.getMessage());
					callback.onFailure(thr);
				}

				public void onSuccess(List<RelationInfo> relInfos) {
					VineMain.relInfos = relInfos;
					Main.log("getRelationInfos: retrieved " +relInfos.size()+ " relations");
					relInfoMap.clear();
					for ( final RelationInfo relInfo : relInfos ) {
						relInfoMap.put(relInfo.getUri(), relInfo);
					}
					callback.onSuccess(VineMain.relInfos);
				}
			};

			Main.log("Getting relations ...");
			Main.ontmdService.getVineRelationInfos(myCallback);
		}
		else {
			callback.onSuccess(VineMain.relInfos);
		}
	}

	
	public static void setWorkingUrisWithGivenNamespaces(Set<String> namespaces) {
		
		workingUris.clear();
		Main.log("setWorkingUrisWithGivenNamespaces: " +namespaces);
		if ( namespaces != null ) {
			for ( String namespace : namespaces ) {
				Main.log("   namespace: " +namespace);
				RegisteredOntologyInfo roi = allUrisMap.get(namespace);
				if ( roi != null ) {
					addWorkingUri(roi);
				}
				else {
					Main.log("   namespace: " +namespace+ " NOT FOUND");
				}
			}
		}
	}

}
