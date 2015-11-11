package org.mmisw.orrclient.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.core.util.StringManipulationUtil;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyDataCreationInfo;
import org.mmisw.orrportal.gwt.server.OrrConfig;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Dispatchs the conversion.
 *
 * <p>
 * TODO This could be superseded by the new voc2skos functionality.
 *
 * <p>
 * Adapted from org.mmi.web.MetadataBean. Note that changes have been kept to a minimum, basically
 * to whatever is strictly necessary to make the whole thing work. Time permitting, this will be
 * cleaned up at some point.
 *
 * @author Carlos Rueda
 */
public class VocabCreator {

	// 2013-08-05 adjustments addressing "truncated vocabulary" issue reported by IOOS.

	private VocabularyDataCreationInfo vocabularyDataCreationInfo;

	private String orgAbbreviation;
	private String shortName;

	private String ascii;
	private String fieldSeparator;

	private String namespaceRoot;

	// To set TransProperties.NS
	private String finalUri;

	private String finalShortName;

	public String getFinalUri() {
		return finalUri;
	}

	public String getFinalShortName() {
		return finalShortName;
	}


	private String primaryClass;


//	private static final String ONE_CLASS_ALL_INSTANCES = OwlCreatorComplex.ONE_CLASS_ALL_INSTANCES;
//
//	private String convertionType = ONE_CLASS_ALL_INSTANCES;


	private Object version;

	private final Log log = LogFactory.getLog(VocabCreator.class);



	private final Map<String, String> values;


	private Resource[] res;
	private OntModel newOntModel;
	private String ns_;
	private String base_;

	private OntClass classForTerms;

	private StringManipulationUtil stringManipulation = new StringManipulationUtil();

	/** Is the key for the terms to be used exclusively as the term URI? */
	private boolean keyIsUri = false;


	/**
	 * TODO createUniqueBaseName(): need a more robust way to get a unique name.
	 */
	private static String _createUniqueBaseName() {
		return String.valueOf(System.currentTimeMillis());

	}
	private final String uniqueBaseName = _createUniqueBaseName();


	//////////////////////////////////////////////////////////////////////////////////////


	private String _createAscii() {

		final String SEPARATOR = ",";
		final String QUOTE = "\"";

		fieldSeparator = "csv";

		String sep;
		StringBuilder sb = new StringBuilder();

		List<String> header = vocabularyDataCreationInfo.getColNames();
		sep = "";
		for (String str : header ) {
			sb.append(sep + QUOTE + str + QUOTE);
			sep = SEPARATOR;
		}
		sb.append("\n");

		List<List<String>> rows = vocabularyDataCreationInfo.getRows();
		for ( List<String> values : rows ) {

			int col = 0;

			sep = "";
			for (String str : values ) {
				if ( col == header.size() ) { // no more values than columns as indicated in header
					break;
				}
				sb.append(sep + QUOTE + str + QUOTE);
				sep = SEPARATOR;
				col++;
			}

			// missing cols?
			for ( ; col < header.size(); col++ ) {           // fill with blanks
				sb.append(sep + QUOTE + "" + QUOTE);
				sep = SEPARATOR;
			}
			sb.append("\n");
		}

		String ascii = sb.toString();
		return ascii;
	}


	/**
	 *
	 * @param createOntologyInfo      Metadata for the ontology
	 * @param vocabularyDataCreationInfo    Data for the ontology
	 * @throws Exception If verification of UTF-8 fails
	 */
	public VocabCreator(
			CreateOntologyInfo createOntologyInfo,
			VocabularyDataCreationInfo vocabularyDataCreationInfo
	) throws Exception {


		this.vocabularyDataCreationInfo = vocabularyDataCreationInfo;


		Map<String, String> values = createOntologyInfo.getMetadataValues();


		String ontServiceUrl = OrrConfig.instance().ontServiceUrl;
		this.namespaceRoot = ontServiceUrl;
		this.orgAbbreviation = createOntologyInfo.getAuthority();
		this.shortName = createOntologyInfo.getShortName();

		this.primaryClass = vocabularyDataCreationInfo.getClassName();
		this.ascii = _createAscii();
		this.values = values;

		log.info("!!!!!!!!!!!!!!!! VocabCreator: values = " +values);
		log.info("setting primary class " + primaryClass);

		if ( orgAbbreviation == null ) {
			orgAbbreviation = values.get(OmvMmi.origMaintainerCode.getURI());
		}

		if ( shortName == null ) {
			shortName = values.get(Omv.acronym.getURI());
		}

	}

