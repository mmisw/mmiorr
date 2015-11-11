package org.mmisw.orrclient.core.test.reg;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.OrrClientFactory;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.HostingType;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.OtherDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;


/**
 * Test of registration is re-hosted mode.
 *
 * Requires:
 * <ul>
 * <li> Ont service running in the indicated URL (hardcoded)
 * <li> system property "orr.user" with username and passowrd (separated by a comma), eg.,
 *          "foouser,foopw"
 * </ul>
 *
 * @author Carlos Rueda
 */
public class ReHostedRegTest extends BaseTestCase {

	private static final String ONT_SERVICE_URL = "http://localhost:8080/ont";
	private static final String fileType = "RDF/XML";
	private static final String PREVIEW_DIRECTORY  = "/tmp/orrclient/";
	private static final String USER = System.getProperty("orr.user");

	private static final String authorityClassUri = "http://mmisw.org/ont/mmi/authority/Authority";
	private static final String resourceTypeClassUri = "http://mmisw.org/ont/mmi/resourcetype/ResourceType";

	private static String username;
	private static String password;



	public void verifyRequiredParams() throws Exception {
		if ( USER == null ) {
			throw new Exception("system property 'orr.user' required");
		}
		String[] toks = USER.split(",");
		username = toks[0];
		password = toks[1];
	}

	public void testRegister_01() throws Exception {
		verifyRequiredParams();
		_testRegister("resource/onts/P021_current-small-w-xmlbase.rdf", "P021-");
	}

	/**
	 * Tests the registration of the given file.
	 *
	 * @param filename
	 * @param prefix    used for the ontology metadata attributes
	 * @throws Exception
	 */
	private void _testRegister(String filename, String prefix) throws Exception {

		log.info("=============== INIT OrrClient =================");
		IOrrClient orrClient = OrrClientFactory.init();
		orrClient.getMetadataBaseInfo(false, resourceTypeClassUri, authorityClassUri);


		log.info("=============== GET getTempOntologyInfo =================");

		TempOntologyInfo tempOntologyInfo = orrClient.getTempOntologyInfo(fileType, filename, false, false);
		assertNull("No error in getTempOntologyInfo", tempOntologyInfo.getError());
		String namespace = tempOntologyInfo.getUri();
		log.info("tempOntologyInfo.getUri() = " +namespace);
		assertNotNull("namespace must be present", namespace);

		log.info("=============== authenticate user =================");
		LoginResult loginResult = _authenticateUser(orrClient);

		log.info("=============== createOntology =================");
		CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
		OtherDataCreationInfo dataCreationInfo = new OtherDataCreationInfo();
		dataCreationInfo.setTempOntologyInfo(tempOntologyInfo);
//		createOntologyInfo.setBaseOntologyInfo(tempOntologyInfo);
		createOntologyInfo.setUri(namespace);
		createOntologyInfo.setDataCreationInfo(dataCreationInfo);
		createOntologyInfo.setHostingType(HostingType.RE_HOSTED);
		Map<String, String> metadataValues = _getMetadataValues(prefix);
		createOntologyInfo.setMetadataValues(metadataValues);

		CreateOntologyResult createOntologyResult = orrClient.createOntology(createOntologyInfo);
		assertNull("No error in createOntologyResult", createOntologyResult.getError());
		log.debug("createOntologyResult = " +createOntologyResult);


		log.info("=============== registerOntology =================");
		RegisterOntologyResult registerOntologyResult = orrClient.registerOntology(createOntologyResult, loginResult);
		assertNull("No error in registerOntology", registerOntologyResult.getError());
		log.debug("createOntologyResult = " +createOntologyResult.getUri());

		log.info("=============== getOntologyInfo =================");
		RegisteredOntologyInfo registeredOntologyInfo = orrClient.getOntologyInfo(namespace);
		assertNull("No error in registeredOntologyInfo", registeredOntologyInfo.getError());


		orrClient.getOntologyContents(registeredOntologyInfo, null);

		if ( UNREGISTER ) {
			_unregister(orrClient, loginResult, registeredOntologyInfo);
		}
	}

	@BeforeClass
	private LoginResult _authenticateUser(IOrrClient orrClient) {
		LoginResult loginResult = orrClient.authenticateUser(username, password);
		assertNull("No error in login", loginResult.getError());

		return loginResult;
	}
	private Map<String, String> _getMetadataValues(String prefix) {
		Map<String, String> metadataValues = new HashMap<String, String>();
		metadataValues.put(OmvMmi.hasResourceType.getURI(), prefix+ "test-resource-type");
		metadataValues.put(Omv.name.getURI(), prefix+ " title");
		metadataValues.put(Omv.acronym.getURI(), prefix+ "acronym");
		metadataValues.put(OmvMmi.hasContentCreator.getURI(), prefix+ "content creator");
		metadataValues.put(Omv.hasCreator.getURI(), prefix+ "ontology creator");
		metadataValues.put(Omv.description.getURI(), prefix+ "some description");
		return metadataValues;
	}

	private void _unregister(IOrrClient orrClient, LoginResult loginResult, RegisteredOntologyInfo registeredOntologyInfo) {
		log.info("=============== unregisterOntology =================");
		UnregisterOntologyResult unregisterOntologyResult = orrClient.unregisterOntology(loginResult, registeredOntologyInfo);
		assertNull("No error in unregisterOntologyResult", unregisterOntologyResult.getError());
		log.debug("unregisterOntologyResult = " +unregisterOntologyResult);
	}

}

