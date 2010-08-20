package org.mmisw.iserver.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.StringManipulationUtil;
import org.mmisw.iserver.core.util.Utf8Util;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.VocabularyDataCreationInfo;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.infomata.data.CSVFormat;
import com.infomata.data.DataFile;
import com.infomata.data.DataRow;
import com.infomata.data.TabFormat;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;

/**
 * Dispatchs the conversion.
 * 
 * <p>
 * Adapted from org.mmi.web.MetadataBean. Note that changes have been kept to a minimum, basically
 * to whatever is strictly necessary to make the whole thing work. Time permitting, this will be
 * cleaned up at some point.
 * 
 * @author Carlos Rueda
 */
public class VocabCreator {

	// TODO take this from a config parameter:
	private static final String tmp = "/Users/Shared/registry/tmp/";

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
	private OwlModel newOntModel;
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
	
	
	private String _createAscii(VocabularyDataCreationInfo createOntologyInfo) {

		final String SEPARATOR = ",";
		final String QUOTE = "\"";
		
		fieldSeparator = "csv";
		
		String sep;
		StringBuilder sb = new StringBuilder();
		
		List<String> header = createOntologyInfo.getColNames();
		sep = "";
		for (String str : header ) {
			sb.append(sep + QUOTE + str + QUOTE);
			sep = SEPARATOR;
		}
		sb.append("\n");
		
		List<List<String>> rows = createOntologyInfo.getRows();
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
		
		
		Map<String, String> values = createOntologyInfo.getMetadataValues();
		
		
		this.namespaceRoot = ServerConfig.Prop.ONT_SERVICE_URL.getValue();
		this.orgAbbreviation = createOntologyInfo.getAuthority();
		this.shortName = createOntologyInfo.getShortName();
		
		this.primaryClass = vocabularyDataCreationInfo.getClassName();
		this.ascii = _createAscii(vocabularyDataCreationInfo);
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
		String full_path_csv = ServerConfig.Prop.ONTMD_VOC2RDF_DIR.getValue() + getPathOnServer() + ".csv";
		try {
			FileWriter os = new FileWriter(full_path_csv);
			IOUtils.copy(new StringReader(ascii), os);
			os.close();
		}
		catch (IOException ex) {
			String msg = "Error saving copy of text file: " +full_path_csv;
			log.error(msg, ex);
			createVocabResult.setError(msg);
			return;
		}

		// now save the RDF:
		String full_path = ServerConfig.Prop.ONTMD_VOC2RDF_DIR.getValue() + getPathOnServer();
		try {
			FileWriter os = new FileWriter(full_path);
			os.write(rdf);
			os.close();
		}
		catch (IOException ex) {
			String msg = "Error writing generated RDF file: " +full_path; 
			log.error(msg, ex);
			createVocabResult.setError(msg);
			return;
		}

		// OK:
		createVocabResult.setFullPath(full_path);
	}
	
	
	private void processCreateOntology() throws Exception {

		String fileInText = tmp + uniqueBaseName + ".txt";
		saveInFile(fileInText);
		
		newOntModel = new OwlModel(JenaUtil.createDefaultOntModel());
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
			String value = values.get(uri);
			if ( value.trim().length() > 0 ) {
				Property prop = uriPropMap.get(uri);
				if ( prop == null ) {
					log.warn("No property found for uri='" +uri+ "'");
					continue;
				}
				ont.addProperty(prop, value.trim());
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
		
		createOntologIndividuals(fileInText);
	}


	
	
	private void createOntologIndividuals(String fileInText) throws Exception {

		DataFile read = DataFile.createReader("8859_1");
		if ( "csv".equalsIgnoreCase(fieldSeparator) ) {
			read.setDataFormat(new CSVFormat());
		}
		else if ( "tab".equalsIgnoreCase(fieldSeparator) ) {
			read.setDataFormat(new TabFormat());
		}
		else {
			throw new Exception("Unexpected field separator value: " +fieldSeparator+
					"\nMust be one of: csv, tab");
		}
		
		read.open(new File(fileInText));

		try {
			DataRow row = null;
			// first row
			row = read.next();
			if (row != null) {
				createPropertiesAndClasses(row);
			}

			try {
				for (row = read.next(); row != null; row = read.next()) {
					createIndividual(row);
				}
			} catch (ArrayIndexOutOfBoundsException ae) {
				throw (ae);
			}

		} catch (Exception e) {
			throw (e);
		}

		finally {
			read.close();
		}
	}

	private void createPropertiesAndClasses(DataRow row) {
		int size = row.size();
		res = new Resource[size];

		// object properties is set up later, when creating individuals

		classForTerms = createClassNameGiven();
		System.out.println("class for terms created " + classForTerms);


		for (int i = 0; i < size; i++) {

			log.info("converting column header " + i
					+ " to a datatype property");
			res[i] = createDatatypeProperty(row, i);
		}

	}

	private void createIndividual(DataRow row) throws Exception {
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
	
	
	private void createNotHierarchyIndividual(Individual ind, DataRow row) throws Exception {

		if (ind != null) {
			for (int i = 0; i < row.size(); i++) {
				// this contains either classes or datatypeproperties
				Resource r = res[i];
				if (row.getString(i).length() > 0) {
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
						ind.addProperty(dp, row.getString(i).trim());

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




	private boolean isGood(DataRow row, int id) {
		return row.getString(id).trim().length() > 0;

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
	private Individual createIndividual(DataRow row, int id, OntClass cs) throws Exception {

		if (isGood(row, id)) {
			boolean allowColon = true;
			String resourceString;
			
			if ( id == 0 && keyIsUri ) {
				
				// OLD:
				//// just use the simple name given in this column
				////resourceString = getGoodName(row, id, allowColon).toLowerCase();
				
				// New: regarding Issue #164 "Keep periods and case in term URI" and 
				// more generally, just check that it's a valid URI and do no conversions at all
				String uri = row.getString(id).trim();
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
				resourceString = ns_ + getGoodName(row, id, allowColon);
			}
			
			Individual ind = newOntModel.createIndividual(resourceString, cs);
			ind.addProperty(RDFS.label, row.getString(id).trim());
			log.info("ind created " + ind);
			return ind;
		}
		return null;
	}


	
	private DatatypeProperty createDatatypeProperty(DataRow row, int id) {
		boolean allowColon = false;
		
		String keyName = row.getString(id).trim();
		// #221 retain the original camelCase in the URLs
		//String goodName = getGoodName(row, id, allowColon).toLowerCase();
		String goodName = getGoodName(row, id, allowColon);
		
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
		p.addProperty(RDFS.label, row.getString(id).trim());
		p.addDomain(classForTerms);
		return p;
	}

	private String getGoodName(DataRow row, int id, boolean allowColon) {
//		return finalUri + cleanStringforID(row.getString(id).trim());
//		return ns_ + cleanStringforID(row.getString(id).trim());
		return       cleanStringforID(row.getString(id).trim(), allowColon);
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


	private void saveInFile(String fileLocation) {
		try {
			FileWriter fileWriter = new FileWriter(fileLocation);
			fileWriter.write(getAscii());
			fileWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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