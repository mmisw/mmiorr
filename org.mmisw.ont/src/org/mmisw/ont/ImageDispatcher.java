package org.mmisw.ont;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Unfinished;


/**
 * Dispatches an image output.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
@Unfinished
public class ImageDispatcher {
	
	private final Log log = LogFactory.getLog(ImageDispatcher.class);
	
//	private OntConfig ontConfig;
//	private Db db;
	
	ImageDispatcher(OntConfig ontConfig, Db db) {
//		this.ontConfig = ontConfig;
//		this.db = db;
	}


	/**
	 * Dispatches a request for an "image" representation of the ontology.
	 * Only implemented for the graphviz "dot" format, so, instead of an image, the text
	 * of the dot specification is returned to the client.
	 * 
	 * @return true if outFormat equals "dot" ignoring case. false otherwise.
	 */
	boolean dispatch(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, boolean unversionedRequest, Ontology ontology, String outFormat) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("dispatch: " +mmiUri+ "  outFormat=" +outFormat);
		}
		
		if ( outFormat.equalsIgnoreCase("dot") ) {
//			DotGenerator dot = new  DotGenerator(ontology.ge)
			
			// TODO Implement!
			
			return true;
		}
		
		else {
			// this is not implemented yet
			return false;
		}

	}
	
}
