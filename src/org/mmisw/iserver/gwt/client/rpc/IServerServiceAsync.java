package org.mmisw.iserver.gwt.client.rpc;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for {@link IServerService}.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface IServerServiceAsync {

	void getAppInfo(AsyncCallback<AppInfo> callback);
	
	void getAllOntologies(AsyncCallback<List<OntologyInfo>> callback);
	
	void getEntities(OntologyInfo ontologyInfo, AsyncCallback<OntologyInfo> callback);
	
	
	void getRelationInfos(AsyncCallback<List<RelationInfo>> callback);
	
	// TODO
	void performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms,
			AsyncCallback<String> callback);
}
