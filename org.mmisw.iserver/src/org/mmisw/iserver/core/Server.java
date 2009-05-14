package org.mmisw.iserver.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;



/**
 * Implementation of IServerService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Server implements IServer {
	private static final long serialVersionUID = 1L;
	
	private static final String ONT = "http://mmisw.org/ont";
	private static final String VOCABS = ONT + "?vocabs";
//	private static final String MAPPINGS = ONT + "?mappings";

	private static List<OntologyInfo> onts;

	
	private final AppInfo appInfo = new AppInfo("MMISW IServer");
	private final Log log = LogFactory.getLog(Server.class);
	
	
	private static IServer _instance ;
	
	public static IServer getInstance() {
		if ( _instance == null ) {
			_instance = new Server();
		}
		
		return _instance;
	}
	
	private Server() {
		log.info("initializing " +appInfo.getAppName()+ "...");
		try {
			Config.getInstance();
			
			appInfo.setVersion(
					Config.Prop.VERSION.getValue()+ " (" +
						Config.Prop.BUILD.getValue()  + ")"
			);
					
			log.info(appInfo.toString());
			
		}
		catch (Throwable ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			// TODO throw ServletException
			// NOTE: apparently this happens because getServletConfig fails in hosted mode (GWT 1.5.2). 
			// Normally we should throw a Servlet exception as the following: 
//			throw new ServletException("Cannot initialize", ex);
			// but, I'm ignoring it as this is currently only for version information.
		}
	}
	
	public void destroy() {
		log.info(appInfo+ ": destroy called.\n\n");
	}
	
	public AppInfo getAppInfo() {
		return appInfo;
	}
	

	
	/**
	 * 
	 */
	public List<OntologyInfo> getAllOntologies() {
		onts = new ArrayList<OntologyInfo>();
		
		String uri = VOCABS;
		if ( log.isDebugEnabled() ) {
			log.debug("getAsString. uri= " +uri);
		}
		String response;
		try {
			response = getAsString(uri, Integer.MAX_VALUE);
		}
		catch (Exception e) {
			// TODO A notification mechanism (like in ontmd) for exceptions
			e.printStackTrace();
			return onts;
		}
		String[] lines = response.split("\n|\r\n|\r");
		for ( String line : lines ) {
			String[] toks = line.split("\\s*,\\s*");
			OntologyInfo ontologyInfo = new OntologyInfo();
			ontologyInfo.setUri(toks[0]);
			ontologyInfo.setDisplayLabel(toks[1]);
			onts.add(ontologyInfo);
		}
		
		return onts;
	}
	
	public OntologyInfo getEntities(OntologyInfo ontologyInfo) {
		if ( log.isDebugEnabled() ) {
			log.debug("getEntities starting");
		}
		Util.getEntities(ontologyInfo);
		return ontologyInfo;
	}


	private static String getAsString(String uri, int maxlen) throws Exception {
		HttpClient client = new HttpClient();
	    GetMethod meth = new GetMethod(uri);
	    try {
	        client.executeMethod(meth);

	        if (meth.getStatusCode() == HttpStatus.SC_OK) {
	            return meth.getResponseBodyAsString(maxlen);
	        }
	        else {
	          throw new Exception("Unexpected failure: " + meth.getStatusLine().toString());
	        }
	    }
	    finally {
	        meth.releaseConnection();
	    }
	}

}
