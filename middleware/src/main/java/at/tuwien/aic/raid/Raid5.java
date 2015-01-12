package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.aic.raid.connector.ConnectorConstructor;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;


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
	
	ConnectorInterface dbox = ConnectorConstructor.dropBoxInstance();
	ConnectorInterface box = ConnectorConstructor.boxInstance();
	ConnectorInterface s3 = ConnectorConstructor.s3Instance();

	private ConnectorInterface[] connectorInterface = null;
	
	public Raid5()
	{
		System.out.println( "NEW Raid5" );
	}

	public int getMaxId()
	{
		return 3;
	}

	public void initConnectorInterface()
	{
		if( connectorInterface == null )
		{
			connectorInterface = new ConnectorInterface[3];
			
			for( int ii = 0 ; ii < this.getMaxId() ; ii++ )
			{
				connectorInterface[ii] = this.getInterface( ii );
			}
		}
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

	public synchronized ArrayList<FileViewObject> listFiles() 
				throws IOException
	{
		ArrayList<FileViewObject> ret = new ArrayList<FileViewObject>();

 		HashMap<String,FileViewObject> compareViewMap = new HashMap<String,FileViewObject>();
 		
		// FUTURE CODING STYLE
	    // initialization of connector interfaces
		initConnectorInterface();

		ArrayList<FileObject> fileObjectList;
		int errorCount = 0;
		int interfaceId = 0; // running from 0 .. 2
		
		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : connectorInterface )
		{
			try
			{
				log.fine( "Querying files from " + ci.getName() + "." );
				
				fileObjectList = ci.listFiles();
			}
			catch( Exception e )
			{
				errorCount++;
				log.fine( "Querying files from " + ci.getName() + " failed: " + e.getMessage() );
				throw new IOException( e );
			}
			
			log.fine( "Got " + fileObjectList.size() + " files from " + ci.getName() + "." );
			
			// Now we build up the matrix using fileObjectList
			for( FileObject aFO : fileObjectList )
			{
				// we take the filename
				String aFileName = aFO.getName();
				
				// and in RAID5 we check if the filename is constructed in special way
				
				Pattern p = Pattern.compile( "[HLP][01]_.*" );
				Matcher m = p.matcher( aFileName );
				boolean b = m.matches();
				
				if( b == true )
				{
					// in this case the file names start with char [3] !
					String mainFileName = aFileName.substring( 3 );
System.out.println( "1: " + mainFileName );					
					if( mainFileName.length() > 0 )
					{
						String raidType = aFileName.substring( 0, 3 );
System.out.println( "2: " + raidType );

						FileViewObject foundViewObject = compareViewMap.get( mainFileName );
						
						if( foundViewObject == null )
						{
							// we have a new file here - create a new entry
							FileViewObject aNewViewEntry = new FileViewObject();
							
							// here we have to manipulate the FileObject
							FileObject newFo = new FileObject( mainFileName );
							aNewViewEntry.setGlobalFo( newFo );
							
							FileObject[] interfaceInformationFos = new FileObject[3];
							
							aFO.setName( raidType );
							interfaceInformationFos[interfaceId] = aFO;
							
							aNewViewEntry.setInterfaceInformationFos( interfaceInformationFos );
							
							compareViewMap.put( mainFileName, aNewViewEntry );
						}
						else
						{
							// entry exists - append data
							FileObject[] interfaceInformationFos = foundViewObject.getInterfaceInformationFos();
							
							aFO.setName( raidType );
							interfaceInformationFos[interfaceId] = aFO;
							
							foundViewObject.setInterfaceInformationFos( interfaceInformationFos );				
						}
					}
				}
			}
			
			interfaceId++;
		}
	
		if( errorCount == 3 )
		{
			throw( new IOException( "No connection available." ) );
		}
		
		// Move it to return value
		for( String key : compareViewMap.keySet() )
		{
			FileViewObject toView = compareViewMap.get( key );
			
			// TODO here we have to distinguish if
			// History - or ACTUELL
			// 	and in both cases
			// RAID5 (else RAID5) 
			
			
			// maybe we will update the hash - and 
			// TODO delete the data - not necessary for viewing
/*
			FileObject[] interfaceInformationFos = toView.getInterfaceInformationFos();
			int ii = 0;
			
			for( ConnectorInterface ci : cis )
			{
				FileObject actFO = interfaceInformationFos[ii];
				
				if( actFO != null )
				{
					FileObject newFO;

					// in RAID5 we do not HASHing
					// we only show the naming parts

					try
					{
						newFO = ci.read( actFO );
						actFO.setHash( newFO.getHash() );
					}
					catch( Exception e )
					{
						log.fine( "An error occured while reading file "+ actFO.getName() 
								+ " from " + ci.getName() + ": " + e.toString() );
					}
					
					interfaceInformationFos[ii] = actFO;			
				}
			
				actFO.setHash( actFO.getMd5() );
				
				interfaceInformationFos[ii] = actFO;			
				
				
				ii++;
			}
	
			toView.setInterfaceInformationFos( interfaceInformationFos );
 */
			
			// TODO here we do another round if we want to duplicate files
			
			ret.add( toView );
		}

/*		
		// add an additional line for showing the Interfaces
		FileViewObject toView = new FileViewObject();
		FileObject global = new FileObject();
		global.setName( " === INTERFACE ===");
		toView.setGlobalFo( global );
		
		FileObject[] interfaceInformationFos = new FileObject[3];
		
		int ii = 0;
		
		for( ConnectorInterface ci : cis )
		{
			FileObject actFO = new FileObject();
			
			actFO.setHash( ci.getName() );
			interfaceInformationFos[ii] = actFO;
			
			ii++;
		}		
		
		toView.setInterfaceInformationFos( interfaceInformationFos );
		
		ret.add( toView );	
 */
		
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
		// here we generate the 3 further files
//		String[] fileNames = this.generateFileNames( fn, <HERE I NEED THE (SIZE % 2) );
		
	    // initialization of connector interfaces
		initConnectorInterface();
		
		ArrayList<FileViewObject> listViewObjects = listFiles();
		FileObject actFileObject = null;
		FileObject[] interfaceInformationFos = null;
		
		for( FileViewObject listViewObject : listViewObjects )
		{
			actFileObject = listViewObject.getGlobalFo();
			
			if( actFileObject.getName().compareTo( fn ) == 0 )
			{
				interfaceInformationFos = listViewObject.getInterfaceInformationFos();
				
				break;
			}
		}
		
		if( interfaceInformationFos == null )
		{
			
		}		

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		int ii = 0;
		
		for( ConnectorInterface ci : connectorInterface )
		{
			FileObject aFileObject = interfaceInformationFos[ii];
			
			if( aFileObject != null )
			{
				String fileName = aFileObject.getName() + fn;
			
				try
				{
					log.fine( "Deleting " + fileName + " from " + ci.getName() + "." );
					ci.delete( new FileObject( fileName ) );
				}
				catch( Exception e )
				{
					log.fine( "Deleting " + fileName + " from " + ci.getName() + " failed: " + e.getMessage() );
					throw new IOException( e );
				}
				
				log.fine( "File " + fileName + "deleted from " + ci.getName() + "." );
			}
			else
			{
				log.fine( "File " + fn + " can't be found @ " + ci.getName() + "." );
			}
		}

		ii++;
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
	{
		// RAID5 LOGIK
		FileObject readFile;
		FileObject[] fileObjects = new FileObject[3];
		
	    // initialization of connector interfaces
		initConnectorInterface();

		
		ArrayList<FileViewObject> listViewObjects = listFiles();
		FileObject actFileObject = null;
		FileObject[] interfaceInformationFos = null;
		
		for( FileViewObject listViewObject : listViewObjects )
		{
			actFileObject = listViewObject.getGlobalFo();
			
			if( actFileObject.getName().compareTo( fn ) == 0 )
			{
				interfaceInformationFos = listViewObject.getInterfaceInformationFos();
				
				break;
			}
		}
		
		
		if( interfaceInformationFos == null )
		{
			
		}
		
		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		int ii = 0;
		
		// Here we have the problem - that the filename fn is only a part
		// of the downloadable files!
		// We have to generate a listFile to determine where which file is download able!
		
		for( ConnectorInterface ci : connectorInterface )
		{
			FileObject aFileObject = interfaceInformationFos[ii];
			
			if( aFileObject != null )
			{
				String fileName = aFileObject.getName() + fn;
				
				try
				{
					log.fine( "Read" + fn + "from " + ci.getName() + "." );
					fileObjects[ii] = ci.read( new FileObject( fileName ) );
				}
				catch( Exception e )
				{
					log.fine( "Reading from " + ci.getName() + " failed" + e.getMessage() );
					throw new IOException( e );
				}
			
				log.fine( "File " + fileName + " read from " + ci.getName() + "." );
			}
			else
			{
				log.fine( "File " + fn + " can't be found @ " + ci.getName() + "." );
			}

			ii++;
		}
		
		readFile = reconstructFile( fileObjects );

		return readFile;
	}

	/**
	 * Stores the file in at least TWO connectors
	 * 
	 * @param f
	 * @throws IOException
	 *             if no write operation success
	 *
	 */

	public synchronized void write( FileObject f ) throws IOException
	{
		// TODO IMPLEMENT RAID5 LOGIK
		int b = 0;
		
	    // initialization of connector interfaces
		initConnectorInterface();
	    
	    FileObject[] generatedFiles = generateFiles( f );
	    


	    ArrayList<FileObject> aList = new ArrayList<FileObject>( Arrays.asList( generatedFiles ) );
	    
	    // TODO here we should manage some randomness
	    long mil = System.currentTimeMillis();
	    long first = mil % 3;	// 0, 1, 2
	    long second = mil % 2;	// 0, 1 
	    
	    ArrayList<FileObject> newList = new ArrayList<FileObject>();
	    
	    newList.add( aList.get( (int) first ) );
	    aList.remove( (int) first );
	    newList.add( aList.get( (int) second ) );
	    aList.remove( (int) second );
	    newList.add( aList.get( 0 ) );
	    
	    
	    
	    int index = 0;

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : connectorInterface )
		{
			FileObject writeFO = newList.get( index );
			
			try
			{
				log.fine( "Write" + writeFO.getName() + " to " + ci.getName() );
				ci.create( writeFO );
			}
			catch( Exception e )
			{
				b = b + 1;
				log.fine( "Write problem at Interface" + ci.getName() + " Error" + e.getMessage() );
				e.printStackTrace();
			}
			
			log.fine( "Write" + writeFO.getName() + " to " + ci.getName() + " ... OK." );
			
			index++;
		}
	}
}