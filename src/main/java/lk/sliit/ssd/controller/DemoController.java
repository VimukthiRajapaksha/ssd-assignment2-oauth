package lk.sliit.ssd.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;

import lk.sliit.ssd.service.DocsQuickstart;

//@Controller
public class DemoController {

	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);

//	@GetMapping("/greeting")
//	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
//			Model model, Principal principal) {
//		try {
//
//			final DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
//
//			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//			System.out.println("isauthenticated ::" + authentication.isAuthenticated());
//			if (!(authentication instanceof AnonymousAuthenticationToken)) {
//				DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();
//				model.addAttribute("name", user.getFullName());
//
//				final String CREDENTIALS_FILE_PATH = "/client_secrets.json";
//
//				InputStream in = DocsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//				if (in == null) {
//					throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//				}
//				GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//				GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
//						clientSecrets, Collections.singletonList(DocsScopes.DOCUMENTS_READONLY))
//								.setDataStoreFactory(dataStoreFactory).build();
//
//				//Drive driveservice = new Drive.Builder(httpTransport, JSON_FACTORY, flow.getRequestInitializer())
//				//		.setApplicationName("ssd-assignment2-oauth").build();
//
//				final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//				Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, flow.getRequestInitializer())
//						.setApplicationName("ssd-assignment2-oauth").build();
//
//				String DOCUMENT_ID = "10wRfKpRW6jBLelvowVGwf--di1tIhk_le4sf_T7c3r0";
//				// Prints the title of the requested doc:
//				// https://docs.google.com/document/d/195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE/edit
//				Document response = service.documents().get(DOCUMENT_ID).execute();
//				String title = response.getTitle();
//
//				System.out.println("doc title:::" + title);
//
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			model.addAttribute("name", name);
//			// TODO: handle exception
//		}
//		return "greeting";
//	}

	@Autowired
	OAuth2AuthorizedClientService clientService;

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			Model model, HttpServletRequest request) {
		try {

			final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); // new NetHttpTransport();
			
			// final DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new
			// java.io.File(TOKENS_DIRECTORY_PATH));

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			System.out.println("isauthenticated ::" + authentication.isAuthenticated());
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();

				model.addAttribute("name", user.getFullName());

				//String CREDENTIALS_FILE_PATH = "/client_secrets.json";
				String CREDENTIALS_FILE_PATH = "/credentials.json";

				InputStream in = DocsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
				if (in == null) {
					throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
				}
				GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

				
				// Build flow and trigger user authorization request.
				GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
						clientSecrets, SCOPES)
								.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
								.setApprovalPrompt("auto")
								.setAccessType("online")
								.build();

				//LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8083).setCallbackPath("/greeting").build();
				//Credential credentials = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(authentication.getName());

				
				
				Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, flow.getRequestInitializer())
						.setApplicationName("ssd-assignment2-oauth")
						.build();

				// https://docs.google.com/document/d/1CasGTt_o5Sf_LfUwB0DHFoMoee9JOf0O_MiX2d-rTIY/edit

				String DOCUMENT_ID = "1CasGTt_o5Sf_LfUwB0DHFoMoee9JOf0O_MiX2d-rTIY";
				// Prints the title of the requested doc:
				// https://docs.google.com/document/d/195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE/edit
				Document response = service.documents().get(DOCUMENT_ID).execute();
				String title = response.getTitle();

				System.out.println("doc title:::" + title);

			}

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("name", name);
			// TODO: handle exception
		}
		return "greeting";
	}

}
