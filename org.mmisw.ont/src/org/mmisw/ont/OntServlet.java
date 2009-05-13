package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

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
	
	
	private final MiscDispatcher miscDispatcher = new MiscDispatcher(ontConfig, db);

	


	private final UriResolver uriResolver = new UriResolver(ontConfig, db, ontGraph);
	
	//
	// NOTE: Refactoring underway
	// I keep both instances of UriResolver and the new UriResolver2.
	// uriResolver2 is used only when the parameter "ur2" is included in the request
	private final UriResolver2 uriResolver2 = new UriResolver2(ontConfig, db, ontGraph);
	
	/**
	 * A request object.
	 */
	class Request {
		final ServletContext servletContext;
		final HttpServletRequest request; 
		final HttpServletResponse response;
		
		final List<String> userAgentList;
		
		final Accept accept;
		
		String outFormat;
		
		
		Request(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
			this.servletContext = servletContext;
			this.request = request;
			this.response = response;
			
			userAgentList = Util.getHeader(request, "user-agent");
			accept = new Accept(request);
			
			if ( log.isDebugEnabled() ) {
				String fullRequestedUri = request.getRequestURL().toString();
				List<String> pcList = Util.getHeader(request, "PC-Remote-Addr");
				log.debug("__Request: fullRequestedUri: " +fullRequestedUri);
				log.debug("                 user-agent: " +userAgentList);
				log.debug("             PC-Remote-Addr: " +pcList);
				log.debug("             Accept entries: " +accept.getEntries());
				log.debug("           Dominating entry: \"" +accept.dominating+ "\"");

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
	
	
	private void dispatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Request req = new Request(getServletContext(), request, response);
		
		// first, see if there are any testing requests to dispatch 
		
		// show request info?
		if ( Util.yes(req.request, "showreq")  ) {
			Util.showReq(req.request, req.response);
			return;
		} 
		
		// dispatch list of ontologies?
		if ( Util.yes(req.request, "list")  ) {
			miscDispatcher.listOntologies(req.request, req.response);
			return;
		}
		
		// dispatch list of vocabularies?
		if ( Util.yes(req.request, "vocabs")  ) {
			miscDispatcher.listVocabularies(req.request, req.response);
			return;
		}
		
		// dispatch list of mappings?
		if ( Util.yes(req.request, "mappings")  ) {
			miscDispatcher.listMappings(req.request, req.response);
			return;
		}
		
		// if the "_lpath" parameter is included, reply with full local path of ontology file
		// (this is just a quick way to help ontmd to so some of its stuff ;)
		if ( Util.yes(req.request, "_lpath") ) {
			miscDispatcher.resolveGetLocalPath(req.request, req.response);
			return;
		}
		
		// if the "_csv" parameter is included, reply with contents of associated CSV file
		// (this is just a quick way to help ontmd to so some of its stuff ;)
		if ( Util.yes(req.request, "_csv") ) {
			miscDispatcher.resolveGetCsv(req.request, req.response);
			return;
		}
		
		// if the "_versions" parameter is included, reply with a list of the available
		// version associated with the req.request
		if ( Util.yes(req.request, "_versions") ) {
			miscDispatcher.resolveGetVersions(req.request, req.response);
			return;
		}
		
		// if the "_debug" parameter is included, show some info about the URI parse
		// and the ontology from the database (but do not serve the contents)
		if ( Util.yes(req.request, "_debug") ) {
			miscDispatcher.resolveUriDebug(req.request, req.response);
			return;
		}

		// reload graph?
		if ( Util.yes(req.request, "_reload")  ) {
			ontGraph.reinit();
			return;
		}
		
		// dispatch a db-query?
		if ( Util.yes(req.request, "dbquery")  ) {
			Util.doDbQuery(req.request, req.response, db);
			return;
		}
		
		
		
		//////////////////////////////////////////////////////////////////////
		// now, main dispatcher:

		if ( Util.yes(req.request, "ur2")  ) {
			uriResolver2.service(req);
		}
		else {
			uriResolver.service(req);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dispatch(request, response);
	}
	
	/**
	 * The main dispatcher.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dispatch(request, response);
	}
	
	
	/**
	 * Gets the output format according to the given MmiUri and other request parameters.
	 * @param req
	 * @param mmiUri
	 * @param log
	 */
	static void getOutFormatForMmiUri(Request req, MmiUri mmiUri, Log log) {
		// The response type depends (initially) on the following elements:
		String extension = mmiUri.getExtension();
		req.outFormat = Util.getParam(req.request, "form", "");
		
		// NOTE: I use this 'outFormat' variable to handle the extension of the topic as well as the
		// optional parameter "form".  This parameter, if given, takes precedence over the extension.
		
		if ( log.isDebugEnabled() ) {
			log.debug("===getOutFormatForMmiUri ====== ");
			log.debug("===extension = \"" +extension+ "\"");
			log.debug("===form = \"" +req.outFormat+ "\"");
		}

		// prepare 'outFormat' according to "form" parameter (if given) and file extension:
		if ( req.outFormat.length() == 0 ) {
			// no "form" parameter given. Ok, use the variable to hold the extension
			// without any leading dots:
			req.outFormat = extension.replaceAll("^\\.+", "");
		}
		else {
			// "form" parameter given. Use it regardless of file extension.
			if ( log.isDebugEnabled() && extension.length() > 0 ) {
				log.debug("form param (=" +req.outFormat+ ") will take precedence over file extension: " +extension);
			}
		}
		
		assert !req.outFormat.startsWith(".");
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using outFormat = " +req.outFormat+ " for format resolution");
		}
	}

	/**
	 * Gets the output format for a NON MmiUri request, so, only based on the "form" parameter.
	 * @param req
	 * @param log
	 */
	static void getOutFormatForNonMmiUri(Request req, Log log) {
		req.outFormat = Util.getParam(req.request, "form", "");
		
		if ( log.isDebugEnabled() ) {
			log.debug("===getOutFormatForNonMmiUri ====== ");
			log.debug("===form = \"" +req.outFormat+ "\"");
		}
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

	/** 
	 * Gets the serialization of a model in the given language.
	 * <p>
	 * (Similar to JenaUtil.getOntModelAsString(OntModel model).) 
	 */
	static StringReader serializeModel(Model model, String lang) {
		StringWriter sw = new StringWriter();
		String uriForEmptyPrefix = model.getNsPrefixURI("");
		RDFWriter writer = model.getWriter(lang);
		String baseUri = null;
		if ( uriForEmptyPrefix != null ) {
			baseUri = JenaUtil2.removeTrailingFragment(uriForEmptyPrefix);
			writer.setProperty("xmlbase", baseUri);
		}
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document");
		writer.setProperty("tab", "4");
		writer.write(model, sw, baseUri);

		StringReader reader = new StringReader(sw.toString());
		return reader;
	}



}
