package org.mmi.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.mmi.ont.html.XMLtoHTML;
//import org.mmi.ont.voc2owl.trans.OwlCreatorComplex;
//import org.mmi.ont.voc2owl.trans.Transformer;
import org.mmi.util.ISO8601Date;
import org.mmisw.ontmd.transf.OwlCreatorComplex;
import org.mmisw.ontmd.transf.TransProperties;
import org.mmisw.ontmd.transf.Transformer;




public class MetadataBean {
	
	
	static {
		System.out.println("  !!!!!!!!!!!!USING " +OwlCreatorComplex.class.getName());
		System.out.println("  !!!!!!!!!!!!USING " +Transformer.class.getName());
	}

	private static String tmp = "/Users/Shared/registry/tmp/";

	private String title = "Project X Parameters";

	private String description = "parameters used in project X";

	private String orgAbbreviation = "mmi";

	private String ascii = getASCII();

	private String creator = "John Smith";

//	private String email = "email";

	// <carueda> originally:
//-	private String namespace = "http://mmisw.org/";
	
	// NOTE temporary initialization to facilitate resolution in the regtest
	private String namespace = "http://mmisw.org/ont";
	// actually it should be: "http://mmisw.org/ont";
	// where "ont" is the 'root' component as explained in the mmi document.
	
	// To set TransProperties.NS
	private String finalNamespace;
	
	public String getFinalNamespace() {
		return finalNamespace;
	}

	// carueda: was only used in setNamespace() previously. TODO: remove
//-	private String basic_namespace = "http://mmisw.org/ont";

	private String primaryClass = "parameter";

//	private String ontologyString = null;

	private String ONE_CLASS_ALL_INSTANCES = OwlCreatorComplex.ONE_CLASS_ALL_INSTANCES;

//	private String CLASS_HIERARCHY = OwlCreatorComplex.CLASS_HIERARCHY;

	private String convertionType = ONE_CLASS_ALL_INSTANCES;

	private Transformer trans;

//	private SelectItem item_csv = new SelectItem("csv", "comma");
//
//	private SelectItem item_tab = new SelectItem("tab", "tab");
//
//	private SelectItem currentFieldSeparator = item_csv;

	// private String user=null;
	private String userName = null;

	private String password = null;

	private boolean isLogged = false;

	private String fieldSeparator = "csv";

	private boolean uploadNext;

	private String uid = "1000";

	private String fileName = "test.owl";

	private static Logger logger = Logger.getLogger("org.mmi.web.MetadataBean");

