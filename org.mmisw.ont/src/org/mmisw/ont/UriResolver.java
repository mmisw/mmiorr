package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntServlet.Request;
import org.mmisw.ont.sparql.SparqlDispatcher;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.drexel.util.rdf.JenaUtil;


/**
 * The "ont" service to resolve ontology and term URIs.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class UriResolver {
	
	//
	//
	// TODO Clean up and refactor this class
	// (2009-05-12: doing it -- see OntServlet and UriResolver2)
	//
	// The implementation of this class has become a bit messy as things were added sometimes 
	// very hastily (not much proper preparation and design).
	// In particular, a new design should allow to better differenciate the steps to resolve a request:
	//	- Determine if the requested resource is a whole ontology or an entity
	//  - Associate a corresponding object for the requested resource
	//  - Determine the output format
	//	- then dispatch the requested resource in that output format
	//
	
	
	private final Log log = LogFactory.getLog(UriResolver.class);

	private final OntConfig ontConfig;
	private final Db db;
	
	private final SparqlDispatcher sparqlDispatcher;
	
	private final HtmlDispatcher htmlDispatcher;
	
	private final ImageDispatcher imgDispatcher;
	
	private final RegularFileDispatcher regularFileDispatcher = new RegularFileDispatcher();

	private final UriDispatcher uriDispatcher;
	
	
	private enum OntFormat { RDFXML, N3 };
	
	
	private Request req;
	
	
	public UriResolver(OntConfig ontConfig, Db db, OntGraph ontGraph) {
		this.ontConfig = ontConfig;
		this.db = db;

		sparqlDispatcher = new SparqlDispatcher(ontGraph);
		htmlDispatcher = new HtmlDispatcher(ontConfig, db);
		imgDispatcher = new ImageDispatcher(ontConfig, db);
		
		uriDispatcher = new UriDispatcher(sparqlDispatcher);
	}

	
	/**
	 * The main dispatcher.
	 */
	void service(Request req) throws ServletException, IOException {
		this.req = req;
		
		// first, see if there are any testing requests to dispatch 
		
		// dispatch a sparql-query?
		if ( Util.yes(req.request, "sparql")  ) {
			sparqlDispatcher.execute(req.request, req.response);
			return;
		}
		
		
		// if the "uri" parameter is included, resolve by the given URI
		if ( Util.yes(req.request, "uri") ) {
			uriDispatcher.dispatchUri(req.request, req.response);
			return;
		}
		

		
		// resolve URI?
		if ( _resolveUri(req.request, req.response)  ) {
			// OK, no more to do here.
			return;
		}
		
		// Else, try to resolve the requested resource.
		regularFileDispatcher.dispatch(req.servletContext, req.request, req.response);
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
					// fix for issue #150: "file extension is lost in $ request" -> append the
                    // extension
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
			else if ( req.userAgentList.size() > 0 && req.userAgentList.get(0).startsWith("Java/") ) {
				
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
		
		// some image format:
		else if ( outFormat.equalsIgnoreCase("dot")
		     ||   outFormat.equalsIgnoreCase("png")
		) {
			return imgDispatcher.dispatch(request, response, mmiUri, unversionedRequest, mostRecentOntology, outFormat);
		}
		

		return false;   // not dispatched here.
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
		
		
		File file = OntServlet.getFullPath(ontology, ontConfig, log);
		
		
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
					StringReader is = OntServlet.serializeModel(termModel, "RDF/XML-ABBREV");
					IOUtils.copy(is, os);
					break;
				}
				case N3 : {
					String contentType = "text/plain";  // NOTE: "text/rdf+n3" is not registered.
					response.setContentType(contentType);
					StringReader is = OntServlet.serializeModel(termModel, "N3");
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
					StringReader is = OntServlet.serializeModel(unversionedModel, "RDF/XML-ABBREV");
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
					StringReader is = OntServlet.serializeModel(unversionedModel, "N3");
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
	 * Creates the N3 version of the model stored in the given file. 
	 */
	private StringReader _getN3(File file) {
		log.debug("_getN3: " +file);
		Model model = ModelFactory.createDefaultModel();
		String absPath = "file:" + file.getAbsolutePath();
		model.read(absPath, "", null);
		
		return OntServlet.serializeModel(model, "N3");
	}


}
