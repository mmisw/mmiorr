package org.mmisw.ont;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
	 * This is similar to JenaUtil.getURIForNS(String uri) (which always uses hash, #).
	 * The name appendFragment better reflects what this method actually does.
	 * 
	 * @param uri  A URI
	 * @return The URI with a trailing fragment separator.
	 */
	public static String appendFragment(String uri) {
		if ( ! uri.endsWith(FRAG_SEPARATOR) ) {
			return uri + FRAG_SEPARATOR;
		}
		return uri;
	}
	
	/**
	 * Removes any trailing fragment separators from the given URI.
	 * 
	 * <p>
	 * This is similar to JenaUtil.getURIForBase(String uri) (which always uses hash, #).
	 * The name removeTrailingFragment better reflects what this method actually does.
	 * 
	 * @param uri  A URI
	 * @return The URI without any trailing fragment separators.
	 */
	public static String removeTrailingFragment(String uri) {
		return uri.replaceAll("(/|#)+$", "");
	}
	
	
	/**
	 * Replacement for JenaUtil.getOntModelAsString(OntModel model).
	 */	
	public static String getOntModelAsString(Model model, String lang) {
		StringWriter sw = new StringWriter();
		RDFWriter writer = model.getWriter(lang);
		String baseUri = null;
		String uriForEmptyPrefix = model.getNsPrefixURI("");
		if ( uriForEmptyPrefix != null ) {
			baseUri = removeTrailingFragment(uriForEmptyPrefix);
			writer.setProperty("xmlbase", baseUri);
		}
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document");
		writer.setProperty("tab", "4");
		writer.write(model, sw, baseUri);
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

	/**
	 * NOTE: only the first ontology is taken into account
	 * @param ontModel
	 * @return value of {@link Omv#version}, or null if missing.
	 */
	public static String getOmvVersion(OntModel ontModel) {
		ExtendedIterator onts = ontModel.listOntologies();
		if ( onts == null || ! onts.hasNext() ) {
			return null;
		}
		// NOTE: only the first ontology is taken into account
		Ontology ontology = (Ontology) onts.next();
		RDFNode node = ontology.getPropertyValue(Omv.version);
		if ( node == null ) {
			return null;
		}
		return node.toString();
	}

}
