package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.admin.OntologyDeleter;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.sparql.SparqlDispatcher;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.triplestore.TripleStore;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.Analytics;
import org.mmisw.ont.util.OntUtil;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.shared.UnknownPropertyException;

/**
 * The Ont servlet.
 * 
 * <p>
 * Thread-safety: This class is considered thread-safe.
 * 
 * @author Carlos Rueda
 */
@ThreadSafe
public class OntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(OntServlet.class);
	
	private final OntConfig ontConfig = new OntConfig();
	private final Db db = new Db(ontConfig);
	
	private final AdminDispatcher adminDispatcher = AdminDispatcher.createInstance(db);
	
	private final ITripleStore tripleStore = new TripleStore(db, adminDispatcher);
	
	private final MiscDispatcher miscDispatcher = new MiscDispatcher(ontConfig, db);

	private final SparqlDispatcher sparqlDispatcher = new SparqlDispatcher(tripleStore);

	private final UriDispatcher uriDispatcher = new UriDispatcher(sparqlDispatcher);

	private final UriResolver2 uriResolver2 = new UriResolver2(this, ontConfig, db, tripleStore);
	
	private final RegularFileDispatcher regularFileDispatcher = new RegularFileDispatcher();
	
	private final Analytics analytics = Analytics.getInstance();
	
	
	private final ThreadLocal<OntRequest> perThreadOntRequest = new ThreadLocal<OntRequest>();
	
	
	/**
	 * Initializes this service.
	 * This basically consists of:
	 * retrieval of configuration parameters, 
	 * initialization of the database helper, 
	 * initialization of the triple store, 
	 * initialization of the admin dispatcher, 
	 * initialization of analytics. 
	 */
	public void init() throws ServletException {
		
		log.info(OntVersion.getFullTitle()+ ": initializing");
		
		try {
			ServletConfig servletConfig = getServletConfig();
			ontConfig.init(servletConfig);
			db.init();
			tripleStore.init();
			adminDispatcher.init();
			analytics.init();
			
			log.info(OntVersion.getFullTitle()+ ": init complete.");
		} 
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}

	}
	
	public void destroy() {
		log.info(OntVersion.getFullTitle()+ ": destroy called.\n\n");
		try {
			tripleStore.destroy();
		}
		catch (ServletException e) {
			log.error("error while destroying triple store object", e);
		}
	}
	
	private void _dispatch() throws ServletException, IOException {
		OntRequest req = getThreadLocalOntRequest();
		// first, see if there are any testing requests to dispatch 
		
		// show request info?
		if ( Util.yes(req.request, "showreq")  ) {
			Util.showReq(this, req.request, req.response);
			return;
		} 
		
		// dispatch list of ontologies for orrclient?
		if ( Util.yes(req.request, "listall")  ) {
			miscDispatcher.listAll(req.request, req.response);
			return;
		}
		
		// report Ont service version?
		if ( Util.yes(req.request, "_version")  ) {
			miscDispatcher.reportOntVersion(req.request, req.response);
			return;
		}
		
		// report aquaportal rest url for orrclient?
		if ( Util.yes(req.request, "_aqrest")  ) {
			miscDispatcher.reportAquaportalRestUrl(req.request, req.response);
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
		// (this is just a quick way to help orrportal to so some of its stuff ;)
		if ( Util.yes(req.request, "_lpath") ) {
			miscDispatcher.resolveGetLocalPath(req.request, req.response);
			return;
		}
		
		// if the "_csv" parameter is included, reply with contents of associated CSV file
		// (this is just a quick way to help orrportal to so some of its stuff ;)
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

		// load an ontology into the graph?
		if ( Util.yes(req.request, "_lo")  ) {
			_loadOntologyIntoGraph(req);
			return;
		}
		
		// reload triple store?
		if ( Util.yes(req.request, "_reload")  ) {
			_reload(req);
			return;
		}
		// reindex triple store?
		if ( Util.yes(req.request, "_reidx")  ) {
			_reindex(req);
			return;
		}
		// clear triple store?
		if ( Util.yes(req.request, "_clear")  ) {
			_clear(req);
			return;
		}
		
		// get users RDF?
		if ( Util.yes(req.request, "_usrsrdf")  ) {
			adminDispatcher.getUsersRdf(req);
			return;
		}
		
		// dispatch a db-query?
		if ( Util.yes(req.request, "dbquery")  ) {
			Util.doDbQuery(req.request, req.response, db);
			return;
		}
		
		// "ontology exists" request?
		if ( Util.yes(req.request, "oe") ) {
			String uri = Util.getParam(req.request, "oe", "");
			_dispatchIsOntologyRegistered(uri);
			return;
		}
		
		if ( _dispatchAuthority() ) {
			return;
		}
		
		// if the "uri" parameter is included, resolve by the given URI
		if ( Util.yes(req.request, "uri") ) {
			// get (ontology or entity) URI from the parameter:
			String ontOrEntUri = Util.getParam(req.request, "uri", "");
			if ( ontOrEntUri.length() == 0 ) {
				req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing uri parameter");
				return;
			}
			if ( ! _dispatchUri(ontOrEntUri) ) {
				// the explicit given uri could not be resolved, so respond with NOT_FOUND
				req.response.sendError(HttpServletResponse.SC_NOT_FOUND, ontOrEntUri);
			}
			return;
		}
		
		// dispatch a sparql-query?
		if ( Util.yes(req.request, "sparql")  ) {
			sparqlDispatcher.execute(req.request, req.response);
			return;
		}
		

		// get user information?
		if ( Util.yes(req.request, "_usri")  ) {
			_getUserInfo(req);
			return;
		}
		
		// unregister ontology?
		if ( Util.yes(req.request, "_unr")  ) {
			_unregisterOntology(req);
			return;
		}
		
		
		boolean resolved = false;

		if ( req.mmiUri != null ) {
			String ontOrEntUri = req.mmiUri.getTermUri();
			log.debug("To dispatch MmiUri: " +ontOrEntUri);
			resolved = _dispatchUri(ontOrEntUri);
		}
		else {
			log.debug("To dispatch non-MmiUri: " +req.fullRequestedUri);
		}
		
		if ( ! resolved ) {
			// try to resolve request as a regular resource.
			regularFileDispatcher.dispatch(req.servletContext, req.request, req.response);
			return;
		}
		
//		//////////////////////////////////////////////////////////////////////
//		// old dispatching:
//
//		if ( Util.yes(req.request, "ur2")  ) {
//			uriResolver2.service(req);
//		}
//		else {
//			uriResolver.service(req);
//		}
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
		OntRequest req = new OntRequest(getServletContext(), request, response);
		try {
			perThreadOntRequest.set(req);
			_dispatch();
			if ( log.isDebugEnabled() ) {
				log.debug("\n");
			}
		}
		finally {
			perThreadOntRequest.set(null);
		}
	}
	
	OntRequest getThreadLocalOntRequest() {
		return perThreadOntRequest.get();
	}
	
	/**
	 * Dispatches authority if that's the case
	 * 
	 * @return true iff dispatch completed here.
	 */
	private boolean _dispatchAuthority() throws ServletException, IOException {
		OntRequest req = getThreadLocalOntRequest();
		
		// #294: "ontology listing for a particular authority"
		if ( req.authority != null ) {
			// got an "authority" request.
			if ( log.isDebugEnabled() ) {
				log.debug("_dispatchAuthority: '" +req.authority+ "'");
			}
			
			return miscDispatcher.listOntologiesForAuthority(req.request, req.response, 
					req.authority, req.outFormat);
		}
		return false;
	}	
	
	/**
	 * Dispatches the given uri.
	 * If the uri corresponds to a stored ontology, then the ontology is resolved
	 * as it were a regular self-served ontology.
	 * If the uri corresponds to an entity (ie, that can be resolved to a non-empty result using SPARQL),
	 * then it is dispatched here.
	 * Otherwise, return false--not dispatched here.
	 * 
	 * @return true iff dispatch completed here.
	 */
	private boolean _dispatchUri(String ontOrEntUri) throws ServletException, IOException {
		OntRequest req = getThreadLocalOntRequest();
		if ( log.isDebugEnabled() ) {
			log.debug("_dispatchUri: ontOrEntUri=" +ontOrEntUri);
		}
		
		// TODO (#158: analytics) under preliminary testing
		analytics.trackPageview(ontOrEntUri);

		String finalVersion = null;
		
		// explicit version and MmiUri with version given?
		if ( req.version != null ) {
			if ( req.mmiUri != null && req.mmiUri.getVersion() != null ) {		
				//
				// Both, versioned URI and "version" parameter given.
				// Check that the two components are equal.
				//
				if ( ! req.version.equals(req.mmiUri.getVersion()) ) {
					// versioned request AND explicit version parameter -> BAD REQUEST:
					req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
					"Versioned URI and \"version\" parameter requested simultaneously; both values must be equal.");
					return true;
				}
				finalVersion = req.version;
			}
		}
		else if ( req.mmiUri != null  ) {
			finalVersion = req.mmiUri.getVersion();
		}
		
		
		// will be non-null if request corresponds to an ontology (not term)
		OntologyInfo ontology = null;
		
		if ( finalVersion != null ) {
			//
			// explicit version requested (either from version parameter or from MmiUri).
			//
			if ( log.isDebugEnabled() ) {
				log.debug("Explicit version requested: " +finalVersion);
			}
			
			// 2011-07-13
			if ( finalVersion.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
				if ( req.mmiUri != null ) {
					_handleLatestVersionRedirection(req, ontOrEntUri);
					return true;
				}
				else {
					// this shouldn't happen(?)
					if ( log.isDebugEnabled() ) {
						log.debug("request with latest version indicator but not req.mmiUri available");
					}
					// TODO should send error response or something
				}
			}
			else {
				ontology = db.getOntologyVersion(ontOrEntUri, finalVersion);
			}
		}
		else {
			//
			// No explicit version requested.
			//
			log.debug("No explicit version requested.");
			ontology = _getRegisteredOntology(ontOrEntUri);
		}
		
		if ( ontology != null ) {
			//
			// The requested URI corresponds to  a stored ontology.
			//
			if ( log.isDebugEnabled() ) {
				log.debug("dispatching "+ ontOrEntUri+ " as whole ontology (not entity)");
			}
			
			// use ontology member in req and make sure the Uri attribute is set
			req.ontology = ontology;
			req.ontology.setUri(ontOrEntUri);
			uriResolver2.serviceForOntology();
			return true;
		}
		else {
			/*
			 * It is not a registered ontology.
			 * If it is an Ont-resolvable URI corresponding to an ontology (ie., not term),
			 * then respond NotFound
			 */
			if ( req.mmiUri != null 
			&& req.mmiUri.getTerm().length() == 0
			&& OntUtil.isOntResolvableUri(req.mmiUri.getOntologyUri()) ) {
				if (log.isDebugEnabled()) {
					log.debug("_dispatchUri: ontOrEntUri=" +ontOrEntUri + " => " +
							"Ont-resolvable URI for non-registered ontology, responding 404");
				}
				req.response.sendError(HttpServletResponse.SC_NOT_FOUND, req.mmiUri.getOntologyUri());
				return true;
			}
			
			// Else: try to dispatch as it were an entity URI (not complete ontology).
			return uriDispatcher.dispatchEntityUri(req.request, req.response, ontOrEntUri,
					req.outFormat
			);
		}
	}
	
	/**
	 * Does the uri correspond to a registered ontology?
	 * This method always completes the dispatch to the client.
	 */
	private void _dispatchIsOntologyRegistered(String uri) throws ServletException, IOException {
		/*
		 * Based on _dispatchUri
		 */
		OntRequest req = getThreadLocalOntRequest();
		if ( log.isDebugEnabled() ) {
			log.debug("_dispatchIsOntologyRegistered: uri=" +uri);
		}
		
		if ( uri == null || uri.length() == 0 ) {
			req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing value for 'oe' parameter");
			return;
		}
		
		String finalVersion = null;
		
		// explicit version and MmiUri with version given?
		if ( req.version != null ) {
			if ( req.mmiUri != null && req.mmiUri.getVersion() != null ) {		
				//
				// Both, versioned URI and "version" parameter given.
				// Check that the two components are equal.
				//
				if ( ! req.version.equals(req.mmiUri.getVersion()) ) {
					// versioned request AND explicit version parameter -> BAD REQUEST:
					req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
					"Versioned URI and \"version\" parameter requested simultaneously; both values must be equal.");
					return;
				}
				finalVersion = req.version;
			}
		}
		else if ( req.mmiUri != null  ) {
			finalVersion = req.mmiUri.getVersion();
		}
		
		// will be non-null if request corresponds to an ontology
		OntologyInfo ontology = null;
		
		if ( finalVersion != null ) {
			//
			// explicit version requested (either from version parameter or from MmiUri).
			//
			if ( log.isDebugEnabled() ) {
				log.debug("Explicit version requested: " +finalVersion);
			}
			
			if ( finalVersion.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
				// LATEST_VERSION_INDICATOR not handled in this method -> BAD REQUEST:
				req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
					"Latest version indicator in MmiUri not handled.");
				return;
			}
			else {
				ontology = db.getOntologyVersion(uri, finalVersion);
			}
		}
		else {
			//
			// No explicit version requested.
			//
			log.debug("No explicit version requested.");
			ontology = _getRegisteredOntology(uri);
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("_dispatchIsOntologyRegistered: uri=" +uri + 
					" => " + (ontology != null ? "YES" : "NO"));
		}

		if ( ontology != null ) {
			req.response.setStatus(HttpServletResponse.SC_OK);
		}
		else {
			req.response.sendError(HttpServletResponse.SC_NOT_FOUND, uri);
		}
	}
	
	/**
	 * Handles redirection for a request containing the latest version indicator.
	 */
	private void _handleLatestVersionRedirection(OntRequest req, String ontOrEntUri) 
	throws ServletException, IOException {
		OntologyInfo ontology = db.getMostRecentOntologyVersion(req.mmiUri);
		if ( ontology != null ) {
			ontOrEntUri = ontology.getUri();
			
			MmiUri foundMmiUri;
			
			try {
				//
				// Note that mostRecentOntology.getUri() won't have the term component.
				// So, we have to transfer it to foundMmiUri:
				//
				foundMmiUri = new MmiUri(ontology.getUri()).copyWithTerm(req.mmiUri.getTerm());
				
				if ( log.isDebugEnabled() ) {
					log.debug("Found ontology version: " +ontology.getUri());

					if ( ! req.mmiUri.getExtension().equals(foundMmiUri.getExtension()) ) {
						log.debug("Restored requested extension to: " +req.mmiUri);
					}
				}
				
				// also, restore the original requested extension:
				foundMmiUri = foundMmiUri.copyWithExtension(req.mmiUri.getExtension());
			}
			catch (URISyntaxException e) {
				if ( log.isDebugEnabled() ) {
					log.debug("Shouldn't happen", e);
				}
			}
			
			if ( log.isDebugEnabled() ) {
				log.debug("Redirecting to latest version: " + ontOrEntUri);
			}
			String redir = req.response.encodeRedirectURL(ontOrEntUri);
			req.response.sendRedirect(redir);
		}
		else {
			req.response.sendError(HttpServletResponse.SC_NOT_FOUND, ontOrEntUri);
		}
	}

	/**
	 * Dispatches the given uri.
	 * If the uri corresponds to a stored ontology, then the ontology is resolved
	 * as it were a regular self-served ontology.
	 * If the uri corresponds to an entity (ie, that can be resolved to a non-empty result using SPARQL),
	 * then it is dispatched here.
	 * Otherwise, return false--not dispatched here.
	 * 
	 * @return true iff dispatch completed here.
	 */
	@SuppressWarnings("unused")
	// TODO remove this old method.  Actual method is _dispatchUri
	private boolean _dispatchUri_Old(OntRequest req, String ontOrEntUri) throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_dispatchUri: ontOrEntUri=" +ontOrEntUri);
		}
		
		// TODO (#158: analytics) under preliminary testing
		analytics.trackPageview(ontOrEntUri);
		
		OntologyInfo ontology = null;
		
		// explicit version?
		if ( req.version != null ) {
			// 
			// Dispatch explicit version request.
			//
			log.debug("Explicit version requested: " +req.version);
			
			if ( req.mmiUri != null ) {
				if ( req.mmiUri.getVersion() != null ) {		
					//
					// Both, versioned URI and "version" parameter given.
					// Check that the two components are equal.
					//
					if ( req.version.equals(req.mmiUri.getVersion()) ) {
						ontology = db.getOntologyVersion(ontOrEntUri, req.version);
					}
					else {
						// versioned request AND explicit version parameter -> BAD REQUEST:
						req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
								"Versioned URI and \"version\" parameter requested simultaneously; both values must be equal.");
						return true;
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
			log.debug("No explicit version requested.");
			ontology = _getRegisteredOntology(ontOrEntUri);
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
			uriResolver2.serviceForOntology();
			return true;
		}
		else {
			// try to dispatch entity URI (not complete ontology).
			return uriDispatcher.dispatchEntityUri(req.request, req.response, ontOrEntUri,
					req.outFormat
			);
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
	private OntologyInfo _getRegisteredOntology(String potentialOntUri) throws ServletException {
		if ( log.isDebugEnabled() ) {
			log.debug("_getRegisteredOntology: potentialOntUri=" +potentialOntUri);
		}

		OntologyInfo ontology = null;
		if ( OntUtil.isOntResolvableUri(potentialOntUri) ) {
			
			if ( log.isDebugEnabled() ) {
				log.debug("_getRegisteredOntology: isOntResolvableUri: yes");
			}

			try {
				MmiUri mmiUri = new MmiUri(potentialOntUri);
				
				if ( mmiUri.getTerm().length() > 0 ) {
					// potentialOntUri corresponds to a term, not an ontology:
					if ( log.isDebugEnabled() ) {
						log.debug("_getRegisteredOntology: is a term, returning null.");
					}
					return null;
				}
				
				if ( mmiUri.getVersion() == null ) {
					// unversioned request.  Get most recent
					if ( log.isDebugEnabled() ) {
						log.debug("_getRegisteredOntology: unversioned request.");
					}
					
					ontology = db.getMostRecentOntologyVersion(mmiUri);
				}
				else {
					// versioned request. Just try to use the argument as given:
					if ( log.isDebugEnabled() ) {
						log.debug("_getRegisteredOntology: versioned request.");
					}

					ontology = db.getOntology(potentialOntUri);
				}
			}
			catch (URISyntaxException e) {
				// Not an MmiUri. Just try to use the argument as given:
				if ( log.isDebugEnabled() ) {
					log.debug("_getRegisteredOntology: not an MmiUri.");
				}

				ontology = db.getOntology(potentialOntUri);
			}
		}
		else {
			if ( log.isDebugEnabled() ) {
				log.debug("_getRegisteredOntology: is OntResolvableUri: no");
			}

			ontology = db.getOntology(potentialOntUri);
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("_getRegisteredOntology: " +(ontology == null ? "not resolved. returning null." : "resolved."));
		}
		return ontology;
	}
	
	/**
	 * Gets a registered ontology
	 * 
	 * @param potentialOntUri. The URI that will be used to try to find a corresponding registered
	 *                     ontology.
	 * @return the ontology if found; null if not found.
	 * @throws ServletException
	 */
	@SuppressWarnings("unused")
	// TODO remove this old method.  Actual method is _getRegisteredOntology
	private OntologyInfo _getRegisteredOntology_Old(String potentialOntUri) throws ServletException {
		if ( log.isDebugEnabled() ) {
			log.debug("getRegisteredOntology: potentialOntUri=" +potentialOntUri);
		}

		OntologyInfo ontology = null;
		if ( OntUtil.isOntResolvableUri(potentialOntUri) ) {
			
			if ( log.isDebugEnabled() ) {
				log.debug("getRegisteredOntology: isOntResolvableUri.");
			}

			try {
				MmiUri mmiUri = new MmiUri(potentialOntUri);
				if ( mmiUri.getVersion() == null ) {
					// unversioned request.  Get most recent
					if ( log.isDebugEnabled() ) {
						log.debug("getRegisteredOntology: unversioned request.");
					}
					
					ontology = db.getMostRecentOntologyVersion(mmiUri);
				}
				else {
					// versioned request. Just try to use the argument as given:
					if ( log.isDebugEnabled() ) {
						log.debug("getRegisteredOntology: versioned request.");
					}

					ontology = db.getOntology(potentialOntUri);
				}
			}
			catch (URISyntaxException e) {
				// Not an MmiUri. Just try to use the argument as given:
				if ( log.isDebugEnabled() ) {
					log.debug("getRegisteredOntology: not an MmiUri.");
				}

				ontology = db.getOntology(potentialOntUri);
			}
		}
		else {
			if ( log.isDebugEnabled() ) {
				log.debug("getRegisteredOntology: is NOT OntResolvableUri.");
			}

			ontology = db.getOntology(potentialOntUri);
		}
		
		return ontology;
	}

	
	/**
	 * Gets the output format according to the given MmiUri and other request parameters.
	 * @param formParam
	 * @param accept
	 * @param extension
	 * @param log
	 */
	static String getOutFormatForMmiUri(String formParam, Accept accept, String extension, Log log) {
		// The response type depends (initially) on the following elements:
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
	static String getOutFormatForNonMmiUri(String formParam, Log log) {
		String outFormat = formParam;
		
		if ( log.isDebugEnabled() ) {
			log.debug("===getOutFormatForNonMmiUri ====== ");
			log.debug("===form = \"" +outFormat+ "\"");
		}
		
		return outFormat;
	}

	/**
	 * Gets the full path to get to the uploaded ontology file.
	 * @param ontology
	 * @return
	 */
	static File getFullPath(OntologyInfo ontology, OntConfig ontConfig, Log log) {
		String full_path = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue() 
			+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
		
		File file = new File(full_path);
		
		if ( ! file.canRead() ) {
			if ( full_path.toLowerCase().endsWith(".owl") ) {
				// try without ".owl":
				full_path = full_path.substring(0, full_path.length() - 4);
			}
			else {
				// Note: the following is a quick workaround for submissions whose URI don't have
				// the .owl extension (the general rule, btw), but whose uploaded files do. 
				// I had to add the .owl extension in orrportal for the aquaportal parsing jobs to work.
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
	 */
	static void serializeModelToOutputStream(Model model, String lang, OutputStream os) {
		String uriForEmptyPrefix = model.getNsPrefixURI("");
		RDFWriter writer = model.getWriter(lang);
		
		String baseUri = null;
		if ( uriForEmptyPrefix != null ) {
			baseUri = JenaUtil2.removeTrailingFragment(uriForEmptyPrefix);
			_setWriterProperty(writer, "xmlbase", baseUri);
		}
		_setWriterProperty(writer, "showXmlDeclaration", "true");
		_setWriterProperty(writer, "relativeURIs", "same-document");
		_setWriterProperty(writer, "tab", "4");
		
		writer.write(model, os, baseUri);
	}

	/** Sets a writer property silently ignoring any UnknownPropertyException */
	private static final void _setWriterProperty(RDFWriter writer, String propName, Object propValue) {
		try {
			writer.setProperty(propName, propValue);
		}
		catch (UnknownPropertyException ex) {
			// ignore.
		}
	}

	/**
	 * Loads the ontology indicated with the "_lo" parameter in the request.
	 * The value is taken as the URI of the ontology.
	 */
	private void _loadOntologyIntoGraph(OntRequest req) throws ServletException, IOException {
		String ontUri = Util.getParam(req.request, "_lo", "");
		if ( ontUri.length() == 0 ) {
			req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing value for _lo parameter");
			return;
		}

		String graphId = Util.getParam(req.request, "_gi", null);

		// explicit version?
		if ( req.version != null ) {
			req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "version parameter not accepted with _lo parameter");
			return;
		}

		OntologyInfo ontology = db.getRegisteredOntologyLatestVersion(ontUri);
		
		if ( ontology == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_loadOntologyIntoGraph: not found: " +ontUri);
			}
			req.response.sendError(HttpServletResponse.SC_NOT_FOUND, ontUri);
			return;
		}
		
		// Load the stored ontology:
		if ( log.isDebugEnabled() ) {
			log.debug("_loadOntologyIntoGraph: loading " +ontUri);
		}
		try {
			tripleStore.loadOntology(ontology, graphId);
		}
		catch (Exception e) {
			log.error("Error loading ontology.", e);
			throw new ServletException("Error loading ontology.", e);
		}
		
		req.response.setContentType("text/plain");
		ServletOutputStream os = req.response.getOutputStream();
		IOUtils.write(ontUri+ " loaded in graph.", os);
		os.close();
	}

	/**
	 * _usri=username
	 */
	private void _getUserInfo(OntRequest req) throws ServletException, IOException {
		StringBuilder result = new StringBuilder();
		
		String ontUri = Util.getParam(req.request, "_usri", "");
		if ( ontUri.length() == 0 ) {
			result.append("ERROR: missing username");
		}
		else {
			Map<String, String> ui = db.getUserInfo(ontUri);
			if ( ui != null ) {
				for (Entry<String, String> pair : ui.entrySet() ) {
					result.append(pair.getKey()+ ": " +pair.getValue()+ "\n");
				}
			}
		}
		
		req.response.setContentType("text/plain");
		ServletOutputStream os = req.response.getOutputStream();
		IOUtils.write(result.toString(), os, "UTF-8");
		os.close();
	}

	
	/**
	 * Executes the reload operation.
	 */
	private void _reload(OntRequest req) throws ServletException, IOException {
		tripleStore.reinit();
	}
	
	/**
	 * Executes the reindex operation.
	 */
	private void _reindex(OntRequest req) throws ServletException, IOException {
		String _reidx = Util.getParam(req.request, "_reidx", "");
		boolean wait = _reidx.length() == 0 || _reidx.equals("wait");
		tripleStore.reindex(wait);
	}

	/**
	 * Executes the clear operation.
	 */
	private void _clear(OntRequest req) throws ServletException, IOException {
		tripleStore.clear();
	}
	
	
	/**
	 * Unregisters a concrete version of an ontology.
	 * 
	 * Required parameter: _unr=ontUri
	 * Optional parameter: version=vvv
	 * 
	 * If the ontUri is "ont"-resolvable and can be parsed as an MmiUri, then the version will be
	 * extracted from it if it's in versioned form. If both the "version" parameter and the version
	 * from the MmiUri can be obtained, then it checks that they are the same. If it is a non-versioned
	 * MmiUri, then the ontUri is adjusted to include the given version for purposes of searching
	 * the database.
	 * 
	 */
	private void _unregisterOntology(OntRequest req) throws ServletException, IOException {
		
		String ontUri = Util.getParam(req.request, "_unr", "");
		if ( ontUri.length() == 0 ) {
			req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing ontology URI");
			return;
		}

		// Determine the concrete version to be deleted.
		// This is based on the parameter "version" or the versioned URI, as appropriate.
		
		// version from parameter "version" if given:
		String version = Util.getParam(req.request, "version", null);

		// version from versioned ontology mmiUri, if given:
		String version2 = null;
		
		if ( OntUtil.isOntResolvableUri(ontUri) ) {
			try {
				MmiUri mmiUri = new MmiUri(ontUri);
				version2 = mmiUri.getVersion();
				
				if ( version2 == null || version2.length() == 0 ) {
					// insert the version fragment so we are able to search in the database
					ontUri = mmiUri.copyWithVersion(version).getOntologyUri();
					if ( log.isDebugEnabled() ) {
						log.debug("_unregisterOntology: inserted version fragment: " +ontUri);
					}

				}
			}
			catch (URISyntaxException e) {
				// Not an MmiUri. Just try to use the argument as given:
				// continue below.
			}
		}

		if ( version == null || version.length() == 0 ) {
			if ( version2 == null || version2.length() == 0 ) {
				req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing ontology version");
				return;
			}
			
			// take version from the MmiUri:
			version = version2;
		}
		else {
			// version parameter given.
			// If the two versions are given, check they are the same:
			if ( version2 != null && ! version.equals(version2) ) {
				req.response.sendError(HttpServletResponse.SC_BAD_REQUEST, "version mismatch");
				return;
			}
		}
		
		// we have the desired version:
		assert version != null && version.length() > 0 ;

		
		String uriAndVerion = "ontUri=" +ontUri+ "  version=" +version;
		
		if ( log.isDebugEnabled() ) {
			log.debug("_unregisterOntology: Deleting " +uriAndVerion);
		}
		
		// get ontology ID (version specific) from the database
		OntologyInfo ontology = db.getOntologyVersion(ontUri, version);
		if ( ontology == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_unregisterOntology: NOT FOUND " +uriAndVerion);
			}
			req.response.sendError(HttpServletResponse.SC_NOT_FOUND, uriAndVerion);
			return;
		}
		
		// TODO capture sessionId appropriately
		String sessionId = "9c188a9b8de0fe0c21b9322b72255fb939a68bb2";
		OntologyDeleter del = new OntologyDeleter(sessionId , ontology.getId());
		
		String result;
		try {
			result = del.execute();
		}
		catch (Exception e) {
			throw new ServletException("Error requesting deletion", e);
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("_unregisterOntology: " +uriAndVerion+ ". Result from aquaportal " +result);
		}
		
		if ( result.startsWith("OK:") ) {
			// successful deletion from bioportal back-end.  
			// Remove ontology from graph:
			try {
				tripleStore.removeOntology(ontology);
			}
			catch (Exception e) {
				log.error("Error removing ontology from graph", e);
				throw new ServletException("Error removing ontology from graph", e);
			}
		}

		req.response.setContentType("text/plain");
		ServletOutputStream os = req.response.getOutputStream();
		IOUtils.write(result, os);
		os.close();
	}

}
