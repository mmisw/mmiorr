package org.mmisw.orrclient.core;



/**
 * Provides the configuration parameter values.
 * 
 * @author Carlos Rueda
 */
@Deprecated
public class OrrClientConfig {
	
	/**
	 * The configuration properties used by the service.
	 */
	public enum Prop {
		
		/** where the previewed files are stored: */
		ONTMD_PREVIEW_DIR             ("ontmd.preview.dir", "/Users/Shared/mmiregistry/ontmd/previews/"),
		
		ONTMD_VOC2RDF_DIR             ("ontmd.voc2rdf.dir", "/Users/Shared/mmiregistry/ontmd/preuploads/voc2rdf/"),
		
		BIOPORTAL_REST_URL            ("bioportal.rest.url", "http://localhost:8080/bioportal/rest"),
		
		ONT_SERVICE_URL               ("ont.service.url", "http://localhost:8080/ont"),
		
		MAIL_USER                     ("mail.usr", null),
		MAIL_PASSWORD                 ("mail.pw", null),
		;
		
		private final String name;
		private String value;

		Prop(String name, String value) { 
			this.name = name;
			this.value = value;
		};
		
		public String getName() { return name; }

		public String getValue() { return value; }
		
		public void setValue(String value) { this.value = value; }
	}
	
	private static final OrrClientConfig instance = new OrrClientConfig();
	
	/** 
	 * Gets the unique instance of this class.
	 */
	public static OrrClientConfig getInstance() {
		return instance;
	}
	
	private OrrClientConfig() {}
}
