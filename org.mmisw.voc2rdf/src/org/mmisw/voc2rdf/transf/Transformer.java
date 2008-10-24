package org.mmisw.voc2rdf.transf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

//import org.apache.log4j.Category;
import org.apache.log4j.Logger;
//import org.eclipse.core.runtime.IProgressMonitor;
import org.mmi.util.ResourceLoader;

//import com.hp.hpl.jena.ontology.OntModel;

import edu.drexel.util.rdf.JenaUtil;

/**
 * (Copied/Adapted from org.mmi.ont.voc2owl.trans
 * for "easy" adjustments -- Carlos Rueda)
 * <p>
 * <p>
 *
 * <p>
 * Converts an ASCII file to an ontology given a transforamtion property file.
 * The property file is loaded into a Transformation Object. The actual
 * transformation is being performed by the {@link OwlCreatorComplex}.
 * 
 * OwlCreatorComplexa vocabulary in ASCII
 * </p>
 * <hr>
 * 
 * @author : $Author: luisbermudez $
 * @version : $Revision: 1.1 $
 * @since : Aug 8, 2006
 */

public class Transformer {
	private Properties properties;

//	private Properties actions;

	private Transformation trans;

	public static final String format_csv = "CSV";

	public static final String format_tab = "TAB";
	static Logger logger = Logger.getLogger(Transformer.class.getName());

	



	private IProgressMonitor monitor;

	private OwlCreatorComplex creator;
	
	

	public Transformer(String propertyFileLocation) {
		properties = new Properties();
			try {
			properties.load(new FileInputStream(propertyFileLocation));
		} catch (FileNotFoundException e) {
			try {
				properties.load(new FileInputStream(ResourceLoader
						.getPath(propertyFileLocation)));
			} catch (IOException f) {
				f.printStackTrace();
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		
	

	}

	public Transformer(Properties properties) {
		this.properties = properties;
		
		

	}

	public Transformer(Properties properties, IProgressMonitor monitor) {
		this(properties);
		this.monitor = monitor;
	

	}
	
	public Transformer(Transformation transformation, IProgressMonitor monitor){
		this.trans = transformation;
		this.monitor = monitor;
		}

	private void createTransformation() {
		logger.info("Creating transformation");
		trans = new Transformation();
		Set set = properties.keySet();
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			String prop = (String) iter.next();
			String value = properties.getProperty(prop);
			assign(prop, value);

		}
	}
	
	// an upgrades the transformation
	public void createOWLCreator(){
		if (trans==null){
			createTransformation();
		}
		creator = new OwlCreatorComplex(trans, monitor);
	}

	private boolean getStringAsBoolean(String s) {
		if (s.equalsIgnoreCase("true") || (s.equalsIgnoreCase("yes"))) {
			return true;
		}
		return false;
	}

	private void assign(String prop, String value) {
		if (prop.equalsIgnoreCase(TransProperties.format)) {
			if (value.equalsIgnoreCase(format_csv)) {
				trans.setFormat(Transformation.CSV);
			} else if (value.equalsIgnoreCase(format_tab)) {
				trans.setFormat(Transformation.TAB);

			} else {
				trans.setFormat(Transformation.UNKOWN);
			}

		} else if (prop.equalsIgnoreCase(TransProperties.fileIn)) {
			trans.setFileIn(value);
		} else if (prop.equalsIgnoreCase(TransProperties.fileOut)) {
			trans.setFileOut(value);
		} else if (prop.equalsIgnoreCase(TransProperties.NS)) {
			trans.setNS(value);
		} else if (prop.equalsIgnoreCase(TransProperties.description)) {
			trans.setDescription(value);
		} else if (prop.equalsIgnoreCase(TransProperties.contributor)) {
			trans.setContributor(value);
		} else if (prop.equalsIgnoreCase(TransProperties.convertToClass)) {
			trans.setConvertToClass(getIntValues(value));
		} else if (prop.equalsIgnoreCase(TransProperties.columnForPrimaryClass)) {
			if (value.length() > 0) {
				trans.setColumnForPrimaryClass(Integer.parseInt(value));
			}
		} else if (prop.equalsIgnoreCase(TransProperties.nameForPrimaryClass)) {
			trans.setNameForPrimaryClass(value);
		} else if (prop.equalsIgnoreCase(TransProperties.treatAsHierarchy)) {

			trans.setTreatAsHierarchy(getStringAsBoolean(value));
			System.out.println("treat as a hier " + trans.isTreatAsHierarchy());
		} else if (prop
				.equalsIgnoreCase(TransProperties.createAllRelationsHierarchy)) {
			trans.setCreateAllRelationsHierarchy(getStringAsBoolean(value));
		} else if (prop.equalsIgnoreCase(TransProperties.generateAutoIds)) {
			trans.setGenerateAutoIds(getStringAsBoolean(value));
		} else if (prop.equalsIgnoreCase(TransProperties.source)) {
			trans.setSource(value);
		} else if (prop.equalsIgnoreCase(TransProperties.subject)) {
			trans.setSubject(value);
		} else if (prop.equalsIgnoreCase(TransProperties.title)) {
			trans.setTitle(value);
		} else if (prop.equalsIgnoreCase(TransProperties.URLMoreInformation)) {
			trans.setURLMoreInformation(value);
		} else if (prop.equalsIgnoreCase(TransProperties.creator)) {
			trans.setCreator(value);
			
		} else if (prop.equalsIgnoreCase(TransProperties.convertionType)){
			trans.setConvertionType(value);
		}else

		{
			logger.error("value for transformation not assign: " + value);
		}

	}

	private int[] getIntValues(String value) {
		List list = new ArrayList();

		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		while (tokenizer.hasMoreTokens()) {
			String s = tokenizer.nextToken();
			int i = Integer.parseInt(s);
			list.add(new Integer(i));
		}
		int[] integers = new int[list.size()];
		int i = 0;
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Integer inte = (Integer) iter.next();
			integers[i++] = inte.intValue();

		}
		return integers;
	}

	public void transformAndSave() throws Exception {
		try {
			createOWLCreator();
//			creator.convert();    --> in original code, so conversion was done twice.
			creator.convertAndSave();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void transform() throws Exception {
		try {
			createOWLCreator();
			creator.convert();
		
		} catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Transforms a file using the given stringManipulation to create URIs.
	 * 
	 * @param stringManipulation
	 *            dictates the strategy for manipulating strings that need to be
	 *            created as local part names of URIs
	 * 
	 */
	public void transform(StringManipulationInterface stringManipulation)
			throws Exception {
		createOWLCreator();
		logger.info("starting transformation");
		creator.setStringManipulation(stringManipulation);
		creator.convertAndSave();

	}

	/**
	 * Convenient method to call this class externally.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args != null && args[0].endsWith(".properties")) {
			String file = args[0];
			System.out.println("file is " + file);
			Transformer t = new Transformer(file);
			t.transformAndSave();
		} else {
			System.out.println("Please give the filePath of the property file");
		}
	}

	public String getOntologyAsString(){
		return JenaUtil.getOntModelAsString(creator.getOntology());
		
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
		creator =null;
		//update OWL creator based on this properties
		createOWLCreator();
	}

	

}