	public void createOntology(CreateOntologyResult createVocabResult) {
		if ( log.isDebugEnabled() ) {
			log.debug("VocabCreator.createOntology");
		}

		//
		// generate RDF:
		//
		setFinalUri();
		try {
			processCreateOntology();
		}
		catch (Exception e) {
			String error = e.getMessage();
			log.error(error, e);
			createVocabResult.setError(error);
			return;
		}
		String rdf = getOntologyStringXml();

		//
		// before saving the RDF, save a copy of the text contents:
		//
		final File voc2rdfDirectory = OrrConfig.instance().voc2rdfDir;
		File full_path_csv = new File(voc2rdfDirectory, getPathOnServer() + ".csv");
		log.debug("saving copy of text contents in " + full_path_csv);
		try {
			FileOutputStream os = new FileOutputStream(full_path_csv);
			IOUtils.write(ascii, os, "UTF-8");
			os.close();
		}
		catch (IOException ex) {
			String msg = "Error saving copy of text file: " +full_path_csv;
			log.error(msg, ex);
			createVocabResult.setError(msg);
			return;
		}

		// now save the RDF:
		File full_path = new File(voc2rdfDirectory, getPathOnServer());
		log.debug("saving RDF in " + full_path);
		try {
			FileOutputStream os = new FileOutputStream(full_path);
			IOUtils.write(rdf, os, "UTF-8");
			os.close();
		}
		catch (IOException ex) {
			String msg = "Error writing generated RDF file: " +full_path;
			log.error(msg, ex);
			createVocabResult.setError(msg);
			return;
		}

		// OK:
		createVocabResult.setFullPath(full_path.getAbsolutePath());
	}

	private OntModel _createOntModel() {
		OntModel ontModel = JenaUtil2.createDefaultOntModel();

		// TODO: from the previous code based on OwlModel, ie.,
//		ontModel = new OwlModel(ontModel);
		// it seems the following "layer" is unnecesary. So, returning JenaUtil2.createDefaultOntModel()
		// directly should be fine.

		ontModel = ModelFactory.createOntologyModel(ontModel.getSpecification(), ontModel);

		return ontModel;
	}


	private void processCreateOntology() throws Exception {

		// TODO instead of getPreviewDirectory, use a "tmp" directory explicitly
		File fileInText = new File(OrrConfig.instance().previewDir, uniqueBaseName + ".txt");
		saveInFile(fileInText);

		newOntModel = _createOntModel();
		ns_ = JenaUtil2.appendFragment(finalUri);
		base_ = JenaUtil2.removeTrailingFragment(finalUri);

		// set NS prefixes:
		newOntModel.setNsPrefix("", ns_);
		Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
		for ( String uri : preferredPrefixMap.keySet() ) {
			String prefix = preferredPrefixMap.get(uri);
			newOntModel.setNsPrefix(prefix, uri);
		}

		Ontology ont = newOntModel.createOntology(base_);
		log.info("New ontology created with namespace " + ns_ + " base " + base_);

		// Indicate Voc2RDF as the engineering tool:
		ont.addProperty(Omv.usedOntologyEngineeringTool, OmvMmi.voc2rdf);


		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		for ( String uri : values.keySet() ) {
			String value = values.get(uri).trim();
			if ( value.length() > 0 ) {
				Property prop = uriPropMap.get(uri);
				if ( prop == null ) {
					log.warn("No property found for uri='" +uri+ "'");
					continue;
				}
				//log.debug("adding property " + prop + " = " + value);
				ont.addProperty(prop, value);
			}
		}

		// set Omv.uri from final
		ont.addProperty(Omv.uri, finalUri);

		// set Omv.acronym from primaryClass
		ont.addProperty(Omv.acronym, primaryClass);

		String classUri = values.get("classUri");
		if ( classUri != null ) {
			ont.addProperty(OmvMmi.shortNameUri, classUri);
		}

		// fixed issue #120: "title doesn't get carried forward"
		// problem was that Omv.name was assigned BOTH the class name and the fulltitle.
		// Only the fullTitle is now assigned.
//		ont.addProperty(Omv.name,
//		setFirstUpperCase(cleanStringforID(primaryClass)) + " Vocabulary");
		String fullTitle = values.get("fullTitle");
		if ( fullTitle != null ) {
			ont.addProperty(Omv.name, fullTitle);
		}

		String creator = values.get("creator");
		if ( creator != null ) {
			ont.addProperty(Omv.hasCreator, creator);
		}

		String briefDescription = values.get("briefDescription");
		if ( briefDescription != null ) {
			ont.addProperty(Omv.description, briefDescription);
		}

		if ( orgAbbreviation != null && orgAbbreviation.trim().length() > 0 ) {
			ont.addProperty(OmvMmi.origMaintainerCode, orgAbbreviation.trim());
		}

		createOntologIndividuals();
	}




