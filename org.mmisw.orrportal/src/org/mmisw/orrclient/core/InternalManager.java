package org.mmisw.orrclient.core;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.client.IOntClient;
import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.client.util.AquaUtil;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.mmisw.orrportal.gwt.server.OrrConfig;

/**
 * Functionality related with internal information (users, groups, permissions, issues)
 *
 * <p>
 * Status: preliminary implementation
 *
 * @author Carlos Rueda
 */
public class InternalManager {

	private static final Log log = LogFactory.getLog(InternalManager.class);


	/**
	 * Prepares the users instantiation ontology.
	 * @param orrClient Used to obtain previous version, if any.
	 * @param loginResult Only the administrator can perform this operation.
	 * @param result to return the result of the operation
	 * @throws Exception
	 */
	static void prepareUsersOntology(
			IOrrClient orrClient,
			IOntClient ontClient,
			LoginResult loginResult,
			InternalOntologyResult result
	) throws Exception {

		log.debug("prepareUsersOntology called.");

		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			result.setError("Only an administrator can perform this operation.");
			return;
		}

		// get the user RDF from the Ont service:
		String rdf = OntServiceUtil.getUsersRdf();

		// get the users RDF URI, assumed to be versioned:
		String generatedUsersUri = _getUsersUri(rdf);
		MmiUri mmiUri = new MmiUri(generatedUsersUri);
		String version = mmiUri.getVersion();

		// unversioned URI for purposes of geting possible prior version, set up auqportal filename,
		// graphId, and report it in the result back to caller:
		final String unversUsersUri = mmiUri.copyWithVersion(null).getOntologyUri();

		// info from previous version if any:
		String ontologyId = null;
		String ontologyUserId = null;
		RegisteredOntologyInfo usersOntology = orrClient.getOntologyInfo(unversUsersUri);
		if ( usersOntology != null ) {
			ontologyId = usersOntology.getOntologyId();
			ontologyUserId = usersOntology.getOntologyUserId();
		}

		// set some associated attributes for the registration:
		Map<String, String> newValues = _getValues(loginResult, "MMI ORR Users", version);
		String fileName = AquaUtil.getAquaportalFilename(unversUsersUri);

		// register:
        String res = uploadOntology(ontClient,
                generatedUsersUri, fileName, rdf,
                loginResult,
                ontologyId, ontologyUserId,
                newValues
        );

		if ( res.startsWith("OK") ) {
			result.setUri(unversUsersUri);
			result.setInfo(res);

			// TODO: indicate graph for the internal information.
			// for now, only associting with graph of the same URI, so we indicate null here::
			String graphId = null;

			// request that the ontology be loaded in the desired graph:
			OntServiceUtil.loadOntologyInGraph(unversUsersUri, graphId);

			log.info("prepareUsersOntology = " +result);
		}
		else {
			result.setError(res);
		}
	}


	/** Obtains the URI of the associated RDF, which it's assumed to be *versioned*; see OntServlet in Ont module. */
	private static String _getUsersUri(String rdf) {
		final Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(rdf), null);
		String ns = model.getNsPrefixURI("");

		String uri = JenaUtil2.removeTrailingFragment(ns);
		return uri;
	}


	private static Map<String, String> _getValues(LoginResult loginResult, String name, String version) {
		Map<String, String> values = new HashMap<String, String>();

		values.put(Omv.name.getURI(), name);
		values.put(Omv.hasCreator.getURI(), loginResult.getUserName());
		values.put(Omv.version.getURI(), version);

		return values;
	}

	private static String _getVersionCurrentTime() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String version = sdf.format(date);
		return version;
	}


	/**
	 * Creates and registers the groups instantiation ontology.
	 * @param orrClient Used to obtain previous version, if any.
	 * @param loginResult Only the administrator can perform this operation.
	 * @param result to return the result of the operation
	 * @throws Exception
	 */
	static void createGroupsOntology(
			IOrrClient orrClient,
			IOntClient ontClient,
			LoginResult loginResult,
			InternalOntologyResult result
	) throws Exception {

		log.debug("createGroupsOntology called.");

		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			result.setError("Only an administrator can perform this operation.");
			return;
		}

		String ontServiceUrl = OrrConfig.instance().ontServiceUrl;
		final String unversGroupsUri = ontServiceUrl+ "/mmiorr-internal/groups";

		RegisteredOntologyInfo groupsOntology = orrClient.getOntologyInfo(unversGroupsUri);
		if ( groupsOntology != null ) {
			result.setError(unversGroupsUri+ ": ontology already registered.");
			return;
		}

		final String ontologyId = null;
		final String ontologyUserId = null;

		String version = _getVersionCurrentTime();
		// set some associated attributes for the registration:
		Map<String, String> newValues = _getValues(loginResult, "MMI ORR Groups",  version);
		String fileName = AquaUtil.getAquaportalFilename(unversGroupsUri);

		// the versioned form for the registration:
		String versionedUsersUri = new MmiUri(unversGroupsUri).copyWithVersion(version).getOntologyUri();

		String rdf = _getGroupsRdf(versionedUsersUri, version);
		// register:
        String res = uploadOntology(ontClient,
                versionedUsersUri, fileName, rdf,
                loginResult,
                ontologyId, ontologyUserId,
                newValues
        );

		if ( res.startsWith("OK") ) {
			result.setUri(unversGroupsUri);
			result.setInfo(res);

			// TODO: indicate graph for the internal information.
			// for now, only associting with graph of the same URI, so we indicate null here:
			String graphId = null;

			// request that the ontology be loaded in the desired graph:
			OntServiceUtil.loadOntologyInGraph(unversGroupsUri, graphId);

			log.info("createGroupsOntology = " +result);
		}
		else {
			result.setError(res);
		}
	}

	/**
	 * Responds the basic RDF for groups.
	 */
	private static String _getGroupsRdf(String versionedUsersUri, String version) {
		final String MMIORR_NS = "http://mmisw.org/ont/mmi/mmiorr/";

		log.debug("_getGroupsRdf called.");

		String ontServiceUrl = OrrConfig.instance().ontServiceUrl;
		final String groups_ns = ontServiceUrl+ "/mmiorr-internal/" +version+ "/groups/";

		final Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("mmiorr", MMIORR_NS);
		model.setNsPrefix("", groups_ns);

		String result = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		return result;
	}


