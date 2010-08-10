package org.mmisw.watchdog.onts.sweet.test;

import junit.framework.TestCase;

import org.junit.Before;
import org.mmisw.watchdog.onts.sweet.Sweet;
import org.mmisw.watchdog.util.WdConstants;

/**
 * Common stuff for the tests.
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public abstract class BaseTestCase extends TestCase {

	protected static String ontServiceUrl = WdConstants.DEFAULT_ONT_SERVICE_URL;
	
	protected static String diregServiceUrl = WdConstants.DEFAULT_DIREG_SERVICE_URL;
	
	protected static String sweetAllUri = Sweet.DEFAULT_SWEET_ALL_URI;
	

	@Before
	protected void setUp() {
		ontServiceUrl = System.getProperty("ontServiceUrl", "http://localhost:8080/ont");
		diregServiceUrl = System.getProperty("diregServiceUrl", "http://localhost:8080/orr/direg");
	}
	
	/** gets a system property */
	static String getRequiredSystemProperty(String key) {
		String val = System.getProperty(key);
		if ( val == null || val.trim().length() == 0 ) {
			throw new IllegalStateException("Required system property '" +key+ "' not specified");
		}
		return val;
	}

	void _log(String msg) {
		String prefix = "[" +getClass().getSimpleName()+ "] ";
		System.out.println(prefix +msg.replaceAll("\n", "\n" +prefix));
	}

}
