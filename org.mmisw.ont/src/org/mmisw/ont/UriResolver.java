package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.drexel.util.rdf.JenaUtil;


/**
 * The "ont" service to resolve ontology URIs.
 * 
 * @author Carlos Rueda
 */
public class UriResolver extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	private static final String VERSION = "0.1.7 (20081110)";
	static final String TITLE = "MMI Ontology URI resolver. Version " +VERSION;


	private final Log log = LogFactory.getLog(UriResolver.class);

	private final OntConfig ontConfig = new OntConfig();
	private final Db db = new Db(ontConfig);
	private final OntGraph ontGraph = new OntGraph(ontConfig, db);
	
	private final SparqlDispatcher sparqlDispatcher = new SparqlDispatcher(ontGraph);
	
	
	private final MdDispatcher mdDispatcher = new MdDispatcher(ontConfig, db);
	
	private final HtmlDispatcher htmlDispatcher = new HtmlDispatcher(ontConfig, db, mdDispatcher);

	
	private enum OntFormat { RDFXML, N3 };
	
	
	private List<String> userAgentList;
	

	public void init() throws ServletException {
		log.info(TITLE+ ": initializing");
		
		try {
			ontConfig.init(getServletConfig());
			db.init();
			ontGraph.init();
		} 
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		log.info(TITLE+ ": destroy called.\n\n");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
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
		} 
		
		// dispatch list of ontologies?
		else if ( Util.yes(request, "list")  ) {
			_doListOntologies(request, response);
		}
		
		// dispatch a sparql-query?
		else if ( Util.yes(request, "sparql")  ) {
			sparqlDispatcher.execute(request, response);
		}
		
		
		// reload graph?
		else if ( Util.yes(request, "_reload")  ) {
			ontGraph.reinit();
		}
		
		// dispatch a db-query?
		else if ( Util.yes(request, "dbquery")  ) {
			Util.doDbQuery(request, response, db);
		}
		
		// resolve URI?
		else if ( _resolveUri(request, response)  ) {
			// OK, no more to do here.
		}
		
		// Else, try to resolve the requested resource.
		// Note, since I'm using <url-pattern>/*</url-pattern> in web.xml, *everything* 
		// gets dispatched through this servlet, so I have to resolve other possible resources.
		else {
			String path = request.getPathTranslated();
			File file = new File(path);
			if ( !file.canRead() || file.isDirectory() ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Other resource: " +path+ ": not found or cannot be read");
				}
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");

				return;
			}
			
			String mime = getServletContext().getMimeType(path);
			if ( mime != null ) {
				response.setContentType(mime);
			}
			
			if ( false &&  // to avoid too many messages for now 
				 log.isDebugEnabled() ) {
				log.debug(path+ ": FOUND. " +
						(mime != null ? "Mime type set to: " +mime : "No Mime type set.")
				);
			}

			FileInputStream is = new FileInputStream(file);
			ServletOutputStream os = response.getOutputStream();
			IOUtils.copy(is, os);
			os.close();
		}
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
		
		// dispatch metadata?
		if ( Util.yes(request, "_md")  ) {
			//
			// This option is mainly to support metadata display in the RoR front-end.
			// In general, this parameter is used without any value, so only the
			// table component is generated.
			// For convenience, the following are also accepted:
			//    _md=completepage         -> to generate a complete HTML page
			//    _mdtableclass=somestyle  -> to specify the style for the table; by default, 
			//                                "metadata", the one used in the RoR front-end.
			//
			String _md = Util.getParam(request, "_md", "");
			boolean completePage = "completepage".equalsIgnoreCase(_md);
			String tableClass = Util.getParam(request, "_mdtableclass", null);   
			mdDispatcher.execute(request, response, null, completePage, tableClass, null);
			return true;
		}
		
		// if the "_lpath" parameter is included, reply with full local path of ontology file
		// (this is just a quick way to help ontmd to so some of its stuff ;)
		if ( Util.yes(request, "_lpath") ) {
			_resolveGetLocalPath(request, response);
			return true;
		}
		
		// if the "_versions" parameter is included, reply with a list of the available
		// version associated with the request
		if ( Util.yes(request, "_versions") ) {
			_resolveGetVersions(request, response);
			return true;
		}
		
		// if the "_debug" parameter is included, show some info about the URI parse
		// and the ontology from the database (but do not serve the contents)
		if ( Util.yes(request, "_debug") ) {
			_resolveUriDebug(request, response);
			return true;
		}
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			// Not dispatched here; allow caller to dispatch in any other convenient way:
			if ( false &&  // to avoid too many messages, while keeping the DEBUG level I want. 
			     log.isDebugEnabled() ) {
				log.debug("MMI URI not well formed: " +e.getMessage());
			}
			return false;   
		}
		
		
		////////////////////////////////////////////////////////////////////////////////
		//    Version component?
		////////////////////////////////////////////////////////////////////////////////
		
		String version = mmiUri.getVersion();
		if ( version == null || version.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
			
			//
			// handling of unversioned and latest-version requests.
			//
			
			// Get latest version with all possible topic extensions:
			
			
			Ontology mostRecentOntology = db.getMostRecentOntologyVersion(mmiUri);
			if ( mostRecentOntology != null ) {
				try {
					MmiUri foundMmiUri = MmiUri.create(mostRecentOntology.getUri());
					if ( log.isDebugEnabled() ) {
						log.debug("Found version: " +foundMmiUri);
					}
					
					String rememberExt = mmiUri.getTopicExtension();
					// but restore the requested file extension if different
					if ( ! rememberExt.equals(foundMmiUri.getTopicExtension()) ) {
						mmiUri = MmiUri.create(foundMmiUri.getOntologyUriWithTopicExtension(rememberExt));

						if ( log.isDebugEnabled() ) {
							log.debug("Restored requested extension to: " +mmiUri);
						}
					}
					else {
						mmiUri = foundMmiUri;
					}
				}
				catch (URISyntaxException e) {
					log.error("shouldnt happen", e);
					return false;   
				}
				
				// OK: here, mmiUri refers to the latest version.
				
				// If the request was with version = MmiUri.LATEST_VERSION_INDICATOR, then redirect to versioned
				// URI:   (see Issue 24)
				if ( version == null ) {
					// Let the dispatch continue.
					
					// TODO: Actually there is still the need to convert the latest
					// version into an "unversioned" one...  Not yet implemented; for now,
					// just let the process continue with the dereferencing rules as usual.
				}
				else {
					// Use a redirect so the user gets the actual latest version:
					String latestUri = mmiUri.getOntologyUri();
					log.debug("Redirecting to latest version: " + latestUri);
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
		
		// Dereferencing is done according to the "accept" header and the topic extension.

		// The response type depends (initially) on the following elements:
		String topicExt = mmiUri.getTopicExtension();
		Accept accept = new Accept(request);
		String outFormat = Util.getParam(request, "form", "");
		
		// NOTE: I use this 'outFormat' variable to handle the extension of the topic as well as the
		// optional parameter "form".  This parameter, if given, takes precedence over the extension.
		
		String dominating = accept.getDominating();
		
		if ( log.isDebugEnabled() ) {
			log.debug("===Starting dereferencing ====== ");
			log.debug("===Accept entries: " +accept.getEntries());
			log.debug("===Dominating entry: \"" +dominating+ "\"");
			log.debug("===topicExt = \"" +topicExt+ "\"");
			log.debug("===form = \"" +outFormat+ "\"");
		}

		// prepare 'outFormat' according to "form" parameter (if given) and file extension:
		if ( outFormat.length() == 0 ) {
			// no "form" parameter given. Ok, use the variable to hold the extension
			// without any leading dots:
			outFormat = topicExt.replaceAll("^\\.+", "");
		}
		else {
			// "form" parameter given. Use it regardless of file extension.
			if ( log.isDebugEnabled() && topicExt.length() > 0 ) {
				log.debug("form param (=" +outFormat+ ") will take precedence over file extension: " +topicExt);
			}
		}
		
		assert !outFormat.startsWith(".");
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using outFormat = " +outFormat+ " for format resolution");
		}

		// OK, from here I use 'outFormat' to check the requested format for the response.
		// 'topicExt' not used from here any more.
		
		if ( outFormat.length() == 0                 // No explicit outFormat 
		||   outFormat.equalsIgnoreCase("owl")       // OR outFormat is "owl"
		||   outFormat.equalsIgnoreCase("rdf")       // OR outFormat is "rdf"
		) {
			// dereferenced according to content negotiation as:
			
			// (a) an OWL document (if Accept: application/rdf+xml dominates)
			if ( "application/rdf+xml".equalsIgnoreCase(dominating) ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
			}
			
			// (a.1) firefox doesn't explicitly say "application/rdf+xml" and I guess this
			// is also the case with other standard browsers. In particular, my firefox sends:
			//     text/html
			//     application/xhtml+xml	
			//     application/xml; q = 0.9
			//      */*; q = 0.8
			// Since the extension is not ".html", I would consider the following case:
			//
			else if ( accept.contains("application/xml") ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
			}
			
			// (a.2) an OWL document if is the user-agent is "Java/*"
			// This is a workaround for the following situation in VINE:
			// The underlying Jena library should include the accept element:
			// "application/rdf+xml" but it's not doing so, see bug:
			//   Wrong Accept-Header in HTTP-Connections - ID: 1424091
			//   http://sourceforge.net/tracker2/?func=detail&aid=1424091&group_id=40417&atid=430288
			//
			else if ( userAgentList.size() > 0 && userAgentList.get(0).startsWith("Java/") ) {
				
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
			}
			
			// (b) an HTML document (if Accept: text/html but not application/rdf+xml)
			else if ( accept.contains("text/html") ) {
				
				return _resolveUriHtml(request, response, mmiUri);
			}
			
			// (c) an HTML document (if Accept: text/html, application/rdf+xml or Accept: */*)
			else if ( accept.contains("text/html") ||
					  accept.contains("application/rdf+xml") ||
					  accept.contains("*/*")
			) {
				
				if ( outFormat.equalsIgnoreCase("owl") ) {
					return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
				}
				else {
					return _resolveUriHtml(request, response, mmiUri);
				}
			}
			
			// (d) an OWL document with referenced style sheet (if no Accept)
			else if ( accept.isEmpty() ) {
				// TODO accept list empty
				// arbitrarely returning in HTML:
				log.warn("Case (d): \"accept\" list is empty. " +
						"'OWL document with referenced style sheet' Not implemented yet." +
						" Returning HTML temporarily.");
				
				return _resolveUriHtml(request, response, mmiUri);
			}
			
			
			// Else: arbitrarely returning in HTML:
			else {
				log.warn("Default case: Returning HTML.");
				return _resolveUriHtml(request, response, mmiUri);
			}
		}
		
		// Else: non-empty outFormat (other than "owl" and "rdf"):
		
		// "html":
		else if ( outFormat.equalsIgnoreCase("html") ) {
			return _resolveUriHtml(request, response, mmiUri);
		}
			
		// "n3":
		else if ( outFormat.equalsIgnoreCase("n3") ) {
			return _resolveUriOntFormat(request, response, mmiUri, OntFormat.N3);
		}
			
		// "pdf":
		else if ( outFormat.equalsIgnoreCase("pdf") ) {
			// TODO "pdf" Not implemented yet.
			log.warn("PDF format requested, but not implemented yet.");
			return false;   // handle this by saying "not dispatched here."
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
	 * Helper method to dispatch a request with response in the givenontology format.
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 */
	private boolean _resolveUriOntFormat(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, OntFormat ontFormat) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriOntFormat: starting response.");
		}
		
		//String ontologyUri = mmiUri.getOntologyUri();
	
		// obtain info about the ontology:
    	Ontology ontology = db.getOntologyWithExts(mmiUri, null);
		if ( ontology == null ) {
			return false;   // not dispatched here.
		}
		
		
		File file = UriResolver._getFullPath(ontology, ontConfig, log);
		
		if ( file.canRead() ) {
			String term = mmiUri.getTerm();
			
			// Term included?
			if ( term.length() > 0 ) {
				
				String uriFile = file.toURI().toString();
				Model model = JenaUtil.loadModel(uriFile, false);

				// TODO Handle requested ontology format for this term.
				// This would be probably in a form similar to a response from
				// a sparql query about the term.
				// For now, replying with the HTML format:
				htmlDispatcher.dispatchTerm(request, response, mmiUri, model, true);

//				String termContents = _resolveTerm(request, mmiUri, model);
//				StringReader is = new StringReader(termContents);
//				response.setContentType("text/html");
//				ServletOutputStream os = response.getOutputStream();
//				IOUtils.copy(is, os);
//				os.close();
			}
			
			// No term included:
			else {
				ServletOutputStream os = response.getOutputStream();
				switch ( ontFormat ) {
				case RDFXML: {
					// TODO this assumes ALL ontologies are stored in RDF/XML format!!
					response.setContentType("Application/rdf+xml");
					FileInputStream is = new FileInputStream(file);
					IOUtils.copy(is, os);
					break;
				}
				case N3 : {
					String contentType = "text/plain";  // NOTE: "text/rdf+n3" is not registered.
					response.setContentType(contentType);
					StringReader is = _getN3(file);
					IOUtils.copy(is, os);
					break;
				}
				default:
					throw new AssertionError(ontFormat+ " unexpected case");
				}
				os.close();
			}
		}
		else {
			// This should not happen.
			// Log the error and respond with a NotFound error:
			String msg = file.getAbsolutePath()+ ": internal error: uploaded file ";
			msg += file.exists() ? "exists but cannot be read." : "not found.";
			msg += "Please, report this bug.";
			log.error(msg, null);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, msg); 
		}
		
		return true;   // dispatched here.
	}

	/** Creates the N3 version of the model */
	private StringReader _getN3(File file) {
		log.debug("_getN3: " +file);
		Model model = ModelFactory.createDefaultModel();
		String absPath = "file:" + file.getAbsolutePath();
		model.read(absPath, "", null);
		StringWriter writer = new StringWriter();
		model.getWriter("N3").write(model, writer, null);
		StringReader reader = new StringReader(writer.toString());
		return reader;
	}

	/**
	 * Helper method to dispatch an "HTML" request.
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 */
	private boolean _resolveUriHtml(HttpServletRequest request, HttpServletResponse response, MmiUri mmiUri) 
	throws ServletException, IOException {

		if ( true ) {
			return htmlDispatcher.dispatch(request, response, mmiUri);
		}
		
		// TODO remove the rest of this
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriHtml: starting response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
    	Ontology ontology = db.getOntologyWithExts(mmiUri, null);
		if ( ontology == null ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}

		File file = UriResolver._getFullPath(ontology, ontConfig, log);

		if ( ! file.canRead() ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}
		
		
		
		PrintWriter out = response.getWriter();
		

		// start the response page:
		response.setContentType("text/html");
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +fullRequestedUri+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		
		String tableClass = "inline";
		// start with the metadata:
		mdDispatcher.execute(request, response, mmiUri, false, tableClass, "Metadata");
		
		
		out.println("<br/>");

		String uriFile = file.toURI().toString();
		Model model = JenaUtil.loadModel(uriFile, false);

		if ( mmiUri.getTerm().length() > 0 ) {
			_showTermInfo(mmiUri, model, out);
		}
		else {
			_showAllTerms(mmiUri, model, out, false);
		}
		return true;
	}

	
	/**
	 * Helper method to dispatch a "_lpath" request.
	 * The dispatch is always completed here.
	 */
	private void _resolveGetLocalPath(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveGetPath: starting '_path' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/plain");
		out = response.getWriter();
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			out.println("ERROR: " +e.getReason());
			return;
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		// obtain info about the ontology:
		String[] foundUri = { null };
    	Ontology ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
    	if ( ontology == null ) {
    		out.println("ERROR: " +ontologyUri+ ": Not found.");
    		return;
    	}

		
		// just return the path, without any further checks:
    	File file = UriResolver._getFullPath(ontology, ontConfig, log);

    	out.println(file.getAbsolutePath());
	}
	

	/**
	 * Helper method to dispatch a "_versions" request.
	 * The dispatch is always completed here.
	 */
	private void _resolveGetVersions(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveGetVersions: starting '_versions' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/plain");
		out = response.getWriter();
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			out.println("ERROR: " +e.getReason());
			return;
		}
		
		List<Ontology> onts = db.getOntologyVersions(mmiUri, true);
		
		for ( Ontology ontology : onts ) {
			
			// report the URI:
			out.println(ontology.getUri());
			
		}
	}
	

	
	/**
	 * Helper method to dispatch a "_debug" request.
	 * The dispatch is always completed here.
	 */
	private void _resolveUriDebug(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriInfo: starting '_debug' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +TITLE+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<b>" +TITLE+ "</b><br/><br/>");
		out.println(" Full requested URI: <code>" + fullRequestedUri + "</code> <br/><br/>");
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			out.println("<font color=\"red\">ERROR: " +e.getReason()+ "</font><br/>");
			out.println("</body>");
			out.println("</html>");
			return;
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		// show the parse result:
		String authority = mmiUri.getAuthority();
		String topic = mmiUri.getTopic();
		String version = mmiUri.getVersion();
		String term = mmiUri.getTerm();
		out.println("Parse result: OK<br/>");
		out.println("<pre>");
		out.println("       Ontology URI: " + ontologyUri);
		out.println("          authority: " + authority);
		out.println("            version: " + (version != null ? version : "(not given)"));
		out.println("              topic: " + topic);
		out.println("               Term: " + term);
		out.println("</pre>");

		
		// report something about the available versions:
		out.println("Available versions:");
		out.println("<pre>");
		for ( Ontology ont : db.getOntologyVersions(mmiUri, true) ) {
			out.println("   " +ont.getUri());
		}
		out.println("</pre>");
		
		
		
		// obtain info about the ontology:
		String[] foundUri = { null };
    	Ontology ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
    	out.println("<br/>Database result:<br/> ");
		
    	if ( ontology != null ) {
			out.println(foundUri[0]+ ": <font color=\"green\">Found.</font> <br/>");
    	}
    	else {
    		out.println(ontologyUri+ ": <font color=\"red\">Not found.</font> <br/>");
    		out.println("</body>");
    		out.println("</html>");
    		return;
    	}

		
		// prepare info about the path to the file on disk:
    	File file = UriResolver._getFullPath(ontology, ontConfig, log);

		// report the db info and whether the file can be read or not:
		out.println(" Ontology entry FOUND: <br/>");
		out.println("<pre>");
		out.println("                 id: " + ontology.id);
		out.println("        ontology_id: " + ontology.ontology_id);
		out.println("          file_path: " + ontology.file_path);
		out.println("           filename: " + ontology.filename);
		out.println("</pre>");
		out.println(" Full path: <code>" + file.getAbsolutePath() + "</code> ");
		out.println(" Can read it: <code>" + file.canRead() + "</code> <br/>");

		if ( file.canRead() ) {
			out.println("<br/>");

			String uriFile = file.toURI().toString();
			Model model = JenaUtil.loadModel(uriFile, false);

			if ( mmiUri.getTerm().length() > 0 ) {
				_showTermInfo(mmiUri, model, out);
			}
			else {
				_showAllTerms(mmiUri, model, out, true);
			}
		}
	}
	
	
	/** Generated a table with all the terms */
	private void _showAllTerms(MmiUri mmiUri, Model model, PrintWriter out, boolean debug) {
		out.printf(" All subjects in the ontology:<br/>%n"); 
		out.println("<table class=\"inline\" width=\"100%\">");
		out.printf("<tr>%n");
		out.printf("<th>URI</th>");
		out.printf("<th>Resolve</th>");
		if ( debug ) {
			out.printf("<th>_debug</th>");
		}
		//out.printf("<th>Name</th>%n");
		out.printf("</tr>%n");

		ResIterator iter = model.listSubjects();
		while (iter.hasNext()) {
			Resource elem = iter.nextResource();
			String elemUri = elem.getURI();
			if ( elemUri != null ) {
				String elemUriSlash = elemUri.replace('#' , '/');
				
				// generate anchor for the term using "id" in the row: 
				out.printf("<tr id=\"%s\">%n", elem.getLocalName());
				
				// Original URI (may be with # separator):
				out.printf("<td> <a href=\"%s\">%s</a> </td> %n", elemUri, elemUri);
				
				// resolve value with any # replaced with /
				out.printf("<td> <a href=\"%s\">resolve</a> </td> %n", elemUriSlash);
				
				if ( debug ) {
					out.printf("<td> <a href=\"%s?_debug\">_debug</a> </td> %n", elemUriSlash);
				}
				
				//out.printf("<td> %s </td> %n", elem.getLocalName());
				
				out.printf("</tr>%n");
			}
		}
		out.println("</table>");
	}

	/**
	 * Shows information about the requested term.
	 * @param mmiUri
	 * @param file
	 * @param out
	 */
	private void _showTermInfo(MmiUri mmiUri, Model model, PrintWriter out) {
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		// construct URI of term.
		// First, try with "#" separator:
		String termUri = mmiUri.getTermUri(true, "#");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			// then, try with "/" separator
			termUri = mmiUri.getTermUri(true, "/");
			termRes = model.getResource(termUri);
		}
		
		if ( termRes == null ) {
			out.println("   No resource found for URI: " +termUri);
			return;
		}
		
