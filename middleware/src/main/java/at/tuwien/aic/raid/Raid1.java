package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import at.tuwien.aic.raid.connector.BoxImpl;
import at.tuwien.aic.raid.connector.DropBoxImpl;
import at.tuwien.aic.raid.connector.S3Connector;
import at.tuwien.aic.raid.data.FileObject;

public class Raid1 {
	
	 java.util.logging.Logger log = java.util.logging.Logger.getLogger("Raid1");
	    ConnectorInterface dbox = new DropBoxImpl();
	    static ConnectorInterface box = new BoxImpl();
	    ConnectorInterface s3 = new S3Connector();
	
	
	
	public Raid1() {
		System.out.println("NEW Raid1");
	}

	/**
	 * Compares the content of all connectors and try to fix missing files If
	 * three is a file missing in a connector restore from other connectors
	 * 
	 * @return list of files which should be consistent on every connector
	 * @throws IOException
	 *             If ALL connectors fail. Log all other exception
	 * 
	 */

	public ArrayList<FileObject> listFiles() throws IOException 
	{ 
		ArrayList<FileObject> ret = new ArrayList<FileObject>();
		
		HashMap<String,FileObject> compare = new HashMap<String,FileObject>();
		
		ArrayList<FileObject> boxFiles = null;
		HashMap<String,FileObject> boxHash = new HashMap<String,FileObject>();
		ArrayList<FileObject> dBoxFiles = null;
		HashMap<String,FileObject> dBoxHash = new HashMap<String,FileObject>();
		ArrayList<FileObject> s3Files = null;
		HashMap<String,FileObject> s3Hash = new HashMap<String,FileObject>();
		
		// IMPLEMENT
		// RAID1
		try
		{
			boxFiles = box.listFiles();
		}
		catch( Exception e1 )
		{
			log.fine( "BOX Connector failed ro retrieve files." );
			e1.printStackTrace();
		}
		
		try
		{
			dBoxFiles = dbox.listFiles();
		}
		catch( Exception e1 )
		{			
			log.fine( "DROP_BOX Connector failed ro retrieve files." );
			e1.printStackTrace();
		}
		
		try
		{
			s3Files = as3.listFiles();
		}
		catch( Exception e1 )
		{
			log.fine( "AMAZON_S3 Connector failed ro retrieve files." );
			e1.printStackTrace();
		}
		
		if( boxFiles == null && dBoxFiles == null && s3Files == null )
		{
			throw( new IOException( "No connection available." ) );
		}

		// 1 st initialize the first ret - via box
		for( FileObject aFO : boxFiles )
		{
			compare.put( aFO.getName(), aFO );
			boxHash.put( aFO.getName(), aFO );
		}

		// 2 st initialize the ret 
		for( FileObject aFO : dBoxFiles )
		{
			// search in ret the aFO.getName();
			String aFileName = aFO.getName();
			
			FileObject foundObject = compare.get( aFileName );
			
			if( foundObject == null )
			{
				// we have a new file there!
				compare.put( aFileName, aFO );
			}
			
			dBoxHash.put( aFileName, aFO );
		}
			
		// 3nd initialize the ret 
		for( FileObject aFO : s3Files )
		{
			// search in ret the aFO.getName();
			String aFileName = aFO.getName();
			
			FileObject foundObject = compare.get( aFileName );
			
			if( foundObject == null )
			{
				// we have a new file there!
				compare.put( aFileName, aFO );
			}
			
			s3Hash.put( aFileName, aFO );
		}	
		
		// Now we have the other way round
		for( String key : compare.keySet() )
		{
			FileObject toCreate = compare.get( key );
			// search for each connector:
			FileObject foundObject = boxHash.get( key );
			
			if( foundObject == null )
			{	
				try
				{
					box.create( toCreate );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate + " failed at BOX Connection." );
					e.printStackTrace();
				}
			}
			
			// search for each connector:
			foundObject = dBoxHash.get( key );
			
			if( foundObject == null )
			{	
				try
				{
					dbox.create( toCreate );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate + " failed at DROP_BOX Connection." );
					e.printStackTrace();
				}
			}
			
			// search for each connector:
			foundObject = s3Hash.get( key );
			
