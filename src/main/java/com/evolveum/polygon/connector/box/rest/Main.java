package com.evolveum.polygon.connector.box.rest;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;

import org.json.JSONObject;

public class Main {
	
	private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String GRANT_TYPE = "authorization_code";
	private static final Log LOG = Log.getLog(BoxConnector.class);
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("Generate credentials for Box Connector");
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("You have to created and registered App in Box API.");
        System.out.println("Add these credentials into configuration fields in Box Connector");
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("");
        String clientId;
        String clientSecret;
        String redirectUri;
        String authCode;
        
        File file = new File(CLIENTSECRETS_LOCATION);
        FileWriter fileWriter = new FileWriter(file);
        Scanner user_input = new Scanner(System.in);
        System.out.println("Enter Client ID: ");
        clientId = user_input.next();
        System.out.println("Enter Client Secret: ");
        clientSecret = user_input.next();
        System.out.println("Enter Redirect Uri: ");
        redirectUri = user_input.next();
       
        
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("account.box.com")
                .setPath("/api/oauth2/authorize?")
                .setParameter("response_type", "code")
                .setParameter("client_id",clientId)
                .setParameter("redirect_uri", redirectUri)
                .addParameter("state","48d2LMuz8" )
                .build();
        HttpGet httpget = new HttpGet(uri);
        
        URI uriOpen = new URI(uri.toString());
        Desktop.getDesktop().browse(uriOpen);
        
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("If page do not open in your web browser please copy and open the following link in your web browser: ");
        System.out.println("");
        System.out.println(httpget.getURI());
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("Please copy the Authorization Code that you get in URL after clicking on Grant access to Box");
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println("-------------!!!Authorization Code is valid only for 30sec. Please by hurry!!!-------------");
        System.out.println("Enter Authorization Code: ");
        authCode = user_input.next();
        
        if (!authCode.isEmpty()){
        HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://api.box.com/oauth2/token");
		post.setHeader("Content-Type", CONTENT_TYPE);
		StringBuilder sb = new StringBuilder();
		
		sb.append("grant_type").append('=').append(GRANT_TYPE).append('&')
		  .append("code").append('=').append(authCode).append('&')
		  .append("client_id").append('=').append(clientId).append('&')
		  .append("client_secret").append('=').append(clientSecret);
		
		post.setEntity(new StringEntity(sb.toString()));
		HttpResponse response = client.execute(post);
		
		LOG.ok("response: {0}", response.getStatusLine().getStatusCode());
		
		JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
		
		
		if(response.getStatusLine().getStatusCode() == 200){
			String refreshToken = (String) json.get("refresh_token");
			String tokenType = (String) json.get("token_type");
			String accessToken = (String) json.get("access_token");
			System.out.println("-------------------------------------------------------------------------------------------");
			System.out.println("Your Refresh Token: " + refreshToken);
			System.out.println("Token Type: " + tokenType);
			System.out.println("Your Access Token: " + accessToken);
		} else {
			System.out.println("-------------------------------------------------------------------------------------------");
			System.out.println("Please Try It Again");
		}
		user_input.close();
		fileWriter.close();
        }
   }
	
	

}
