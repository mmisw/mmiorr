package org.mmisw.ontmd.gwt.server;

import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.MmiUri;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * Some utils related with metadata
 * 
 * @author Carlos Rueda
 */
class MdUtil {
	
	private static final Log log = LogFactory.getLog(MdUtil.class);
	
	/**
	 * @returns null iff Ok;  otherwise an error message
	 */
	static String readAuthorities(AttrDef authorityAttrDef) {
		
		// TODO remove this old way of getting this vocabulary
		//String error = _readAuthorities(authorityAttrDef);

		readAuthorityOntology(authorityAttrDef);

		if ( false /* TODO some error ? */ ) {
			// Should not happen
			authorityAttrDef.addOption(
					new Option("error", "Could not read " +Config.AUTHORITY_ONTOLOGY)
			);
		}
		
		return null;
	}

	
	
	/**
	 * TODO
	 */
	static String readResourceTypes(AttrDef mainClassAttrDef) {
		
		return null;
	}
	
	
	private static void readAuthorityOntology(AttrDef authorityAttrDef) {
		MmiUri mmiUri;
		MmiUri classUri;
		try {
			mmiUri = new MmiUri(Config.AUTHORITY_ONTOLOGY);
			
			//TODO: get the name of the class in a flexible way
			classUri = new MmiUri(mmiUri.getOntologyUri()+ "/" +"Authority");
		}
		catch (URISyntaxException e) {
			log.error("should not happen", e);
			return;
		}

		log.debug("reading: " +mmiUri.getOntologyUri());

		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.read(Config.AUTHORITY_ONTOLOGY);
		
		Resource classRes = ResourceFactory.createResource(classUri.getTermUri());
		
		ExtendedIterator iter = ontModel.listIndividuals(classRes);
		while ( iter.hasNext() ) {
			Resource idv = (Resource) iter.next();
			String idvName = idv.getLocalName();
			String idvUri = idv.getURI();
			
			authorityAttrDef.addOption(new Option(idvName, idvUri));
			
			log.debug("  added option: " +idvName+ " : " + idvUri);

		}

	}
	

	

	
//	/**
//	 * @returns null iff Ok;  otherwise an error message
//	 */
//	@Deprecated
//	private static String _readAuthorities(AttrDef authorityAttrDef) {
//		File file = new File(Config.AUTHORITIES_CSV_FILE);
//		if ( !file.canRead() ) {
//			return "Cannot read file: " +file;
//		}
//		
//		List<?> lines;
//		try {
//			lines = IOUtils.readLines(new FileReader(file));
//		}
//		catch (Exception e) {
//			return "Error reading file: " +e.getMessage();
//		}
//		
//		if ( lines.size() == 0 ) {
//			return "Empty file: " +file;
//		}
//		
//		// first line is the header: abbreviation, name [, perhaps some more columns -- ignored]
//
//		for ( int lineno = 1, no_lines = lines.size(); lineno <= no_lines; lineno++ ) {
//			String line = (String) lines.get(lineno -1);
//			String[] toks = line.split("\\s*,\\s*");
//			if ( toks.length < 2 ) {
//				return file+ ":" +lineno+": expecting at least 2 columns";
//			}
//			
//			if ( lineno == 1 ) {
//				if ( ! toks[0].equalsIgnoreCase("abbreviation")
//						||   ! toks[1].equalsIgnoreCase("name")
//				) {
//					return "Header line invalid: " +line;
//				}
//				
//				// OK
//				continue;
//			}
//			
//			String optName = toks[0];
//			String optLabel = optName+ ": " +toks[1];
//			
//			authorityAttrDef.addOption(new Option(optName, optLabel));
//		}
//
//		return null;   // ok
//	}
//	
//	
//	/**
//	 * @returns null iff Ok;  otherwise an error message
//	 */
//	@Deprecated
//	private static String _readResourceTypes(AttrDef mainClassAttrDef) {
//		File file = new File(Config.RESOURCE_TYPES_CSV_FILE);
//		if ( !file.canRead() ) {
//			return "Cannot read file: " +file;
//		}
//		
//		List<?> lines;
//		try {
//			lines = IOUtils.readLines(new FileReader(file));
//		}
//		catch (Exception e) {
//			return "Error reading file: " +e.getMessage();
//		}
//		
//		if ( lines.size() == 0 ) {
//			return "Empty file: " +file;
//		}
//		
//		// first line is the header: type, name [, perhaps some more columns -- ignored]
//
//		for ( int lineno = 1, no_lines = lines.size(); lineno <= no_lines; lineno++ ) {
//			String line = (String) lines.get(lineno -1);
//			String[] toks = line.split("\\s*,\\s*");
//			if ( toks.length < 2 ) {
//				return file+ ":" +lineno+": expecting at least 2 columns";
//			}
//			
//			if ( lineno == 1 ) {
//				if ( ! toks[0].equalsIgnoreCase("type")
//						||   ! toks[1].equalsIgnoreCase("name")
//				) {
//					return "Header line invalid: " +line;
//				}
//				
//				// OK
//				continue;
//			}
//			
//			String optName = toks[0];
//			String optLabel = optName+ ": " +toks[1];
//			
//			mainClassAttrDef.addOption(new Option(optName, optLabel));
//		}
//
//		return null;   // ok
//	}

	

	private MdUtil() { }
	
}
