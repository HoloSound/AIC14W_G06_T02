package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import at.tuwien.aic.raid.connector.BoxImpl;
import at.tuwien.aic.raid.connector.DropBoxImpl;
import at.tuwien.aic.raid.connector.S3Connector;
import at.tuwien.aic.raid.data.FileObject;


/**
 * RAID5 - implementation 
 * 
 * @author Schwarzinger Rainer aka Holosound
 *
 */
public class Raid5
{
	/**
	 * Here in RAID5 we support a special file naming shema
	 * 
	 * IN FILE: 	<FILE>.<EXT>
	 * 
	 * This file will be split up into 3 different files
	 * named:
	 * 
	 * L<ZERO_PADDING>_<FILE>.<EXT>
	 * H<ZERO_PADDING>_<FILE>.<EXT>
	 * P<ZERO_PADDING>_<FILE>.<EXT>
	 * 
	 * Why do we have to do this?
	 * 
	 * The L, H, P describes the contents of the file.
	 * L ... Low order nibble of data
	 * H ... High order nibble of data
	 * P ... XOR (Parity) of data
	 * 
	 * ZERO_PADDING - in case of odd file size - the last byte has to be removed
	 * by reconstructing the file.
	 * 
	 * TWO nibbles are stored into ONE Byte --> 2 File sizes will result in ONE 
	 * target filesize - because 1 Byte will be extended with padding to get 1 Byte
	 * and also 2 Bytes will be combined to a file with 1 Byte!
	 * 
	 * To reconstruct the file size the information of padding we use from filename.
	 * (Because from the given data it is not possible to reconstruct it!)
	 */

	java.util.logging.Logger log = java.util.logging.Logger.getLogger( "Raid5" );
	ConnectorInterface dbox = new DropBoxImpl();
	static ConnectorInterface box = new BoxImpl();
	ConnectorInterface s3 = new S3Connector();

	public Raid5()
	{
		System.out.println( "NEW Raid5" );
	}

	
	public String[] generateFileNames( String fileName, int size )
	{
		String[] ret = new String[3];
		int pad = size % 2;
		
		ret[0] = "L" + pad + "_" + fileName;
		ret[1] = "H" + pad + "_" + fileName;
		ret[2] = "P" + pad + "_" + fileName;
		
		return ret;
	}
	
	/**
	 *  for RAID5 we generate 3 files.
	 *  
	 * The L, H, P describes the contents of the file.
	 * L ... Low order nibble of data
	 * H ... High order nibble of data
	 * P ... XOR (Parity) of data	 
	 * 
	 *  ZERO_PADDING - in case of odd file size - the last byte has to be removed
	 * by reconstructing the file.  
	 * 
	 * + "_" prefix!
	 *  
	 * @param ONE FileObject
	 * @return THREE FileObjects splitted into necessary parts
	 */
	public FileObject[] generateFiles( FileObject in )
	{
		FileObject[] ret = new FileObject[3];
		
		byte[] data = in.getData();
		String[] fileNames = generateFileNames( in.getName(), data.length );
		
		int newSize = data.length / 2 + data.length % 2;
		// 1 --> 1, 2 --> 1, 3 --> 2, ...
		
		// define LOw oder Nibble, HIgh order Nibble, and PARity arrays and act values
		byte[] lon = new byte[newSize];
		byte[] hin = new byte[newSize];
		byte[] par = new byte[newSize];
		int targetIndex = 0;
		
		int act_lon = 0;
		int act_hin = 0;
		
		int ii;
		for( ii = 0 ; ii < data.length ; ii++ )
		{
			int pad = ii % 2;
			int act_value = (int) data[ii];
			
			if( pad == 0 )
			{
				// here we have 2 cases - %2 == 0 
				act_lon = ( act_value & 0x0f ) << 4;
				act_hin = ( act_value & 0xf0 );
			}
			else
			{
				// and %2 == 1
				act_lon |= ( act_value & 0x0f );
				act_hin |= (( act_value & 0xf0 ) >> 4);
				
				// and store it
				lon[targetIndex] = (byte) (act_lon & 0xff);
				hin[targetIndex] = (byte) (act_hin & 0xff);
				par[targetIndex] = (byte) ( (act_lon ^ act_hin) & 0xff);
				
				targetIndex++;
				
				// re-init values - should not be necessary!
			}
		}
		
		// in the last round we have to add optionally a padding. 
		// or it's done implicitly!
		// but we have to store the generated data!
		if( data.length % 2 == 1 )
		{
			// act_lon and act_hin set - has now to be stored!
			lon[targetIndex] = (byte) (act_lon & 0xff);
			hin[targetIndex] = (byte) (act_hin & 0xff);
			par[targetIndex] = (byte) ( (act_lon ^ act_hin) & 0xff);
			
			targetIndex++;			
		}
		
		ret[0] = new FileObject();
		ret[0].setName( fileNames[0] );
		ret[0].setData( lon );
		
		ret[1] = new FileObject();
		ret[1].setName( fileNames[1] );
		ret[1].setData( hin );		

		ret[2] = new FileObject();
		ret[2].setName( fileNames[2] );
		ret[2].setData( par );
		
		return ret;
	}
	
