package org.mmisw.orrclient.core.test.reg;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base class for Vine tests.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseTestCase extends TestCase {
	
	// by default unregister the registrations
	protected static boolean UNREGISTER = true;
	
	static {
		// crude test to see if I'm running from within eclipse to enable all logging
		boolean ECLIPSE = System.getProperty("java.class.path").contains("/configuration/org.eclipse.osgi");
		if ( ECLIPSE ) {
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.mmisw");
			logger.setLevel(org.apache.log4j.Level.ALL);
			
			// but keep the registrations when testing under my IDE
			UNREGISTER = false;
		}
		
		BaseTestCase.class.getClassLoader().setDefaultAssertionStatus(true);
	}
	
	protected static final Log log = LogFactory.getLog(BaseTestCase.class);

}