	private void createOntologIndividuals() throws Exception {

		List<String> header     = vocabularyDataCreationInfo.getColNames();
		List<List<String>> rows = vocabularyDataCreationInfo.getRows();
		log.debug("createOntologIndividuals: header=" + header + ", processing rows: " + rows.size());

		createPropertiesAndClasses(header);

		for (int i = 0; i < rows.size(); i++) {
			List<String> row = rows.get(i);
			createIndividual(row);
		}


	}

	private void createPropertiesAndClasses(List<String> header) {
		int size = header.size();
		res = new Resource[size];

		// object properties is set up later, when creating individuals

		classForTerms = createClassNameGiven();
		System.out.println("class for terms created " + classForTerms);


		for (int i = 0; i < size; i++) {

			log.info("converting column header " + i
					+ " to a datatype property");

			String str = header.get(i).trim();
			res[i] = createDatatypeProperty(str, i);
		}

	}

	private void createIndividual(List<String> row) throws Exception {
		// create individual
		Individual ind = null;
		try {
			int id = 0;

			ind = createIndividual(row, id, classForTerms);

			// add the properties
			createNotHierarchyIndividual(ind, row);

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}

	}


	private void createNotHierarchyIndividual(Individual ind, List<String> row) throws Exception {

		if (ind != null) {
			for (int i = 0; i < row.size(); i++) {
				// this contains either classes or datatypeproperties
				Resource r = res[i];
				final String str = row.get(i).trim();
				if (str.length() > 0) {
					if (r instanceof OntClass) {
						OntClass cls = (OntClass) r;
						Individual ind2 = createIndividual(row, i, cls);
						ObjectProperty p = getPropertyForARangeClass(cls);
						if (cls == null || ind2 == null || p == null
								|| ind == null) {
							System.err.println("cls " + cls + "  p " + p
									+ "  ind2 " + ind2 + " ind " + ind);
						} else {
							ind.addProperty(p, ind2);
						}

					} else {
						DatatypeProperty dp = (DatatypeProperty) r;
						String value = str;
						//log.debug("adding datatype property: " + dp + " = " + value);
						ind.addProperty(dp, value);

					}
				}

			}
		}

	}


	private ObjectProperty getPropertyForARangeClass(OntClass cs) {
		String nameOfProperty = "has" + cs.getLocalName();
		ObjectProperty op = newOntModel.createObjectProperty(cs.getNameSpace()
				+ nameOfProperty);
		op.setDomain(classForTerms);
		op.setRange(cs);

		return op;

	}




	private boolean isGood(String str) {
		return str.length() > 0;

	}

	/**
	 * Creates an individual from the given column (id) in the given row.
	 *
	 * Note: colons are OK for the name of an individual (issue #124).
	 * This is especially relevant when the first column is going to exclusively
	 * determine the URI for each term (issue #135)
	 *
	 * @param row
	 * @param id
	 * @param cs
	 * @return
	 * @throws Exception
	 */
	private Individual createIndividual(List<String> row, int id, OntClass cs) throws Exception {

		final String str = row.get(id).trim();
		if (isGood(str)) {
			boolean allowColon = true;
			String resourceString;

			if ( id == 0 && keyIsUri ) {

				// OLD:
				//// just use the simple name given in this column
				////resourceString = getGoodName(row, id, allowColon).toLowerCase();

				// New: regarding Issue #164 "Keep periods and case in term URI" and
				// more generally, just check that it's a valid URI and do no conversions at all
				String uri = str;
				try {
					new URI(uri);
					resourceString = uri; // good URI
				}
				catch (URISyntaxException e) {
					throw new Exception("Error in URI value: " +uri, e);
				}
			}
			else {
				// "locate" the individual within the namespace of the ontology
				// #221 retain the original camelCase in the URLs
				//resourceString = ns_ + getGoodName(row, id, allowColon).toLowerCase();
				resourceString = ns_ + getGoodName(str, allowColon);
			}

			Individual ind = newOntModel.createIndividual(resourceString, cs);
			ind.addProperty(RDFS.label, str);
			log.info("ind created " + ind);
			return ind;
		}
		return null;
	}



