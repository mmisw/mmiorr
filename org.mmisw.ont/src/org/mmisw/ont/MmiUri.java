package org.mmisw.ont;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents a decomposition of a given requested URI.
 * 
 * <p>
 * TODO: NOTE: Constructor is rather ackward (based on full requested URI because that was
 * the main mechanism when this class was created).  Need to make the contructor more friedly,
 * ie., specifically with the fields involved in the contruction!!.
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
	
	// TODO put LATEST_VERSION_INDICATOR as a configuration parameter
	public static final String LATEST_VERSION_INDICATOR = "$";
	

	/**
	 * Syntantically validates a string to be an instance of the pattern:
	 *    <code> ^yyyy[mm[dd][Thh[mm[ss]]]$ </code> 
	 * (where each y, m, d, h, and s is a decimal digit),
	 * or equal to {@link #LATEST_VERSION_INDICATOR}.
	 * 
	 * <p>
	 * Note that this checks for the general appearance of a version; 
	 * TODO full checking that is a valid ISO date.   
	 * 
	 * @throws URISyntaxException if the string is invalid as version 
	 */
	static void checkVersion(String version) throws URISyntaxException {
		boolean ok = version.equals(LATEST_VERSION_INDICATOR) ||
			VERSION_PATTERN.matcher(version).find();
		
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
			// if parts[1] starts with a digit or is LATEST_VERSION_INDICATOR, take that part as the version:
			if ( parts[1].length() > 0 
			&& ( Character.isDigit(parts[1].charAt(0)) 
			     || parts[1].equals(LATEST_VERSION_INDICATOR) ) 
			) {
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
		if ( Character.isDigit(authority.charAt(0)) ) {
			throw new URISyntaxException(fullRequestedUri, "Authority cannot start with digit");
		}
		if ( topic.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing topic in URI");
		}
		if ( Character.isDigit(topic.charAt(0)) ) {
			throw new URISyntaxException(fullRequestedUri, "Topic cannot start with digit");
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
	
	
	public static MmiUri create(String ontologyUri) throws URISyntaxException {
		URI juri = URI.create(ontologyUri);
		
		String path = juri.getPath();
		if ( !path.startsWith("/") ) {
			throw new URISyntaxException(ontologyUri, "not absolute path");
		}
		int idx = path.indexOf('/', 1);
		if ( idx < 0 ) {
			throw new URISyntaxException(ontologyUri, "No root");
		}
		String root = path.substring(0, idx); // include leading slash  
		
		String reqUri = path;
		String contextPath = root;
		MmiUri mmiUri = new MmiUri(ontologyUri, reqUri, contextPath);
		
		return mmiUri;
	}


	
	private MmiUri(String untilRoot, 
			String authority,
			String version,
			String topic,
			String term
	) {
		super();
		this.untilRoot = untilRoot;
		this.authority = authority;
		this.version = version;
		this.topic = topic;
		this.term = term;
		
		this.ontologyUri = untilRoot 
			+ authority 
			+ (version == null ? "" : "/" +version)
			+ "/" +topic
			//+ (term == null || term.length() == 0 ? "" : "/" +term)
		;
	}

	
	public MmiUri clone() {
		return new MmiUri(untilRoot, authority, version, topic, term);
	}
	
	/**
	 * Makes a clone except for the given version, which can be null.
	 * 
	 * @param version the new version.
	 * 
	 * @throws URISyntaxException if version is not null and is invalid. 
	 */
	public MmiUri copyWithVersion(String version) throws URISyntaxException {
		if ( version != null ) {
			checkVersion(version);
		}
		return new MmiUri(untilRoot, authority, version, topic, term);
	}
	
	/**
	 * Makes a clone except for the given version, which can be null.
	 * The regular validation check is skipped: insteasd, if the version if not null,
	 * t's only checked that it does not contain any slashes.
	 * 
	 * @param version the new version.
	 * 
	 * @throws URISyntaxException if version is not null and contains a slash.
	 */
	public MmiUri copyWithVersionNoCheck(String version) throws URISyntaxException {
		if ( version != null && version.indexOf('/') >= 0 ) {
			throw new URISyntaxException(version, "version contains a slash");
		}
		return new MmiUri(untilRoot, authority, version, topic, term);
	}
	
	public boolean equals(Object other) {
		if ( ! (other instanceof MmiUri) ) {
			return false;
		}
		MmiUri o = (MmiUri) other;
		if ( !untilRoot.equals(o.untilRoot) 
		||   !authority.equals(o.authority)
		||   !topic.equals(o.topic)
		||   !term.equals(o.term)
		) {
			return false;
		}
		
		if ( version == null ) {
			return o.version == null;
		}
		
		return version.equals(o.version);
	}

	/** 
	 * @returns the URI corresponding to the ontology (not including the term).
	 *          (<code>http://mmisw.org/ont/mmi/someVocab.owl</code>)
	 */
	public String getOntologyUri() {
		return ontologyUri;
	}

	/** 
	 * @returns the same as {@link #getOntologyUri()}.
	 */
	public String toString() {
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

	/**
	 * Makes a clone except for the given term, which can be null.
	 */
	public MmiUri copyWithTerm(String term) {
		if ( term == null ) {
			term = "";
		}
		return new MmiUri(untilRoot, authority, version, topic, term);
	}
	

}
