package at.tuwien.aic.raid.connector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

import at.tuwien.aic.raid.ConnectorInterface;
import at.tuwien.aic.raid.data.FileObject;

public class DropBoxImpl 
		implements ConnectorInterface
{
	java.util.logging.Logger log= java.util.logging.Logger.getLogger( "DropBox" );
	
	// directory is RAID level specific
	private File baseDirectory = new File( "/" );
	private DbxClient client = null;
	private String name = "DropBox";
	
	private void log( String string ) 
	{
		log.log( Level.INFO, string );
	}
	
	public DropBoxImpl( File baseDirectory )
	{
		super();
		this.baseDirectory = baseDirectory;
	}

	public DropBoxImpl()
	{
		// TODO Auto-generated constructor stub
		
		// PropertyFile pf = new PropertyFile();
		Properties pf = new Properties();
		
		// pf.setPropertyFileName( "dropBox.properties" );
		try
		{
			pf.load( DropBoxImpl.class.getResourceAsStream( "dropBox.properties" ) );
		}
		catch( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
			// extension: In this registered dorpBox application it is
			// possible to register a recall URL.
			// This URL would get a &code= parameter which has to be inserted
			// into to properties file.
			log( "1. Go to: " + authorizeUrl );
			log( "2. Click \"Allow\" (you might have to log in first)" );
			log( "3. Copy the authorization code." );
			
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

			// pf.addProperty( "accessToken", accessToken );
			pf.setProperty( "accessToken", accessToken );
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
		String fn = absPath.getPath();
		String ret = fn;
		
//		log(  "PATH-Seperator: " +  absPath.separator + " WINDOWs like?" );
		
		if( absPath.separator.indexOf( "\\" ) != -1 )
		{
//			log( "Found: \\" );
			ret = fn.replaceAll( "\\\\", "/" );
		}
		
//		log( "Name: " + ret );	
		
		return ret;
	}
	
	/**
	 * creates a FileObject
	 */
	@Override
	public void create( FileObject file )
	{
		File absPath = new File( baseDirectory, file.getName() );
		
		/** 
		 * split path into basedir and file
		 */
		File dirName = absPath.getParentFile();
		
		while( dirName!= null && ! dirName.toString().equals( File.pathSeparator ) )
		{
			log( "create: check existance of file in dir " + dirName.toString() );
			
			dirName = dirName.getParentFile();
		}
		
		/**
		 * check if baseDirecory exists
		 */
		dirName = absPath.getParentFile();
		
		String unixDir = getUnixPath( dirName );
		ArrayList<FileObject> list = this.lsContent( unixDir );
		
		for( FileObject aFO : list )
		{
			log( " List objects: " + aFO.getName() + " compare with: " +  absPath.getName() );
			
			if( aFO.getName().equals( absPath.getName() ) )
			{
				log( "DELETE: " + aFO.getName() );
		
				this.delete( file );
				// there can only be ONE file with the same name!
				break;
			}	
		}

		unixDir = getUnixPath( absPath );
		
		/**
		 * write data to file
		 */
		InputStream is = null;
		
		try 
		{
			/*
				log( "client.getAccountInfo()" + client.getAccountInfo() ); 
				log( "Linked account: " + client.getAccountInfo().displayName );
			 */

			byte[] bytes = file.getData();
			is = new ByteArrayInputStream( bytes );
			
			String unixFn = getUnixPath( absPath );
		
			DbxEntry.File uploadedFile = client.uploadFile( unixFn,
					DbxWriteMode.add(), bytes.length, is );
			
			log( "Uploaded: " + uploadedFile.toString() );
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
			throws IOException
	{
		/**
		 * check file existence ?
		 */
		// File aFile = new File( name.getName() );
		File absPath = new File( baseDirectory, name.getName() );

		String unixFn = getUnixPath( absPath );
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DbxEntry.File downloadedFile = null;
		
		try 
		{		
			downloadedFile = client.getFile( unixFn, null, os );
			
			if( downloadedFile == null )
			{
				throw new FileNotFoundException();
			}	
			
			byte[] bytes = os.toByteArray();
			
			name.setData( bytes );
			name.setName( downloadedFile.path.replace( "/", "" ) );
			
			if( downloadedFile.isFile() )
			{
				name.setIsFile();
			}
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
		// log( "Function delete - at the moment not implemented!" );
		// See: http://stackoverflow.com/questions/17703250/how-can-i-delete-a-file-folder-from-dropbox-using-the-java-api
		// a rest implementation!? - Not beautiful!
		File absPath = new File( baseDirectory, file.getName() );
		// File aFile = new File( file.getName() );
		String unixFn = getUnixPath( absPath );
		
		try 
		{	
			client.delete( "/"+file.getName() );
		}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<FileObject> listFiles()
	{
		return lsContent( "/" );
	}

	 public void mkDir( FileObject aFileObject )
	 {
			File absPath = new File( baseDirectory, aFileObject.getName() );
			// File aFile = new File( file.getName() );
			String unixFn = getUnixPath( absPath );		 
		 
			try 
			{	
				client.createFolder( unixFn );
			}
			catch( DbxException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	 
	 
	 }

	public void rmDir( FileObject aFileObject )
	{
		File absPath = new File( baseDirectory, aFileObject.getName() );
		// File aFile = new File( file.getName() );
		String unixFn = getUnixPath( absPath );		 
	 
		try 
		{	
			// There is no difference between file and directory!
			client.delete( unixFn );
		}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	private ArrayList<FileObject> lsContent( String unixFn )
	{
		ArrayList<FileObject> ret = new ArrayList<FileObject>();
		DbxEntry.WithChildren listing;
		
		try
		{
			listing = client.getMetadataWithChildren( unixFn );

			for( DbxEntry child : listing.children )
			{
				FileObject aFileObject = new FileObject();
				aFileObject.setName(  child.name.replace( "/", "" ) );
				ret.add( aFileObject );
				
				log( "    " + child.name + ": " + child.toString() );
			}
		}
		catch( DbxException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		return ret;
	}
	
	public ArrayList<FileObject> lsContent( FileObject file )
	{
		File aFile = new File( file.getName() );
		String unixFn = getUnixPath( aFile );
		
		return lsContent( unixFn );
	}
	
	public String getName()
	{
		return name;
	}
}
