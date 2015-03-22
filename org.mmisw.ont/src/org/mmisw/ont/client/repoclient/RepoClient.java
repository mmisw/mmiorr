package org.mmisw.ont.client.repoclient;

import org.mmisw.ont.client.SignInResult;

import java.util.Map;

public interface RepoClient {

    SignInResult getSession(String userName, String userPassword) throws Exception;

    SignInResult createUpdateUserAccount(Map<String, String> values) throws Exception;
}
