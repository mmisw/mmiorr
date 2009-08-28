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
	// Map: URI -> RegisteredOntologyInfo
	private static final Map<String, RegisteredOntologyInfo> allUrisMap = new LinkedHashMap<String,RegisteredOntologyInfo>();
	
	// selected ontologies to work on:
	// the code for the i-th entry is code = 'A' + i
	private static final List<String> workingUris = new ArrayList<String>();
	
	
	private static List<RelationInfo> relInfos;
	private static final Map<String, RelationInfo> relInfoMap = new LinkedHashMap<String,RelationInfo>();
	
	
	
	public static List<RegisteredOntologyInfo> getAllUris() {
		return new ArrayList<RegisteredOntologyInfo>(allUrisMap.values());
	}

	public static RegisteredOntologyInfo getRegisteredOntologyInfo(String uri) {
		return allUrisMap.get(uri);
	}

	public static void setAllUris(List<RegisteredOntologyInfo> allUris) {
		allUrisMap.clear();
		for ( RegisteredOntologyInfo roi : allUris ) {
			allUrisMap.put(roi.getUri(), roi);
		}
	}

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
	 * Adds a working URI.
	 * @param uri
	 * @return The corresponding code.
	 */
	public static char addWorkingUri(String uri) {
		char code = (char) ((int) 'A' + VineMain.workingUris.size());
		VineMain.workingUris.add(uri);
		return code;
	}

	public static boolean containsWorkingUri(RegisteredOntologyInfo roi) {
		return workingUris.indexOf(roi.getUri()) >= 0 ;
	}

	/**
	 * Called to notify the load of an ontology. Note that even if it was just to load
	 * the contents of an entry already in the list of all ontologies, this should
	 * be called to use the returned object reference (which may be different after the
	 * RPC call).
	 * 
	 * @param roi
	 */
	public static void ontologySucessfullyLoaded(RegisteredOntologyInfo roi) {
		allUrisMap.put(roi.getUri(), roi);
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
				addWorkingUri(namespace);
			}
		}
	}

	public static int getWorkingUriIndex(String uri) {
		int idx = 0;
		for ( String workingUri : workingUris ) {
			if ( uri.indexOf(workingUri) == 0 ) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

}
