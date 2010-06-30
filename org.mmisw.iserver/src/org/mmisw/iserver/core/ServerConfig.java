package org.mmisw.iserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides the configuration parameter values.
 * 
 * @author Carlos Rueda
 */
public class ServerConfig {
	
	/**
	 * The configuration properties used by the service.
	 */
	public enum Prop {
		
		VERSION                       ("iserver.app.version",  "versionPend"),
		BUILD                         ("iserver.app.build",    "buildPend"),
		
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
	
	private static final ServerConfig instance = new ServerConfig();
	
	/** 
	 * Gets the unique instance of this class.
	 */
	public static ServerConfig getInstance() {
		return instance;
	}
	
	
	private final Log log = LogFactory.getLog(ServerConfig.class);
	
	private ServerConfig() {
		InputStream is = getClass().getResourceAsStream("version.properties");
		if ( is == null ) {
			log.warn("Could not get stream version.properties");
			return;
		}
		Properties verProps = new Properties();
		try {
			verProps.load(is);
		}
		catch (IOException e) {
			log.warn("Could not load version.properties", e);
		}
		
		Prop[] props = { Prop.VERSION, Prop.BUILD };
		for ( Prop prop : props ) {
			String val = verProps.getProperty(prop.getName());
			if ( val == null ) {
				log.warn("version.properties does not define " +prop.getName());
			}
			else {
				prop.setValue(val);
				log.debug(prop.getName()+ " set to " +prop.getValue());
			}
		}
		
		IOUtils.closeQuietly(is);
	}

}
