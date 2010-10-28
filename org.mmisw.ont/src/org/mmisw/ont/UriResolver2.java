package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.util.ServletUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * This is main dispatcher used by the entry point OntServlet.
 * 
 * <p>
 * TODO: update documentation.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * @author Carlos Rueda
 */
@ThreadSafe
public class UriResolver2 {
	
	private final Log log = LogFactory.getLog(UriResolver2.class);
	
	private final OntConfig ontConfig;
	private final Db db;

	/** Used to obtain the thread-local OntRequest */
	private final OntServlet ontServlet;
	
	/**
	 * 
	 * @param ontServlet 
	 *                    Used to obtain the thread-local OntRequest.
	 * @param ontConfig
	 * @param db
	 * @param tripleStore
	 */
	public UriResolver2(OntServlet ontServlet, OntConfig ontConfig, Db db, ITripleStore tripleStore) {
		this.ontServlet = ontServlet;
		this.ontConfig = ontConfig;
		this.db = db;
	}
	
	private OntRequest _getThreadLocalOntRequest() {
		return ontServlet.getThreadLocalOntRequest();
	}
	
	/**
	 * Represents a response to be sent to the client
	 */
	abstract class Response {
		/** performs the actual response */
		abstract void dispatch() throws IOException ;
	}
	
	class RedirectResponse extends Response {
		final String url;
		
		RedirectResponse(String url) {
			this.url = url;
		}
		
		void dispatch() throws IOException {
			if ( log.isDebugEnabled() ) {
				log.debug("Redirecting to latest version: " + url);
			}
			OntRequest ontReq = _getThreadLocalOntRequest();
			String redir = ontReq.response.encodeRedirectURL(url);
			ontReq.response.sendRedirect(redir);
		}
	}
	
	class NotFoundResponse extends Response {
		final String res;
		
		public NotFoundResponse(String res) {
			this.res = res;
		}
		void dispatch() throws IOException {
			OntRequest ontReq = _getThreadLocalOntRequest();
			ontReq.response.sendError(HttpServletResponse.SC_NOT_FOUND, res);
		}
	}
	
	class InternalErrorResponse extends Response {
		final String msg;
		final Throwable thr;
		
		public InternalErrorResponse(String msg) {
			this(msg, null);
		}
		public InternalErrorResponse(String msg, Throwable thr) {
			this.msg = msg;
			this.thr = thr;
		}
		void dispatch() throws IOException {
			log.error(msg, thr);
			OntRequest ontReq = _getThreadLocalOntRequest();
			ontReq.response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
		}
	}
	

	class ModelResponse extends Response {
		final Model model;
		
		ModelResponse(Model model) {
			this.model = model;
		}
		
		void dispatch() throws IOException {
			OntRequest ontReq = _getThreadLocalOntRequest();
			
			if ( log.isDebugEnabled() ) {
				log.debug(this.getClass().getName()+ ": dispatching with outFormat=" +ontReq.outFormat);
			}

			ServletOutputStream os = ontReq.response.getOutputStream();

			///////////////////////////////////////////////////////////////////
			// OWL
			if ( ontReq.outFormat.equalsIgnoreCase("owl")
			||   ontReq.outFormat.equalsIgnoreCase("rdf")
			) {
				if ( log.isDebugEnabled() ) {
					log.debug(this.getClass().getName()+ ": Serializing to RDF/XML-ABBREV");
				}
				ServletUtil.setContentTypeRdfXml(ontReq.response);
//				is = OntServlet.serializeModel(model, "RDF/XML-ABBREV");
				OntServlet.serializeModelToOutputStream(model, "RDF/XML-ABBREV", os);
			}
			
			///////////////////////////////////////////////////////////////////
			// N3
			else if ( ontReq.outFormat.equalsIgnoreCase("n3") ) {
				ServletUtil.setContentTypeTextPlain(ontReq.response);
				OntServlet.serializeModelToOutputStream(model, "N3", os);
			}
			
			///////////////////////////////////////////////////////////////////
			// NT
			else if ( ontReq.outFormat.equalsIgnoreCase("nt") ) {
				ServletUtil.setContentTypeTextPlain(ontReq.response);
				OntServlet.serializeModelToOutputStream(model, "N-TRIPLE", os);
			}
			
			///////////////////////////////////////////////////////////////////
			// TTL
			else if ( ontReq.outFormat.equalsIgnoreCase("ttl") ) {
				ServletUtil.setContentTypeTextPlain(ontReq.response);
				OntServlet.serializeModelToOutputStream(model, "TURTLE", os);
			}
			
			///////////////////////////////////////////////////////////////////
			// HTML
			else if ( ontReq.outFormat.equalsIgnoreCase("html") ) {
				
				// Redirect to the appropriate service.
				
				String ontologyUri;
				
				if ( ontReq.ontology != null ) {
					ontologyUri = ontReq.ontology.getUri();
				}
				else if ( ontReq.mmiUri != null ) {
					// Note: drop any extension here (the Orr service will do the appropriate request
					// back to this Ont service): 
					ontologyUri = ontReq.mmiUri.getOntologyUriWithExtension("");
				}
				else {
					ontologyUri = ontReq.fullRequestedUri;
				}
				
				String portalServiceUrl = OntConfig.Prop.PORTAL_SERVICE_URL.getValue();
				
				String url = portalServiceUrl+ "/#" +ontologyUri;
				if ( log.isDebugEnabled() ) {
					log.debug("REDIRECTING TO: " +url);
				}
				String redir = ontReq.response.encodeRedirectURL(url);
				ontReq.response.sendRedirect(redir);
				return;
			}
			
			///////////////////////////////////////////////////////////////////
			// BAD REQUEST
			else {
				ontReq.response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"ModelResponse: outFormat " +ontReq.outFormat+ " not recognized"
				);
				return;
			}
			
