package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.sparql.SparqlDispatcher;
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


	private final SparqlDispatcher sparqlDispatcher = new SparqlDispatcher(ontGraph);

	private final UriDispatcher uriDispatcher = new UriDispatcher(sparqlDispatcher);

	
	private final UriResolver uriResolver = new UriResolver(ontConfig, db, ontGraph);
	
	//
	// NOTE: Refactoring underway
	// I keep both instances of UriResolver and the new UriResolver2.
	// uriResolver2 is used only when the parameter "ur2" is included in the request
	// but also in other requests as I moved to it instead of original UriResolver.
	private final UriResolver2 uriResolver2 = new UriResolver2(ontConfig, db, ontGraph);
	
	/**
	 * A request object. It keep info associated with the request from the client.
	 */
	class Request {
		final ServletContext servletContext;
		final HttpServletRequest request; 
		final HttpServletResponse response;
		
		final List<String> userAgentList;
		
		final Accept accept;
		
		final String fullRequestedUri;
		final MmiUri mmiUri;
		final String outFormat;
		
		// in case the ontology info is obtained somehow, use it:
		Ontology ontology;
		
		// in case an explicit version is requested
		final String version;
		
		
		Request(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
			this.servletContext = servletContext;
			this.request = request;
			this.response = response;
			
			userAgentList = Util.getHeader(request, "user-agent");
			accept = new Accept(request);
			
			fullRequestedUri = request.getRequestURL().toString();
			
			String formParam = Util.getParam(request, "form", "");
			
			//////////////////////////////////////
			// get the requested MmiUri:
			
			MmiUri mmiUriTest = null;
			String outFormatTest;
			String versionTest = null;
			
			try {
				if ( Util.yes(request, "uri") ) {
					// when the "uri" parameter is passed, its value is used.
					
					String entityUri = Util.getParam(request, "uri", "");
					mmiUriTest = new MmiUri(entityUri);
				}
				else {
					mmiUriTest = new MmiUri(fullRequestedUri);
				}
				// We have an MmiUri request.
				
				// get output format to be used:
				outFormatTest = OntServlet.getOutFormatForMmiUri(formParam, accept, mmiUriTest, log);
			}
			catch (URISyntaxException e) {
				// NOT a regular MmiUri request.
				outFormatTest = OntServlet.getOutFormatForNonMmiUri(formParam, log); 
			}
			
			
			if ( outFormatTest.length() == 0 ) {     
				// No explicit outFormat.
				// use content negotiation:
				
				outFormatTest = getOutFormatByContentNegotiation(accept);
				
				log.debug("Not explicit output format given (either file extension or form parameter). " +
						"Using [" +outFormatTest+ "] by content negotiation."
				);
			}

			
			if ( Util.yes(request, "version") ) {
				// explicit version given:
				versionTest = Util.getParam(request, "version", null);
			}
			
			
			mmiUri = mmiUriTest;
			outFormat = outFormatTest;
			version = versionTest;

			if ( log.isDebugEnabled() ) {
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
			Util.showReq(this, req.request, req.response);
			return;
		} 
		
		// dispatch list of ontologies for iserver?
		if ( Util.yes(req.request, "listall")  ) {
			miscDispatcher.listAll(req.request, req.response);
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
		
		// if the "uri" parameter is included, resolve by the given URI
		if ( Util.yes(req.request, "uri") ) {
			_dispatchUri(req);
			return;
		}
		
		// dispatch a sparql-query?
		if ( Util.yes(req.request, "sparql")  ) {
			sparqlDispatcher.execute(req.request, req.response);
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
	 * Dispatches the URI indicated with the "uri" parameter in the request.
	 * If the uri corresponds to a stored ontology, then the ontology is resolved
	 * as it were a regular self-served ontology.
	 */
	private void _dispatchUri(Request req) 
	throws ServletException, IOException {
		// get (ontology or entity) URI from the parameter:
		String ontOrEntUri = Util.getParam(req.request, "uri", "");
		if ( ontOrEntUri.length() == 0 ) {
			req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing uri parameter");
			return;
		}
		
		Ontology ontology = null;
		
		// explicit version?
		if ( req.version != null ) {
			// 
			// Dispatch explicit version request.
			//
			log.debug("Explicit version requested: " +req.version);
			
			if ( req.mmiUri != null ) {
				if ( req.mmiUri.getVersion() != null ) {		
					//
					// Both, veriones URI and "version" parameter given.
					// Check that the two components are equal.
					//
					if ( req.version.equals(req.mmiUri.getVersion()) ) {
						ontology = db.getOntologyVersion(ontOrEntUri, req.version);
					}
					else {
						// versioned request AND explicit version parameter -> BAD REQUEST:
						req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
								"Versioned URI and \"version\" parameter requested simultaneously; both values must be equal.");
						return;
					}
				}
				else {
					// unversioned request.  Get most recent
					ontology = db.getMostRecentOntologyVersion(req.mmiUri);
				}
			}
			else {
				// possibly a re-hosted ontology.
				ontology = db.getOntologyVersion(ontOrEntUri, req.version);
			}		
		}
		else {
			// No explicit version.
			ontology = getRegisteredOntology(ontOrEntUri);
		}
		
		// see if the given URI corresponds to a registered ontology
		if ( ontology != null ) {
			//
			// yes, it's a stored ontology--dispatch as if it were a regular call to resolve the ontology
			
			if ( log.isDebugEnabled() ) {
				log.debug("dispatching "+ ontOrEntUri+ " as whole ontology (not entity)");
			}
			
			// use ontology member in req and make sure the Uri attribute is set
			req.ontology = ontology;
			req.ontology.setUri(ontOrEntUri);
			uriResolver2.serviceForOntology(req);
		}
		else {
			// dispatch entity URI (not complete ontology)
			uriDispatcher.dispatchUri(req.request, req.response);
		}
	}

	/**
	 * Gets a registered ontology
	 * 
	 * @param potentialOntUri. The URI that will be used to try to find a corresponding registered
	 *                     ontology.
	 * @return the ontology if found; null if not found.
	 * @throws ServletException
	 */
	Ontology getRegisteredOntology(String potentialOntUri) throws ServletException {
		Ontology ontology = null;
		try {
			MmiUri mmiUri = new MmiUri(potentialOntUri);
			if ( mmiUri.getVersion() == null ) {
				// unversioned request.  Get most recent
				ontology = db.getMostRecentOntologyVersion(mmiUri);
			}
			else {
				// versioned request. Just try to use the argument as given:
				ontology = db.getOntology(potentialOntUri);
			}
		}
		catch (URISyntaxException e) {
			// Not an MmiUri (likely a Re-hosted ontology). Just try to use the argument as given:
			ontology = db.getOntology(potentialOntUri);
		}
		
		return ontology;
	}

	
	/**
	 * Gets the output format according to the given MmiUri and other request parameters.
	 * @param req
	 * @param mmiUri
	 * @param log
	 */
	private static String getOutFormatForMmiUri(String formParam, Accept accept, MmiUri mmiUri, Log log) {
		// The response type depends (initially) on the following elements:
		String extension = mmiUri.getExtension();
		String outFormat = formParam;
		
		// NOTE: I use this 'outFormat' variable to handle the extension of the topic as well as the
		// optional parameter "form".  This parameter, if given, takes precedence over the extension.
		
		if ( log.isDebugEnabled() ) {
			log.debug("===getOutFormatForMmiUri ====== ");
			log.debug("===file extension = \"" +extension+ "\"");
			log.debug("===form parameter = \"" +outFormat+ "\"");
		}

		// prepare 'outFormat' according to "form" parameter (if given) and file extension:
		if ( outFormat.length() == 0 ) {
			// no "form" parameter given. Ok, use the variable to hold the extension
			// without any leading dots:
			outFormat = extension.replaceAll("^\\.+", "");
		}
		else {
			// "form" parameter given. Use it regardless of file extension.
			if ( log.isDebugEnabled() && extension.length() > 0 ) {
				log.debug("form param (=" +outFormat+ ") will take precedence over file extension: " +extension);
			}
		}
		
		assert !outFormat.startsWith(".");
		
		return outFormat;
	}
	
	

	/**
	 * Gets the output format for a NON MmiUri request, so, only based on the "form" parameter.
	 * @param req
	 * @param log
	 */
	private static String getOutFormatForNonMmiUri(String formParam, Log log) {
		String outFormat = formParam;
		
		if ( log.isDebugEnabled() ) {
			log.debug("===getOutFormatForNonMmiUri ====== ");
			log.debug("===form = \"" +outFormat+ "\"");
		}
		
		return outFormat;
	}

	private String getOutFormatByContentNegotiation(Accept accept) {
		// accept empty? --> OWL
		if ( accept.isEmpty() ) {
			return "owl";
		}

		// Dominating Accept: application/rdf+xml dominates? --> OWL
		else if ( accept.getDominating().equalsIgnoreCase("application/rdf+xml") ) {
			return "owl";
		}

		// Dominating Accept contains "xml"? --> OWL
		else if ( accept.getDominating().indexOf("xml") >= 0 ) {
			return "owl";
		}

		// Dominating Accept: text/html dominates? --> HTML
		else if ( accept.getDominating().equalsIgnoreCase("text/html") ) {
			return "html";
		}

		// default:
		else {
			return "owl";
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
