package org.mmisw.ont.client;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntVersion;

import com.hp.hpl.jena.ontology.OntModel;
import org.mmisw.ont.client.repoclient.RepoClient;
import org.mmisw.ont.client.repoclient.bioportal.BioportalClient;

/**
 * Implementation of OntClient operations.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
class OntClientImpl implements IOntClient {

	private final Log log = LogFactory.getLog(OntClientImpl.class);

	private final OntReadOnlyConfiguration config;
	private final RepoClient repoClient;

	private static IOntClient _instance;

	/**
	 * Initializes the library. This must be called before any other library
	 * operation.
	 * 
	 * @param config
	 *            The configuration for the library. Note that a copy is made
	 *            internally. Subsequent changes to the given configuration will
	 *            not have any effect. If you need to change any configuration
	 *            parameters, the library will need to be initialized again.
	 *            Pass a null reference to use a default configuration.
	 * 
	 * @return A library interface object.
	 * @throws Exception
	 *             if an error occurs
	 */
	public static IOntClient init(OntClientConfiguration config)
			throws Exception {
		if (config == null) {
			config = new OntClientConfiguration();
		}
		_instance = new OntClientImpl(config);
		return _instance;
	}

	/**
	 * Returns the library interface object created by
	 * {@link #init(OntClientConfiguration)}.
	 * 
	 * @return the library interface object created by
	 *         {@link #init(OntClientConfiguration)}.
	 */
	public static IOntClient getInstance() {
		return _instance;
	}

	private OntClientImpl(OntClientConfiguration config) throws Exception {
		// copy the given configuration:
		this.config = new OntReadOnlyConfiguration(config);

        this.repoClient = new BioportalClient(OntClientUtil.getAquaportalRestUrl(this.config));

		log.info("Ont library version = " + OntVersion.getVersion() + " ("
				+ OntVersion.getBuild() + ")");
		log.info("ontServiceUrl = " + config.getOntServiceUrl());

	}

	/**
	 * Gets a read-only version of the configuration given at creation time. Any
	 * setXXX call on this configuration object will throw
	 * UnsupportedOperationException. If you need to change the configuration
	 * for the OntClient library, you will need to re-create the OntClient
	 * object.
	 * 
	 * @return a read-only version of the configuration given at creation time.
	 */
	public OntClientConfiguration getConfiguration() {
		return config;
	}

	public Map<String, String> getUserInfo(String username) throws Exception {
		return OntClientUtil.getUserInfo(username);
	}

	public String getUsersRdf() throws Exception {
		return OntClientUtil.getUsersRdf();
	}

	public boolean isOntResolvableUri(String uri) {
		return OntClientUtil.isOntResolvableUri(uri);
	}

	public boolean isRegisteredOntologyUri(String uriModel) throws Exception {
		return OntClientUtil.isRegisteredOntologyUri(uriModel);
	}

	public boolean loadOntologyInGraph(String uriModel, String graphId)
			throws Exception {
		return OntClientUtil.loadOntologyInGraph(uriModel, graphId);
	}

	public String resolveOntologyUri(String uriModel, String version,
			String... acceptEntries) throws Exception {
		return OntClientUtil.resolveOntologyUri(uriModel, version,
				acceptEntries);
	}

	public OntModel retrieveModel(String uriModel, String version)
			throws Exception {
		return OntClientUtil.retrieveModel(uriModel, version);
	}

	public String runSparqlQuery(String query, boolean infer, String format,
			String... acceptEntries) throws Exception {
		return OntClientUtil.runSparqlQuery(query, infer, format, acceptEntries);
	}

	public String runSparqlQuery(String endPoint, String query, boolean infer, 
			String format, String... acceptEntries) throws Exception {
		return OntClientUtil.runSparqlQuery(endPoint, query, infer, format,
				acceptEntries);
	}

	public boolean unregisterOntology(String ontUri, String version)
			throws Exception {
		return OntClientUtil.unregisterOntology(ontUri, version);
	}

    public boolean markTestingOntology(String ontUri, String version, boolean markTesting)
            throws Exception {
        return OntClientUtil.markTestingOntology(ontUri, version, markTesting);
    }

	public SignInResult getSession(String userName, String userPassword)
			throws Exception {
		return repoClient.getSession(userName, userPassword);
	}

	public SignInResult createUpdateUserAccount(Map<String, String> values)
			throws Exception {

		UserAccountManager uacu = new UserAccountManager(values);
		return uacu.doIt();
	}

}