	private static String getASCII() {
		String s = "name,description" + "\r"
				+ "sea surface salinity, salinity at the sea surface >10 m."
				+ "\r" + "sst, sea surface temperature";
		return s;
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
		fileName = fileOutRDF;

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

	public String verify() {

		return "success";

	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
//		if (title.length() < 2) {
//			addMessage("loginForm:title", "title length must be greater than 2");
//		}
		this.title = title;
	}

	/**
	 * @return the ascii
	 */
	public String getAscii() {
		return ascii;
	}

	/**
	 * @param ascii
	 *            the ascii to set
	 */
	public void setAscii(String ascii) {
		this.ascii = ascii;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		// return "http://marinemetadata.org/ont/"
		// + ISO8601Date.getCurrentDateForNamespace() + "/"
		// + getOrgAbbreviation() + "_" + getPrimaryClass();
		return namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the primaryClass
	 */
	public String getPrimaryClass() {

		return primaryClass;
	}

	/**
	 * @param primaryClass
	 *            the primaryClass to set
	 */
	public void setPrimaryClass(String primaryClass) {
		logger.info("setting primary class " + primaryClass);
		this.primaryClass = primaryClass;
	}

	/**
	 * Sets a message for a value of a JSP object. it should be in the form
	 * idForm:idObject. Specially use for validation.
	 * 
	 * @param objectJSP
	 * @param message
	 */
//	private void addMessage(String objectJSP, String message) {
//		FacesContext.getCurrentInstance().addMessage(objectJSP,
//				new FacesMessage(message));
//	}

	/**
	 * @return the getOntologyString
	 */
	public String getOntologyString() {
		XMLtoHTML xmlToHTML = new XMLtoHTML();
		return xmlToHTML.convert(trans.getOntologyAsString());
		// return trans.getOntologyAsString();

	}

	public String getOntologyStringXml() {
		return trans.getOntologyAsString();
	}

	/**
	 * @param getOntologyString
	 *            the getOntologyString to set
	 */
//	public void setOntologyString(String ontologyString) {
//		this.ontologyString = ontologyString;
//	}

//	public List getfieldSeparatorList() {
//		List list = new ArrayList();
//
//		list.add(item_csv);
//		list.add(item_tab);
//		return list;
//
//	}

	/**
	 * @param fieldSeparator
	 *            the fieldSeparator to set
	 */
//	public void setCurrentFieldSeparator(SelectItem item) {
//		logger.info("Setting field separator " + item);
//		this.currentFieldSeparator = item;
//	}
//
//	public SelectItem getCurrentFieldSeparator() {
//		return currentFieldSeparator;
//	}

	/**
	 * @return the fieldSeparator
	 */
	public String getFieldSeparator() {
		return fieldSeparator;
	}

	/**
	 * @param fieldSeparator
	 *            the fieldSeparator to set
	 */
	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	// /**
	// * @return the user
	// */
	// public String getUser() {
	// return user;
	// }
	//
	// /**
	// * @param user the user to set
	// */
	// public void setUser(String user) {
	// String users = ResourceLoader.getUrlResource("users.txt");
	// Properties usersProp = new Properties();
	// String user_ = usersProp.getProperty(user);
	// if (user_!=null){
	// this.user = user;
	// }else{
	// addMessage("uploadForm:user", "user not found");
	// }
	//		
	// }

	public String uploadOntologyToServer() {
		if (isLogged) {
			logger.info("is logged");
			setUploadNext(false);
			return "OntologyUploaded";
		} else {
			setUploadNext(true);
			return "UserNotLogged";
		}

	}

	// //////

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isOk() {
		if (getUserName().equals("guest") && getPassword().equals("guest")) {
			return true;
		} else {
			return false;
		}

	}

	// String users = ResourceLoader.getUrlResource("users.txt");
	// Properties usersProp = new Properties();
	// if (user!=null){
	// String pass = usersProp.getProperty(user);
	// if (pass.equals(password)){
	// this.password = password;
	// }else{
	// addMessage("uploadForm:password", "password is wrong for user");
	// }
	// }else{
	// addMessage("uploadForm:password", "user not found");
	// }

//	public String loginUser() {
//
//		if (isOk()) {
//			isLogged = true;
//			return "loginSuccess";
//
//		} else {
//			FacesContext facesContext = FacesContext.getCurrentInstance();
//			FacesMessage facesMessage = new FacesMessage(
//					"You have entered an invalid user name and/or password");
//			facesContext.addMessage("loginForm", facesMessage);
//			isLogged = false;
//			return "loginFailure";
//		}
//	}

	/**
	 * @return the isLogged
	 */
	public boolean getIsLogged() {
		return isLogged;
	}

	/**
	 * @param isLogged
	 *            the isLogged to set
	 */
	public void setIsLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	/**
	 * @return the uploadNext
	 */
	public boolean isUploadNext() {
		return uploadNext;
	}

	/**
	 * @return the uploadNext
	 */
	public boolean getUploadNext() {
		return uploadNext;
	}

	/**
	 * @param uploadNext
	 *            the uploadNext to set
	 */
	public void setUploadNext(boolean uploadNext) {
		logger.info("next is upload = " + uploadNext);
		this.uploadNext = uploadNext;
	}

	public String signOut() {
		userName = null;
		password = null;
		isLogged = false;
		return "signOut";
	}

	/**
	 * @return the orgAbbreviation
	 */
	public String getOrgAbbreviation() {
		return orgAbbreviation;
	}

	/**
	 * @param orgAbbreviation
	 *            the orgAbbreviation to set
	 */
	public void setOrgAbbreviation(String orgAbbreviation) {
		this.orgAbbreviation = orgAbbreviation;
	}

	/**
	 * @return the convertionType
	 */
	public String getConvertionType() {
		return convertionType;
	}

	/**
	 * @param convertionType
	 *            the convertionType to set
	 */
	public void setConvertionType(String convertionType) {
		this.convertionType = convertionType;
	}

	// public void sendToRegistry(){
	// String fileIn =
	// trans.getProperties().getProperty(TransProperties.fileIn);
	//		
	//		
	//		
	//	
	//	
	// }

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
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	// /**
	// * @param fileName the fileName to set
	// */
	// public void setFileName(String fileName) {
	// this.fileName = fileName;
	// }
	//	

	public String changeNamespace() {
		namespace = namespace + getOrgAbbreviation();
		return "success";
	}

}