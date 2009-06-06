package org.mmisw.iserver.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.MdHelper;
import org.mmisw.iserver.gwt.client.rpc.BaseResult;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.drexel.util.rdf.JenaUtil;

public class Util2 {

	private static final Log log = LogFactory.getLog(Util2.class);
	
	
	/** Ontology URI prefix including root: */
	// TODO read namespaceRoot from a configuration parameter
	public static final String namespaceRoot = "http://mmisw.org/ont";

	
	/**
	 * Checks the preexistence of an ontology to determine the possible conflict with an ontology that
	 * is about to be uploaded as *new*.
	 * 
	 * @param orgAbbreviation  Part of the key combination
	 * @param shortName        Part of the key combination
	 * 
	 * @param result setError will be called on this object in case an ontology exists with the given parameters
	 *           or if any error occurred while doing the check.
	 * 
	 * @return true if there is NO existing ontology with the given parameters; false if there IS an existing
	 *            ontology OR some error occurred.  If false is returned, result.getError() will be non-null.
	 */
	public static boolean checkNoPreexistingOntology(String orgAbbreviation, String shortName, BaseResult result) {
		// See issue 63: http://code.google.com/p/mmisw/issues/detail?id=63
		
		// the (unversioned) URI to check for preexisting ontology:
		String possibleOntologyUri = namespaceRoot + "/" +
        							orgAbbreviation + "/" +
        							shortName;

		if ( log.isDebugEnabled() ) {
			log.debug("New submission; checking for preexisting ontology with unversioned URI: " +possibleOntologyUri);
		}
		
		// we just need to know whether this URI resolves:
		boolean possibleOntologyExists = false;
		try {
			int statusCode = HttpUtil.httpGet(possibleOntologyUri, "application/rdf+xml");
			if ( log.isDebugEnabled() ) {
				log.debug("HTTP GET status code: " +statusCode+ ": " +HttpStatus.getStatusText(statusCode));
			}
			possibleOntologyExists = statusCode == HttpStatus.SC_OK;
		}
		catch (Exception e) {
			// report the error and return false (we shouldn't continue with the upload):
			String info = "Exception while checking for existence of URI: " +possibleOntologyUri+ " : " +e.getMessage();
			log.error(info, e);
			result.setError(info+ "\n\n Please try later.");
			return false;
		}
		
		if ( possibleOntologyExists ) {
			String info = "There is already a registered ontology with the same " +
							"authority and resource type combination:\n" +
							"   " +possibleOntologyUri;
			
			if ( log.isDebugEnabled() ) {
				log.debug(info);
			}
			
			result.setError(info+ "\n\n" +
					"You will need to change the authority and/or resource topic to be able to " +
					"submit your ontology.\n" +
					"\n" +
					"Note: if you want to submit a new version for the above ontology, " +
					"then you would need to browse to that entry in the main repository interface " +
					"and use one of the available options to create a new version."
			);
			return false;
		}
		
		// OK, no preexisting ontology:
		return true;
	}

	/**
	 * Checks the new ontology URI key combination for possible changes.
	 * 
	 * @param originalOrgAbbreviation  Part of the original key combination
	 * @param originalShortName        Part of the original key combination
	 * 
	 * @param orgAbbreviation  Part of the key combination
	 * @param shortName        Part of the key combination
	 * 
	 * @param result setError will be called on this object if there are any changes in the key combination.
	 * 
	 * @return true if OK. 
	 *         false if there IS any error (result.getError() will be non-null).
	 */
	public static boolean checkUriKeyCombinationForNewVersion(
			String originalOrgAbbreviation, String originalShortName, 
			String orgAbbreviation, String shortName, BaseResult result) {
		
		// See issue 98: http://code.google.com/p/mmisw/issues/detail?id=98
		//               "new version allows the shortName and authority to be changed"
		
		
		if ( originalOrgAbbreviation == null ) {
			result.setError("No original authority given!");
			return false;
		}
		else if ( originalShortName == null ) {
			result.setError("No short name given!");
			return false;
		}
		
		
		StringBuffer error = new StringBuffer();

		if ( ! originalOrgAbbreviation.equals(orgAbbreviation) ) {
			error.append("\n   New authority: \"" +orgAbbreviation+ "\"" +
					"  Original: \"" +originalOrgAbbreviation+ "\"");
		}

		if ( ! originalShortName.equals(shortName) ) {
			error.append("\n   New resource type: \"" +shortName+ "\"" +
					"  Original: \"" +originalShortName+ "\"");
		}
		
		if ( error.length() > 0 ) {
			String info = "Key component(s) for the ontology URI have changed: " +error;
			
			if ( log.isDebugEnabled() ) {
				log.debug(info);
			}
			
			result.setError(info+ "\n\n" +
					"The ontology would be submitted as a new entry in the repository " +
					"and not as a new version of the base ontology. " +
					"Please make sure the resource type and the authority are unchanged.\n" +
					"\n" +
					"Note: To submit a new ontology (and not a new version of an existing ontology), " +
					"please use the \"Submit New Ontology\" option in the main repository interface."
			);
			return false;
		}
		
		// OK:
		return true;
	}

