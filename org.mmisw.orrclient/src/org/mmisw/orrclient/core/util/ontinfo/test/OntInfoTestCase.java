package org.mmisw.orrclient.core.util.ontinfo.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.core.util.Util2;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Base class.
 * 
 * @author Carlos Rueda
 */
public abstract class OntInfoTestCase extends TestCase {
	
	static {
		// crude test to see if I'm running from within eclipse to enable all logging
		boolean ECLIPSE = System.getProperty("java.class.path").contains("/configuration/org.eclipse.osgi");
		if ( ECLIPSE ) {
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.mmisw");
			logger.setLevel(org.apache.log4j.Level.ALL);
		}
	}
	
	protected static final Log log = LogFactory.getLog(OntInfoTestCase.class);

	protected OntModel _load(File file) throws Exception {
		return Util2.loadModelWithCheckingUtf8(file, Util2.JENA_DEFAULT_LANG);
	}
}
