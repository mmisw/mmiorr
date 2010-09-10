package org.mmisw.orrclient.core.voc2skos.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.mmisw.orrclient.core.voc2skos.Voc2Skos;

import com.hp.hpl.jena.rdf.model.Model;

import edu.drexel.util.rdf.JenaUtil;

/**
 * Tests for the Voc2Rdf functionality.
 * 
 * <p>
 * The structure under directory {project-root}/resource/voc2skos/ (on which
 * these tests are based) is:
 * <ul>
 * <li>input/ -- contains input files in CSV for the tests
 * <li>expected/ -- contains corresponding files in RDF (these were externally
 * inspected to be correct outputs for the inputs)
 * <li>output/ -- working directory for output generation during the tests (this
 * directory is no checked in the repository).
 * </ul>
 * The general scheme is that a test takes an input, does conversion (writing
 * result to output/), loads expected model, and compares generated model with
 * the expected one.
 * <p>
 * 
 * @author Carlos Rueda
 */
public class Voc2SkosTest extends TestCase {

	static {
		// crude test to see if I'm running from within eclipse to enable all logging
		boolean ECLIPSE = System.getProperty("java.class.path").contains("/configuration/org.eclipse.osgi");
		if ( ECLIPSE ) {
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.mmisw");
			logger.setLevel(org.apache.log4j.Level.ALL);
		}
		Voc2SkosTest.class.getClassLoader().setDefaultAssertionStatus(true);
	}
	
	/** base dir for the resources needed/generated */
	private static final String BASE_DIR = "resource/voc2skos/";

	/** <code>_assertGoodConversion("voc2skos-0");</code> */
	public void test0() throws IOException {
		_assertGoodConversion("voc2skos-0");
	}

	/** <code>_assertGoodConversion("voc2skos-1");</code> */
	public void test1() throws IOException {
		_assertGoodConversion("voc2skos-1");
	}
	
	/**
	 * <ol>
	 * <li> Does a conversion from an input file generating model M 
	 * <li> Writes M out to a file 
	 * <li> Loads expected model E from a file that was inspected previously 
	 * <li> asserts E <i>isIsomorphicWith</i> M
	 * </ol>
	 * 
	 * @param baseFilename Used to compose the names of the involved files.
	 */
	private void _assertGoodConversion(String baseFilename) throws IOException {

		File file = new File(BASE_DIR + "/input/" +baseFilename+ ".csv");
		Model model = Voc2Skos.loadModel(file);

		_saveModel(model, baseFilename);

		// now compare output with expected result
		File fileExpected = new File(BASE_DIR + "/expected/" +baseFilename+ ".rdf");
		Model modelExpected = JenaUtil.loalRDFModel(fileExpected.toURI()
				.toString());

		assertTrue(modelExpected.isIsomorphicWith(model));
	}

	/**
	 * Saves model to File(BASE_DIR + "/output/" +baseFilename+ ".rdf")
	 */
	private static void _saveModel(Model model, String baseFilename) throws IOException {
		File fileOut = new File(BASE_DIR + "/output/" +baseFilename+ ".rdf");
		String base = model.getNsPrefixURI("");
		Voc2Skos.saveModelXML(model, fileOut, base);
	}

}
