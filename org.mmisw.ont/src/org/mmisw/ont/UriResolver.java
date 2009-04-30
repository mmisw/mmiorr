package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.sparql.SparqlDispatcher;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

import edu.drexel.util.rdf.JenaUtil;


/**
 * The "ont" service to resolve ontology and term URIs.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class UriResolver extends HttpServlet {
	
	//
	//
	// TODO Clean up and refactor this class
	// The implementation of this class has become a bit messy as things were added sometimes 
	// without a minimum of preparation and design.
	// In particular, a new design should allow to better differenciate the steps to resolve a request:
	//	- Determine if the requested resource is a whole ontology or an entity
	//  - Associate a corresponding object for the requested resource
	//  - Determine the output format
	//	- then dispatch the requested resource in that output format
	//
	
	
	private static final long serialVersionUID = 1L;
	
	static final String TITLE = "MMI Ontology and Term URI Resolver";
	
	private static String VERSION = "?";   // determined at init() time -- see build.xml and version.properties
	static String FULL_TITLE = TITLE + ". Version " +VERSION;


	private final Log log = LogFactory.getLog(UriResolver.class);

	private final OntConfig ontConfig = new OntConfig();
	private final Db db = new Db(ontConfig);
	private final OntGraph ontGraph = new OntGraph(ontConfig, db);
	
	private final SparqlDispatcher sparqlDispatcher = new SparqlDispatcher(ontGraph);
	
	private final HtmlDispatcher htmlDispatcher = new HtmlDispatcher(ontConfig, db);
	
	private final ImageDispatcher imgDispatcher = new ImageDispatcher(ontConfig, db);
	
	private final MiscDispatcher miscDispatcher = new MiscDispatcher(ontConfig, db);
	
	private final RegularFileDispatcher regularFileDispatcher = new RegularFileDispatcher();

	
	private enum OntFormat { RDFXML, N3 };
	
	
	private List<String> userAgentList;
	
	/**
	 * Initializes this service.
	 * This basically consists of
	 * retrieval of configuration parameters, 
	 * initialization of the database connection, 
	 * loading of the ontology graph,
	 * and retrieval of version information.
	 */
	public void init() throws ServletException {
		log.info(TITLE+ ": initializing");
		
		try {
			ontConfig.init(getServletConfig());
			
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
		doGet(request, response);
	}
	
	/**
	 * The main dispatcher.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
						response.sendError(HttpServletResponse.SC_NO_CONTENT);
						return;
					}
				}
			}
		}
		
		// first, see if there are any testing requests to dispatch 
		
		// show request info?
		if ( Util.yes(request, "showreq")  ) {
			Util.showReq(request, response);
			return;
		} 
		
		// dispatch list of ontologies?
		if ( Util.yes(request, "list")  ) {
			miscDispatcher.listOntologies(request, response);
			return;
		}
		
		// dispatch list of vocabularies?
		if ( Util.yes(request, "vocabs")  ) {
			miscDispatcher.listVocabularies(request, response);
			return;
		}
		
		// dispatch list of mappings?
		if ( Util.yes(request, "mappings")  ) {
			miscDispatcher.listMappings(request, response);
			return;
		}
		
		// dispatch a sparql-query?
		if ( Util.yes(request, "sparql")  ) {
			sparqlDispatcher.execute(request, response);
			return;
		}
		
		
		// reload graph?
		if ( Util.yes(request, "_reload")  ) {
			ontGraph.reinit();
			return;
		}
		
		// dispatch a db-query?
		if ( Util.yes(request, "dbquery")  ) {
			Util.doDbQuery(request, response, db);
			return;
		}
		
		
		// if the "_lpath" parameter is included, reply with full local path of ontology file
		// (this is just a quick way to help ontmd to so some of its stuff ;)
		if ( Util.yes(request, "_lpath") ) {
			miscDispatcher.resolveGetLocalPath(request, response);
			return;
		}
		
		// if the "_versions" parameter is included, reply with a list of the available
		// version associated with the request
		if ( Util.yes(request, "_versions") ) {
			miscDispatcher.resolveGetVersions(request, response);
			return;
		}
		
		// if the "_debug" parameter is included, show some info about the URI parse
		// and the ontology from the database (but do not serve the contents)
		if ( Util.yes(request, "_debug") ) {
			miscDispatcher.resolveUriDebug(request, response);
			return;
		}
		

		
		// resolve URI?
		if ( _resolveUri(request, response)  ) {
			// OK, no more to do here.
			return;
		}
		
		// Else, try to resolve the requested resource.
		regularFileDispatcher.dispatch(getServletContext(), request, response);
	}

	/**
	 * Resolves the ontology identified by its URI as indicated by <code>request.getRequestURL()</code>.
	 * 
	 * <p>
	 * This is the main operation in this servlet.
	 * 
	 * @param request
	 * @param response
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 * 
	 * @throws ServletException 
	 * @throws IOException 
	 */
	private boolean _resolveUri(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		
		// parse the given URI:
		MmiUri mmiUriCheck;
		try {
			mmiUriCheck = new MmiUri(fullRequestedUri);
		}
		catch (URISyntaxException e) {
			// Not dispatched here; allow caller to dispatch in any other convenient way:
			return false;   
		}
		
		final MmiUri mmiUri = mmiUriCheck;
		
		////////////////////////////////////////////////////////////////////////////////
		//    Version component?
		////////////////////////////////////////////////////////////////////////////////
		
		// this flag will be true if we have an unversioned request, see Issue 24.
		boolean unversionedRequest = false;
		Ontology mostRecentOntology = null;
		MmiUri foundMmiUri = null;
		
		String version = mmiUri.getVersion();
		if ( version == null || version.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
			
			//
			// handling of unversioned and latest-version requests.  (see Issue 24)
			//
			
			// Get latest version trying all possible topic extensions:
			mostRecentOntology = db.getMostRecentOntologyVersion(mmiUri);

			if ( mostRecentOntology != null ) {
				
				try {
					//
					// Note that mostRecentOntology.getUri() won't have the term component.
					// So, we have to transfer it to foundMmiUri:
					//
					foundMmiUri = new MmiUri(mostRecentOntology.getUri()).copyWithTerm(mmiUri.getTerm());
					
					if ( log.isDebugEnabled() ) {
						log.debug("Found ontology version: " +mostRecentOntology.getUri());

						if ( ! mmiUri.getExtension().equals(foundMmiUri.getExtension()) ) {
							log.debug("Restored requested extension to: " +mmiUri);
						}
					}
					
					// also, restore the original requested extension:
					foundMmiUri = foundMmiUri.copyWithExtension(mmiUri.getExtension());
				}
				catch (URISyntaxException e) {
					log.error("shouldnt happen", e);
					return false;   
				}
				
				// OK: here, foundMmiUri refers to the latest version.
				
				if ( version == null ) {
					// unversioned request.
					unversionedRequest = true;
					// and let the dispatch continue.
				}
				else {
					// request was with version = MmiUri.LATEST_VERSION_INDICATOR.
					// Use a redirect so the user gets the actual latest version:
					//
					// NOTE: I was using mmiUri.getOntologyUri(), but this only returns the
					// URI for the ontology, so any possible term was ignored. Now getTermUri is used:
					String latestUri = foundMmiUri.getTermUri();
					if ( log.isDebugEnabled() ) {
						log.debug("Redirecting to latest version: " + latestUri);
					}
					latestUri = response.encodeRedirectURL(latestUri);
					response.sendRedirect(latestUri);
					
					return true;  // Done here.
				}
			}
			else {
				// No versions available!
				if ( log.isDebugEnabled() ) {
					log.debug("No versions found.");
				}
				// TODO: Since we assume NO un-versioned ontologies are stored, then we could
				// safely return 404 here; but let the dispatch continue for now.
			}
		}
		else {
			// Version explicitly given: let the dispatch continue.
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////////
		//    Dereferencing rules
		////////////////////////////////////////////////////////////////////////////////
		
		// Dereferencing is done according to the "accept" header, the extension, and output format.

		// The response type depends (initially) on the following elements:
		String extension = mmiUri.getExtension();
		Accept accept = new Accept(request);
		String outFormat = Util.getParam(request, "form", "");
		
		// NOTE: I use this 'outFormat' variable to handle the extension of the topic as well as the
		// optional parameter "form".  This parameter, if given, takes precedence over the extension.
		
		String dominating = accept.getDominating();
		
		if ( log.isDebugEnabled() ) {
			log.debug("===Starting dereferencing ====== ");
			log.debug("===Accept entries: " +accept.getEntries());
			log.debug("===Dominating entry: \"" +dominating+ "\"");
			log.debug("===extension = \"" +extension+ "\"");
			log.debug("===form = \"" +outFormat+ "\"");
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
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using outFormat = " +outFormat+ " for format resolution");
		}

		// OK, from here I use 'outFormat' to check the requested format for the response.
		// 'extension' not used from here any more.
		
		
		if ( outFormat.length() == 0                 // No explicit outFormat 
		||   outFormat.equalsIgnoreCase("owl")       // OR outFormat is "owl"
		||   outFormat.equalsIgnoreCase("rdf")       // OR outFormat is "rdf"
		) {
			// dereferenced according to content negotiation as:
			
			// (a) an OWL document (if Accept: application/rdf+xml dominates)
			if ( "application/rdf+xml".equalsIgnoreCase(dominating) ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML, unversionedRequest, mostRecentOntology);
			}
			
			// (a.1) Since the extension is not ".html", I'm considering the following case:
			//
			else if ( accept.contains("application/xml") ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML, unversionedRequest, mostRecentOntology);
			}
			
			// (a.2) an OWL document if is the user-agent is "Java/*"
			// This is a workaround for the following situation in VINE:
			// The underlying Jena library should include the accept element:
			// "application/rdf+xml" but it's not doing so, see bug:
			//   Wrong Accept-Header in HTTP-Connections - ID: 1424091
			//   http://sourceforge.net/tracker2/?func=detail&aid=1424091&group_id=40417&atid=430288
			//
			else if ( userAgentList.size() > 0 && userAgentList.get(0).startsWith("Java/") ) {
				
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML, unversionedRequest, mostRecentOntology);
			}
			
			// (b) an HTML document (if Accept: text/html but not application/rdf+xml)
			else if ( accept.contains("text/html") ) {
				
				return htmlDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
			}
			
			// (c) an HTML document (if Accept: text/html, application/rdf+xml or Accept: */*)
			else if ( accept.contains("text/html") ||
					  accept.contains("application/rdf+xml") ||
					  accept.contains("*/*")
			) {
				
				if ( outFormat.equalsIgnoreCase("owl") ) {
					return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML, unversionedRequest, mostRecentOntology);
				}
				else {
					return htmlDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
				}
			}
			
			// (d) an OWL document with referenced style sheet (if no Accept)
			else if ( accept.isEmpty() ) {
				// TODO accept list empty
				// arbitrarely returning in HTML:
				log.warn("Case (d): \"accept\" list is empty. " +
						"'OWL document with referenced style sheet' Not implemented yet." +
						" Returning HTML temporarily.");
				
				return htmlDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
			}
			
			
			// Else: arbitrarely returning in HTML:
			else {
				log.warn("Default case: Returning HTML.");
				return htmlDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
			}
		}
		
		// Else: non-empty outFormat (other than "owl" and "rdf"):
		
		// "html":
		else if ( outFormat.equalsIgnoreCase("html") ) {
			return htmlDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
		}
			
		// "n3":
		else if ( outFormat.equalsIgnoreCase("n3") ) {
			return _resolveUriOntFormat(request, response, mmiUri, OntFormat.N3, unversionedRequest, mostRecentOntology);
		}
			
		// "pdf":
		else if ( outFormat.equalsIgnoreCase("pdf") ) {
			// TODO "pdf" Not implemented yet.
			log.warn("PDF format requested, but not implemented yet.");
			return false;   // handle this by saying "not dispatched here."
		}
		
		// some image format, currently only "dot"
		else if ( outFormat.equalsIgnoreCase("dot") ) {
			return imgDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology);
		}
		

		return false;   // not dispatched here.
	}
	
	
	/**
	 * Gets the full path to get to the uploaded ontology file.
	 * @param ontology
	 * @return
	 */
	static File _getFullPath(Ontology ontology, OntConfig ontConfig, Log log) {
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
	 * Helper method to dispatch a request with response in the given ontology format.
	 * 
	 * @param unversionedRequest If the original request was for the "unversioned" version.
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 */
	private boolean _resolveUriOntFormat(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, OntFormat ontFormat, boolean unversionedRequest, Ontology ontology) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriOntFormat: starting response. mmiUri = " +mmiUri);
		}
		
		//String ontologyUri = mmiUri.getOntologyUri();
	
		if ( ontology == null ) {
			// obtain info about the ontology:
			ontology = db.getOntologyWithExts(mmiUri, null);
		}
		
		if ( ontology == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_resolveUriOntFormat: not dispatched here. mmiUri = " +mmiUri);
			}
			return false;   // not dispatched here.
		}
		
		
		File file = UriResolver._getFullPath(ontology, ontConfig, log);
		
		
		if ( !file.canRead() ) {
			// This should not happen.
			// Log the error and respond with a NotFound error:
			String msg = file.getAbsolutePath()+ ": internal error: uploaded file ";
			msg += file.exists() ? "exists but cannot be read." : "not found.";
			msg += "Please, report this bug.";
			log.error(msg, null);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
			
			return true;
		}

		// original model:
		OntModel model = null;
		
		// corresponding unversioned model in case is requested: 
		OntModel unversionedModel = null;
		
		if ( unversionedRequest ) {
			String uriFile = file.toURI().toString();
			
			model = JenaUtil.loadModel(uriFile, false);

			unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
			
			if ( unversionedModel != null ) {
				// but put both variables to the same unversioned model
				model = unversionedModel;
				if ( log.isDebugEnabled() ) {
					log.debug("_resolveUriOntFormat: using obtained unversioned model");
				}
			}
			else {
				// error in conversion to unversioned version.
				// this is unexpected. 
				// Continue with original model, if necessary; see below.
				log.error("_resolveUriOntFormat: unexpected: error in conversion to unversioned version.  But continuing with original model");
			}
		}

		String term = mmiUri.getTerm();
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriOntFormat: term=[" +term+ "].");
		}

		/////////////////////////////////////////////////////////////////////
		// Term included?
		if ( term.length() > 0 ) {

			String uriFile = file.toURI().toString();
			if ( model == null ) {
				model = JenaUtil.loadModel(uriFile, false);
			}
			
			Model termModel = TermExtractor.getTermModel(model, mmiUri);
			if ( termModel != null ) {
				ServletOutputStream os = response.getOutputStream();
				switch ( ontFormat ) {
				case RDFXML: {
					response.setContentType("Application/rdf+xml");
					StringReader is = _serializeModel(termModel, "RDF/XML-ABBREV");
					IOUtils.copy(is, os);
					break;
				}
				case N3 : {
					String contentType = "text/plain";  // NOTE: "text/rdf+n3" is not registered.
					response.setContentType(contentType);
					StringReader is = _serializeModel(termModel, "N3");
					IOUtils.copy(is, os);
					break;
				}
				default:
					throw new AssertionError(ontFormat+ " unexpected case");
				}
				os.close();
				
				return true;
			}
			
			// Else: term unexistent: return 404 to client:
			response.sendError(HttpServletResponse.SC_NOT_FOUND, mmiUri.getTermUri());

			return true;    // dispatched here.
		}

		// No term included:
		else {
			if ( log.isDebugEnabled() ) {
				log.debug("_resolveUriOntFormat: returning response with format=" +ontFormat);
			}

			ServletOutputStream os = response.getOutputStream();
			
			switch ( ontFormat ) {
			case RDFXML: {

				response.setContentType("Application/rdf+xml");
				
				if ( unversionedRequest ) {
					StringReader is = _serializeModel(unversionedModel, "RDF/XML-ABBREV");
					IOUtils.copy(is, os);
				}
				else {
					FileInputStream is = new FileInputStream(file);
					IOUtils.copy(is, os);
				}
				break;
			}
			case N3 : {
				String contentType = "text/plain";  // NOTE: "text/rdf+n3" is not registered.
				response.setContentType(contentType);
				
				if ( unversionedRequest ) {
					StringReader is = _serializeModel(unversionedModel, "N3");
					IOUtils.copy(is, os);
				}
				else {
					StringReader is = _getN3(file);
					IOUtils.copy(is, os);
				}
				break;
			}
			default:
				throw new AssertionError(ontFormat+ " unexpected case");
			}
			os.close();
		}
		
		return true;   // dispatched here.
	}
	
	

	/** 
	 * Gets the serialization of a model in the given language.
	 * <p>
	 * (Similar to JenaUtil.getOntModelAsString(OntModel model).) 
	 */
	private StringReader _serializeModel(Model model, String lang) {
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


	/** 
	 * Creates the N3 version of the model stored in the given file. 
	 */
	private StringReader _getN3(File file) {
		log.debug("_getN3: " +file);
		Model model = ModelFactory.createDefaultModel();
		String absPath = "file:" + file.getAbsolutePath();
		model.read(absPath, "", null);
		
		return _serializeModel(model, "N3");
	}

}
