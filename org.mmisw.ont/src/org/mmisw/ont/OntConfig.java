package org.mmisw.ont;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ont configuration properties.
 * These properties are obtained from the ServletConfig object.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * <p>
 * Thread-safety: This class is not strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers because modification to the 
 * configuration object happens only at initialization of the servlet. 
 * Subsequent accesses are read-only.
 * 
 * @author Carlos Rueda
 */
public class OntConfig {
	
	/**
	 * The Names of the properties that are used by the Ont service.
	 */
	public enum Prop {
		
		AQUAPORTAL_REST_URL            ("aquaportal.rest.url"),
		
		AQUAPORTAL_UPLOADS_DIRECTORY  ("aquaportal.uploads.directory"),
		AQUAPORTAL_DATASOURCE         ("aquaportal.datasource"),
		
		AQUAPORTAL_VOC2RDF_DIR        ("aquaportal.voc2rdf.dir"),
		
		APPSERVER_HOST                ("appserver.host"),
		
		ONT_SERVICE_URL               ("ont.service.url", false),
		
		PORTAL_SERVICE_URL            ("portal.service.url"),
		
		VIRTUOSO_HOST                 ("virtuoso.host", false),
		VIRTUOSO_USERNAME             ("virtuoso.username", false),
		VIRTUOSO_PASSWORD             ("virtuoso.password", false),
		
		JENA_TDB_DIR                  ("jena.tdb.dir", false),
		JENA_TDB_ASSEMBLER            ("jena.tdb.assembler", false),
		
		AGRAPH_VERSION                ("agraph.version", false),
		AGRAPH_HOST                   ("agraph.host", false),
		AGRAPH_PORT                   ("agraph.port", false),
		AGRAPH_TS_DIR                 ("agraph.ts.dir", false),
		AGRAPH_TS_NAME                ("agraph.ts.name", false),
		AGRAPH_USERNAME               ("agraph.username", false),
		AGRAPH_PASSWORD               ("agraph.password", false),
		
		ONT_INTERNAL_DIR              ("ont.internal.dir"),
		
		/** Google analytics UA number (aka web property ID) */
		GA_UA_NUMBER                  ("ga.uanumber", false),
		GA_DOMAIN_NAME                ("ga.domainName", false),
		GA_DIR                        ("ga.dir", false),
		
		;
		
		private String name;
		private boolean required;
		private String value;
		
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
		
		public void setValue(String value) { this.value = value; }
		public String getValue() { return value; }
	}
	
	private final Log log = LogFactory.getLog(OntConfig.class);
	
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
		
		for ( Prop prop : Prop.values() ) {
			String value = sc.getInitParameter(prop.getName());
			if ( value == null || value.trim().length() == 0 ) {
				if ( prop.required ) {
					throw new Exception("Required init parameter not defined: " +prop.getName());
				}
			}
			else {
				prop.setValue(value);
			}
		}
		
		String contextPath = sc.getServletContext().getContextPath();
		Prop.ONT_SERVICE_URL.setValue(Prop.APPSERVER_HOST.getValue() + contextPath);
		
		if ( log != null && log.isDebugEnabled() ) {
			for ( Prop prop : Prop.values() ) {
				String val = prop.getName().toLowerCase().indexOf("password") >= 0 
						? "*****" : prop.getValue();					
				log.debug(prop.getName()+ " = " +val);
			}
		}
	}
}