	/**
	 * if we have all three files - we make a consistency check
	 * if we have only two file --> we are able to restore file - and generate the missing
	 * if we have only ONE file --> we thrown an exception - we can't restore anything
	 * 
	 * @param fileObjects
	 * @return
	 * @throws IOException 
	 */
	
	public FileObject reconstructFile( FileObject[] fileObjects ) throws IOException 
	{
		FileObject ret = new FileObject();
		
		// check how many files are available
		switch( fileObjects.length )
		{
		case 0:
			throw( new IOException( "No arguments exception." ) );	
			// break;
			
		case 1:
			throw( new IOException( "No arguments exception." ) );		
			// break;
			
		case 2:
			// reconstruct	
			break;			
		case 3:
			// reconstruct and check
			break;	
			
		default:
			throw( new IOException( "To many arguments exception." ) );	
			// break;
		}
		
		// select the files we have
		boolean even = true;
		int ii = 0;
		FileObject lonFo = null;
		FileObject hinFo = null;
		FileObject parFo = null;
		String targetEven = null;
		String targetName = null;
		int targetSize = -1;
		
		for( ii = 0 ; ii < fileObjects.length ; ii++ )
		{
			FileObject actFo = fileObjects[ii];
			String fn = actFo.getName();
			int actSize = actFo.getData().length;
			
			String typeStr = fn.substring( 0, 1 );
			String evenStr = fn.substring( 1, 2 );
			String padStr = fn.substring( 2, 3 );
			String sourceFn = fn.substring( 3 );
			
			if( padStr.equalsIgnoreCase( "_" ) == false )
			{
				throw( new IOException( "Wrong FileNameFormat of " + fn + "." ) );
			}
			
			if( typeStr.toUpperCase().equalsIgnoreCase( "L" ) == false  
					&& typeStr.toUpperCase().equalsIgnoreCase( "H" ) == false
					&& typeStr.toUpperCase().equalsIgnoreCase( "P" ) == false )
			{
				throw( new IOException( "Wrong FileNameFormat of " + fn + " - type has to be { H, L, P }." ) );
			}
			
			// all evenStr have to be equal!
			if( evenStr.equalsIgnoreCase( "0" ) == false  
					&& evenStr.equalsIgnoreCase( "1" ) == false )
			{
				throw( new IOException( "Wrong FileNameFormat of " + fn + " - even has to be { 0, 1 }." ) );
			}
			
			if( targetEven == null )
			{
				targetEven = evenStr;
			}
			else
			{
				if( targetEven.compareTo( evenStr ) != 0 )
				{
					throw( new IOException( "Filenames of different size markers given." ) );
				}
			}
			
			if( targetSize == -1 )
			{
				targetSize = actSize;
			}
			else
			{
				if( targetSize != actSize )
				{
					throw( new IOException( "Filenames of different sizes given." ) );
				}
			}
			
			// have to be different for every file!
			if( typeStr.toUpperCase().equalsIgnoreCase( "L" ) )
			{
				if( lonFo == null )
				{
					lonFo = actFo;
				}
				else
				{
					throw( new IOException( "TWO low order nibble files given." ) );
				}
			}
			else if( typeStr.toUpperCase().equalsIgnoreCase( "H" ) )
			{
				if( hinFo == null )
				{
					hinFo = actFo;
				}
				else
				{
					throw( new IOException( "TWO high order nibble files given." ) );
				}
			}
			else if( typeStr.toUpperCase().equalsIgnoreCase( "P" ) )
			{
				if( parFo == null )
				{
					parFo = actFo;
				}
				else
				{
					throw( new IOException( "TWO parity files given." ) );
				}
			}
			
			// all sourceFn's have to be equal!
			if( targetName == null )
			{
				targetName = sourceFn;
			}
			else
			{
				if( targetName.compareTo( sourceFn ) != 0 )
				{
					throw( new IOException( "Filenames of given files different. File1: " + targetName
							+ " File2: " + sourceFn + ".") );
				}
			}
		}
		
		ret.setName( targetName );
		
		// reconstruct file
		byte[] lonData = null;
		byte[] hinData = null;
		byte[] parData = null;
		int dataSize = -1;
		
		if( lonFo != null )
		{
			lonData = lonFo.getData();
			dataSize = lonData.length;
		}
		
		if( hinFo != null )
		{
			hinData = hinFo.getData();
			
			if( dataSize != -1 )
			{
				if( dataSize != hinData.length )
				{
					throw( new IOException( "Wrong size between LON (=" + dataSize + ") " 
							+ "/ HIN (=" + hinData.length + ")." ) );
				}
			}
			else
			{
				dataSize = hinData.length;
			}
		}		
		
		if( parFo != null )
		{
			parData = parFo.getData();
			
			if( dataSize != -1 )
			{
				if( dataSize != parData.length )
				{
					throw( new IOException( "Wrong size between LON/HIN (=" + dataSize + ") " 
								+ "/ PAR (=" + parData.length + ")." ) );
				}
			}
			else
			{
				dataSize = parData.length;
			}
		}
		
		// now we have to make a case distinction between accessable data!
		byte[] resultData = new byte[dataSize*2];
		int writeIndex = 0;
		
		// first implementation ( two further must follow! )
		if( lonData != null && hinData != null )
		{
			for( ii = 0 ; ii < dataSize ; ii++ )
			{
				// we simply unfold the data
				byte lon = lonData[ii];
				byte hin = hinData[ii];

				int byte1 = (hin & 0xf0) | (lon & 0xf0) >> 4;
				int byte2 = ((hin & 0x0f) << 4) | (lon & 0x0f);

				if( parData != null )
				{
					// check parity byte
					if( (byte) ((lon ^ hin) & 0xff) != parData[ii] )
					{
						throw(new IOException( "File inconsistency of "
								+ ret.getName() + " parity error at position "
								+ ii + "." ));
					}
				}

				resultData[writeIndex] = (byte) (byte1 & 0xff);
				writeIndex++;
				resultData[writeIndex] = (byte) (byte2 & 0xff);
				writeIndex++;
			}
		}
		else if( lonData != null && parData != null )
		{
			for( ii = 0 ; ii < dataSize ; ii++ )
			{
				// we simply unfold the data
				byte lon = lonData[ii];
				byte par = parData[ii];
				byte hin = (byte) ((lon ^ par) & 0xff);

				int byte1 = (hin & 0xf0) | (lon & 0xf0) >> 4;
				int byte2 = ((hin & 0x0f) << 4) | (lon & 0x0f);

				resultData[writeIndex] = (byte) (byte1 & 0xff);
				writeIndex++;
				resultData[writeIndex] = (byte) (byte2 & 0xff);
				writeIndex++;
			}
		}
		else if( hinData != null && parData != null )
		{
			for( ii = 0 ; ii < dataSize ; ii++ )
			{
				// we simply unfold the data
				byte par = parData[ii];
				byte hin = hinData[ii];
				byte lon = (byte) ((hin ^ par) & 0xff);

				int byte1 = (hin & 0xf0) | (lon & 0xf0) >> 4;
				int byte2 = ((hin & 0x0f) << 4) | (lon & 0x0f);

				resultData[writeIndex] = (byte) (byte1 & 0xff);
				writeIndex++;
				resultData[writeIndex] = (byte) (byte2 & 0xff);
				writeIndex++;
			}
		}

		
		// we have to shorten the file if odd byte size!
		if( targetEven.compareTo( "0" ) == 0 )
		{
			ret.setData( resultData );
		}
		else
		{
			// we have to shorten the data
			resultData = Arrays.copyOf( resultData, resultData.length -1 );
			ret.setData( resultData );
		}
		
		return ret;
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

		HashMap<String, FileObject> compare = new HashMap<String, FileObject>();
		HashMap<String, ConnectorInterface> sourceIF = new HashMap<String, ConnectorInterface>();

		ArrayList<FileObject> boxFiles = null;
		HashMap<String, FileObject> boxHash = new HashMap<String, FileObject>();
		ArrayList<FileObject> dBoxFiles = null;
		HashMap<String, FileObject> dBoxHash = new HashMap<String, FileObject>();
		ArrayList<FileObject> s3Files = null;
		HashMap<String, FileObject> s3Hash = new HashMap<String, FileObject>();

		// IMPLEMENT
		// RAID5
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
			throw(new IOException( "No connection available." ));
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

					log.fine( "Creation of file " + fileName
							+ " (because missing) on BOX connection." );

					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					box.create( newFileObject );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate
							+ " failed at BOX connection." );
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

