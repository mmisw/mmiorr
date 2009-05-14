package org.mmisw.iserver.gwt.client;

import org.mmisw.iserver.gwt.client.rpc.IServerService;
import org.mmisw.iserver.gwt.client.rpc.IServerServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * The entry point for iserver.
 * 
 * @author Carlos Rueda
 */
public class IServer implements EntryPoint {

	private static IServerServiceAsync _service;

	public void onModuleLoad() {
		getIServerService();
	}

	public static IServerServiceAsync getIServerService() {
		if ( _service == null ) {
			String moduleRelativeURL = GWT.getModuleBaseURL() + "iserverService";
			_service = (IServerServiceAsync) GWT.create(IServerService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) _service;
			endpoint.setServiceEntryPoint(moduleRelativeURL);
		}
		
		return _service;
	}

}
