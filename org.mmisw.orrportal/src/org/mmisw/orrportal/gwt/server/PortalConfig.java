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
		
		VERSION                       ("orrportal.app.version", "pendVersion"),
		BUILD                         ("orrportal.app.build", "pendBuild"),

		WORKSPACE_DIR         ("orrportal.workspace.dir", "/Users/Shared/mmiregistry/"),
		
		/** where the pre-loaded files are stored: */
		PRE_UPLOADS_DIR         ("orrportal.pre.uploads.dir", "/Users/Shared/mmiregistry/ontmd/preuploads/"),

		/** where voc2rdf-generated files are stored: */
		VOC2RDF_DIR             ("orrportal.voc2rdf.dir", "/Users/Shared/mmiregistry/ontmd/preuploads/voc2rdf/"),
		
		/** where the previewed files are stored: */
		PREVIEW_DIR             ("orrportal.preview.dir", "/Users/Shared/mmiregistry/ontmd/previews/"),
		
		/** where the resource files are stored: */
		RESOURCES_DIR           ("orrportal.resource.dir", "/Users/Shared/mmiregistry/ontmd/resources/"),
		
		/** URI of the OWL class: resource type */
		RESOURCE_TYPE_CLASS           ("orrportal.resourcetype.class", "http://mmisw.org/ont/mmi/resourcetype/ResourceType"),
		
		/** URI of the OWL class: authority  */
		AUTHORITY_CLASS               ("orrportal.authority.class", "http://mmisw.org/ont/mmi/authority/Authority"),
		
		
		APPSERVER_HOST                ("appserver.host", "http://localhost:8080"),
		
		/** URL of "Ont" URI resolution service */
		ONT_SERVICE_URL                ("ont.service.url", "http://localhost:8080/ont"),
		
		/** URL of Ontology Browser service.
		 * Note: it's required;  use the "-" value in build.properties to disable this functionality.
		 */
		ONTBROWSER_SERVICE_URL         ("ontbrowser.service.url", "http://localhost:8080/browser"),
		
		
		// Note: when I test the password reset functionality from within eclipse, I put the correct 
		// mail values here. This is because I haven't found an easy way to get these from 
		// build.properties (perhaps GWT 2.x has better support for this situation). 
		MAIL_USER                     ("mail.usr", "-"),
		MAIL_PASSWORD                 ("mail.pw",  "-"),
		
		
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
					if ( Prop.MAIL_PASSWORD == prop ) {
						// do not show passwords:
						log.debug(prop.getName()+ " = " +"XXXXX");
					}
					else {
						log.debug(prop.getName()+ " = " +value);						
					}
				}
			}
			
			// actually, if the ontbrowser property value does not start with "http", then
			// it is ignored.
			String ontbrowserUrl = Prop.ONTBROWSER_SERVICE_URL.getValue();
			if ( ! ontbrowserUrl.toLowerCase().startsWith("http") ) {
				Prop.ONTBROWSER_SERVICE_URL.setValue(null);
			}

		}
	}

}
