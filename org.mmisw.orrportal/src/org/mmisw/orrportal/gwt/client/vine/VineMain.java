package org.mmisw.orrportal.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.vine.img.VineImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The entry point for the Web VINE application.
 * 
 * @author Carlos Rueda
 */
public class VineMain {
	
	public String footer;
	
	public static VineImageBundle images = (VineImageBundle) GWT.create(VineImageBundle.class);


	// cached list of all ontologies
	// Map: URI -> BaseOntologyInfo
	private static final Map<String, BaseOntologyInfo> allUrisMap = new LinkedHashMap<String,BaseOntologyInfo>();
	
	// selected ontologies to work on:
	// the code for the i-th entry is code = 'A' + i
	private static final List<String> workingUris = new ArrayList<String>();
	
	
	private static List<RelationInfo> relInfos;
	private static final Map<String, RelationInfo> relInfoMap = new LinkedHashMap<String,RelationInfo>();
	
	
	
	public static List<BaseOntologyInfo> getAllUris() {
		return new ArrayList<BaseOntologyInfo>(allUrisMap.values());
	}

	/**
	 * Gets ontology info for the given URI.
	 * @param uri
	 * @return
	 */
	public static BaseOntologyInfo getOntologyInfo(String uri) {
		return allUrisMap.get(uri);
	}

	/**
	 * Resets the list of all ontologies available for mapping.
	 * @param allUris
	 */
	public static void setAllUris(List<RegisteredOntologyInfo> allUris) {
		allUrisMap.clear();
		for ( BaseOntologyInfo roi : allUris ) {
			allUrisMap.put(roi.getUri(), roi);
		}
	}

	/**
	 * Gets the selected ontologies to work on.
	 * the code for the i-th entry is code = 'A' + i
	 * 
	 * @return the list of working ontologies.
	 */
	public static List<String>  getWorkingUris() {
		return workingUris;
	}
	
	/**
	 * Gets the "coded" style for the given term.
	 * @param termUri
	 * @return
	 */
	static String getCodedTerm(String termUri) {
		int idx = VineMain.getWorkingUriIndex(termUri);
		if ( idx >= 0 ) {
			String namespace = VineMain.getWorkingUris().get(idx);
			char code = (char) ('A' + idx);
			return code+ ":" +termUri.substring(namespace.length());
		}
		return termUri;
	}

	/**
	 * Gets the "expanded" (not coded) style for the given term.
	 * @param termUri
	 * @return
	 */
	public static String getExpandedTerm(String termUri) {
		
		String[] toks = termUri.split(":", 2);
		if ( toks.length == 2 && toks[0].length() == 1 ) {
			char code = toks[0].charAt(0);
			int idx = code - 'A';
			String namespace = VineMain.getWorkingUris().get(idx);
			return namespace + toks[1];
		}
		else {
			return termUri;
		}
	}



	/**
	 * Adds a working URI, if not already in the list.
	 * @param uri
	 * @return The corresponding code.
	 */
	public static char addWorkingUri(String uri) {
		int idx = VineMain.workingUris.indexOf(uri);
		if ( idx < 0 ) {
			idx = VineMain.workingUris.size();
			VineMain.workingUris.add(uri);
		}
		char code = (char) ('A' + idx);
		return code;
	}

	public static boolean containsWorkingUri(String uri) {
		return workingUris.indexOf(uri) >= 0 ;
	}

	/**
	 * Called to notify the load of an ontology. Note that even if it was just to load
	 * the contents of an entry already in the list of all ontologies, this should
	 * be called to use the returned object reference (which may be different after the
	 * RPC call).
	 * 
	 * @param roi
	 */
	public static void ontologySucessfullyLoaded(BaseOntologyInfo roi) {
		allUrisMap.put(roi.getUri(), roi);
	}
	
	
	public static Map<String, RelationInfo> getRelInfoMap() {
		return relInfoMap;
	}

	/**
	 * Gets the list of default Vine relations. This list is retrieved from the back-end on demand
	 * and only once.
	 * 
	 * @param callback Called to provide the retrieved list.
	 */
	public static void getDefaultVineRelationInfos(final AsyncCallback<List<RelationInfo>> callback) {
		if ( VineMain.relInfos != null ) {
			callback.onSuccess(VineMain.relInfos);
			return;
		}
		
		AsyncCallback<List<RelationInfo>> myCallback = new AsyncCallback<List<RelationInfo>>() {
			public void onFailure(Throwable thr) {
				Orr.log("getRelationInfos: ERROR: " +thr.getMessage());
				callback.onFailure(thr);
			}

			public void onSuccess(List<RelationInfo> relInfos) {
				VineMain.relInfos = relInfos;
				Orr.log("getRelationInfos: retrieved " +relInfos.size()+ " relations");
				relInfoMap.clear();
				for ( RelationInfo relInfo : relInfos ) {
					relInfoMap.put(relInfo.getUri(), relInfo);
				}
				callback.onSuccess(VineMain.relInfos);
			}
		};

		Orr.log("Getting default Vine relations ...");
		Orr.service.getDefaultVineRelationInfos(myCallback);
	}

	
	public static void setWorkingUrisWithGivenNamespaces(Set<String> namespaces) {
		
		workingUris.clear();
		Orr.log("setWorkingUrisWithGivenNamespaces: " +namespaces);
		if ( namespaces != null ) {
			for ( String namespace : namespaces ) {
				addWorkingUri(namespace);
			}
		}
	}

	/**
	 * returns the index of the namespace associated with the given uri.
	 * @param uri
	 * @return
	 */
	public static int getWorkingUriIndex(String uri) {
		int idx = 0;
		for ( String workingUri : workingUris ) {
			if ( uri.indexOf(workingUri) == 0 ) {
				String localName = uri.substring(workingUri.length());
				if ( localName.length() > 0 && (localName.charAt(0) == '/' || localName.charAt(0) == '#') ) { 
					return idx;
				}
			}
			idx++;
		}
		return -1;
	}

}
