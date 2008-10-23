package org.mmisw.voc2rdf.gwt.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.mmi.ont.voc2owl.trans.OwlCreatorComplex;
import org.mmi.ont.voc2owl.trans.TransProperties;
import org.mmi.ont.voc2owl.trans.Transformer;
import org.mmi.util.ISO8601Date;

/**
 * Dispatchs the conversion.
 * 
 * <p>
 * Adapted from org.mmi.web.MetadataBean.
 * 
 * @author Carlos Rueda
 */
class Converter {

	private static String tmp = "/Users/Shared/registry/tmp/";

	private String title = "Project X Parameters";

	private String description = "parameters used in project X";

	private String orgAbbreviation = "mmi";

	private String ascii = getASCII();

	private String creator = "John Smith";

	private String namespace = "http://mmisw.org/ont";
	
	// To set TransProperties.NS
	private String finalNamespace;
	
	public String getFinalNamespace() {
		return finalNamespace;
	}


	private String primaryClass = "parameter";


	private String ONE_CLASS_ALL_INSTANCES = OwlCreatorComplex.ONE_CLASS_ALL_INSTANCES;


	private String convertionType = ONE_CLASS_ALL_INSTANCES;

	private Transformer trans;


	private String fieldSeparator = "csv";

	private String uid = "1000";

	private static Logger logger = Logger.getLogger(Converter.class.getName());

	private static String getASCII() {
		String s = "name,description" + "\r"
				+ "sea surface salinity, salinity at the sea surface >10 m."
				+ "\r" + "sst, sea surface temperature";
		return s;
	}
	
	
	
	Converter(Map<String, String> values) {
		this.setCreator(values.get("creator"));
		this.setOrgAbbreviation(values.get("orgAbbreviation"));
		this.setTitle(values.get("title"));
		this.setDescription(values.get("description"));
		this.setPrimaryClass(values.get("primaryConcept"));
		this.setAscii(values.get("ascii"));
		this.setFieldSeparator(values.get("fieldSeparator"));
		this.setNamespace(values.get("namespace"));
	}

	public String createOntology() {
		String status = verify();
		if (status == "failure") {
			logger.warning("Failure to convert");
			return "failure";
		} else {

			setNamespace();
			processCreateOntology();

			logger.info("Sucessfull creation");
			return "success";
		}

	}

	private void setNamespace() {

		String orgAbbrev = orgAbbreviation.trim().replace(" ", "");
		
		// <carueda> 2008-10-02:  setNamespace(ns) was not honored ...
		// this was the original code:
//-		namespace = basic_namespace + "/" + orgAbbrev + "/"+getPrimaryClass()+".owl";
		// so namespace would always start with basic_namespace (which never changed)
		
		// A quick solution (to minimize changes in the code) is to consider the
		// bean property "namespace" as actually the namespaceRoot to construct
		// the final TransProperties.NS property:
		namespace = namespace.replaceAll("(/|\\\\)+$", "");  // remove any trailing slashes
		finalNamespace = namespace + "/" + orgAbbrev + "/"+getPrimaryClass()+".owl";
		// see createProperties()
		
		logger.info("setNamespace() finalNamespace = " +finalNamespace);
	}

	private Properties createProperties() {
		Properties prop = new Properties();
		logger.info("setting up properties from form");
		prop.setProperty(TransProperties.title, getTitle());
		prop.setProperty(TransProperties.description, getDescription());
		
//-		prop.setProperty(TransProperties.NS, getNamespace());
		logger.info("createProperties() finalNamespace = " +finalNamespace);
		prop.setProperty(TransProperties.NS, finalNamespace);
		
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
	 * @param namespace
	 *            the namespace to set
	 */
	private void setNamespace(String namespace) {
		this.namespace = namespace;
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
//		FacesContext context = FacesContext.getCurrentInstance();
//
//		String param = (String) context.getExternalContext()
//				.getRequestParameterMap().get("uid");

		long l = System.currentTimeMillis();
		return getUid() + "" + l;

	}

	/**
	 * @return the uid
	 */
	private String getUid() {
		return uid;
	}

}