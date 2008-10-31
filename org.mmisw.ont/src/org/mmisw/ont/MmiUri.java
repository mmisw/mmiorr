package org.mmisw.ont;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents a decomposition of a given requested URI.
 * 
 * <p>
 * TODO: NOTE: <i>version</i> component not yet handled.
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

	private static Pattern VERSION_PATTERN = 
		Pattern.compile("^\\d{4}(\\d{2}(\\d{2})?)?(T\\d{2})?(\\d{2}(\\d{2})?)?$");
	
	
	/**
	 * Syntantically validates a string according a pattern that can be written as:
	 *    <code> ^yyyy[mm[dd][Thh[mm[ss]]]$ </code>,
	 * where each y, m, d, h, and s is a decimal digit.
	 * 
	 * <p>
	 * Note that this checks for the general appearance of a version; 
	 * TODO full checking that is a valid ISO date.   
	 * 
	 * @throws URISyntaxException if the string is invalid as version 
	 */
	static void checkVersion(String version) throws URISyntaxException {
		boolean ok = VERSION_PATTERN.matcher(version).find();
		if ( ! ok ) {				
			throw new URISyntaxException(version, "Invalid version string: " +version);
		}

	}
	
	private final String authority;
	private final String version;
	private final String topic;
	private final String term;
	private final String ontologyUri;
	
	private final String untilRoot;
	
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
		
		int rootIdx = fullRequestedUri.indexOf(afterRoot);
		untilRoot = fullRequestedUri.substring(0, rootIdx);
		assert untilRoot.endsWith("/");
		
		String[] parts = afterRoot.split("/");

		// Either:  2 parts = { mmi, someVocab.owl }
		//     or:  3 parts = { mmi, someVocab.owl, someTerm }
		//               or = { mmi, someVersion, someVocab.owl}
		//     or:  4 parts = { mmi, someVersion, someVocab.owl, someTerm }
		if ( parts.length < 2 || parts.length > 4 ) {
			throw new URISyntaxException(fullRequestedUri, "2, 3, or 4 parts expected: "
					+Arrays.asList(parts));
		}

		authority =  parts[0];
		
		String _version = null; // will remain null if not given.
		String _topic = "";
		String _term = "";     // will remain "" if not given
		
		if ( parts.length == 2 ) {
			_topic = parts[1];
		}
		else {
			int idx_topic = 1;
			// if parts[1] starts with a digit, take that part as the version:
			if ( parts[1].length() > 0 && Character.isDigit(parts[1].charAt(0)) ) {
				// Ok, so take the version and update index for topic
				_version = parts[1];
				idx_topic = 2;
			}
			_topic = parts[idx_topic];
			if ( idx_topic + 1 < parts.length ) {
				_term = parts[idx_topic + 1];
			}
		}
		
		version = _version;
		topic =   _topic;
		term =    _term;
		
		if ( authority.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing authority in URI");
		}
		if ( topic.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing topic in URI");
		}
		
		// check version, if given:
		if ( version != null ) {
			checkVersion(version);
		}
		
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
	 * @returns the version 
	 *          (<code>null</code> in the example.)
	 */
	public String getVersion() {
		return version;
	}

	/** 
	 * @returns the topic.
	 *          (<code>someVocab.owl</code>)
	 * */
	public String getTopic() {
		return topic;
	}

	/** 
	 * @returns the extension of the topic.
	 *          (<code>.owl</code>)
	 * */
	public String getTopicExtension() {
		String ext = "";
		int dotIdx = topic.lastIndexOf('.');
		if ( dotIdx >= 0) {
			ext = topic.substring(dotIdx);
		}
		return ext;
	}

	/**
	 * @returns the term.
	 *          (<code>someTerm</code>)
	 */
	public String getTerm() {
		return term;
	}
	
	/** 
	 * Returns the URI corresponding to the term.
	 * 
	 * @param removeExt true to remove ontology extension, if any; false to keep ontologyUri.
	 *        Note that any trailing pound signs are always removed.
	 *        
	 * @param sep The separator to use between the ontology and the term, typically "#" or "/".
	 * 
	 * @returns the URI corresponding to the term.
	 *          If removeExt is true: <code>http://mmisw.org/ont/mmi/someVocab#someTerm</code>;
	 *          <br/>
	 *          otherwise: <code>http://mmisw.org/ont/mmi/someVocab.owl#someTerm</code>
	 *          (assumming sep == "#").
	 */
	public String getTermUri(boolean removeExt, String sep) {
		String termUri;
		if ( removeExt ) {
			String ext = getTopicExtension();
			if ( ext.length() > 0 ) {
				// replace any dot with \\. 
				ext = ext.replaceAll("\\.", "\\\\.");
				// so, the replacing pattern is well formed:
				termUri = ontologyUri.replaceAll(ext+ "(#)*$", "") + sep + term;
			}
			else {
				termUri = ontologyUri.replaceAll("#+$", "") + sep + term;	
			}
		}
		else {
			termUri = ontologyUri.replaceAll("#+$", "") + sep + term;
		}
		return termUri;
	}
	
	public String getUntilRoot() {
		return untilRoot;
	}

	/** 
	 * Gets an Ontology URI with the given topic extension, which can be empty.
	 */
	public String getOntologyUriWithTopicExtension(String topicExt) {
		String topicNoExt = topic;
		String ext = getTopicExtension();
		if ( ext.length() > 0 ) {
			int idx = topic.lastIndexOf(ext);
			topicNoExt = topic.substring(0, idx);
		}
		String uri = untilRoot + authority+ "/" 
				   + (version != null ? version + "/" : "")
				   + topicNoExt + topicExt;
		
		return uri;
	}

}
