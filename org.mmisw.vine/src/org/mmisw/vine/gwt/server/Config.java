package org.mmisw.vine.gwt.server;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;

/**
 * Provides the configuration parameter values.
 * 
 * @author Carlos Rueda
 */
public class Config {
	
	/**
	 * The configuration properties used by the service.
	 */
	public enum Prop {
		
		VERSION                       ("vine.app.version", "PEND_VERSION"),
		BUILD                         ("vine.app.build", "PEND_BUILD"),

		/** Base URL of bioportal REST service */
		BIOPORTAL_REST_URL            ("bioportal.rest.url", "http://localhost:8080/bioportal/rest"),
		
		/** URL of "Ont" URI resolution service */
		ONT_SERVICE_URL                ("ont.service.url", "http://localhost:8080/ont"),
		
		;
		
		private final String name;
		private String value;

		Prop(String name) { 
			this(name, null); 
		};
		
		Prop(String name, String value) { 
			this.name = name; 
			this.value = value;
		};
		
		public String getName() { return name; }

		public String getValue() { return value; }
		
		private void setValue(String value) { this.value = value; }
	}

	
	private static final Config instance = new Config();
	
	/** 
	 * Gets the unique instance of this class.
	 * Call {@link #init(ServletConfig)} to have it properly initialized.
	 */
	public static Config getInstance() {
		return instance;
	}
	
	
	private volatile boolean initCalled = false;
	
	private Config() {};

	
	/**
	 * Initializes this object loading the initialization parameters.
	 * Does nothing if this method has already been called, even if the previous
	 * call threw any exception.
	 * 
	 * @throws Exception If any required parameter is undefined.
	 */
	public void init(ServletConfig sc, Log log) throws Exception {
		if ( ! initCalled ) {
			initCalled = true;
			// Read in the required properties:
			for ( Prop prop : Prop.values() ) {
				String value = sc.getInitParameter(prop.getName());
				if ( value != null && value.trim().length() > 0 ) {
					prop.setValue(value);
				}
				else if ( prop.getValue() == null ) {
					throw new Exception("Required init parameter not defined: " +prop.getName());
				}
			}
			
			if ( log != null && log.isDebugEnabled() ) {
				for ( Prop prop : Prop.values() ) {
					log.debug(prop.getName()+ " = " +prop.getValue());
				}
			}
		}
	}

}
