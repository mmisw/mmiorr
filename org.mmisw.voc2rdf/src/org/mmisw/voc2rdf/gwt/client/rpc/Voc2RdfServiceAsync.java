package org.mmisw.voc2rdf.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for Voc2RdfService
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface Voc2RdfServiceAsync {

	void getBaseInfo(AsyncCallback<BaseInfo> callback);
	
	void convert(Map<String,String> values, AsyncCallback<ConversionResult> callback);
	
	void upload(ConversionResult result, Map<String,String> values, AsyncCallback<UploadResult> callback);
	
}
