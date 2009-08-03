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
		
		VERSION                       ("ont.app.version"),
		BUILD                         ("ont.app.build"),
		AQUAPORTAL_UPLOADS_DIRECTORY  ("aquaportal.uploads.directory"),
		AQUAPORTAL_DATASOURCE         ("aquaportal.datasource"),
		
		AQUAPORTAL_VOC2RDF_DIR        ("aquaportal.voc2rdf.dir"),
		
		APPSERVER_HOST                ("appserver.host"),
		
		ONT_SERVICE_URL               ("ont.service.url", false),
		
		PORTAL_SERVICE_URL            ("portal.service.url"),
		
		;
		
		private String name;
		private boolean required;
		
		Prop(String name) { 
			this(name, true);
		}
		
		Prop(String name, boolean required) { 
			this.name = name; 
			this.required = required;
		}
		
		public String getName() { return name; }
		
		public boolean isRequired() {
			return required;
		}
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
		for ( Prop prop : Prop.values() ) {
			if ( prop.isRequired() && ! props.containsKey(prop.getName()) ) {
				throw new Exception("Required parameter not defined: " +prop.getName());
			}
		}
		
		String contextPath = sc.getServletContext().getContextPath();
		props.setProperty(Prop.ONT_SERVICE_URL.getName(), 
				props.getProperty(Prop.APPSERVER_HOST.getName()) + contextPath
		);
		log.debug(Prop.ONT_SERVICE_URL.getName()+ " = " +props.getProperty(Prop.ONT_SERVICE_URL.getName()));
	}

	/**
	 * @returns the value of a configuration property from the Prop enumeration.
	 */
	public String getProperty(Prop prop) {
		return props.getProperty(prop.getName());
	}
}
