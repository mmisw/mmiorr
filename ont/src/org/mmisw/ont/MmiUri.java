package org.mmisw.ont;

import java.net.URISyntaxException;

/**
 * Represents a decomposition of a given requested URI.
 * 
 * <p>
 * TODO: NOTE: <i>version</> component not yet handled.
 * 
 * <p>
 * The following requested URI is used as example to illustrate the various
 * operations:
 * <pre>
       http://mmisw.org/ont/mmi/someVocab.owl/someTerm
 * </pre>
 *  
 * The parsing is done in a way that is independent of the actual
 * server host:port ("http://mmisw.org" in the example) and the "root" directory component
 * ("ont" in the example).
 * 
 * @author Carlos Rueda
 */
public class MmiUri {

	private final String authority;
	private final String topic;
	private final String term;
	private final String ontologyUri;
	
	/**
	 * Creates an MmiUri by analysing the given request.
	 * 
	 * @param fullRequestedUri (<code>http://mmisw.org/ont/mmi/someVocab.owl/someTerm</code>)
	 * @param requestedUri     (<code>/ont/mmi/someVocab.owl/someTerm</code>)
	 * @param contextPath      (<code>/ont</code>)
	 * @throws URISyntaxException
	 * 
	 * @throws URISyntaxException if the requested URI is invalid according to
	 *         the MMI recommendation:
	 *         <ul>
	 *            <li> <i>authority</i> is missing  </li>
	 *            <li> <i>topic</i> is missing  </li>
	 *         </ul>
	 */
	public MmiUri(String fullRequestedUri, String requestedUri, String contextPath) throws URISyntaxException {
		// parsing described with an example:
		
		// afterRoot = /mmi/someVocab.owl/someTerm
		String afterRoot = requestedUri.substring(contextPath.length());
		if ( afterRoot.startsWith("/") ) {
			afterRoot = afterRoot.substring(1);
		}
		
		// parts = { mmi, someVocab.owl, someTerm }
		String[] parts = afterRoot.split("/", 3);
		
		authority =  parts.length >= 1 ? parts[0] : "";
		if ( authority.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing authority in URI");
		}
		
		topic = parts.length >= 2 ? parts[1] : "";
		if ( topic.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing topic in URI");
		}
		
		term =  parts.length >= 3 ? parts[2] : "";
		
		// ontologyUri is everything but the term and without trailing slashes
		if ( term.length() > 0 ) {
			int idxTerm = fullRequestedUri.lastIndexOf(term);
			ontologyUri = fullRequestedUri.substring(0, idxTerm).replaceAll("/+$", ""); 
		}
		else {
			ontologyUri = fullRequestedUri.replaceAll("/+$", "");
		}
	}

	/** 
	 * @returns the URI corresponding to the ontology (not including the term).
	 *          (<code>http://mmisw.org/ont/mmi/someVocab.owl</code>)
	 */
	public String getOntologyUri() {
		return ontologyUri;
	}

	/** 
	 * @returns the authority, e.g, "mmi" 
	 *          (<code>mmi</code>)
	 */
	public String getAuthority() {
		return authority;
	}

	/** 
	 * @returns the topic.
	 *          (<code>someVocab.owl</code>)
	 * */
	public String getTopic() {
		return topic;
	}

	/**
	 * @returns the term.
	 *          (<code>someTerm</code>)
	 */
	public String getTerm() {
		return term;
	}

}
