package org.mmisw.orrclient.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides version information about the ORR Client library.
 * 
 * If the associated resource cannot be read or the corresponding properties are
 * not defined, warnings are logged out ({@link org.apache.commons.logging.Log#warn(Object)}) 
 * and values of the form "undefXXX" are reported.
 * 
 * @author Carlos Rueda
 */
final class OrrClientVersion {
	
	// This info is retrieved from the version.properties resource in this package.
	// This resource is prepared by build.xml from a master version.properties.
	
	/** 
	 * Returns the version.
	 */
	static String getVersion() {
		return version;
	}
	
	/**
	 * Returns the build.
	 */
	static String getBuild() {
		return build;
	}
	
		
	private static String version;
	private static String build;
	
	
	private static final String VERSION_PROP = "orrclient.version";
	private static final String BUILD_PROP = "orrclient.build";
	private static final String VERSION_UNDEF_VALUE = "undefVersion";
	private static final String BUILD_UNDEF_VALUE = "undefBuild";
	
	
	
	static {
		Log log = LogFactory.getLog(OrrClientVersion.class);
		InputStream is = OrrClientVersion.class.getResourceAsStream("version.properties");
		if ( is == null ) {
			log.warn("Cannot get resource: version.properties");
		}
		else {
			Properties verProps = new Properties();
			try {
				verProps.load(is);
				version = verProps.getProperty(VERSION_PROP, VERSION_UNDEF_VALUE);
				build = verProps.getProperty(BUILD_PROP, BUILD_UNDEF_VALUE);
			}
			catch (IOException e) {
				log.warn("Cannot load version.properties", e);
			}
			IOUtils.closeQuietly(is);
		}
	}
	
	private OrrClientVersion() {}
}
