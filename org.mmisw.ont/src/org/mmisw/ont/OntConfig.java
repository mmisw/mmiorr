package org.mmisw.ont;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ont configuration properties.
 * These properties are obtained from the ServletConfig object.
 * 
 * @author Carlos Rueda
 */
class OntConfig {
	
	/**
	 * The Names of the properties that are used by the Ont service.
	 */
	public enum Prop {
		AQUAPORTAL_UPLOADS_DIRECTORY  ("aquaportal.uploads.directory"),
		AQUAPORTAL_DATASOURCE         ("aquaportal.datasource");
		
		private String name;
		Prop(String name) { this.name = name; };
		
		public String getName() { return name; }
	}
	
	private final Log log = LogFactory.getLog(OntConfig.class);
	
	private final Properties props = new Properties();
	
	/** Call {@link #init(ServletConfig)} to initialize. */
	OntConfig() {
	}

	/**
	 * Initializes this object loading the initialization parameters
	 * and checking that the required ones are defined.
	 * 
	 * @throws Exception If any required parameter is undefined.
	 */
	void init(ServletConfig sc) throws Exception {
		// load the initParameters:
		Enumeration<?> parNames = sc.getInitParameterNames();
		while ( parNames.hasMoreElements() ) {
			String parName = (String) parNames.nextElement();
			String parValue = sc.getInitParameter(parName);
			props.setProperty(parName, parValue);
			log.debug(parName+ " = " +parValue);
		}
		
		// check the required properties, ie, the members of the Prop enumeration:
		for ( Prop parName : Prop.values() ) {
			if ( ! props.containsKey(parName.getName()) ) {
				throw new Exception("Required parameter not defined: " +parName.getName());
			}
		}
	}

	/**
	 * @returns the value of a configuration property from the Prop enumeration.
	 */
	public String getProperty(Prop prop) {
		return props.getProperty(prop.getName());
	}
}
