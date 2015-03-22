package org.mmisw.ont.client.repoclient;

import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.client.SignInResult;

import java.util.Map;

public interface RepoClient {

    SignInResult getSession(String userName, String userPassword) throws Exception;

    SignInResult createUpdateUserAccount(Map<String, String> values) throws Exception;

    String uploadOntology(String uri, String fileName, String RDF,
                SignInResult signInResult,
                String ontologyId, String ontologyUserId,
                Map<String, String> values
    ) throws Exception;

    String unregisterOntology(OntologyInfo ontology) throws Exception;
}