	private DatatypeProperty createDatatypeProperty(String str, int id) {
		boolean allowColon = false;

		String keyName = str;
		// #221 retain the original camelCase in the URLs
		//String goodName = getGoodName(row, id, allowColon).toLowerCase();
		String goodName = getGoodName(str, allowColon);

		if ( id == 0 && keyName.equalsIgnoreCase("uri")) {
			// use the key values in this column as the complet URI of each term.
			keyIsUri = true;

			if ( log.isDebugEnabled() ) {
				log.debug("first column labeled 'uri', so will use column values as full URIs for the terms");
			}
		}

		String resourceString = ns_ + goodName ;
		log.info("datatype Property created " + resourceString);
		DatatypeProperty p = newOntModel.createDatatypeProperty(resourceString);
		p.addProperty(RDFS.label, str);
		p.addDomain(classForTerms);
		return p;
	}

	private String getGoodName(String str, boolean allowColon) {
//		return finalUri + cleanStringforID(row.getString(id).trim());
//		return ns_ + cleanStringforID(row.getString(id).trim());
		return       cleanStringforID(str, allowColon);
	}

	private String cleanStringforID(String s, boolean allowColon) {

		return stringManipulation.replaceStringAllowColon(s.trim(),  allowColon);

	}



	private OntClass createClassNameGiven() {
		//		String resourceString = finalUri + setFirstUpperCase(cleanStringforID(primaryClass));

		boolean allowColon = false;
		String firtUcClass = setFirstUpperCase(cleanStringforID(primaryClass, allowColon));
		String resourceString = ns_ + firtUcClass ;

		OntClass cls = newOntModel.createClass(resourceString);

		log.info("KKKKKKKKKKKKKK cls.getNameSpace() = " +cls.getNameSpace());
		log.info("KKKKKKKKKKKKKK cls.getLocalName() = " +cls.getLocalName());

		cls.addProperty(RDFS.label, primaryClass);
		log.info("class created " + resourceString);

		return cls;

	}

	private String setFirstUpperCase(String s) {
		// return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}





	private void setFinalUri() {

		// If given, ontologyUri will take precedence (see VocabPanel)
		String ontologyUri = values.get("ontologyUri");

		if ( ontologyUri != null ) {
			log.debug("Using given ontologyUri and finalUri: " +ontologyUri);
			finalUri = ontologyUri.replaceAll("(/|\\\\)+$", "");
		}
		else {
			// remove any trailing slashes
			namespaceRoot = namespaceRoot.replaceAll("(/|\\\\)+$", "");

			//
			// replace any colon (:) in the pieces that go to the ontology URI
			// with underscores (_):
			//

			String orgAbbrev = orgAbbreviation.replaceAll("\\s+", "").replace(':', '_');

			finalUri = namespaceRoot + "/" + orgAbbrev;

			if ( version != null ) {
				finalUri +=  "/" + version;
			}

			finalShortName = shortName.toLowerCase().replace(':', '_');
			finalUri += "/" + finalShortName;
		}

		// see createProperties()

		log.info("setFinalUri: " +finalUri);
	}


	private void saveInFile(File fileLocation) throws Exception {
		String str = getAscii();
		FileOutputStream os = new FileOutputStream(fileLocation);
		IOUtils.write(str, os, "UTF-8");
		os.close();
	}


	/**
	 * @return the ascii
	 */
	private String getAscii() {
		return ascii;
	}


	public String getOntologyStringXml() {
		return JenaUtil2.getOntModelAsString(newOntModel, "RDF/XML-ABBREV");
	}

	public String getPathOnServer() {
		return uniqueBaseName;
	}

}
