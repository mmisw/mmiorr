package org.mmisw.ontmd.gwt.server;

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
		
		VERSION                       ("ontmd.app.version", "pendVersion"),
		BUILD                         ("ontmd.app.build", "pendBuild"),

		/** where the pre-loaded files are stored: */
		ONTMD_PRE_UPLOADS_DIR         ("ontmd.pre.uploads.dir", "/Users/Shared/mmiregistry/ontmd/preuploads/"),
		
		/** where voc2rdf-generated files are stored: */
		ONTMD_VOC2RDF_DIR             ("ontmd.voc2rdf.dir", "/Users/Shared/mmiregistry/ontmd/preuploads/voc2rdf/"),
		
		/** where the previewed files are stored: */
		ONTMD_PREVIEW_DIR             ("ontmd.preview.dir", "/Users/Shared/mmiregistry/ontmd/previews/"),
		
		/** where the resource files are stored: */
		ONTMD_RESOURCES_DIR           ("ontmd.resource.dir", "/Users/Shared/mmiregistry/ontmd/resources/"),
		
		/** URI of the OWL class: resource type */
		RESOURCE_TYPE_CLASS           ("ontmd.resourcetype.class", "http://mmisw.org/ont/mmi/resourcetype/ResourceType"),
		
		/** URI of the OWL class: authority  */
		AUTHORITY_CLASS               ("ontmd.authority.class", "http://mmisw.org/ont/mmi/authority/Authority"),
		;
		
		private final String name;
		private final String defValue;
		private String value;

		Prop(String name, String defValue) {
			this.name = name; 
			this.defValue = defValue; 
		}
		
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
	 * @param useDefault true to allow the usage of pre-defined values in the code when the
	 *        servlet configuration doesn't provide them. If false, any undefined parameter
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
					if ( useDefault ) {
						value = prop.defValue;
						if ( log != null && log.isDebugEnabled() ) {
							log.debug("Using internal default value for " +prop.getName());
						}
					}
					else {
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

	
	// TODO Remove the following once the above mechanism is completed 
//	
//	public static final String ONTMD_WORKSPACE_DIR = "/Users/Shared/mmiregistry/ontmd/";
//	
//	// where the pre-loaded files are stored:
//	public static final String ONTMD_PRE_UPLOADS_DIR = ONTMD_WORKSPACE_DIR+ "preuploads/";
//	
//	// where voc2rdf-generated files are stored:
//	public static final String ONTMD_VOC2RDF_DIR = ONTMD_PRE_UPLOADS_DIR+ "voc2rdf/";
//	
//	// where the previewed files are stored:
//	public static final String ONTMD_PREVIEW_DIR = ONTMD_WORKSPACE_DIR+ "previews/";
//
//
//	// where the resource files are stored:
//	public static final String ONTMD_RESOURCES_DIR = ONTMD_WORKSPACE_DIR+ "resources/";
//
//	
//	/** URI of the OWL class: resource type */
//	public static final String RESOURCE_TYPE_CLASS = "http://mmisw.org/ont/mmi/resourcetype/ResourceType";
//
//	/** URI of the OWL class: authority  */
//	public static final String AUTHORITY_CLASS = "http://mmisw.org/ont/mmi/authority/Authority";

}
