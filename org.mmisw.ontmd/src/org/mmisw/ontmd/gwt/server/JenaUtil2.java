package org.mmisw.ontmd.gwt.server;

import java.io.StringWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * Some of the methods in JenaUtil but with some adjustments.
 * @author Carlos Rueda
 */
public class JenaUtil2 {
	private JenaUtil2() {}


	/** Fragment separator */
	private static final String FRAG_SEPARATOR = "#";
	
	/**
	 * Adds a fragment separator to the given URI if it doesn't end already with a fragment separator.
	 * (This is a replacement for JenaUtil.getURIForNS(String uri), which uses hash, #).
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
	 * (This is a replacement for JenaUtil.getURIForBase(String uri), which uses hash, #).
	 * @param uri  A URI
	 * @return The URI without any trailing fragment separators.
	 */
	public static String getURIForBase(String uri) {
		return uri.replaceAll(FRAG_SEPARATOR + "+$", "");
	}
	
	
	/**
	 * Replacement for JenaUtil.getOntModelAsString(OntModel model).
	 */	
	public static String getOntModelAsString(OntModel model) {
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
