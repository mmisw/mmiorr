package org.mmisw.ont;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
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
	 * Retrieves the value of a given property in the firt ontology resource in the model.
	 * @param ontModel
	 * @param prop Property
	 * @return value of the property, or null if missing.
	 */
	public static String getOntologyPropertyValue(OntModel ontModel, Property prop) {
		Ontology ontology = _getFirstOntology(ontModel);
		if ( ontology == null ) {
			return null;
		}
		RDFNode node = ontology.getPropertyValue(prop);
		if ( node == null ) {
			return null;
		}
		return node.toString();
	}

	/**
	 * Retrieves the value of a given property in the firt ontology resource in the model.
	 * @param ontModel
	 * @param prop Property
	 * @return value of the property, or null if missing.
	 */
	private static Ontology _getFirstOntology(OntModel ontModel) {
		ExtendedIterator<Ontology> onts = ontModel.listOntologies();
		if ( onts == null || ! onts.hasNext() ) {
			return null;
		}
		Ontology ontology = (Ontology) onts.next();
		return ontology;
	}

	
	
	/**
	 * Uses Omv.creationDate to get a string suitable for Omv.version
	 * 
	 * @param model the model
	 * @return string in version format taken from value of creationDate; null if Omv.creationDate
	 *         value not available or cannot be parsed.
	 */
	// Method created as part of the fix to issue #252: "omv:version gone?".
	public static String getVersionFromCreationDate(OntModel model) {
		String version = null;
		String creationDate = JenaUtil2.getOntologyPropertyValue(model, Omv.creationDate);
		if ( creationDate != null ) {
			version = _getVersionFromCreationDate(creationDate);
		}
		
		return version;
	}

	
	/**
	 * Obtains the version (appropriate format) from the given creationDate value.
	 * 
	 * @param creationDate creationDate value
	 * @return string in version format taken from value of creationDate; null if this cannot be parsed.
	 */
	private static String _getVersionFromCreationDate(String creationDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		Date date = null;
		try {
			date = sdf.parse(creationDate);
		}
		catch (ParseException e) {
			// ignore.
		}
		if ( date == null ) {
			return null;
		}
		
		sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String version = sdf.format(date);
		
		return version;
	}

	
	
	/** 
	 * Sets Omv.version value if missing from the first ontology resource in the
	 * given model, taking the value from Omv.creationDate
	 * @param ontModel
	 * @return the value of omv.version assigned to the ontology; null if no
	 * assignment was performed.
	 */
	// Method created as part of the fix to issue #252: "omv:version gone?".
	public static String setVersionFromCreationDateIfNecessary(OntModel ontModel) {
		Ontology ontology = _getFirstOntology(ontModel);
		if ( ontology == null ) {
			return null;
		}
		RDFNode node = ontology.getPropertyValue(Omv.version);
		if ( node != null ) {
			// already assigned; keep it:
			return null;
		}
		// omv.version not assigned; try to use omv.creationDate:
		node = ontology.getPropertyValue(Omv.creationDate);
		if ( node == null ) {
			// not available, return with no changes:
			return null;
		}
		
		String creationDate = node.toString();
		String version = _getVersionFromCreationDate(creationDate);
		if ( version != null ) {
			Literal versionLit = ResourceFactory.createPlainLiteral(version);
			ontology.setPropertyValue(Omv.version, versionLit);
			return version;
		}
		return null;
	}

}
