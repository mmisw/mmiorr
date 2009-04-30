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
	
	private OntConfig ontConfig;
	private Db db;
	
	ImageDispatcher(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}


	/**
	 * Not implemented yet.
	 * @return always false.
	 */
	boolean dispatch(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, boolean unversionedRequest, Ontology ontology) 
	throws ServletException, IOException {
		
		// this is not implemented yet
		return false;

	}
	
}
