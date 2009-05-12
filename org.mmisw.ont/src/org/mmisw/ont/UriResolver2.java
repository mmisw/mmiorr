package org.mmisw.ont;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is main dispatcher used by the entry point OntServlet.
 * 
 * <p>
 * Note: under development; this class is going to be a refactored version of UriResolver, which
 * will eventually disappear.
 * The strategy is to incrementally provide the central operations through this UriResolver2 class.
 * Some stuff will be moved out from UriResolver to OntServlet and some stuff refactored into
 * UriResolver2.
 * 
 * @author Carlos Rueda
 */
public class UriResolver2 {
	
	private final Log log = LogFactory.getLog(UriResolver2.class);
	
	private final OntConfig ontConfig;
	private final Db db;
	private final OntGraph ontGraph;

	public UriResolver2(OntConfig ontConfig, Db db, OntGraph ontGraph) {
		this.ontConfig = ontConfig;
		this.db = db;
		this.ontGraph = ontGraph;
	}
	
	/**
	 * The main dispatcher.
	 */
	void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}
