package test;

//Include the Dropbox SDK.
import com.dropbox.core.*;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class HelloDropBoxWorld 
{
/*
	public void setup()
	{
		String userLocale = ...;
		 
		DbxRequestConfig requestConfig = new DbxRequestConfig("text-edit/0.1", userLocale);
		DbxAppInfo appInfo = DbxAppInfo.Reader.readFromFile("api.app");
		
		// Select a spot in the session for DbxWebAuth to store the CSRF token.
		javax.servlet.http.HttpServletRequest request = ...;
		javax.servlet.http.HttpSession session = request.getSession(true);
		String sessionKey = "dropbox-auth-csrf-token";
		DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);
		
		String redirectUri = "http://my-server.com/dropbox-auth-finish"
		DbxWebAuth auth = new DbxWebAuth(requestConfig, appInfo, redirectUri, csrfTokenStore);
 		
	}
	
	// handler for authStart()
	public void authStart()
	{
		javax.servlet.http.HttpServletResponse response = ...;
		
		// Start authorization.
		String authorizePageUrl = auth.start();
		
		// Redirect the user to the Dropbox website so they can approve our application.
		// The Dropbox website will send them back to "http://my-server.com/dropbox-auth-finish"
		// when they're done.
		response.sendRedirect( authorizePageUrl );
	}

	
	public void authFinish()
	{
		javax.servlet.http.HttpServletResponse response = ...;
		
		DbxAuthFinish authFinish;
		 
		try 
		{
			authFinish = auth.finish( request.getParameterMap() );
		}
		catch( DbxWebAuth.BadRequestException ex ) 
		{
			log( "On /dropbox-auth-finish: Bad request: " + ex.getMessage() );
			response.sendError(400);
			return;
		}
		catch( DbxWebAuth.BadStateException e x)
		{
			// Send them back to the start of the auth flow.
			response.sendRedirect( "http://my-server.com/dropbox-auth-start" );
			return;
		}
		catch (DbxWebAuth.CsrfException ex) 
		{
			log( "On /dropbox-auth-finish: CSRF mismatch: " + ex.getMessage() );
			return;
		}
		catch( DbxWebAuth.NotApprovedException ex )
		{
			// When Dropbox asked "Do you want to allow this app to access your
			// Dropbox account?", the user clicked "No".
			...
			return;
		}
		catch( DbxWebAuth.ProviderException ex ) 
		{
			log( "On /dropbox-auth-finish: Auth failed: " + ex.getMessage() );
			response.sendError( 503, "Error communicating with Dropbox." );
			return;
		}
		catch( DbxException ex )
		{
			log( "On /dropbox-auth-finish: Error getting token: " + ex.getMessage() );
			response.sendError( 503, "Error communicating with Dropbox." );
			return;
		}
		
		String accessToken = authResponse.accessToken;
		
		// Save the access token somewhere (probably in your database) so you
		// don't need to send the user through the authorization process again.
		...
		
		 // Now use the access token to make Dropbox API calls.
		DbxClient client = new DbxClient( requestConfig, accessToken );
		...	
	}
 */	
	

	
	
	public static void main( String[] args ) 
				throws IOException, DbxException 
	{
		PropertyFile pf = new PropertyFile();
		
		pf.setPropertyFileName( "dropBox.properties" );
		
		// Get your app key and secret from the Dropbox developers website.
		// 	... and add it into private dropBox.properties file.
		String APP_KEY = pf.getProperty( "app_key" );
		String APP_SECRET = pf.getProperty( "app_secret" );

		DbxAppInfo appInfo = new DbxAppInfo( APP_KEY, APP_SECRET );

		DbxRequestConfig config = new DbxRequestConfig( "JavaTutorial/1.0",
				Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect( config, appInfo );

		// Have the user sign in and authorize your app.
		String authorizeUrl = webAuth.start();
		
		String accessToken = null;
		
		accessToken = pf.getProperty( "accessToken" );
		
		if( accessToken ==  null )
		{		
			System.out.println( "1. Go to: " + authorizeUrl );
			System.out.println( "2. Click \"Allow\" (you might have to log in first)" );
			System.out.println( "3. Copy the authorization code." );
			
			String code = new BufferedReader( 
							new InputStreamReader( System.in ) ).readLine().trim();
	
			// This will fail if the user enters an invalid authorization code.
			DbxAuthFinish authFinish = webAuth.finish( code );
			accessToken = authFinish.accessToken;
	
			pf.addProperty( "accessToken", accessToken );
		}
		
		DbxClient client = new DbxClient( config, accessToken );
		
		System.out.println( "Linked account: " + client.getAccountInfo().displayName );

		File inputFile = new File( "working-draft.txt" );
		FileInputStream inputStream = new FileInputStream( inputFile );
		
		try 
		{
			DbxEntry.File uploadedFile = client.uploadFile( "/magnum-opus.txt",
					DbxWriteMode.add(), inputFile.length(), inputStream );
			
			System.out.println( "Uploaded: " + uploadedFile.toString() );
		} 
		finally 
		{
			inputStream.close();
		}

		DbxEntry.WithChildren listing = client.getMetadataWithChildren( "/" );
		System.out.println( "Files in the root path:" );
		
		for( DbxEntry child : listing.children ) 
		{
			System.out.println( "	" + child.name + ": " + child.toString() );
		}

		FileOutputStream outputStream = new FileOutputStream( "magnum-opus.txt" );
		
		try 
		{
			DbxEntry.File downloadedFile = client.getFile( "/magnum-opus.txt",
					null, outputStream );
			
			System.out.println( "Metadata: " + downloadedFile.toString() );
		} 
		finally 
		{
			outputStream.close();
		}
	}
}