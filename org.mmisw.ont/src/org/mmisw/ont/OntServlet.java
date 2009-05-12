package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Util;

/**
 * The entry point.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class OntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static final String TITLE = "MMI Ontology and Term URI Resolver";
	
	private static String VERSION = "?";   // determined at init() time -- see build.xml and version.properties
	static String FULL_TITLE = TITLE + ". Version " +VERSION;



	private final Log log = LogFactory.getLog(OntServlet.class);
	
	private final OntConfig ontConfig = new OntConfig();
	private final Db db = new Db(ontConfig);
	private final OntGraph ontGraph = new OntGraph(ontConfig, db);

	private final UriResolver uriResolver = new UriResolver(ontConfig, db, ontGraph);
	
	
	/**
	 * A request object.
	 */
	class Request {
		final ServletContext servletContext;
		final HttpServletRequest request; 
		final HttpServletResponse response;
		
		final List<String> userAgentList;
		
		Request(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
			this.servletContext = servletContext;
			this.request = request;
			this.response = response;
			
			userAgentList = Util.getHeader(request, "user-agent");
			
			if ( log.isDebugEnabled() ) {
				String fullRequestedUri = request.getRequestURL().toString();
				List<String> pcList = Util.getHeader(request, "PC-Remote-Addr");
				log.debug("___ doGet: fullRequestedUri: " +fullRequestedUri);
				log.debug("                 user-agent: " +userAgentList);
				log.debug("             PC-Remote-Addr: " +pcList);
				
				// filter out Googlebot?
				if ( false ) {   // Disabled as the robots.txt is now active.
					for ( String ua: userAgentList ) {
						if ( ua.matches(".*Googlebot.*") ) {
							log.debug("returning NO_CONTENT to googlebot");
							try {
								response.sendError(HttpServletResponse.SC_NO_CONTENT);
							}
							catch (IOException ignore) {
							}
							return;
						}
					}
				}
			}
			

		}
	}
	
	/**
	 * Initializes this service.
	 * This basically consists of
	 * retrieval of configuration parameters, 
	 * initialization of the database connection, 
	 * loading of the ontology graph,
	 * and retrieval of version information.
	 */
	public void init() throws ServletException {
		log.info("init");
		
		log.info(TITLE+ ": initializing");
		
		try {
			ServletConfig servletConfig = getServletConfig();
			ontConfig.init(servletConfig);
			
			VERSION = ontConfig.getProperty(OntConfig.Prop.VERSION)+ " (" +
			          ontConfig.getProperty(OntConfig.Prop.BUILD)  + ")";
			FULL_TITLE = TITLE + ". Version " +VERSION;
			
			log.info(FULL_TITLE);

			db.init();
			ontGraph.init();
		} 
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}

	}
	
	public void destroy() {
		log.info(FULL_TITLE+ ": destroy called.\n\n");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request req = new Request(getServletContext(), request, response);
		uriResolver.service(req);
	}
	
	/**
	 * The main dispatcher.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request req = new Request(getServletContext(), request, response);
		uriResolver.service(req);
	}
	
	
	
	/**
	 * Gets the full path to get to the uploaded ontology file.
	 * @param ontology
	 * @return
	 */
	static File getFullPath(Ontology ontology, OntConfig ontConfig, Log log) {
		String full_path = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY) 
			+ "/" +ontology.file_path + "/" + ontology.filename;
		
		File file = new File(full_path);
		
		if ( ! file.canRead() ) {
			if ( full_path.toLowerCase().endsWith(".owl") ) {
				// try without ".owl":
				full_path = full_path.substring(0, full_path.length() - 4);
			}
			else {
				// Note: the following is a quick workaround for submissions whose URI don't have
				// the .owl extension (the general rule, btw), but whose uploaded files do. 
				// I had to add the .owl extension in ontmd for the aquaportal parsing jobs to work.
				// try with ".owl":
				full_path += ".owl";
			}
			if ( log.isDebugEnabled() ) {
				log.debug("TRYING: " +full_path);
			}
			file = new File(full_path);
		}

		return file;
	}


}
