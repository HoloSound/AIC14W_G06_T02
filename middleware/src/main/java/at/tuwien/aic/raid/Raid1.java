package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import at.tuwien.aic.raid.connector.ConnectorConstructor;
import at.tuwien.aic.raid.data.FileObject;

public class Raid1 {
	
	 java.util.logging.Logger log = java.util.logging.Logger.getLogger("Raid1");
	 
    ConnectorInterface dbox =  ConnectorConstructor.dropBoxInstance();
    // static
    ConnectorInterface box =  ConnectorConstructor.boxInstance();
    ConnectorInterface s3 =  ConnectorConstructor.s3Instance();


	
	
	public Raid1() 
	{
		log.fine( "NEW Raid1" );
	}

	public int getMaxId()
	{
		return 3;
	}
	
	public ConnectorInterface getInterface( int ii )
	{
		if( ii == 0 )
		{
			return (ConnectorInterface) dbox;
		}
		else if( ii == 1 )
		{
			return (ConnectorInterface) box;
		}
		else
		{
			return (ConnectorInterface) s3;
		}
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

	public synchronized ArrayList<FileObject> listFiles() throws IOException 
	{ 
		ArrayList<FileObject> ret = new ArrayList<FileObject>();
		
		HashMap<String,FileObject> compare = new HashMap<String,FileObject>();
		HashMap<String,ConnectorInterface> sourceIF = new HashMap<String,ConnectorInterface>();
		
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
			s3Files = s3.listFiles();
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
			sourceIF.put( aFO.getName(), box );
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
				sourceIF.put( aFileName, dbox );
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
				sourceIF.put( aFileName, s3 );
			}
			
			s3Hash.put( aFileName, aFO );
		}	
		
		// Now we have the other way round to create the files
		for( String key : compare.keySet() )
		{
			FileObject toCreate = compare.get( key );
			// search for each connector:
			FileObject foundObject = boxHash.get( key );
			
			if( foundObject == null )
			{	
				try
				{
					String fileName = toCreate.getName();
					
					log.fine( "Creation of file " + fileName + " (because missing) on BOX connection." );
					
					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					box.create( newFileObject );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate + " failed at BOX connection." );
					e.printStackTrace();
				}
			}
			
			// search for each connector:
			foundObject = dBoxHash.get( key );
			
