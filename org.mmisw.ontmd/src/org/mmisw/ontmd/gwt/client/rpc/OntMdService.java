package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OntMdService extends RemoteService {

	BaseInfo getBaseInfo();
	
	LoginResult login(String userName, String userPassword);
	
	OntologyInfo getOntologyInfo(String uploadResults);
	
	ReviewResult review(OntologyInfo ontologyInfo, LoginResult loginResult);
	
	UploadResult upload(ReviewResult reviewResult, LoginResult loginResult);
	
}
