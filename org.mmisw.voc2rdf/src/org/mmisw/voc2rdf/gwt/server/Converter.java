package org.mmisw.voc2rdf.gwt.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.mmi.util.ISO8601Date;

//import org.mmi.ont.voc2owl.trans.OwlCreatorComplex;
//import org.mmi.ont.voc2owl.trans.Transformer;
import org.mmisw.voc2rdf.transf.OwlCreatorComplex;
import org.mmisw.voc2rdf.transf.TransProperties;
import org.mmisw.voc2rdf.transf.Transformer;

/**
 * Dispatchs the conversion.
 * 
 * <p>
 * Adapted from org.mmi.web.MetadataBean.
 * 
 * @author Carlos Rueda
 */
class Converter {

	private static final String tmp = "/Users/Shared/registry/tmp/";

	private String title;

	private String description;

	private String orgAbbreviation;

	private String ascii;

	private String creator;

	private String namespaceRoot = "http://mmisw.org/ont";
	
	// To set TransProperties.NS
	private String finalUri;
	
	public String getFinalUri() {
		return finalUri;
	}


	private String primaryClass;


	private static final String ONE_CLASS_ALL_INSTANCES = OwlCreatorComplex.ONE_CLASS_ALL_INSTANCES;

	private String convertionType = ONE_CLASS_ALL_INSTANCES;

	private Transformer trans;


	private String fieldSeparator;

	private Object version;

	private static Logger logger = Logger.getLogger(Converter.class.getName());

	
	
	
	Converter(Map<String, String> values) {
		this.setCreator(values.get("creator"));
		this.setOrgAbbreviation(values.get("orgAbbreviation"));
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setPrimaryClass(values.get("primaryConcept"));
		this.setAscii(values.get("ascii"));
		this.setFieldSeparator(values.get("fieldSeparator"));
		this.setNamespaceRoot(values.get("namespaceRoot"));
	}

	public String createOntology() {
		logger.info("!!!!!!!!!!!!!!!! Converter.createOntology");
		String status = verify();
		if (status == "failure") {
			logger.warning("Failure to convert");
			return "failure";
		} else {

			setFinalUri();
			processCreateOntology();

			logger.info("Sucessfull creation");
			return "success";
		}

	}

	private void setFinalUri() {

		// remove any trailing slashes
		namespaceRoot = namespaceRoot.replaceAll("(/|\\\\)+$", "");  
		
		String orgAbbrev = orgAbbreviation.replaceAll("\\s+", "");
		
		finalUri = namespaceRoot + "/" + orgAbbrev;
		
		if ( version != null ) {
			finalUri +=  "/" + version;
		}
		
		finalUri += "/" + getPrimaryClass() + ".owl";
		
		// see createProperties()
		
		logger.info("setFinalUri: " +finalUri);
	}

	private Properties createProperties() {
		Properties prop = new Properties();
		logger.info("setting up properties from form");
		prop.setProperty(TransProperties.title, getTitle());
		prop.setProperty(TransProperties.description, getDescription());
		
//-		prop.setProperty(TransProperties.NS, getNamespace());
		logger.info("createProperties() finalNamespace = " +finalUri);
		prop.setProperty(TransProperties.NS, finalUri);
		
		prop.setProperty(TransProperties.creator, getCreator());
		prop
				.setProperty(TransProperties.nameForPrimaryClass,
						getPrimaryClass());
		prop.setProperty(TransProperties.columnForPrimaryClass, 0 + "");
		prop.setProperty(TransProperties.createAllRelationsHierarchy, "false");
		prop.setProperty(TransProperties.format, getFieldSeparator());
		prop.setProperty(TransProperties.treatAsHierarchy, "false");
		prop.setProperty(TransProperties.convertionType, getConvertionType());
		// prop.setProperty(TransProperties.format, getCurrentFieldSeparator()
		// .getValue().toString());
		// logger.info("String Separator "
		// + prop.getProperty(TransProperties.format));
		// String tmp = ResourceLoader.getUrlResource("tmp");
		return prop;

	}

	private void processCreateOntology() {
		Properties prop = createProperties();

//		String intermName = ISO8601Date.getCurrentDateBasicFormat();

		// String fileIn = tmp + intermName + ".txt";
		String fileInText = tmp + createUniqueName() + ".txt";
		saveInFile(fileInText);
		String fileOutRDF = tmp + createUniqueName() + ".rdf";
		prop.setProperty(TransProperties.fileIn, fileInText);
		prop.setProperty(TransProperties.fileOut, fileOutRDF);

		trans = new Transformer(prop);
		trans.getProperties();
		try {
			trans.transformAndSave();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String propFile = tmp + createUniqueName() + ".properties";
		storeProperties(prop, propFile);

		String stats = tmp + "stats.txt";
		try {
			File f = new File(stats);
			f.createNewFile();

			FileWriter fw = new FileWriter(f, true);
			fw.write(ISO8601Date.getCurrentDateBasicFormat() + " , "
					+ getTitle() + " , " + propFile + " , " + fileOutRDF);
			fw.write(System.getProperty("line.separator"));
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	private void storeProperties(Properties prop, String propFile) {
		try {
			prop.store(new FileOutputStream(propFile), "voc2rdf properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String verify() {

		return "success";

	}

	/**
	 * @return the description
	 */
	private String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	private void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the title
	 */
	private String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	private void setTitle(String title) {
//		if (title.length() < 2) {
//			addMessage("loginForm:title", "title length must be greater than 2");
//		}
		this.title = title;
	}

	/**
	 * @return the ascii
	 */
	private String getAscii() {
		return ascii;
	}

	/**
	 * @param ascii
	 *            the ascii to set
	 */
	private void setAscii(String ascii) {
		this.ascii = ascii;
	}

	/**
	 * @return the creator
	 */
	private String getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	private void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @param namespaceRoot
	 *            the namespace to set
	 */
	private void setNamespaceRoot(String namespaceRoot) {
		this.namespaceRoot = namespaceRoot;
	}

	/**
	 * @return the primaryClass
	 */
	private String getPrimaryClass() {

		return primaryClass;
	}

	/**
	 * @param primaryClass
	 *            the primaryClass to set
	 */
	private void setPrimaryClass(String primaryClass) {
		logger.info("setting primary class " + primaryClass);
		this.primaryClass = primaryClass;
	}



	public String getOntologyStringXml() {
		return trans.getOntologyAsString();
	}



	/**
	 * @return the fieldSeparator
	 */
	private String getFieldSeparator() {
		return fieldSeparator;
	}

	/**
	 * @param fieldSeparator
	 *            the fieldSeparator to set
	 */
	private void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}



	/**
	 * @param orgAbbreviation
	 *            the orgAbbreviation to set
	 */
	private void setOrgAbbreviation(String orgAbbreviation) {
		this.orgAbbreviation = orgAbbreviation;
	}

	/**
	 * @return the convertionType
	 */
	private String getConvertionType() {
		return convertionType;
	}

	private String createUniqueName() {
		return "" +System.currentTimeMillis();

	}

}