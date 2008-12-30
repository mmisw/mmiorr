package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for VineService
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface VineServiceAsync {

	// TODO

	void getAllOntologies(AsyncCallback<List<OntologyInfo>> callback);
	
	void getOntology(String uri, AsyncCallback<String> callback);
	
	void search(String text, List<OntologyInfo> uris, AsyncCallback<List<String>> callback);
	
	void performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms,
			AsyncCallback<String> callback);
}