			os.close();
		}
		
	}

	/** Response for a term -- currently it behaves exactly as ModelResponse */
	class TermResponse extends ModelResponse {
		TermResponse(Model termModel) {
			super(termModel);
		}
	}
	
	/** Response for an ontology -- currently it behaves exactly as ModelResponse */
	class OntologyResponse extends ModelResponse {
		OntologyResponse(Model ontologyModel) {
			super(ontologyModel);
		}
	}
	
	
	
	/**
	 * The main dispatcher.
	 */
	void service() throws ServletException, IOException {
		OntRequest ontReq = _getThreadLocalOntRequest();
		
		Response resp = null;
		
		
		if ( log.isDebugEnabled() ) {
			log.debug("UriResolver2.service: mmiUri = " +ontReq.mmiUri);
		}
		
		if ( ontReq.mmiUri != null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("UriResolver2._getResponseForMmiUri.");
			}
			resp = _getResponseForMmiUri();
		}
		else {
			// NOT a regular MmiUri request.
			if ( log.isDebugEnabled() ) {
				log.debug("UriResolver2._getResponseForNonMmiUri.");
			}
			resp = _getResponseForNonMmiUri();
		}
		
		if ( resp != null ) {
			resp.dispatch();
		}
		else {
			String fullRequestedUri = ontReq.request.getRequestURL().toString();
			ontReq.response.sendError(HttpServletResponse.SC_NOT_FOUND, fullRequestedUri);		
		}
	}
	
	
	/**
	 * Gets the response for a requested MmiUri.
	 */
	private Response _getResponseForMmiUri() throws ServletException, IOException {
		
		// the ontology we need to access:
		OntologyInfo ontology = null;
		
		
		// this flag will be true if we have an unversioned request, see Issue 24.
		boolean unversionedRequest = false;
		MmiUri foundMmiUri = null;
		
		OntRequest ontReq = _getThreadLocalOntRequest();
		String version = ontReq.mmiUri.getVersion();
		if ( version == null || version.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
			
			//
			// handling of unversioned and latest-version requests.  (see Issue 24)
			//
			
			// Get latest version trying all possible topic extensions:
			OntologyInfo mostRecentOntology = db.getMostRecentOntologyVersion(ontReq.mmiUri);

			if ( mostRecentOntology != null ) {
				
				try {
					//
					// Note that mostRecentOntology.getUri() won't have the term component.
					// So, we have to transfer it to foundMmiUri:
					//
					foundMmiUri = new MmiUri(mostRecentOntology.getUri()).copyWithTerm(ontReq.mmiUri.getTerm());
					
					if ( log.isDebugEnabled() ) {
						log.debug("Found ontology version: " +mostRecentOntology.getUri());

						if ( ! ontReq.mmiUri.getExtension().equals(foundMmiUri.getExtension()) ) {
							log.debug("Restored requested extension to: " +ontReq.mmiUri);
						}
					}
					
					// also, restore the original requested extension:
					foundMmiUri = foundMmiUri.copyWithExtension(ontReq.mmiUri.getExtension());
				}
				catch (URISyntaxException e) {
					return new InternalErrorResponse("Shouldn't happen", e);
				}
				
				// OK: here, foundMmiUri refers to the latest version.
				
				if ( version == null ) {
					// unversioned request.
					unversionedRequest = true;
					
					ontology = mostRecentOntology;
					
					// and let the dispatch continue.
				}
				else {
					// request was with version = MmiUri.LATEST_VERSION_INDICATOR.
					// Use a redirect so the user gets the actual latest version:
					String latestUri = foundMmiUri.getTermUri();
					return new RedirectResponse(latestUri);
				}
			}
			else {
				// No versions available!
				if ( log.isDebugEnabled() ) {
					log.debug("No versions found.");
				}
				return null;
			}
		}
		else {
			// Version explicitly given: let the dispatch continue.
		}
		

		if ( ontology == null ) {
			// obtain info about the ontology:
			ontology = db.getOntologyWithExts(ontReq.mmiUri, null);
		}
		
		if ( ontology == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_getResponseForMmiUri: not dispatched here. mmiUri = " +ontReq.mmiUri);
			}
			return null;
		}
		
		final File file = OntServlet.getFullPath(ontology, ontConfig, log);
		
		if ( !file.canRead() ) {
			// This should not happen.
			return new InternalErrorResponse(
					file.getAbsolutePath()+ ": internal error: uploaded file "
					+ (file.exists() ? "exists but cannot be read." : "not found.")
					+ "Please, report this bug."
			);
		}

		// original model:
		final String uriFile = file.toURI().toString();
		final OntModel originalModel = JenaUtil2.loadModel(uriFile, false);

		if ( originalModel == null ) {
			// This should not happen.
			return new InternalErrorResponse(
					file.getAbsolutePath()+ ": internal error: uploaded file "
					+ "cannot be read as an ontology model. "
					+ "Please, report this bug."
			);
		}

		// corresponding unversioned model in case is requested: 
		OntModel unversionedModel = null;
		
		if ( unversionedRequest ) {

			unversionedModel = UnversionedConverter.getUnversionedModel(originalModel, ontReq.mmiUri);
			
			if ( unversionedModel != null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("_getResponseForMmiUri: obtained unversioned model");
				}
			}
			else {
				// error in conversion to unversioned version.
				// this is unexpected. 
				// Continue with original model, if necessary; see below.
				log.error("_getResponseForMmiUri: unexpected: error in conversion to unversioned version.  But continuing with original model");
			}
		}

		String term = ontReq.mmiUri.getTerm();
		
		OntModel model = unversionedModel != null ? unversionedModel : originalModel;

		if ( term.length() == 0 ) {
			return new OntologyResponse(model);
		}
		else {
			// Term included.
			if ( log.isDebugEnabled() ) {
				log.debug("_getResponseForMmiUri: term=[" +term+ "].");
			}
			Model termModel = TermExtractor.getTermModel(model, ontReq.mmiUri);
			if ( termModel != null ) {
				return new TermResponse(termModel);
			}
			else {
				return null;
			}
		}
	}

	
	/**
	 * Gets the response for a given ontology
	 */
	void serviceForOntology() throws ServletException, IOException {
		OntRequest ontReq = _getThreadLocalOntRequest();
		
		if ( log.isDebugEnabled() ) {
			log.debug("serviceForOntology: req: " +ontReq);
		}
		
		Response resp = null;
		
		final File file = OntServlet.getFullPath(ontReq.ontology, ontConfig, log);
		
		if ( !file.canRead() ) {
			// This should not happen.
			String error = file.getAbsolutePath()+ ": internal error: uploaded file "
				+ (file.exists() ? "exists but cannot be read." : "not found.")
				+ "Please, report this bug."
			;
			log.warn(error);
			resp = new InternalErrorResponse(error);
		}
		else {
			// original model:
			final String uriFile = file.toURI().toString();
			final OntModel originalModel = JenaUtil2.loadModel(uriFile, false);

			if ( originalModel == null ) {
				// This should not happen.
				String error = file.getAbsolutePath()+ ": internal error: uploaded file "
					+ "cannot be read as an ontology model. "
					+ "Please, report this bug."
				;
				log.warn(error);
				resp = new InternalErrorResponse(error);
			}
			else {
				OntModel model = originalModel;

				// no explicit version requested and it's an unversioned MmiUri?
				if ( ontReq.version == null && ontReq.mmiUri != null && ontReq.mmiUri.getVersion() == null ) {
					model = UnversionedConverter.getUnversionedModel(originalModel, ontReq.mmiUri);
				}
				// issue #252: "omv:version gone?"
				else {
					String assignedVersion = JenaUtil2.setVersionFromCreationDateIfNecessary(model);
					if ( assignedVersion != null ) {
						// there actually was a synthetic assignment to omv.version
						log.info("synthetic assignment to omv.version: " +assignedVersion);
					}
				}

				resp = new OntologyResponse(model);
			}
		}
		
		if ( resp != null ) {
			resp.dispatch();
		}
		else {
			ontReq.response.sendError(HttpServletResponse.SC_NOT_FOUND, ontReq.ontology.getUri());		
		}

	}

	
	private Response _getResponseForNonMmiUri() {
		// ...   
		// NOT HANDLED YET -- I'm still testing the main dispatch above so I should be
		// only making regular MmiUri request for the moment. (see "ur2" in OntServlet)
		return null;
	}


}
