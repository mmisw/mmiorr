package org.mmisw.ont;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The entry point.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class OntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(OntServlet.class);
	
	
	private final UriResolver uriResolver = new UriResolver();
	
	
	public void init() throws ServletException {
		log.info("init");
		uriResolver.init(getServletConfig());
	}
	
	public void destroy() {
		log.info("destroy");
		uriResolver.destroy();
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		uriResolver.doPost(request, response);
	}
	
	/**
	 * The main dispatcher.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		uriResolver.doGet(request, response);
	}
}
