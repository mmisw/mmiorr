package org.mmisw.ont;

import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Some of the methods in JenaUtil but with some adjustments.
 * @author Carlos Rueda
 */
public class JenaUtil2 {
	private JenaUtil2() {}


	/** Fragment separator.
	 * 
	 * This is related with Issue 27: URIs have # signs instead of / before terms:
	 *    http://code.google.com/p/mmisw/issues/detail?id=27
	 *    
	 * 2008-11-14: re-setting to slash (/) to do more tests.
	 */
	private static final String FRAG_SEPARATOR = "/" ;   // "#";
	
	/**
	 * Adds a fragment separator to the given URI if it doesn't end already with a fragment separator.
	 * 
	 * <p>
	 * (This is a replacement for JenaUtil.getURIForNS(String uri), which always uses hash, #.
	 * I keep the name of the method to facilitate the connection, but ... 
	 * TODO a better name would be simply: appendFragment).
	 * 
	 * 
	 * @param uri  A URI
	 * @return The URI with a trailing fragment separator.
	 */
	public static String getURIForNS(String uri) {
		if ( ! uri.endsWith(FRAG_SEPARATOR) ) {
			return uri + FRAG_SEPARATOR;
		}
		return uri;
	}
	
	/**
	 * Removes any trailing fragment separators from the given URI.
	 * 
	 * <p>
	 * (This is a replacement for JenaUtil.getURIForBase(String uri), which always uses hash, #.
	 * I keep the name of the method to facilitate the connection, but ... 
	 * TODO a better name would be simply: removeTrailingFragment).
	 * 
	 * 
	 * @param uri  A URI
	 * @return The URI without any trailing fragment separators.
	 */
	public static String getURIForBase(String uri) {
		return uri.replaceAll(FRAG_SEPARATOR + "+$", "");
	}
	
	
	/**
	 * Replacement for JenaUtil.getOntModelAsString(OntModel model).
	 */	
	public static String getOntModelAsString(Model model) {
		StringWriter sw = new StringWriter();
		String base = getURIForBase(model.getNsPrefixURI(""));
		RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
		writer.setProperty("xmlbase", base);
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document");
		writer.setProperty("tab", "4");
		writer.write(model, sw, base);
		return sw.getBuffer().toString();

	}

}
