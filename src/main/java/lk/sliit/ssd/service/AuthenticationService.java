/**
 * 
 */
package lk.sliit.ssd.service;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;

/**
 * @author vimukthi_r
 *
 */
public interface AuthenticationService {

	public boolean isAuthenticatedUser(String userID) throws IOException;

	public String authenticateUser();

	public Credential getCredentials(String userID) throws IOException;

	public void getTokensFromGoogleCode(String code, String userID) throws IOException;

	public void removeUserSession() throws Exception;

}
