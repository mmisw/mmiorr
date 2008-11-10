package org.mmisw.ontmd.gwt.client.voc2rdf.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface Voc2RdfService extends RemoteService {

	Voc2RdfBaseInfo getBaseInfo();
	
	ConversionResult convert(Map<String,String> values);
	
}
