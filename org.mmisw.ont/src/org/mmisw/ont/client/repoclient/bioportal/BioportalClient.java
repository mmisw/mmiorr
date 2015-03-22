package org.mmisw.ont.client.repoclient.bioportal;

import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.client.repoclient.RepoClient;

import java.util.Map;

public class BioportalClient implements RepoClient {

    private final String aquaportalRestUrl;
    private final UserAuthenticator userAuthenticator;

    public BioportalClient(String aquaportalRestUrl) {
        this.aquaportalRestUrl = aquaportalRestUrl;
        this.userAuthenticator = new UserAuthenticator(this.aquaportalRestUrl);
    }

    @Override
    public SignInResult getSession(String userName, String userPassword) throws Exception {
        return userAuthenticator.getSession(userName, userPassword);
    }

    @Override
    public SignInResult createUpdateUserAccount(Map<String, String> values) throws Exception {
        UserAccountManager uacu = new UserAccountManager(values);
        return uacu.doIt(aquaportalRestUrl);

    }

    @Override
    public String uploadOntology(String uri, String fileName, String RDF,
                          SignInResult signInResult,
                          String ontologyId, String ontologyUserId,
                          Map<String, String> values
    ) throws Exception {

        OntUploader ontUploader = new OntUploader(uri, fileName, RDF,
                signInResult,
                ontologyId, ontologyUserId,
                values);

        return ontUploader.create(aquaportalRestUrl);
    }

    @Override
    public String unregisterOntology(OntologyInfo ontology) throws Exception {
        String sessionId = "9c188a9b8de0fe0c21b9322b72255fb939a68bb2";
        OntologyDeleter del = new OntologyDeleter(sessionId, ontology.getId());
        return del.execute(aquaportalRestUrl);
    }

    @Override
    public String toString() {
        return "BioportalClient{" + aquaportalRestUrl + "}";
    }
}
