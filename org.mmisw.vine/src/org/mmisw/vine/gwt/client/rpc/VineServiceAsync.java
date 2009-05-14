package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for {@link VineService}.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface VineServiceAsync {

	void getAppInfo(AsyncCallback<AppInfo> callback);
	
	void getAllOntologies(AsyncCallback<List<OntologyInfo>> callback);
	
	void getEntities(OntologyInfo ontologyInfo, AsyncCallback<OntologyInfo> callback);
	
	
	void getRelationInfos(AsyncCallback<List<RelationInfo>> callback);
	
	// TODO
	void performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms,
			AsyncCallback<String> callback);
}
