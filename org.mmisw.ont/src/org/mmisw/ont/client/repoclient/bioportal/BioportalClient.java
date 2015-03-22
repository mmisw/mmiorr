package org.mmisw.ont.client.repoclient.bioportal;

import org.mmisw.ont.client.SignInResult;
import org.mmisw.ont.client.repoclient.RepoClient;

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
}
