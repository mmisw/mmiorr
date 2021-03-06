package org.mmisw.ont;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.OntUtil;
import org.mmisw.ont.util.Util;

/**
 * A request object. It keeps info associated with the request from the client.
 */
public class OntRequest {
	private final Log log = LogFactory.getLog(OntRequest.class);
	final ServletContext servletContext;
    public final HttpServletRequest request;
	public final HttpServletResponse response;
	
	final List<String> userAgentList;
	
	final Accept accept;
    public final Map<String, String[]> params;
	
	final String fullRequestedUri;
	final MmiUri mmiUri;
	final String outFormat;
	
	// in case the ontology info is obtained somehow, use it:
	OntologyInfo ontology;
	
	// in case an explicit ontology version is requested
	final String version;
	
	// in case it is a self-resolvable MmiUri but just until the authority component, in which case
	// only the authority string is saved here but mmiUri will still be null.
	final String authority;
	
	/**
	 * Constructor.
	 * @param servletContext
	 * @param request
	 * @param response
	 */
	OntRequest(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
		this.servletContext = servletContext;
		this.request = request;
		this.response = response;
		
		userAgentList = Util.getHeader(request, "user-agent");
		accept = new Accept(request);
		
		fullRequestedUri = request.getRequestURL().toString();

        params = Util.getParams(request);

		String formParam = Util.getParam(params, "form", "");
		
		//////////////////////////////////////
		// get the requested MmiUri:
		
		// after the following block mmiUriTest will be NON-null only if the requested
		// URI (either from the "uri" parameter if given, or from the fullRequestedUri)
		// is "ont"-resolvable (OntUtil.isOntResolvableUri) and a syntactically valid MmiUri:
		MmiUri mmiUriTest = null;
		try {
            String entityUri = Util.getParam(params, "uri", null);
			if (entityUri != null) {
				// when the "uri" parameter is passed, its value is used.
				if ( OntUtil.isOntResolvableUri(entityUri) ) {
					mmiUriTest = new MmiUri(entityUri, true); // allowing only until authority
				}
			}
			else {
				mmiUriTest = new MmiUri(fullRequestedUri, true);  // allowing only until authority
			}
		}
		catch (URISyntaxException e) {
			// Ok, not a regular MmiUri request.
		}
		
		String outFormatTest;
		String versionTest = null;
		
		if ( mmiUriTest != null ) {
			// get output format to be used:
			outFormatTest = OntServlet.getOutFormatForMmiUri(formParam, accept, mmiUriTest.getExtension(), log);
		}
		else {
			// NOT a regular MmiUri request.
			outFormatTest = OntServlet.getOutFormatForNonMmiUri(formParam, log); 
		}
		
		if ( outFormatTest.length() == 0 ) {     
			// No explicit outFormat.
			// use content negotiation:
			
			outFormatTest = Util.getOutFormatByContentNegotiation(accept);
			
			log.debug("Not explicit output format given (either file extension or form parameter). " +
					"Using [" +outFormatTest+ "] by content negotiation."
			);
		}

		if ( Util.yes(params, "version") ) {
			// explicit version given:
			versionTest = Util.getParam(params, "version", null);
		}

		String authorityTest = null;
		
		if ( mmiUriTest != null && mmiUriTest.getTopic() == "" ) {
			// it is only until the authority. Just keep the authority
			authorityTest = mmiUriTest.getAuthority();
			mmiUriTest = null;
		}
		
		authority = authorityTest;
		
		mmiUri = mmiUriTest;
		outFormat = outFormatTest;
		version = versionTest;

		if ( log.isDebugEnabled() ) {
			_debug();
		}
	}


	private void _debug() {
		List<String> pcList = Util.getHeader(request, "PC-Remote-Addr");
		String dominating = accept.dominating == null ? null : "\"" +accept.dominating+ "\"";
		log.debug("__Request: fullRequestedUri: " +fullRequestedUri);

		StringBuilder sbParams = new StringBuilder("{");
		for (Entry<String, String[]> pair : params.entrySet() ) {
			sbParams.append(pair.getKey()+ " => " + Arrays.asList(pair.getValue()));	
		}
		sbParams.append("}");
		log.debug("                     Params: " +sbParams);
		
		log.debug("                 user-agent: " +userAgentList);
		log.debug("             PC-Remote-Addr: " +pcList);
		log.debug("             Accept entries: " +accept.getEntries());
		log.debug("           Dominating entry: " +dominating);
		
		log.debug("                     mmiUri: " +mmiUri);
		log.debug("                  authority: " +authority);
		log.debug("                  outFormat: " +outFormat);
		log.debug("                    version: " +version);			
	}
	
	public String toString() {
		return "<" +
			"fullRequestedUri=" +fullRequestedUri+
			" mmiUri=" +mmiUri+
			" authority=" +authority+
			" outFormat=" +outFormat+
			" version=" +version+
			" " +(ontology != null ? "ontologyUri=" +ontology.getUri() : "ontology=null") +
			">"
		;			
	}
}