	/**
	 * Replaces any statement having an element in the given oldNameSpace with a
	 * correponding statement in the new namespace.
	 * <p>
	 * (Doesn't jena have a utility for doing this?)
	 * 
	 * @param model
	 * @param oldNameSpace
	 * @param newNameSpace
	 */
	// TODO: Use function from "ont" project (the utility is replicated here for the moment)
	public static void replaceNameSpace(OntModel model, String oldNameSpace, String newNameSpace) {
		
		//log.info(" REPLACING NS " +oldNameSpace+ " WITH " +newNameSpace);
		
		// old statements to be removed:
		List<Statement> o_stmts = new ArrayList<Statement>(); 
		
		// new statements to be added:
		List<Statement> n_stmts = new ArrayList<Statement>(); 
		
		StmtIterator existingStmts = model.listStatements();
		while ( existingStmts.hasNext() ) {
			Statement o_stmt = existingStmts.nextStatement();
			Resource sbj = o_stmt.getSubject();
			Property prd = o_stmt.getPredicate();
			RDFNode obj = o_stmt.getObject();
			
			boolean any_change = false;
			Resource n_sbj = sbj;
			Property n_prd = prd;
			RDFNode  n_obj = obj;

			if ( oldNameSpace.equals(sbj.getNameSpace()) ) {
				n_sbj = model.createResource(newNameSpace + sbj.getLocalName());
				any_change = true;
			}
			if ( oldNameSpace.equals(prd.getNameSpace()) ) {
				n_prd = model.createProperty(newNameSpace + prd.getLocalName());
				any_change = true;
			}
			if ( (obj instanceof Resource) && oldNameSpace.equals(((Resource) obj).getNameSpace()) ) {
				n_obj = model.createResource(newNameSpace + ((Resource) obj).getLocalName());
				any_change = true;
			}

			if ( any_change ) {
				o_stmts.add(o_stmt);
				Statement n_stmt = model.createStatement(n_sbj, n_prd, n_obj);
				n_stmts.add(n_stmt);
				//log.info(" #### " +o_stmt);
			}
		}
		
		for ( Statement n_stmt : n_stmts ) {
			model.add(n_stmt);
		}
		
		for ( Statement o_stmt : o_stmts ) {
			model.remove(o_stmt);
		}
	}

	/**
	 * Sets the missing DC attrs that have defined equivalent MMI attrs: 
	 */
	public static void setDcAttributes(Ontology ont_) {
		for ( Property dcProp : MdHelper.getDcPropertiesWithMmiEquivalences() ) {
			
			// does dcProp already have an associated value?
			String value = JenaUtil.getValue(ont_, dcProp); 
			
			if ( value == null || value.trim().length() == 0 ) {
				// No.  
				// Then, take the value from the equivalent MMI attribute if defined:
				Property mmiProp = MdHelper.getEquivalentMmiProperty(dcProp);
				value = JenaUtil.getValue(ont_, mmiProp);
				
				if ( value != null && value.trim().length() > 0 ) {
					// we have a value for DC from the equivalente MMI attr.
					log.info(" Assigning DC attr " +dcProp+ " with " +mmiProp+ " = " +value);
					ont_.addProperty(dcProp, value.trim());
				}
			}
		}
	}

	
	/**
	 * Reads an RDF file.
	 * @param file the file to read in
	 * @return the contents of the text file.
	 * @throws IOException 
	 */
	public static String readRdf(File file) throws IOException {
		
		// make sure the file can be loaded as a model:
		String uriFile = file.toURI().toString();
		try {
			JenaUtil.loadModel(uriFile, false);
		}
		catch (Throwable ex) {
			String error = ex.getClass().getName()+ " : " +ex.getMessage();
			throw new IOException(error);
		}
		

		
		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringWriter sw = new StringWriter();
			PrintWriter os = new PrintWriter(sw);
			IOUtils.copy(is, os);
			os.flush();
			String rdf = sw.toString();
			return rdf;
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Returns <code>model.getNsPrefixURI("")</code> if it's non-null; otherwise the URI
	 * associated with xml:base.
	 * 
	 * @param model  to call <code>model.getNsPrefixURI("")</code>
	 * @param file   to obtain xml:base if necessary.
	 * @param baseResult  setError(e) will be called if not value can be obtained
	 * @return  the namespace.  null in case of not finding any value.
	 */
	public static String getDefaultNamespace(OntModel model, File file, BaseResult baseResult) {

		String namespace = model.getNsPrefixURI("");
		if ( namespace == null ) {
			// issue #140: "ontologies with no declared namespace for empty prefix are not accepted"

			// fix: use xml:base's URI

			try {
				String rdf;
				rdf = readRdf(file);
				URI xmlBaseUri = XmlBaseExtractor.getXMLBase(new InputSource(new StringReader(rdf)));
				if ( xmlBaseUri != null ) {
					namespace = xmlBaseUri.toString();
				}
				else {
					// give up    (TODO there may be other things to try -- but later)
					String error = "Error: Ontology does not define a default namespace nor a URI for xml:base";
					log.info(error);
					baseResult.setError(error);
					return null;
				}
			}
			catch (Exception e) {
				String error = "error while trying to read xml:base attribute: " +e.getMessage();
				log.info(error, e);
				baseResult.setError(error);
				return null;
			}
		}
		
		return namespace;
	}
	
	/**
	 * FIXME implementation is incorrect.
	 * TODO if possible, determine the actual original charset and
	 * then do the conversion to UTF-8.
	 */
	@Deprecated
	public static String convertToUtf8(String str, Log log) {
		try {
			byte[] bytes = str.getBytes();
			String utf8_str = new String(bytes, "UTF-8");
			return utf8_str;
		}
		catch (UnsupportedEncodingException e) {
			if ( log != null ) {
				log.warn("Cannot convert to UTF-8. " +e.toString(),  e);
			}
			else {
				e.printStackTrace();
			}
		}
		return str;
	}



}
