package org.mmisw.ont.mmiuri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

/**
 * Represents an MMI ontology or term URI.
 * 
 * <p>
 * The parsing is done in a way that is independent of the actual
 * server host:port (<code>http://mmisw.org</code> in the example) 
 * and the "root" directory component (<code>ont</code> in the example).
 * 
 * <p>
 * The following requested URI is used as example to illustrate the various operations:
 * <pre>
       http://mmisw.org/ont/mmi/someVocab.owl/someTerm
 * </pre>
 *  
 * Note that the "extension" (<code>.owl</code>) can also be indicated in the term component, eg., 
 * <pre>
       http://mmisw.org/ont/mmi/someVocab/someTerm.owl
 * </pre>
 * The two strings refer to the same ontology and term.
 * 
 * <p>
 * Note that the extension is NOT included in any of the report operations except
 * {@link #getExtension()}.
 * 
 * <p>
 * As a concrete example with all the main operations:
<pre>
	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/someTerm.owl");
	
	assertEquals("http://mmisw.org/ont/mmi/someVocab",          mmiUri.getOntologyUri());
	assertEquals("http://mmisw.org/ont/mmi/someVocab/someTerm", mmiUri.getTermUri());
	
	assertEquals("mmi",       mmiUri.getAuthority());
	assertEquals(null,        mmiUri.getVersion());
	assertEquals("someVocab", mmiUri.getTopic());
	assertEquals("someTerm",  mmiUri.getTerm());
	assertEquals(".owl",      mmiUri.getExtension());
	
	assertEquals("http://mmisw.org/ont/",  mmiUri.getUntilRoot());

</pre>
 * 
 * @author Carlos Rueda
 */
@Immutable
public final class MmiUri implements Cloneable {

	private static final Pattern VERSION_PATTERN = 
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
	public static void checkVersion(String version) throws URISyntaxException {
		boolean ok = version.equals(LATEST_VERSION_INDICATOR) ||
			VERSION_PATTERN.matcher(version).find();
		
		if ( ! ok ) {				
			throw new URISyntaxException(version, "Invalid version string: " +version);
		}

	}
	

	///////////////////////////////////////////////////////////////////////////////////////////
	//                      Instance:
	///////////////////////////////////////////////////////////////////////////////////////////
	
	// Full URI used as an example: http://mmisw.org/ont/mmi/someVocab.owl/someTerm
	
	/** The prefix until the root including the trailing slash 
	 *         (<code>http://mmisw.org/ont/</code>)
	 */
	private final String untilRoot;

	/** The authority (<code>mmi</code>)*/
	private final String authority;
	
	/** The version (<code>null</code>)*/
	private final String version;
	
	/** The topic (<code>someVocab</code>)*/
	private final String topic;
	
	/** The term (<code>someTerm</code>)*/
	private final String term;
	
	/** The extension (<code>.owl</code>)*/
	private final String extension;
	
	/** 
	 * Lazily initialized.
	 * Note that this is still an immutable class; this hashCode member is private, and
	 * is computed from final members. The hash code is computed lazily because instances of
	 * this class will not always be used as keys in maps or something.
	 * (FindBugs generates a warning because this member is not final; I could probably avoid
	 * the warning with a FB annotation but am not including any FB library just for this.) 
	 */
	private volatile Integer hashCode;
	

	/**
	 * Creates an MmiUri by parsing the given string.
	 * 
	 * @param str (<code>http://mmisw.org/ont/mmi/someVocab.owl/someTerm</code>)
	 * 
	 * @throws URISyntaxException if the requested URI is invalid according to the MMI specification.
	 */
	public MmiUri(String str) throws URISyntaxException {
		this(str, false);
	}	

