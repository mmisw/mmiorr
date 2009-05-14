package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class PortalMainPanel extends VerticalPanel {

	
	private LoginResult loginResult;
	
	private List<OntologyInfo> ontologyInfos;
	
	
	PortalMainPanel(final Map<String, String> params, List<OntologyInfo> ontologyInfos) {
		super();
		
		this.ontologyInfos = ontologyInfos;
		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( true ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1002");
				loginResult.setUserName("carueda");
			}
		}
		
		
	    if ( loginResult == null && params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    }

		
	    HeaderPanel headerPanel = new HeaderPanel(params); 
	    OntologyTable ontologyTable = new OntologyTable(params, this.ontologyInfos);

	    headerPanel.setWidth("900");
		add(headerPanel);

		add(ontologyTable);
		
	}
	

}