			if( foundObject == null )
			{	
				try
				{
					as3.create( toCreate );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate + " failed at AMAZON_S3 Connection." );
					e.printStackTrace();
				}
			}
			
			ret.add( toCreate );
		}
				
		return ret;
	}


	/**
	 * Try to remove the file from all connector
	 * 
	 * @param fn
	 * @throws IOException
	 *             if any of the delete operation fails the data will be
	 *             restored lazily
	 * 
	 */
	public void delete(String fn) throws IOException {// TODO IMPLEMENT RAID1
														// LOGIK

		try { 
			System.out.println("Deleting" + fn + "from Box");
			box.delete(new FileObject(fn));
			
		} catch (Exception e) {
			log.fine("Deleting drom Box failed" + e.getMessage()); 
			throw new IOException(e);
			
		
		}
		
		try {
			System.out.println("Seleting" + fn + "from S3");
			s3.delete(new FileObject(fn));
			
		} catch (Exception e) {
			log.fine("Deleting drom S3 failed" + e.getMessage()); 
			throw new IOException(e);
		}
		
		try {
			System.out.println("Deleting" + fn + "from DB");
			dbox.delete(new FileObject(fn));
			
		} catch (Exception e) {
			log.fine("Deleting drom DB failed" + e.getMessage()); 
			throw new IOException(e);
		}

	}
/**
 * 
 * Calculates the hash values for each available  file and logs the result 
 * 
 * @param fn
 * @return 
 * @throws IOException If no connector is reachable, the file does not exist in any connector,  the calculated hashfiles are  inconsistent 
 *
 */
	public FileObject getFile(String fn) throws IOException {// TODO IMPLEMENT
																// RAID1 LOGIK
		FileObject readFile = new FileObject(fn);
		
		FileObject boxFile = null;
		FileObject dboxFile = null;
		FileObject s3File = null;
		
		FileObject returnFile = null;
		
		try {
			//System.out.println("getFile" + fn);
			boxFile = box.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file "+fn+" from Box: "+e.toString());
		}
		
		try {
			dboxFile = dbox.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file \""+fn+"\" from DropBox: "+e.toString());
		}
		
		try {
			s3File = s3.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file "+fn+" from S3: "+e.toString());
		}
		
		if(boxFile == null && dboxFile == null && s3File == null) {
			log.fine("Couldn't read the file \""+fn+"\" from all connectors.");
			throw new IOException("I/O Error");
		}
		
		String boxFileMd5 = null;
		String dboxFileMd5 = null;
		String s3FileMd5 = null;
		
		if(boxFile != null) {
			boxFileMd5 = boxFile.getMd5();
			returnFile = boxFile;
		}
		
		if(dboxFile != null) {
			dboxFileMd5 = dboxFile.getMd5();
			returnFile = dboxFile;
		}
		
		if(s3File != null) {
			s3FileMd5 = s3File.getMd5();
			returnFile = s3File;
		}
		
		if(boxFileMd5 != null && dboxFileMd5 != null) {
			if(!boxFileMd5.equals(dboxFileMd5)) {
				log.fine("File \""+fn+"\" has inconsistency! Box MD5: "+boxFileMd5+" DropBox MD5: "+dboxFileMd5);
				throw new IOException("Inconsistency! Box MD5: "+boxFileMd5+" DropBox MD5: "+dboxFileMd5);
			}
		}
		
		if(boxFileMd5 != null && s3FileMd5 != null) {
			if(!boxFileMd5.equals(s3FileMd5)) {
				log.fine("File \""+fn+"\" has inconsistency! Box MD5: "+boxFileMd5+" S3 MD5: "+dboxFileMd5);
				throw new IOException("Inconsistency! Box MD5: "+boxFileMd5+" S3 MD5: "+s3FileMd5);
			}
		}
		
		if(dboxFileMd5 != null && s3FileMd5 != null) {
			if(!dboxFileMd5.equals(s3FileMd5)) {
				log.fine("File \""+fn+"\" has inconsistency! DropBox MD5: "+boxFileMd5+" S3 MD5: "+dboxFileMd5);
				throw new IOException("Inconsistency! DropBox MD5: "+dboxFileMd5+" S3 MD5: "+s3FileMd5);
			}
		}
		
		log.fine("Successfully read file \""+fn+"\"");
		
		return returnFile;

	}
/**
 * Stores  the  file in atleast one connector
 * 
 * @param f
 * @throws IOException if no write operation success 
 *
 */
	public void write(FileObject f) throws IOException {// TODO IMPLEMENT RAID1
														// LOGIK
		try {
			System.out.println("write" + f.getName());
			box.create(f);
		} catch (Exception e) {

			throw new IOException(e);
		}
	}

}