			if( foundObject == null )
			{	
				try
				{
					String fileName = toCreate.getName();
					
					log.fine( "Creation of file " + fileName + " (because missing) on DROPBOX connection." );
					
					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					dbox.create( newFileObject );
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
					String fileName = toCreate.getName();
					
					log.fine( "Creation of file " + fileName + " (because missing) on AMAZON_S3 connection." );
					
					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					s3.create( newFileObject );
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
	public synchronized void delete( String fn ) throws IOException
	{
		// definition of connector interfaces
		ConnectorInterface[] cis = new ConnectorInterface[3];

		for( int ii = 0 ; ii < this.getMaxId() ; ii++ )
		{
			cis[ii] = this.getInterface( ii );
		}

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : cis )
		{
			try
			{
				log.fine( "Deleting" + fn + "from " + ci.getName() + "." );
				ci.delete( new FileObject( fn ) );
			}
			catch( Exception e )
			{
				log.fine( "Deleting from " + ci.getName() + " failed" + e.getMessage() );
				throw new IOException( e );
			}
			
			log.fine( "File " + fn + "deleted from " + ci.getName() + "." );
		}

		// unrolled loop
/*
		try
		{
			System.out.println( "Deleting" + fn + "from Box" );
			box.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting from Box failed" + e.getMessage() );
			throw new IOException( e );

		}

		try
		{
			System.out.println( "Deleting" + fn + "from S3" );
			s3.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting from S3 failed" + e.getMessage() );
			throw new IOException( e );
		}

		try
		{
			System.out.println( "Deleting" + fn + "from DB" );
			dbox.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting from DB failed" + e.getMessage() );
			throw new IOException( e );
		}
 */
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
	public  synchronized  FileObject getFile(String fn) throws IOException {// TODO IMPLEMENT
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
			log.fine("File \""+fn+"\": BoxMD5: "+boxFileMd5);
			returnFile = boxFile;
		}
		
		if(dboxFile != null) {
			dboxFileMd5 = dboxFile.getMd5();
			log.fine("File \""+fn+"\": DropBoxMD5: "+dboxFileMd5);
			returnFile = dboxFile;
		}
		
		if(s3File != null) {
			s3FileMd5 = s3File.getMd5();
			log.fine("File \""+fn+"\": S3MD5: "+s3FileMd5);
			returnFile = s3File;
		}
		
		//if all hashvalues are available
		if(boxFileMd5 != null && dboxFileMd5 != null && s3FileMd5 != null) {
			//proof if all hashvalues are the same if not proof if two are the same and restore and if all three are different raise exception
			if(boxFileMd5.equals(dboxFileMd5) && boxFileMd5.equals(s3FileMd5)) {
				log.fine("File: "+fn+": All three hashvalues are the same: "+boxFileMd5);
			} else if(boxFileMd5.equals(dboxFileMd5)) {
				returnFile = boxFile;
				
				log.fine("Inconsistency! S3 diffs to the other two hashes: S3: "+s3FileMd5+" Others: "+boxFileMd5+" trying to restore...");
				//restore s3
				try { 
					s3.delete(new FileObject(fn));
					s3.create(boxFile);
					
				} catch (Exception e) {
					log.fine("Restoring of File \""+fn+"\" on S3 failed" + e.getMessage()); 
				}
				
			} else if(boxFileMd5.equals(s3FileMd5)) {
				returnFile = boxFile;
				
				log.fine("Inconsistency! DropBox diffs to the other two hashes: DropBox: "+dboxFileMd5+" Others: "+boxFileMd5+" trying to restore...");
				//restore dbox
				try { 
					dbox.delete(new FileObject(fn));
					dbox.create(boxFile);
					
				} catch (Exception e) {
					log.fine("Restoring of File \""+fn+"\" on DropBox failed" + e.getMessage()); 
				}
				
			} else if(dboxFileMd5.equals(s3FileMd5)) {
				returnFile = dboxFile;
				
				log.fine("Inconsistency! Box diffs to the other two hashes: Box: "+s3FileMd5+" Others: "+dboxFileMd5+" trying to restore...");
				//restore box
				try { 
					box.delete(new FileObject(fn));
					box.create(dboxFile);
					
				} catch (Exception e) {
					log.fine("Restoring of File \""+fn+"\" on Box failed" + e.getMessage()); 
				}
				
			} else {
				log.fine("Inconsistency! All three hashvalues are different! Box: "+boxFileMd5+" DropBox: "+dboxFileMd5+" S3: "+s3FileMd5);
				throw new IOException("Inconsistency! All three hashvalues are different! Box: "+boxFileMd5+" DropBox: "+dboxFileMd5+" S3: "+s3FileMd5);
			}
			
		} else {
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

	public synchronized void write( FileObject f ) throws IOException
	{
		// TODO IMPLEMENT RAID1
		// LOGIK
		int b = 0;
		
	    // definition of connector interfaces
	    ConnectorInterface[] cis = new ConnectorInterface[3];
	    
	    for( int ii = 0 ; ii < this.getMaxId() ; ii++ )
	    {
	    	cis[ii] = this.getInterface( ii );
	    }
	    

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : cis )
		{
			try
			{
				log.fine( "Write" + f.getName() + " to " + ci.getName() );
				ci.create( f );
			}
			catch( Exception e )
			{
				b = b + 1;
				log.fine( "Error" + e.getMessage() );
				e.printStackTrace();
			}
			
			log.fine( "Write" + f.getName() + " to " + ci.getName() + " ... OK." );
		}
	
		// unfolded loop ...
/*		
		try
		{
			log.fine( "write" + f.getName() );
			dbox.create( f );
		}
		catch( Exception e )
		{
			b = b + 1;
			log.fine( "Error" + e.getMessage() );
			e.printStackTrace();
		}

		try
		{
			log.fine( "write" + f.getName() );
			s3.create( f );
		}
		catch( Exception e )
		{
			b = b + 1;
			log.fine( "Error" + e.getMessage() );
			e.printStackTrace();
		}

		try
		{
			log.fine( "write" + f.getName() );
			System.out.println( "write" + f.getName() );
			box.create( f );
		}
		catch( Exception e )
		{
			b = b + 1;
			log.fine( "Error" + e.getMessage() );
			e.printStackTrace();
		}
 */
		
		if( b == 3 )
		{
			throw new IOException(
					"Faild: The file is not stored in any of the connector!" );
		}
	}
}
