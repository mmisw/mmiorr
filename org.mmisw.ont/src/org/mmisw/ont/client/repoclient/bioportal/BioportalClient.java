package org.mmisw.ont.client.repoclient.bioportal;

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
}
