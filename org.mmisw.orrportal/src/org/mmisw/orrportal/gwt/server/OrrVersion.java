package org.mmisw.orrportal.gwt.server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Provides version information about the ORR Portal.
 * Version and build info is retrieved from the version.properties resource in this package.
 * This resource is prepared by build.xml from a master version.properties.
 */
public final class OrrVersion {
	/**
	 * Returns the version.
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Returns the build.
	 */
	public static String getBuild() {
		return build;
	}

	/** retrieved from properties file */
	private static String version;

	/** retrieved from properties file */
	private static String build;


	static {
		Log log = LogFactory.getLog(OrrVersion.class);
		InputStream is = OrrVersion.class.getResourceAsStream("version.properties");
		if ( is == null ) {
			log.warn("Cannot get resource: version.properties");
		}
		else {
			Properties verProps = new Properties();
			try {
				verProps.load(is);
				//log.debug("verProps=" + verProps);
				version = verProps.getProperty("orrportal.version", "?");
				build = verProps.getProperty("orrportal.build", "?");
			}
			catch (IOException e) {
				log.warn("Cannot load version.properties", e);
			}
			finally {
				IOUtils.closeQuietly(is);
			}
		}
	}

	private OrrVersion() {}
}
