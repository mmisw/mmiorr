package org.mmisw.voc2rdf.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface Voc2RdfService extends RemoteService {

	BaseInfo getBaseInfo();
	
	ConversionResult convert(Map<String,String> values);
	
	UploadResult upload(ConversionResult result, Map<String,String> values);
	
}
