package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for {@link VineService}.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface VineServiceAsync {

	void getAllOntologies(AsyncCallback<List<OntologyInfo>> callback);
	
	void getEntities(OntologyInfo ontologyInfo, AsyncCallback<OntologyInfo> callback);
	
	void performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms,
			AsyncCallback<String> callback);
}
