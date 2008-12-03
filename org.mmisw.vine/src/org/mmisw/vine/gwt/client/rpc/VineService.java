package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to get info from the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface VineService extends RemoteService {

	// TODO
	
	List<String> getAllOntologies();
	
	String getOntology(String uri);

	
	List<String> search(String text, List<String> uris);
	
	String performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms);
}
