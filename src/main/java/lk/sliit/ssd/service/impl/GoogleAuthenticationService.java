/**
 * 
 */
package lk.sliit.ssd.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.util.store.DataStoreFactory;

import lk.sliit.ssd.service.AuthenticationService;
import lk.sliit.ssd.util.CommonUtil;

/**
 * @author vimukthi_r
 *
 */
@Service
public class GoogleAuthenticationService implements AuthenticationService {

	@Autowired
	private GoogleAuthorizationCodeFlow flow;
	@Autowired
	private DataStoreFactory dataStoreFactory;

	private Logger logger = LoggerFactory.getLogger(GoogleAuthenticationService.class);

	@Override
	public boolean isAuthenticatedUser(String userID) throws IOException {
		Credential credential = getCredentials(userID);
		if (credential != null) {
			boolean isTokenValid = credential.refreshToken();

			logger.debug("UserID: {}, isTokenValid: {}", userID, isTokenValid);
			return isTokenValid;
		}
		return false;
	}

	@Override
	public String authenticateUser() {
		logger.info("Authenticating an user...");

		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectUrl = url.setRedirectUri(CommonUtil.GOOGLE_CALLBACK_URL).setAccessType("offline").build();

		logger.info("Redirecting to URL: {}", redirectUrl);
		return redirectUrl;
	}

	@Override
	public Credential getCredentials(String userID) throws IOException {
		return flow.loadCredential(userID);
	}

	@Override
	public void getTokensFromGoogleCode(String code, String userID) throws IOException {
		// exchange the code against the access token and refresh token
		GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(CommonUtil.GOOGLE_CALLBACK_URL)
				.execute();
		logger.info("Get access token and refresh token. code: {} - access token: {} - refresh token: {}", code,
				tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
		flow.createAndStoreCredential(tokenResponse, userID);

	}

	@Override
	public void removeUserSession() throws Exception {
		// clear the local storage
		logger.info("clearing the data store...");
		dataStoreFactory.getDataStore("client_secrets.json").clear();
	}

}