//		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
//		String label = labelRes == null ? null : ""+labelRes.getObject();
		
		out.println("<table class=\"inline\" width=\"100%\">");
		out.printf("<tr><th>%s</th></tr> %n", termRes);
		out.println("</table>");

		if ( true ) { // get all statements about the term
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			if (iter.hasNext()) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
				out.printf("<tr><th colspan=\"2\">%s</th></tr> %n", "Statements");

				out.printf("<tr>%n");
				out.printf("<th>%s</th>", "Predicate");
				out.printf("<th>%s</th>", "Object");
				out.printf("</tr>%n");
				
				
				
				while (iter.hasNext()) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					out.printf("<tr>%n");
							
					Property prd = sta.getPredicate();
					String prdUri = prd.getURI();
					if ( prdUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", prdUri, prdUri);
					}
					else {
						out.printf("<td>%s</td>", prd.toString());
					}
					
					RDFNode obj = sta.getObject();
					String objUri = null;
					if ( obj instanceof Resource ) {
						Resource objRes = (Resource) obj;
						objUri = objRes.getURI();
					}
					if ( objUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", objUri, objUri);
					}
					else {
						out.printf("<td>%s</td>", obj.toString());
					}
					
					out.printf("</tr>%n");
				}
				
				out.println("</table>");
			}
		}
		
		if ( true ) { // test for subclasses
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
				out.printf("<tr>%n");
				out.printf("<th>Subclasses</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					out.printf("<tr>%n");
					
					Resource sjt = sta.getSubject();
					String sjtUri = sjt.getURI();

					if ( sjtUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", sjtUri, sjtUri);
					}
					else {
						out.printf("<td>%s</td>", sjt.toString());
					}

					out.printf("</tr>%n");
				}
				out.println("</table>");
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			if ( iter.hasNext() ) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
				out.printf("<tr>%n");
				out.printf("<th>Individuals</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					Resource idv = (Resource) iter.next();
					
					out.printf("<tr>%n");
					
					String idvUri = idv.getURI();
					
					if ( idvUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", idvUri, idvUri);
					}
					else {
						out.printf("<td>%s</td>", idv.toString());
					}
					
					out.printf("</tr>%n");
				}
			}
		}
		
	}		

	
	private void _doListOntologies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection _con = null;
		try {
			_con = db.getConnection();
			Statement _stmt = _con.createStatement();
			String table = "v_ncbo_ontology";
			int limit = Integer.parseInt(Util.getParam(request, "limit", "500"));

			String query = 
				"select id, ontology_id, user_id, urn " +
				"from " +table+ "  limit " +limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<head> <link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\"> </head>");
	        out.println("<title>" +query+ "</title>");
	        out.println("</head>");
	        out.println("<body>");
	        out.println("<code>" +query+ "</code>");
	        out.println("<table class=\"inline\" width=\"100%\">");

			
	        ResultSetMetaData md = rs.getMetaData();
	        int cols = md.getColumnCount();
	        int idxUrn = -1;
	        out.println("<tr>");
	        for (int i = 0; i < cols; i++) {
	        	out.println("<th>");
	        	String colLabel = md.getColumnLabel(i+1);
				out.println(colLabel );
	            out.println("</th>");
	            if ( "urn".equals(colLabel) ) {
	            	idxUrn = i;
	            }
	        }
	        out.println("</tr>");

	        while ( rs.next() ) {
	        	out.println("<tr>");
	        	for (int i = 0; i < cols; i++) {
		        	out.println("<td>");
		        	Object val = rs.getObject(i+1);
		        	if ( val != null ) {
		        		if ( idxUrn == i ) {
		        			out.println("<a href=\"" +val+ "\">" +val+ "</a>");
		        		}
		        		else {
		        			out.println(val);
		        		}
		        	}
		            out.println("</td>");
	        	}
	        	out.println("</tr>");
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
	}

}
