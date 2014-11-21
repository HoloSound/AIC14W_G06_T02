package at.tuwien.aic.raid;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Locale;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

import at.tuwien.aic.raid.data.FileObject;

public class DropBoxImpl 
		implements ConnectorInterface
{
	// directory is RAID level specific
	private File baseDirectory = new File( "/" );
	private DbxClient client = null;
	
	
	public DropBoxImpl( File baseDirectory )
	{
		super();
		this.baseDirectory = baseDirectory;
	}

	public DropBoxImpl()
	{
		// TODO Auto-generated constructor stub
		
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
		
		
		
		String accessToken = pf.getProperty( "accessToken" );
		
		if( accessToken ==  null )
		{		
			// TODO - this won't work with J2EE
			System.out.println( "1. Go to: " + authorizeUrl );
			System.out.println( "2. Click \"Allow\" (you might have to log in first)" );
			System.out.println( "3. Copy the authorization code." );
			
			String code;
			// This will fail if the user enters an invalid authorization code.
			DbxAuthFinish authFinish;
			
			try
			{
				code = new BufferedReader( 
								new InputStreamReader( System.in ) ).readLine().trim();
				
				authFinish = webAuth.finish( code );
				
				accessToken = authFinish.accessToken;
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( DbxException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pf.addProperty( "accessToken", accessToken );
		}
		
		client = new DbxClient( config, accessToken );
	}

	/**
	 * The Drop Box uses Linux like Path Separators - \\ from windows wont work!
	 * 
	 * Is it possible to so File Object to use now the UNIX formating?
	 * 
	 * @param absPath
	 * @return
	 */
	// The Drop Box uses Linux like Path Separators - \\ from windows wont work!
	// Is it possible to so File Object to use now the UNIX formating?
	private String getUnixPath( File absPath )
	{
		String ret = null;
		String fn = absPath.getPath();
		
//		System.out.println(  "PATH-Seperator: " +  absPath.separator + " WINDOWs like?" );
		
		if( absPath.separator.indexOf( "\\" ) != -1 )
		{
//			System.out.println( "Found: \\" );
			ret = fn.replaceAll( "\\\\", "/" );
		}
		
//		System.out.println( "Name: " + ret );	
		
		return ret;
	}
	
	/**
	 * creates a FileObject
	 */
	@Override
	public void create( FileObject file )
	{
		// TODO Auto-generated method stub
		/**
		 * check if baseDirecory exists
		 */
/*		
		if( !baseDirectory.isDirectory() )
		{
			baseDirectory.mkdirs();
		}
 */
		/**
		 * check file existence
		 */
		File absPath = new File( baseDirectory, file.getName() );
/*
 * 		Check if file exists on dropbox
 */
//		if( absPath.isFile() )
//		{
			/**
			 * we delete it to be idempotent
			 */
/*		
			absPath.delete();
		}
 */
		/**
		 * write data to file
		 */
		InputStream is = null;
		
		try 
		{
			/*
			System.out.println( "client.getAccountInfo()" + client.getAccountInfo() ); 
			System.out.println( "Linked account: " + client.getAccountInfo().displayName );
			System.out.flush();
		*/

			byte[] bytes = file.getData();
			is = new ByteArrayInputStream( bytes );
			
			String unixFn = getUnixPath( absPath );
		
			DbxEntry.File uploadedFile = client.uploadFile( unixFn,
					DbxWriteMode.add(), bytes.length, is );
			
			System.out.println( "Uploaded: " + uploadedFile.toString() );
		}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try
			{
				if( is != null )
				{
					is.close();
				}
				
				is = null;
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public FileObject read( FileObject name )
	{
		/**
		 * check file existence ?
		 */
		File aFile = new File( name.getName() );

		String unixFn = getUnixPath( aFile );
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try 
		{			
			DbxEntry.File downloadedFile = client.getFile( unixFn, null, os );
			
			byte[] bytes = os.toByteArray();
			
			name.setData( bytes );
			name.setName( downloadedFile.toString() );
     	}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			if( os != null )
			{
				try
				{
					os.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			os = null;
		}

		return name;
	}

	@Override
	public void update( FileObject file )
	{
		delete( file );		// return - dont care
		create( file );
	}

	@Override
	public void delete( FileObject file )
	{
		// TODO !!!
		// System.out.println( "Function delete - at the moment not implemented!" );
		// See: http://stackoverflow.com/questions/17703250/how-can-i-delete-a-file-folder-from-dropbox-using-the-java-api
		// a rest implementation!? - Not beautiful!
		
		File aFile = new File( file.getName() );
		String unixFn = getUnixPath( aFile );
		
		try 
		{	
			client.delete( unixFn );
		}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<FileObject> listFiles() {
		return null;
	}


}