	/**
	 * Creates an MmiUri by parsing the given string.
	 * 
	 * @param str (<code>http://mmisw.org/ont/mmi/someVocab.owl/someTerm</code>)
	 * @param allowUntilAuthority If true, only until the authority component 
	 *              (<code>http://mmisw.org/ont/mmi</code>) will be accepted.
	 * 
	 * @throws URISyntaxException if the requested URI is invalid according to the MMI specification.
	 */
	public MmiUri(String str, boolean allowUntilAuthority) throws URISyntaxException {
		URI juri = new URI(str);
		
		//
		// Fix of issue #123: "encoded URI not properly handled"
		// str may contain encoded parts, and note that URI.getPath() returns a *decoded* path.
		// So, use URI.getRawPath() to keep everything consistent for extraction purposes.
		//
		final String path = juri.getRawPath();
		
		if ( path == null ) {
			throw new URISyntaxException(str, "not path");
		}

		if ( !path.startsWith("/") ) {
			throw new URISyntaxException(str, "not absolute path");
		}
		int idx = path.indexOf('/', 1);
		if ( idx < 0 ) {
			throw new URISyntaxException(str, "No root");
		}
		String root = path.substring(0, idx); // include leading slash  
		
		String reqUri = path;
		String contextPath = root;
		
		
		
		String fullRequestedUri = str;
		String requestedUri = reqUri;
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

		// Either:  1 part = { mmi }  if allowUntilAuthority
		//     or:  2 parts = { mmi, someVocab.owl }
		//     or:  3 parts = { mmi, someVocab.owl, someTerm }
		//               or = { mmi, someVersion, someVocab.owl}
		//     or:  4 parts = { mmi, someVersion, someVocab.owl, someTerm }
		if ( parts.length < 2 || parts.length > 4 ) {
			// if only one part, then accepted only if allowUntilAuthority
			if ( parts.length == 1 && allowUntilAuthority ) {
				// OK
			}
			else {
				throw new URISyntaxException(fullRequestedUri, "2, 3, or 4 parts expected: "
						+Arrays.asList(parts));
			}
		}

		String _version = null; // will remain null if not given.
		String _topic = "";
		String _term = "";     // will remain "" if not given
		
		if ( parts.length == 1 ) {
			// OK
			assert allowUntilAuthority;
		}
		else if ( parts.length == 2 ) {
			_topic = parts[1];
		}
		else if ( parts.length == 4 ) {
			_version = parts[1];
			_topic = parts[2];
			_term = parts[3];
		}
		else {
			assert parts.length == 3 ;
			
			// Determine which of the two cases (a) or (b) we are dealing with:
			//   (a) { mmi, someVocab.owl, someTerm }
			//   (b) { mmi, someVersion, someVocab.owl}

			// if parts[1] starts with a digit or is LATEST_VERSION_INDICATOR, take that part as the version:
			if ( parts[1].length() > 0 
			&& ( Character.isDigit(parts[1].charAt(0)) 
			     || parts[1].equals(LATEST_VERSION_INDICATOR) ) 
			) {
				// case (b): 
				_version = parts[1];
				_topic = parts[2];
			}
			else {
				// case (a)
				_topic = parts[1];
				_term = parts[2];
			}
		}
		
		String _authority = parts[0];
				
		// remove any extension from _authority, _topic and _term, but remember them to assign this.extension below
		
		Set<String> extensions = new HashSet<String>();
		
		String _authorityExt = "";
		int dotIdx = _authority.lastIndexOf('.');
		if ( dotIdx >= 0) {
			_authorityExt = _authority.substring(dotIdx).toLowerCase();
			_authority = _authority.substring(0, dotIdx);
			extensions.add(_authorityExt);
		}

		String _topicExt = "";
		dotIdx = _topic.lastIndexOf('.');
		if ( dotIdx >= 0) {
			_topicExt = _topic.substring(dotIdx).toLowerCase();
			_topic = _topic.substring(0, dotIdx);
			extensions.add(_topicExt);
		}
		
		String _termExt = "";
		dotIdx = _term.lastIndexOf('.');
		if ( dotIdx >= 0) {
			_termExt = _term.substring(dotIdx).toLowerCase();
			_term = _term.substring(0, dotIdx);
			extensions.add(_termExt);
		}
		
		
		if ( extensions.size() > 1 ) {
			// there are different extensions:
			throw new URISyntaxException(fullRequestedUri, "Explicit extensions given but different: " +extensions);
		}

		////////////////////////////////////////////////////////////////////////////////
		// now, assign to my final fields and do remaining checks:
		
		authority =  _authority;
		
		version = _version;
		topic =   _topic;
		term =    _term;
		
		extension = extensions.size() == 1 ? extensions.iterator().next() : "";
		
		
		if ( authority.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing authority in URI");
		}
		if ( authority.startsWith("-") ) {
			throw new URISyntaxException(fullRequestedUri, "Authority cannot start with hyphen");
		}
		if ( Character.isDigit(authority.charAt(0)) ) {
			throw new URISyntaxException(fullRequestedUri, "Authority cannot start with digit");
		}
		if ( ! allowUntilAuthority && topic.length() == 0 ) {
			throw new URISyntaxException(fullRequestedUri, "Missing topic in URI");
		}
		if ( topic.length() > 0 && Character.isDigit(topic.charAt(0)) ) {
			throw new URISyntaxException(fullRequestedUri, "Topic cannot start with digit");
		}
		
		// check version, if given:
		if ( version != null ) {
			checkVersion(version);
		}
	}

	
	private MmiUri(String untilRoot, 
			String authority,
			String version,
			String topic,
			String term,
			String extension
	) {
		super();
		this.untilRoot = untilRoot;
		this.authority = authority;
		this.version = version;
		this.topic = topic;
		this.term = term;
		this.extension = extension;
	}

	
	public MmiUri clone() {
		return new MmiUri(untilRoot, authority, version, topic, term, extension);
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
		return new MmiUri(untilRoot, authority, version, topic, term, extension);
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
		return new MmiUri(untilRoot, authority, version, topic, term, extension);
	}
	
