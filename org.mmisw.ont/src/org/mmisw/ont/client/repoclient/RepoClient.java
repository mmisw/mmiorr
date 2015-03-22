package org.mmisw.ont.client.repoclient;

import org.mmisw.ont.client.SignInResult;

public interface RepoClient {

    SignInResult getSession(String userName, String userPassword) throws Exception;
}
