package org.mmisw.vine.gwt.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mmisw.vine.core.Util;
import org.mmisw.vine.gwt.client.rpc.OntologyInfo;
import org.mmisw.vine.gwt.client.rpc.VineService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of VineService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class VineServiceImpl extends RemoteServiceServlet implements VineService {
	private static final long serialVersionUID = 1L;
	
	private static final String ONT = "http://mmisw.org/ont";
	private static final String VOCABS = ONT + "?vocabs";
//	private static final String MAPPINGS = ONT + "?mappings";

	
	private static List<OntologyInfo> onts;


	public List<OntologyInfo> getAllOntologies() {
		onts = new ArrayList<OntologyInfo>();
		
		String uri = VOCABS;
		System.out.println("getAsString. uri= " +uri);
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
		System.out.println("getEntities starting");
		Util.getEntities(ontologyInfo);
		System.out.println("getEntities done");
		return ontologyInfo;
	}


	public String performMapping(List<String> leftTerms, int relationCode,
			List<String> rightTerms) {
		// TODO performMapping not implemented yet
		return null;
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