	/**
	 * Makes a clone except for the given extension.
	 * 
	 * @param newExtension the new extension.
	 * 
	 * @throws URISyntaxException if newExtension is null or contains a slash.
	 */
	public MmiUri copyWithExtension(String newExtension) throws URISyntaxException {
		if ( newExtension == null || newExtension.indexOf('/') >= 0 ) {
			throw new URISyntaxException(newExtension, "newExtension is null or contains a slash");
		}
		return new MmiUri(untilRoot, authority, version, topic, term, newExtension);
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
			if ( o.version != null ) {
				return false;
			}
		}
		else if ( !version.equals(o.version) ) {
			return false;			
		}
		
		if ( extension == null ) {
			return o.extension == null;
		}
		
		return extension.equals(o.extension);
	}
	
	public int hashCode() {
		Integer result = hashCode;
		if ( result == null ) {
			synchronized (this) {
				result = hashCode;
				if ( result == null ) {
					hashCode = result = _computeHashCode();
				}
			}
		}
		return result.intValue();
	}

	private int _computeHashCode() {
		int result = 17;
		result += 31 * result + untilRoot.hashCode();
		result += 31 * result + authority.hashCode();
		result += 31 * result + (version != null ? version.hashCode() : 0);
		result += 31 * result + topic.hashCode();
		result += 31 * result + (extension != null ? extension.hashCode() : 0);
		result += 31 * result + (term != null ? term.hashCode() : 0);
		return result;
	}


	/** 
	 * @returns the URI corresponding to the ontology (not including the term).
	 *          (<code>http://mmisw.org/ont/mmi/someVocab.owl</code>)
	 */
	public String getOntologyUri() {
		String uri = untilRoot + authority+ "/" 
		   + (version != null ? version + "/" : "")
		   + topic;
		return uri;
	}

	/** 
	 * Returns the URI corresponding to the term using a slash as separator, so
	 * it returns the same as <code>getTermUri("/")</code>.
	 * 
	 * @returns the URI corresponding to the term.
	 *          If no term is associated, then it returns the same as {@link #getOntologyUri()}.
	 */
	public String getTermUri() {
		return getTermUri("/");
	}

	/** 
	 * Returns the URI corresponding to the term with desired separator before the term.
	 * 
	 * @param sep The separator to use between the ontology and the term, typically "/" or "#".
	 * 
	 * @returns the URI corresponding to the term.
	 *          If no term is associated, then it returns the same as {@link #getOntologyUri()}.
	 */
	public String getTermUri(String sep) {
		if ( term == null || term.length() == 0 ) {
			return getOntologyUri();
		}
		else {
			return getOntologyUri() + sep + term;
		}
	}

	/** 
	 * @returns the same as {@link #getOntologyUri()}.
	 */
	public String toString() {
		return getOntologyUri();
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
	 * @returns the term.
	 *          (<code>someTerm</code>)
	 */
	public String getTerm() {
		return term;
	}
	
	
	/** 
	 * @returns the file extension given to this MmiUri
	 *          (<code>.owl</code>)
	 */
	public String getExtension() {
		return extension;
	}


	public String getUntilRoot() {
		return untilRoot;
	}

	/** 
	 * Gets the ontology URI but with the the given desired extension, which can be empty.
	 */
	public String getOntologyUriWithExtension(String desiredExtension) {
		String uri = untilRoot + authority+ "/" 
				   + (version != null ? version + "/" : "")
				   + topic + desiredExtension;
		
		return uri;
	}

	/**
	 * Makes a clone except for the given term, which can be null.
	 */
	public MmiUri copyWithTerm(String term) {
		if ( term == null ) {
			term = "";
		}
		return new MmiUri(untilRoot, authority, version, topic, term, extension);
	}
	

}
