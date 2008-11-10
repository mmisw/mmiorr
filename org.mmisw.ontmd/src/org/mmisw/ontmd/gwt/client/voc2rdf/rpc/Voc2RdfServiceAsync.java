package org.mmisw.ontmd.gwt.client.voc2rdf.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface for Voc2RdfService
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface Voc2RdfServiceAsync {

	void getBaseInfo(AsyncCallback<Voc2RdfBaseInfo> callback);
	
	void convert(Map<String,String> values, AsyncCallback<ConversionResult> callback);
	
}