//	/**
//	 * Adds a group instance to the groups instantiation ontology.
//	 * @param server Used to obtain previous version, if any.
//	 * @param loginResult Only the administrator can perform this operation.
//	 * @param result to return the result of the operation
//	 * @throws Exception
//	 */
//	static void createGroup(
//			IOrrClient server,
//			IOntClient ontClient,
//			LoginResult loginResult,
//			String groupId,
//			String groupDescription,
//			InternalOntologyResult result
//	) throws Exception {
//
//		log.debug("createGroup called.");
//
//		if ( loginResult == null || ! loginResult.isAdministrator() ) {
//			result.setError("Only an administrator can perform this operation.");
//			return;
//		}
//
//		final String unversGroupsUri = _config().getOntServiceUrl()+ "/mmiorr-internal/groups";
//
//		RegisteredOntologyInfo groupsOntology = server.getOntologyInfo(unversGroupsUri);
//		if ( groupsOntology == null ) {
//			result.setError(unversGroupsUri+ ": does not exist.");
//			return;
//		}
//
//		String ontologyId = groupsOntology.getOntologyId();
//		String ontologyUserId = groupsOntology.getOntologyUserId();
//
//		String version = _getVersionCurrentTime();
//		// set some associated attributes for the registration:
//		Map<String, String> newValues = _getValues(loginResult, "MMI ORR Groups",  version);
//		String fileName = AquaUtil.getAquaportalFilename(unversGroupsUri);
//
//		// register:
//		String res = uploadOntology(ontClient,
//				generatedUsersUri, fileName, rdf,
//				loginResult,
//				ontologyId, ontologyUserId,
//				newValues
//		);
//
//		if ( res.startsWith("OK") ) {
//			result.setUri(unversGroupsUri);
//			result.setInfo(res);
//
//			// TODO: indicate graph for the internal information.
//			// for now, only associting with graph of the same URI:
//			String graphId = unversGroupsUri;
//
//			// request that the ontology be loaded in the desired graph:
//			OntServiceUtil.loadOntologyInGraph(unversGroupsUri, graphId);
//
//			log.info("prepareUsersOntology = " +result);
//		}
//		else {
//			result.setError(res);
//		}
//	}


    private static String uploadOntology(IOntClient ontClient,
                                         String uri, String fileName, String RDF,
                                         LoginResult loginResult,
                                         String ontologyId, String ontologyUserId,
                                         Map<String, String> values
    ) throws Exception {
        SignInResult signInResult = new SignInResult();
        signInResult.setSessionId(loginResult.getSessionId());
        signInResult.setUserId(loginResult.getUserId());
        signInResult.setUserName(loginResult.getUserName());
        signInResult.setUserRole(loginResult.getUserRole());

        return ontClient.uploadOntology(uri, fileName, RDF,
                signInResult,
                ontologyId, ontologyUserId,
                values);
    }

}
