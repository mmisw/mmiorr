package org.mmisw.watchdog.onts.sweet.test;

import org.junit.Before;
import org.junit.Test;
import org.mmisw.watchdog.util.jena.AdHocUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Tests associated with the handling of SWEET ontologies.
 * 
 * <p>
 * The structure under directory {project-root}/resource/sweet/ (on which these
 * tests are based) is:
 * <ul>
 * <li>download/ -- downloaded ontologies from the SWEET site
 * </ul>
 * The general scheme is that a test downloads an ontology from SWEET site,
 * registers it at ORR, retrieves it from ORR, and compares the two
 * corresponding models to verify they are isomorphic to each other.
 * <p>
 * 
 * @author Carlos Rueda
 */
public class SweetTest extends BaseTestCase {

	/** base url for the SWEET ontologies */
	private static final String BASE_URL = "http://sweet.jpl.nasa.gov/2.0/";

	protected static String username;
	protected static String password;

	@Before
	protected void setUp() {
		username = getRequiredSystemProperty("username");
		password = getRequiredSystemProperty("password");
		super.setUp();
	}

	@Test
	public void test_top() throws Exception {
		_assertGoodRegistration("top.owl");
	}

	@Test
	public void test_timeGeologic() throws Exception {
		_assertGoodRegistration("timeGeologic.owl");
	}

	/**
	 * <ol>
	 * <li> downloads an ontology S from the SWEET site <li> registers S at ORR
	 * <li> retrieves it from ORR as S' <li> asserts S' <i>isIsomorphicWith</i>
	 * S
	 * </ol>
	 * 
	 * @param ontName
	 *            Used to compose the names of the involved files.
	 * @throws Exception
	 */
	private void _assertGoodRegistration(String ontName) throws Exception {
		_log("\n%%%%%%%%%%%%% Testing SWEET ontology: " + ontName
				+ " %%%%%%%%%%%%%%%%");
		_compare(ontName);
	}

	/**
	 * Verifies that the registered model has been correctly captured. This is
	 * verified by determining that all statements in the original SWEET
	 * ontology are the same as the ones in the registered ontology, modulo
	 * statements about the ontology resource itself. This is because ORR may
	 * have added some statements to such resource--we actually want to compare
	 * the contents of the ontology, not its metadata.
	 */
	private void _compare(String ontName) {

		_log("Loading models ");
		String ontologyUri = BASE_URL + ontName;

		// load ontology from SWEET site:
		Model sweetModel = AdHocUtil.loadModel(ontologyUri, false);

		// download registered model at ORR:
		String orrOntUrl = ontServiceUrl + "?form=rdf&uri=" + ontologyUri;
		Model orrModel = AdHocUtil.loadModel(orrOntUrl, false);

		_log("Models loaded (sweetModel size = " + sweetModel.size()
				+ ", orrModel size = " + orrModel.size() + ")");

		// check that the models are isomorphic but ignoring all statements
		// about the ontology resource:
		_log("Removing statements about ontology resource from both models ");
		Resource ontologyResource = ResourceFactory.createResource(ontologyUri);
		sweetModel.removeAll(ontologyResource, null, null);
		orrModel.removeAll(ontologyResource, null, null);

		_log("Comparing models wih isIsomorphicWith (sweetModel size = "
				+ sweetModel.size() + ", orrModel size = " + orrModel.size()
				+ ")");
		assertTrue(orrModel.isIsomorphicWith(sweetModel));

		_log("OK");
	}

}
