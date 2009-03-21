package org.mmisw.ontmd.gwt.client.voc2rdf.rpc;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.AppInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for Voc2RdfService
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
@Deprecated
public interface Voc2RdfServiceAsync {

	void getAppInfo(AsyncCallback<AppInfo> callback);
	
	void getBaseInfo(AsyncCallback<Voc2RdfBaseInfo> callback);
	
	void convert(Map<String,String> values, AsyncCallback<ConversionResult> callback);
	
}
