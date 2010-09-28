package org.mmisw.orrclient.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.client.util.XmlBaseExtractor;
import org.mmisw.orrclient.core.MdHelper;
import org.mmisw.orrclient.gwt.client.rpc.BaseResult;
import org.mmisw.orrclient.gwt.client.rpc.Errorable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Misc utilities.
 * 
 * @author Carlos Rueda
 */
public class Util2 {

	private static final Log log = LogFactory.getLog(Util2.class);

	/** The list of languages recognized by Jena's method model.read(String, String lang).
	 * @see #loadModelWithCheckingUtf8(File, String)  
	 */
	public static final List<String> JENA_LANGS = Collections.unmodifiableList(Arrays.asList(
		"RDF/XML",
		"N3",
		"N-TRIPLE",
		"TURTLE"
	));
	
	/** The default language when reading files with Jena */
	public static final String JENA_DEFAULT_LANG = JENA_LANGS.get(0);
	
	
	/**
	 * Checks the preexistence of an ontology to determine the possible conflict with an ontology that
	 * is about to be uploaded as *new*.
	 * 
	 * @param namespaceRoot    host-domain + ontologyRoot
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
	public static boolean checkNoPreexistingOntology(String namespaceRoot, String orgAbbreviation, String shortName, BaseResult result) {
		// See issue 63: http://code.google.com/p/mmisw/issues/detail?id=63
		
		// the (unversioned) URI to check for preexisting ontology:
		String possibleOntologyUri = namespaceRoot + "/" +
        							orgAbbreviation + "/" +
        							shortName;
		
		return checkNoPreexistingOntology(possibleOntologyUri, result);
	}
	
	
	/**
	 * Checks the preexistence of an ontology to determine the possible conflict with an ontology that
	 * is about to be uploaded as *new*.
	 * 
	 * @param possibleOntologyUri URI to test.
	 * 
	 * @param result setError will be called on this object in case an ontology exists with the given parameters
	 *           or if any error occurred while doing the check.
	 * 
	 * @return true if there is NO existing ontology with the given parameter; false if there IS an existing
	 *            ontology OR some error occurred.  If false is returned, result.getError() will be non-null.
	 */
	public static boolean checkNoPreexistingOntology(String possibleOntologyUri, BaseResult result) {
		if ( log.isDebugEnabled() ) {
			log.debug("New submission; checking for preexisting ontology with unversioned URI: " +possibleOntologyUri);
		}
		
		boolean possibleOntologyExists = false;
		
		// we just need to know whether this URI resolves against the registry:
		try {
			possibleOntologyExists = OntServiceUtil.isRegisteredOntologyUri(possibleOntologyUri, "application/rdf+xml");
		}
		catch (Exception e) {
			// report the error and return false (we shouldn't continue with the upload):
			String info = "Exception while checking for existence of URI: " +possibleOntologyUri+ " : " +e.getMessage();
			log.error(info, e);
			result.setError(info+ "\n\n Please try later.");
			return false;
		}
		
		if ( possibleOntologyExists ) {
			String info = "There is already a registered ontology with URI: " +possibleOntologyUri;
			
			if ( log.isDebugEnabled() ) {
				log.debug(info);
			}
			
			result.setError(info+ "\n\n" +
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
	 * @param namespaceRoot    host-domain + ontologyRoot
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
	 * Determines if the given resource is "located" in the given namespace.
	 * 
	 * <p>
	 * If namespace ends with '/' or '#', this will be true iff namespace.equals(resource.getNameSpace()).
	 * Otherwise, this will be true iff resource.getNameSpace() without the trailing
	 * separator ('/' or '#', if any) is equal to namespace. 
	 * @param resource
	 * @param namespace
	 * @return
	 */
	private static boolean _inNamespace(Resource resource, String namespace) {
		String resourceNamespace = resource.getNameSpace();
		
		if ( namespace.endsWith("/") || namespace.endsWith("#") ) {
			return namespace.equals(resourceNamespace);
		}
		else {
			return resourceNamespace != null && namespace.equals(resourceNamespace.replaceAll("(/|#)$", ""));
		}
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
		
		//log.debug(" REPLACING NS " +oldNameSpace+ " WITH " +newNameSpace);
		
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

//			if ( oldNameSpace.equals(sbj.getNameSpace()) ) {
			if ( _inNamespace(sbj, oldNameSpace) ) {
				n_sbj = model.createResource(newNameSpace + sbj.getLocalName());
				any_change = true;
			}
//			if ( oldNameSpace.equals(prd.getNameSpace()) ) {
			if ( _inNamespace(prd, oldNameSpace) ) {
				n_prd = model.createProperty(newNameSpace + prd.getLocalName());
				any_change = true;
			}
			if ( (obj instanceof Resource) ) {
				Resource objr = (Resource) obj;
//				if ( oldNameSpace.equals(objr.getNameSpace()) ) {
				if ( _inNamespace(objr, oldNameSpace) ) {
					n_obj = model.createResource(newNameSpace + objr.getLocalName());
					any_change = true;
				}
			}

			if ( any_change ) {
				o_stmts.add(o_stmt);
				Statement n_stmt = model.createStatement(n_sbj, n_prd, n_obj);
				n_stmts.add(n_stmt);
				//log.debug(" #### " +o_stmt);
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
			String value = JenaUtil2.getValue(ont_, dcProp); 
			
			if ( value == null || value.trim().length() == 0 ) {
				// No.  
				// Then, take the value from the equivalent MMI attribute if defined:
				Property mmiProp = MdHelper.getEquivalentMmiProperty(dcProp);
				value = JenaUtil2.getValue(ont_, mmiProp);
				
				if ( value != null && value.trim().length() > 0 ) {
					// we have a value for DC from the equivalente MMI attr.
					if ( log.isDebugEnabled() ) {
						log.debug(" Assigning DC attr " +dcProp+ " with " +mmiProp+ " = " +value);
					}
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
			JenaUtil2.loadModel(uriFile, false);
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
	 * Reads an RDF file. This is as {@link #readRdf(File)} but if Jena fails to load the file,
	 * then it checks if the problem is related with the parsing or UTF-8 encoding.
	 * The idea is to be a bit more specific about the UTF-8-related error if that's the most
	 * likely case.
	 * 
	 * @param file the file to read in
	 * @return the contents of the text file.
	 * @throws IOException 
	 */
	public static String readRdfWithCheckingUtf8(File file) throws IOException {
		
		// make sure the file can be loaded as a model:
		String uriFile = file.toURI().toString();
		try {
			JenaUtil2.loadModel(uriFile, false);
		}
		catch (Throwable jenaExc) {
			// XML parse exception?
			String errorMessage = getXmlParseExceptionErrorMessage(jenaExc);
			if ( errorMessage != null ) {
				// Ok, we know it's a parse exception and we could throw error right here;
				
				// But let's try to capture what the possible charsets are:
				// TODO: this re-test may be adding significant processing time especially for large
				// files--the overall detection/verification mechanism should a one-pass thing.
				
				try {
					Utf8Util.verifyUtf8(file);
				}
				catch (Exception utfExc) {
					String utfError = utfExc.getMessage();
					errorMessage += "\n\n" + utfError;
				}
				
				if ( log.isDebugEnabled() ) {
					log.debug("readRdfWithCheckingUtf8: " +errorMessage);
				}
				
				throw new IOExceptionWithCause(errorMessage, jenaExc);
			}
			
			// the following verifyUtf8 was done before I wrote and called the getXmlParseExceptionErrorMessage 
			// method above;   just keeping the verifyUtf8 here although perhaps it's redundant:
			
			// perhaps because not UTF-8? Try the check directly:
			try {
				Utf8Util.verifyUtf8(file);
			}
			catch (Exception utfExc) {
				// yes, it seems the problem is the encoding
				String error = jenaExc.getMessage();
				jenaExc.printStackTrace();
				throw new IOExceptionWithCause(error, jenaExc);
			}

			// other kind of problem:
			String error = jenaExc.getClass().getName()+ " : " +jenaExc.getMessage();
			throw new IOException(error);
		}
		
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			String rdf = IOUtils.toString(is, "UTF-8");
			return rdf;
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}


	/**
	 * Reads an ontology from a file.
	 *  
	 * If Jena fails to load the file,
	 * then it checks if the problem is related with the encoding UTF-8.
	 * The idea is to be a bit more specific about the UTF-8-related error if that's the most
	 * likely case.
	 * 
	 * @param file
	 * @param lang A language recognized by Jena. See {@link #JENA_LANGS}.
	 * @return
	 * @throw IllegalArgumentException if lang is not a valid language. 
	 * @throws IOException
	 */
	public static OntModel loadModelWithCheckingUtf8(File file, String lang) throws IOException {
		
		if ( lang != null && ! JENA_LANGS.contains(lang) ) {
			throw new IllegalArgumentException("lang argument must be null or one of " +JENA_LANGS);
		}
		
		
		String uriFile = file.toURI().toString();
		try {
			OntModel model = _loadModel(uriFile, lang, false);
			return model;
		}
		catch ( Throwable jenaExc ) {
			// XML parse exception?
			String errorMessage = getXmlParseExceptionErrorMessage(jenaExc);
			if ( errorMessage != null ) {
				throw new IOExceptionWithCause(errorMessage, jenaExc);
			}
			
			// the following verifyUtf8 was done before I wrote and called the getXmlParseExceptionErrorMessage 
			// method above;   just keeping the verifyUtf8 here although perhaps it's redundant:
			
			// perhaps because not UTF-8? Try the check directly:
			try {
				Utf8Util.verifyUtf8(file);
			}
			catch (Exception utfExc) {
				// yes, it seems the problem is the encoding"
				String error = jenaExc.getMessage();
				throw new IOExceptionWithCause(error, utfExc);
			}

			// other kind of problem:
			String error = jenaExc.getClass().getName()+ " : " +jenaExc.getMessage();
			throw new IOExceptionWithCause(error, jenaExc);
		}	
	}
	
	private static OntModel _loadModel(String uriModel, String lang, boolean processImports) {

		OntModel model = null;
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		model = _createDefaultOntModel();
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(processImports);
		if ( log.isDebugEnabled() ) {
			log.debug("loading model " + uriModel + "  LANG=" +lang);
		}
		model.read(uriModel, lang);
		return model;
	}
	private static OntModel _createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}
	public static void saveOntModelXML(OntModel model, File file,
			String base) throws IOException {

		String xmlbase = null;
		String namespace = null;
		
		if ( base != null ) {
			xmlbase = JenaUtil2.removeTrailingFragment(base);
			namespace = JenaUtil2.appendFragment(base);
		}
		
		FileOutputStream out = new FileOutputStream(file);
		
		try {
			if ( false ) {
				model.write(out, "RDF/XML-ABBREV", base);
			}
			else {
				// TODO When model is OntModel, the following generates a bunch of stuff
				RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
				writer.setProperty("showXmlDeclaration", "true");
				writer.setProperty("relativeURIs", "same-document,relative");
				writer.setProperty("tab", "4");
				if ( xmlbase != null ) {
					writer.setProperty("xmlbase", xmlbase);
				}

				writer.write(model, out, namespace);
			}
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}



	
	/**
	 * Helper to determine it's a SAXParseException so we can provide a bit of more
	 * information.
	 * @param jenaExc
	 * @return
	 */
	private static String getXmlParseExceptionErrorMessage(Throwable jenaExc) {
		if ( ! (jenaExc instanceof JenaException ) ) {
			return null;
		}
		
		Throwable cause = jenaExc.getCause();
		if ( ! (cause instanceof SAXParseException ) ) {
			return null;
		}
		
		SAXParseException spe = (SAXParseException) cause;
		String errorMessage = spe.getMessage() +
			"\n  Line number: " + spe.getLineNumber()+" Column number: " +spe.getColumnNumber()
//			+(spe.getPublicId() != null ? "\n Public ID: " + spe.getPublicId() : "" )
//			+(spe.getSystemId() != null ? "\n System ID: " + spe.getSystemId() : "" )
		;

		return errorMessage;
			
	}

	/**
	 * Returns the URI associated with xml:base, if defined in the document. If xml:base is not defined, 
	 * then it returns file.toURI().toString(). 
	 * See  
	 * <a href="http://www.w3.org/TR/2003/PR-rdf-syntax-grammar-20031215/#section-Syntax-ID-xml-base"
	 * >this section in the RDF/XML systax spec</a>.
	 * 
	 * @param file   Used to obtain xml:base from its contents, if any, or the URI of the file itself.
	 * @param baseResult  setError(e) will be called if not value can be obtained
	 * @return  the namespace.  null in case of not finding any value.
	 */

	/**
	 * Gets the namespace associated with the given model. The first namespace according to
	 * the following sequence is returned:
	 * <ul>
	 * <li> URI of the first ontology resource, if any, associated to the model;
	 * <li> namespace associated with the empty prefix, if any;
	 * <li> URI of the xml:base of the document, if any;
	 * <li> null, otherwise.
	 * </ul>
	 * 
	 * @param model
	 * @param file
	 * @param baseResult
	 * @return
	 */
	public static String getDefaultNamespace(OntModel model, File file, Errorable baseResult) {

		// See issues #213, #174
		
		// try the first ontology resource, if any:
		Ontology ont = JenaUtil2.getOntology(model);
		if ( ont != null ) {
			String namespace = ont.getURI();	
			return namespace;
		}
		
		// try namespace of the empty prefix, if any:
		String namespace = model.getNsPrefixURI("");
		if ( namespace != null ) {
			return namespace;
			
		}

		// finally, try the xml:base:
		try {
			String rdf;
			rdf = readRdf(file);
			URI xmlBaseUri = XmlBaseExtractor.getXMLBase(new InputSource(new StringReader(rdf)));
			if ( xmlBaseUri != null ) {
				namespace = xmlBaseUri.toString();
				return namespace;
			}
		}
		catch (Exception e) {
			String error = "error while trying to read xml:base attribute: " +e.getMessage();
			log.warn(error, e);
			baseResult.setError(error);
		}
		
		
		return null;
	}
	

//	OLD getDefaultNamespace
//	/**
//	 * Returns <code>model.getNsPrefixURI("")</code> if it's non-null; otherwise the URI
//	 * associated with xml:base, if defined in the document. If xml:base is not defined, 
//	 * then it returns file.toURI().toString(). 
//	 * See for example  
//	 * <a href="http://www.w3.org/TR/2003/PR-rdf-syntax-grammar-20031215/#section-Syntax-ID-xml-base"
//	 * >this section in the RDF/XML systax spec</a>.
//	 * 
//	 * @param model  to call <code>model.getNsPrefixURI("")</code>
//	 * @param file   if necessary, used to obtain xml:base if any, or the URI of the file itself.
//	 * @param baseResult  setError(e) will be called if not value can be obtained
//	 * @return  the namespace.  null in case of not finding any value.
//	 */
//	public static String getDefaultNamespace(OntModel model, File file, BaseResult baseResult) {
//
//		String namespace = model.getNsPrefixURI("");
//		if ( namespace == null ) {
//			// issue #140: "ontologies with no declared namespace for empty prefix are not accepted"
//
//			// fix: use xml:base's URI
//
//			try {
//				String rdf;
//				rdf = readRdf(file);
//				URI xmlBaseUri = XmlBaseExtractor.getXMLBase(new InputSource(new StringReader(rdf)));
//				if ( xmlBaseUri != null ) {
//					namespace = xmlBaseUri.toString();
//				}
//				else {
//					return file.toURI().toString();
//				}
//			}
//			catch (Exception e) {
//				String error = "error while trying to read xml:base attribute: " +e.getMessage();
//				log.warn(error, e);
//				baseResult.setError(error);
//				return null;
//			}
//		}
//		
//		return namespace;
//	}


	private static Random random = new Random();
	
	public static String generatePassword() {
		final int len = 7;
		StringBuilder sb = new StringBuilder();
		
		while ( sb.length() < len ) {
			int nn;
			if ( random.nextDouble() < 2./3 ) {
				if ( random.nextDouble() < 1./5 ) {
					nn = 'a' + random.nextInt('z' - 'a');
				}
				else {
					nn = 'A' + random.nextInt('Z' - 'A');
				}
			}
			else {
				nn = '0' + random.nextInt('9' - '0');
			}
			sb.append((char) nn);
		}
		return sb.toString();
	}

}
