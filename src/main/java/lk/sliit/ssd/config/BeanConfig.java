/**
 * 
 */
package lk.sliit.ssd.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import lk.sliit.ssd.controller.GreetingController;
import lk.sliit.ssd.util.CommonUtil;

/**
 * @author vimukthi_r
 *
 */
@Configuration
public class BeanConfig {

	@Value("${pretty-calendar.google-oauth.credentials.file.path}")
	private String credentialsFilePath;

	@Value("${pretty-calendar.google-oauth.token.folder.path}")
	private String tokensDirectoryPath;

	@Bean
	public HttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Bean
	public GoogleClientSecrets getGoogleClientSecrets() throws IOException {
		InputStream in = GreetingController.class.getResourceAsStream(credentialsFilePath);

		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
		}
		return GoogleClientSecrets.load(CommonUtil.JSON_FACTORY, new InputStreamReader(in));
	}

	@Bean
	public DataStoreFactory getDataStoreFactory() throws IOException {
		return new FileDataStoreFactory(new File(tokensDirectoryPath));
	}

	@Bean
	public GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
		return new GoogleAuthorizationCodeFlow.Builder(getHttpTransport(), CommonUtil.JSON_FACTORY,
				getGoogleClientSecrets(), CommonUtil.SCOPES)
				.setDataStoreFactory(getDataStoreFactory())
				.setAccessType("offline")
				.build();
	}

}
