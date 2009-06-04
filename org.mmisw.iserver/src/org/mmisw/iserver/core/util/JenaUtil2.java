package org.mmisw.iserver.core.util;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NsIterator;
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
	

	public static String appendFragment(String uri) {
		if ( ! uri.endsWith(FRAG_SEPARATOR) ) {
			return uri + FRAG_SEPARATOR;
		}
		return uri;
	}
	

	public static String removeTrailingFragment(String uri) {
		return uri.replaceAll(FRAG_SEPARATOR + "+$", "");
	}
	
	
	/**
	 * Replacement for JenaUtil.getOntModelAsString(OntModel model).
	 */	
	public static String getOntModelAsString(Model model, String lang) {
		StringWriter sw = new StringWriter();
		String base = removeTrailingFragment(model.getNsPrefixURI(""));
		RDFWriter writer = model.getWriter(lang);
		writer.setProperty("xmlbase", base);
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document");
		writer.setProperty("tab", "4");
		writer.write(model, sw, base);
		return sw.getBuffer().toString();

	}

	/**
	 * Removes the unused prefixes (except "") from the model.
	 * 
	 * @param model the model to be updated.
	 */
	@SuppressWarnings("unchecked")
	public static void removeUnusedNsPrefixes(Model model) {
		// will containg the used prefixes:
		Set<String> usedPrefixes = new HashSet<String>();
		
		for ( NsIterator ns = model.listNameSpaces(); ns.hasNext(); ) {
			String namespace = ns.nextNs();
			String prefix = model.getNsURIPrefix(namespace);
			if ( prefix != null ) {
				usedPrefixes.add(prefix);
			}
		}
		
		// now remove all prefix from the model except the ones in usedPrefixes;
		// also, do not remove the empty prefix ("")
		Map<String,String> pm = model.getNsPrefixMap();
		for ( String prefix : pm.keySet() ) {
			if ( ! prefix.equals("") && ! usedPrefixes.contains(prefix) ) {
				// remove ths prefix from the model
				model.removeNsPrefix(prefix);
			}
		}
	}

}
