package org.mmisw.orrportal.gwt.server;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;

/**
 * Provides the configuration parameter values.
 * 
 * @author Carlos Rueda
 */
public class PortalConfig {
	
	/**
	 * The configuration properties used by the service.
	 */
	public enum Prop {
		
		/** Google analytics UA number (aka web property ID) */
		GA_UA_NUMBER                  ("ga.uanumber", null, false),

		;
		
		private final String name;
		private final String defValue;
		private boolean required;
		private String value;

		Prop(String name, String defValue) {
			this(name, defValue, true); 
		}
		
		Prop(String name, String defValue, boolean required) {
			this.name = name; 
			this.defValue = defValue; 
			this.required = required;
		}
		
		public String getName() { return name; }

		public String getValue() { return value; }
		
		private void setValue(String value) { this.value = value; }
	}

	
	private static final PortalConfig instance = new PortalConfig();
	
	/** 
	 * Gets the unique instance of this class.
	 * Call {@link #init(ServletConfig)} to have it properly initialized.
	 */
	public static PortalConfig getInstance() {
		return instance;
	}
	
	
	private volatile boolean initCalled = false;
	
	private PortalConfig() {};

	
	/**
	 * Initializes this object loading the initialization parameters.
	 * Does nothing if this method has already been called, even if the previous
	 * call threw any exception.
	 * 
	 * @param useDefault true to allow the usage of pre-defined values in the code when the
	 *        servlet configuration doesn't provide them. If false, any undefined but required parameter,
	 *        will throw an exception.
	 * 
	 * @throws Exception If any required parameter is undefined.
	 */
	public void init(ServletConfig sc, Log log, boolean useDefault) throws Exception {
		if ( ! initCalled ) {
			initCalled = true;
			// Read in the required properties:
			for ( Prop prop : Prop.values() ) {
				String value = sc.getInitParameter(prop.getName());
				if ( value == null || value.trim().length() == 0 ) {
					if ( useDefault && prop.defValue != null ) {
						value = prop.defValue;
						if ( log != null && log.isDebugEnabled() ) {
							log.debug("--Using internal default value for " +prop.getName());
						}
					}
					else if ( prop.required ) {
						throw new Exception("Required init parameter not defined: " +prop.getName());
					}
				}
				prop.setValue(value);
				if ( log != null && log.isDebugEnabled() ) {
					log.debug(prop.getName()+ " = " +value);
				}
			}
		}
	}

}
