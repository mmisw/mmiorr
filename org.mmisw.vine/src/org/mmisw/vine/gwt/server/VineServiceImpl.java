package org.mmisw.vine.gwt.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	
	public List<String> getAllOntologies() {
		return onts;
	}
	
	public String getOntology(String uri)  {
		// TODO
		return null;
	}


	static List<String> onts = Arrays.asList(new String[] {
			"http://marinemetadata.org/cf",
			"http://marinemetadata.org/gcmd",
			"http://marinemetadata.org/agu.owl",
	});


	public List<String> search(String text, List<String> uris) {
		// TODO 
		List<String> terms = new ArrayList<String>();
		for (String ont : onts ) {
			if ( ont.indexOf(text) >= 0 ) {
				terms.add(ont);
			}
		}
		return terms;
	}


	public String performMapping(List<String> leftTerms, int relationCode,
			List<String> rightTerms) {
		// TODO Auto-generated method stub
		return null;
	}

}
