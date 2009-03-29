package org.mmisw.ontmd.gwt.server.voc2rdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ontmd.gwt.server.Config;
import org.mmisw.ontmd.gwt.server.JenaUtil2;
import org.mmisw.ontmd.gwt.server.MdHelper;
import org.mmisw.voc2rdf.transf.StringManipulationInterface;
import org.mmisw.voc2rdf.transf.StringManipulationUtil;

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
 * Adapted from org.mmi.web.MetadataBean.
 * 
 * @author Carlos Rueda
 */
public class Converter {

	// TODO take this location from a config parameter:
	private static final String tmp = "/Users/Shared/registry/tmp/";

	private String orgAbbreviation;

	private String ascii;
	private String fieldSeparator;

	private String namespaceRoot = "http://mmisw.org/ont";
	
	// To set TransProperties.NS
	private String finalUri;
	
	public String getFinalUri() {
		return finalUri;
	}


	private String primaryClass;


//	private static final String ONE_CLASS_ALL_INSTANCES = OwlCreatorComplex.ONE_CLASS_ALL_INSTANCES;
//
//	private String convertionType = ONE_CLASS_ALL_INSTANCES;


	private Object version;

	private final Log log = LogFactory.getLog(Converter.class);

	
	
	private final Map<String, String> values;
	
	
	private Resource[] res;
	private OwlModel newOntModel;
	private String ns_;
	private String base_;

	private OntClass classForTerms;
	
	private StringManipulationInterface stringManipulation = new StringManipulationUtil();

	private String pathOnServer;

	
	/**
	 * TODO createUniqueBaseName(): need a more robust way to get a unique name. 
	 */
	private static String _createUniqueBaseName() {
		return String.valueOf(System.currentTimeMillis());

	}
	private final String uniqueBaseName = _createUniqueBaseName();
	
	
	public Converter(
			String namespaceRoot, 
			String orgAbbreviation,
			String primaryClass,
			String ascii, 
			String fieldSeparator,
			Map<String, String> values) 
	{
		this.namespaceRoot = namespaceRoot;
		this.orgAbbreviation = orgAbbreviation;
		this.primaryClass = primaryClass;
		this.ascii = _convertToUtf8(ascii);
		this.fieldSeparator = fieldSeparator;
		this.values = values;
		
		log.info("!!!!!!!!!!!!!!!! Converter: values = " +values);
		log.info("setting primary class " + primaryClass);
		
	}
	
	/**
	 * Converts the string to UTF-8 encoding.
	 * FIXME implementation is rather simplistic, just
	 * <code>return new String(str.getBytes(), "UTF-8"))</code>.
	 * A better approach is to determine the actual original charset and
	 * then do the conversion to UTF-8.
	 */
	private String _convertToUtf8(String str) {
		try {
			byte[] bytes = str.getBytes();
			String utf8_str = new String(bytes, "UTF-8");
			return utf8_str;
		}
		catch (UnsupportedEncodingException e) {
			log.warn("Cannot convert to UTF-8. " +e.toString());
			e.printStackTrace();
		}

		return str;
	}



	public String createOntology() throws Exception {
		log.info("!!!!!!!!!!!!!!!! Converter.createOntology");
		
		setFinalUri();
		processCreateOntology();

		// save the converted ontology in the server to enable subsequent
		// metadata edition by ontmd if the user so wishes:
		String rdf = getOntologyStringXml();
		
		// just the simple name:
		setPathOnServer(uniqueBaseName); 
		
		String full_path = Config.Prop.ONTMD_VOC2RDF_DIR.getValue() + getPathOnServer();
		
		try {
			FileWriter os = new FileWriter(full_path);
			os.write(rdf);
			os.close();
		}
		catch (IOException ex) {
			throw new Exception("Error writing generated file: " +full_path, ex);
		}

		return null;   // OK
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
		
		// set Omv.name from primaryClass
		ont.addProperty(Omv.name, 
				setFirstUpperCase(cleanStringforID(primaryClass)) + " Vocabulary");
		
		// set Omv.acronym from primaryClass
		ont.addProperty(Omv.acronym, primaryClass);
		
		String classUri = values.get("classUri");
		if ( classUri != null ) {
			ont.addProperty(OmvMmi.shortNameUri, classUri);
		}
		
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

	private void createIndividual(DataRow row) {
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
	
	
	private void createNotHierarchyIndividual(Individual ind, DataRow row) {

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


	private Individual createIndividual(DataRow row, int id, OntClass cs) {

		if (isGood(row, id)) {
			String resourceString = getGoodName(row, id);
			Individual ind = newOntModel.createIndividual(resourceString, cs);
			ind.addProperty(RDFS.label, row.getString(id).trim());
			log.info("ind created " + ind);
			return ind;
		}
		return null;
	}


	
	private DatatypeProperty createDatatypeProperty(DataRow row, int id) {
		//String resourceString = getGoodName(row, id).toLowerCase();
		String resourceString = ns_ + getGoodName(row, id).toLowerCase();
		log.info("datatype Property created " + resourceString);
		DatatypeProperty p = newOntModel.createDatatypeProperty(resourceString);
		p.addProperty(RDFS.label, row.getString(id).trim());
		p.addDomain(classForTerms);
		return p;
	}

	private String getGoodName(DataRow row, int id) {
//		return finalUri + cleanStringforID(row.getString(id).trim());
//		return ns_ + cleanStringforID(row.getString(id).trim());
		return       cleanStringforID(row.getString(id).trim());
	}

	private String cleanStringforID(String s) {

		return stringManipulation.replaceString(s.trim());

	}

	
	
	private OntClass createClassNameGiven() {
		//		String resourceString = finalUri + setFirstUpperCase(cleanStringforID(primaryClass));
		String firtUcClass = setFirstUpperCase(cleanStringforID(primaryClass));
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
			
			String orgAbbrev = orgAbbreviation.replaceAll("\\s+", "");
			
			finalUri = namespaceRoot + "/" + orgAbbrev;
			
			if ( version != null ) {
				finalUri +=  "/" + version;
			}
			
			finalUri += "/" + getPrimaryClass().toLowerCase() ;
			// NO ".owl" extension!!
			// because it messes up the base URI of the elements
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


	/**
	 * @return the primaryClass
	 */
	private String getPrimaryClass() {

		return primaryClass;
	}




	public String getOntologyStringXml() {
		return JenaUtil2.getOntModelAsString(newOntModel, "RDF/XML-ABBREV");
	}




	private void setPathOnServer(String pathOnServer) {
		this.pathOnServer = pathOnServer;
	}

	public String getPathOnServer() {
		return pathOnServer;
	}

}