					log.fine( "Creation of file " + fileName
							+ " (because missing) on DROPBOX connection." );

					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					dbox.create( newFileObject );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate
							+ " failed at DROP_BOX Connection." );
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

					log.fine( "Creation of file " + fileName
							+ " (because missing) on AMAZON_S3 connection." );

					// We have to read this file first - from where?
					ConnectorInterface readIF = sourceIF.get( fileName );
					FileObject searchFileObject = new FileObject( fileName );
					FileObject newFileObject = readIF.read( searchFileObject );
					s3.create( newFileObject );
				}
				catch( Exception e )
				{
					log.fine( "Creation of " + toCreate
							+ " failed at AMAZON_S3 Connection." );
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
	{// TODO IMPLEMENT RAID5
		// LOGIK

		try
		{
			System.out.println( "Deleting" + fn + "from Box" );
			box.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting drom Box failed" + e.getMessage() );
			throw new IOException( e );
		}

		try
		{
			System.out.println( "Seleting" + fn + "from S3" );
			s3.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting drom S3 failed" + e.getMessage() );
			throw new IOException( e );
		}

		try
		{
			System.out.println( "Deleting" + fn + "from DB" );
			dbox.delete( new FileObject( fn ) );

		}
		catch( Exception e )
		{
			log.fine( "Deleting drom DB failed" + e.getMessage() );
			throw new IOException( e );
		}

	}

	/**
	 * 
	 * Calculates the hash values for each available file and logs the result
	 * 
	 * @param fn
	 * @return
	 * @throws IOException
	 *             If no connector is reachable, the file does not exist in any
	 *             connector, the calculated hashfiles are inconsistent
	 *
	 */
	public synchronized FileObject getFile( String fn ) throws IOException
	{// TODO IMPLEMENT
		// RAID5 LOGIK
		FileObject readFile = new FileObject( fn );

		FileObject boxFile = null;
		FileObject dboxFile = null;
		FileObject s3File = null;

		FileObject returnFile = null;

		try
		{
			// System.out.println("getFile" + fn);
			boxFile = box.read( readFile );

		}
		catch( Exception e )
		{
			log.fine( "An error occured while reading file " + fn
					+ " from Box: " + e.toString() );
		}

		try
		{
			dboxFile = dbox.read( readFile );

		}
		catch( Exception e )
		{
			log.fine( "An error occured while reading file \"" + fn
					+ "\" from DropBox: " + e.toString() );
		}

		try
		{
			s3File = s3.read( readFile );

		}
		catch( Exception e )
		{
			log.fine( "An error occured while reading file " + fn
					+ " from S3: " + e.toString() );
		}

		if( boxFile == null && dboxFile == null && s3File == null )
		{
			log.fine( "Couldn't read the file \"" + fn
					+ "\" from all connectors." );
			throw new IOException( "I/O Error" );
		}

		String boxFileMd5 = null;
		String dboxFileMd5 = null;
		String s3FileMd5 = null;

		if( boxFile != null )
		{
			boxFileMd5 = boxFile.getMd5();
			log.fine( "File \"" + fn + "\": BoxMD5: " + boxFileMd5 );
			returnFile = boxFile;
		}

		if( dboxFile != null )
		{
			dboxFileMd5 = dboxFile.getMd5();
			log.fine( "File \"" + fn + "\": DropBoxMD5: " + dboxFileMd5 );
			returnFile = dboxFile;
		}

		if( s3File != null )
		{
			s3FileMd5 = s3File.getMd5();
			log.fine( "File \"" + fn + "\": S3MD5: " + s3FileMd5 );
			returnFile = s3File;
		}

		// if all hashvalues are available
		if( boxFileMd5 != null && dboxFileMd5 != null && s3FileMd5 != null )
		{
			// proof if all hashvalues are the same if not proof if two are the
			// same and restore and if all three are different raise exception
			if( boxFileMd5.equals( dboxFileMd5 )
					&& boxFileMd5.equals( s3FileMd5 ) )
			{
				log.fine( "File: " + fn
						+ ": All three hashvalues are the same: " + boxFileMd5 );
			}
			else if( boxFileMd5.equals( dboxFileMd5 ) )
			{
				returnFile = boxFile;

				log.fine( "Inconsistency! S3 diffs to the other two hashes: S3: "
						+ s3FileMd5
						+ " Others: "
						+ boxFileMd5
						+ " trying to restore..." );
				// restore s3
				try
				{
					s3.delete( new FileObject( fn ) );
					s3.create( boxFile );

				}
				catch( Exception e )
				{
					log.fine( "Restoring of File \"" + fn + "\" on S3 failed"
							+ e.getMessage() );
				}

			}
			else if( boxFileMd5.equals( s3FileMd5 ) )
			{
				returnFile = boxFile;

				log.fine( "Inconsistency! DropBox diffs to the other two hashes: DropBox: "
						+ dboxFileMd5
						+ " Others: "
						+ boxFileMd5
						+ " trying to restore..." );
				// restore dbox
				try
				{
					dbox.delete( new FileObject( fn ) );
					dbox.create( boxFile );

				}
				catch( Exception e )
				{
					log.fine( "Restoring of File \"" + fn
							+ "\" on DropBox failed" + e.getMessage() );
				}

			}
			else if( dboxFileMd5.equals( s3FileMd5 ) )
			{
				returnFile = dboxFile;

				log.fine( "Inconsistency! Box diffs to the other two hashes: Box: "
						+ s3FileMd5
						+ " Others: "
						+ dboxFileMd5
						+ " trying to restore..." );
				// restore box
				try
				{
					box.delete( new FileObject( fn ) );
					box.create( dboxFile );

				}
				catch( Exception e )
				{
					log.fine( "Restoring of File \"" + fn + "\" on Box failed"
							+ e.getMessage() );
				}

			}
			else
			{
				log.fine( "Inconsistency! All three hashvalues are different! Box: "
						+ boxFileMd5
						+ " DropBox: "
						+ dboxFileMd5
						+ " S3: "
						+ s3FileMd5 );
				throw new IOException(
						"Inconsistency! All three hashvalues are different! Box: "
								+ boxFileMd5 + " DropBox: " + dboxFileMd5
								+ " S3: " + s3FileMd5 );
			}

		}
		else
		{
			if( boxFileMd5 != null && dboxFileMd5 != null )
			{
				if( !boxFileMd5.equals( dboxFileMd5 ) )
				{
					log.fine( "File \"" + fn
							+ "\" has inconsistency! Box MD5: " + boxFileMd5
							+ " DropBox MD5: " + dboxFileMd5 );
					throw new IOException( "Inconsistency! Box MD5: "
							+ boxFileMd5 + " DropBox MD5: " + dboxFileMd5 );
				}
			}

			if( boxFileMd5 != null && s3FileMd5 != null )
			{
				if( !boxFileMd5.equals( s3FileMd5 ) )
				{
					log.fine( "File \"" + fn
							+ "\" has inconsistency! Box MD5: " + boxFileMd5
							+ " S3 MD5: " + dboxFileMd5 );
					throw new IOException( "Inconsistency! Box MD5: "
							+ boxFileMd5 + " S3 MD5: " + s3FileMd5 );
				}
			}

			if( dboxFileMd5 != null && s3FileMd5 != null )
			{
				if( !dboxFileMd5.equals( s3FileMd5 ) )
				{
					log.fine( "File \"" + fn
							+ "\" has inconsistency! DropBox MD5: "
							+ boxFileMd5 + " S3 MD5: " + dboxFileMd5 );
					throw new IOException( "Inconsistency! DropBox MD5: "
							+ dboxFileMd5 + " S3 MD5: " + s3FileMd5 );
				}
			}

		}

		log.fine( "Successfully read file \"" + fn + "\"" );

		return returnFile;

	}

	/**
	 * Stores the file in at least one connector
	 * 
	 * @param f
	 * @throws IOException
	 *             if no write operation success
	 *
	 */

	public synchronized void write( FileObject f ) throws IOException
	{// TODO IMPLEMENT RAID5
		// LOGIK
		int b = 0;

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

		if( b == 3 )
		{
			throw new IOException(
					"Faild: The file is not stored in any of the connector!" );
		}
	}
}
