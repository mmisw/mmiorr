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
		
		VERSION                       ("vine.app.version"),
		BUILD                         ("vine.app.build"),
		;
		
		private final String name;
		private String value;

		Prop(String name) { this.name = name; };
		
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
				if ( value == null || value.trim().length() == 0 ) {
					throw new Exception("Required init parameter not defined: " +prop.getName());
				}
				prop.setValue(value);
				if ( log != null && log.isDebugEnabled() ) {
					log.debug(prop.getName()+ " = " +value);
				}
			}
		}
	}

}
