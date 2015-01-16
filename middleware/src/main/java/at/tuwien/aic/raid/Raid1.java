package at.tuwien.aic.raid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.aic.raid.connector.ConnectorConstructor;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
import at.tuwien.aic.raid.data.Raid1DTO;

public class Raid1 {

	java.util.logging.Logger log = java.util.logging.Logger.getLogger("Raid1");
	
	private static final String DATE_TIME_PREFIX_FORMAT = "yyyyMMdd_HHmmss_";
	private boolean writeHistory = true;

	ConnectorInterface dbox = ConnectorConstructor.dropBoxInstance();
	// static
	ConnectorInterface box = ConnectorConstructor.boxInstance();
	ConnectorInterface s3 = ConnectorConstructor.s3Instance();

	private ConnectorInterface[] connectorInterface = null;
	private String[] connectorNames = null;
	
	private String NON_EXISTENT = "========";
	
	
	public Raid1() 
	{
		log("NEW RAID 1");
	}

	private void log(String string) 
	{
		log.log(Level.INFO,string);	
	}

	public int getMaxId() {
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
		
		if( connectorNames == null )
		{
			connectorNames = new String[3];
			
			for( int ii = 0 ; ii < this.getMaxId() ; ii++ )
			{
				connectorNames[ii] = this.getInterface( ii ).getName();
			}
		}
	}
	
	public ConnectorInterface getInterface(int ii) {
		if (ii == 0) {
			return (ConnectorInterface) dbox;
		} else if (ii == 1) {
			return (ConnectorInterface) box;
		} else {
			return (ConnectorInterface) s3;
		}
	}
	
	
	private HashMap<String, FileViewObject> buildListFileMap()
			throws IOException 
	{
		HashMap<String, FileViewObject> compareViewMap = new HashMap<String, FileViewObject>();
		HashMap<String, ConnectorInterface> sourceIF = new HashMap<String, ConnectorInterface>();
	
		// FUTURE CODING STYLE
		// definition of connector interfaces
	    // initialization of connector interfaces
		initConnectorInterface();

		ArrayList<FileObject> fileObjectList;
		int errorCount = 0;
		int interfaceId = 0; // running from 0 .. 2

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : connectorInterface ) {
			try {
				log("  Querying files from " + ci.getName() + ".");

				fileObjectList = ci.listFiles();
			} catch (Exception e) {
				errorCount++;
				log("  Querying files from " + ci.getName() + " failed: " + e.getMessage());
				throw new IOException(e);
			}

			log("  Got " + fileObjectList.size() + " files from " + ci.getName() + ".");

			// Now we build up the matrix using fileObjectList
			for (FileObject aFO : fileObjectList) {
				// we take the filename
				String aFileName = aFO.getName();

				// in RAID1 we do NOT show RAID5 files!
				Pattern p1 = Pattern.compile( "[HLP][01]_.*" );
				Matcher m1= p1.matcher( aFileName );
				boolean b1 = m1.matches();
							
				if( b1 == false )
				{
					FileViewObject foundViewObject = compareViewMap.get(aFileName);
	
					if (foundViewObject == null) {
						// we have a new file here - create a new entry
						FileViewObject aNewEntry = new FileViewObject();
	
						aNewEntry.setGlobalFo(aFO);
	
						FileObject[] interfaceInformationFos = new FileObject[3];
						interfaceInformationFos[interfaceId] = aFO;
	
						aNewEntry.setInterfaceInformationFos(interfaceInformationFos);
	
						compareViewMap.put(aFileName, aNewEntry);
						sourceIF.put(aFileName, ci);
					} else {
						// entry exists - append data
						FileObject[] interfaceInformationFos = foundViewObject.getInterfaceInformationFos();
	
						interfaceInformationFos[interfaceId] = aFO;
	
						foundViewObject.setInterfaceInformationFos(interfaceInformationFos);
						sourceIF.put(aFileName, ci);
					}
				}
			}

			interfaceId++;
		}

		if (errorCount == 3) {
			throw (new IOException("No connection available."));
		}		
		
		return compareViewMap;
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

	public synchronized Raid1DTO listFiles() 
			throws IOException 
	{
		Raid1DTO ret = new Raid1DTO();
		ArrayList<FileViewObject> dataRow = new ArrayList<FileViewObject>();

		log( "listFiles(): ");
		
		HashMap<String, FileViewObject> compareViewMap = buildListFileMap();

		// Move it to return value
		for (String key : compareViewMap.keySet()) 
		{
			FileViewObject toView = compareViewMap.get(key);

			// here we have to distinguish if
			// History - or ACTUELL
			// and in both cases
			// RAID1 (else RAID5)
			
			// in RAID1 we do NOT show HISTORY files!
			Pattern p2 = Pattern.compile( "[2][0-9][0-9][0-9][0-1][0-9][0-3][0-9]_[0-2][0-9][0-5][0-9][0-5][0-9]_.*" );
			Matcher m2 = p2.matcher( toView.getGlobalFo().getName() );
			boolean b2 = m2.matches();				
			
			if( b2 == false )
			{		
				// maybe we will update the hash - and

				FileObject[] interfaceInformationFos = toView.getInterfaceInformationFos();
	
				toView.setInterfaceInformationFos(interfaceInformationFos);
				
// TODO:
//				toView.setInterfaceNames( connectorNames );
				// TODO here we do another round if we want to duplicate files
				dataRow.add(toView);
			}
			// we do not take data - if not necessary for viewing
		}

		log( "listFiles(): returning " + dataRow.size() + " datasets.");
		
		ret.setFileViewObjects( dataRow );
		ret.setInterfaceNames( connectorNames );
		
		return ret;
	}

	
	public Raid1DTO getFileHistory( String fn )
				throws IOException 
	{
		Raid1DTO ret = new Raid1DTO();
		ArrayList<FileViewObject> dataRow = new ArrayList<FileViewObject>();

		log( "getFileHistory( " + fn + " ): ");
		
		HashMap<String, FileViewObject> compareViewMap = buildListFileMap();

		// Move it to return value
		for (String key : compareViewMap.keySet()) 
		{
			FileViewObject toView = compareViewMap.get(key);
			String keyFileName = toView.getGlobalFo().getName();
			
			log( "getFileHistory(): checking " + keyFileName );
			

			// here we have to distinguish if
			// History - or ACTUELL
			// and in both cases
			// RAID1 (else RAID5)
			
			// in RAID1 we do NOT show HISTORY files!
			Pattern p2 = Pattern.compile( "[2][0-9][0-9][0-9][0-1][0-9][0-3][0-9]_[0-2][0-9][0-5][0-9][0-5][0-9]_.*" );
			Matcher m2 = p2.matcher( toView.getGlobalFo().getName() );
			boolean b2 = m2.matches();				
			
			if( b2 == true )
			{		
				// maybe we will update the hash - and
				FileObject globalFileObject = toView.getGlobalFo();
				String fileName = globalFileObject.getName();
				
				String readFileName = fileName.substring( 16 );
				
				if( readFileName.compareTo( fn ) == 0 )
				{
					// TODO:					
					// toView.setInterfaceNames( connectorNames );
					dataRow.add( toView );
				}
			}
			// we do not take data - if not necessary for viewing
		}

		log( "getFileHistory(): returning " + dataRow.size() + " datasets.");
		
		ret.setFileViewObjects( dataRow );
		ret.setInterfaceNames( connectorNames );
		
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
	public synchronized void delete( String fileName ) throws IOException {
		// definition of connector interfaces
		ConnectorInterface[] cis = new ConnectorInterface[3];

		for (int ii = 0; ii < this.getMaxId(); ii++) {
			cis[ii] = this.getInterface(ii);
		}

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for( ConnectorInterface ci : cis )
		{
			try
			{
				log( "Deleting " + fileName + " from " + ci.getName() + "." );
				ci.delete( new FileObject( fileName ) );
			}
			catch( FileNotFoundException e )
			{
				log( "Deleting " + fileName + " from " + ci.getName() + " failed: " + e.getMessage() );
			}
			catch( Exception e )
			{
				log( "Deleting from " + ci.getName() + " failed"
						+ e.getMessage() );
				throw new IOException( e );
			}

			log( "File " + fileName + "deleted from " + ci.getName() + "." );
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
	public synchronized FileObject getFile(String fn) throws IOException {// TODO
																			// IMPLEMENT
		// RAID1 LOGIK
		FileObject readFile = new FileObject(fn);

		FileObject boxFile = null;
		FileObject dboxFile = null;
		FileObject s3File = null;

		FileObject returnFile = null;

		try {
			// log( "getFile" + fn );
			boxFile = box.read(readFile);

		} catch (Exception e) {
			log("An error occured while reading file " + fn + " from Box: " + e.toString());
		}

		try {
			dboxFile = dbox.read(readFile);

		} catch (Exception e) {
			log("An error occured while reading file \"" + fn + "\" from DropBox: " + e.toString());
		}

		try {
			s3File = s3.read(readFile);

		} catch (Exception e) {
			log("An error occured while reading file " + fn + " from S3: " + e.toString());
		}

		if (boxFile == null && dboxFile == null && s3File == null) {
			log("Couldn't read the file \"" + fn + "\" from all connectors.");
			throw new IOException("I/O Error");
		}

		String boxFileMd5 = null;
		String dboxFileMd5 = null;
		String s3FileMd5 = null;

		if (boxFile != null) {
			boxFileMd5 = boxFile.getMd5();
			log("################### HASH VALUE FOR BOX ###################");
			log("File \"" + fn + "\": BoxMD5: " + boxFileMd5);
			log("##########################################################");
			returnFile = boxFile;
		}

		if (dboxFile != null) {
			dboxFileMd5 = dboxFile.getMd5();
			log("################### HASH VALUE FOR DBOX ###################");
			log("File \"" + fn + "\": DropBoxMD5: " + dboxFileMd5);
			log("###########################################################");
			returnFile = dboxFile;
		}

		if (s3File != null) {
			s3FileMd5 = s3File.getMd5();
			log("################### HASH VALUE FOR S3 ###################");
			log("File \"" + fn + "\": S3MD5: " + s3FileMd5);
			log("#########################################################");
			returnFile = s3File;
		}

		// if all hashvalues are available
		if (boxFileMd5 != null && dboxFileMd5 != null && s3FileMd5 != null) {
			// proof if all hashvalues are the same if not proof if two are the
			// same and restore and if all three are different raise exception
			if (boxFileMd5.equals(dboxFileMd5) && boxFileMd5.equals(s3FileMd5)) {
				log("File: " + fn + ": All three hashvalues are the same: " + boxFileMd5);
			} else if (boxFileMd5.equals(dboxFileMd5)) {
				returnFile = boxFile;

				log("############ Inconsistency ############");
				log("Inconsistency! S3 diffs to the other two hashes: S3: " + s3FileMd5 + " Others: " + boxFileMd5 + " trying to restore...");
				log("#######################################");
				// restore s3
				try {
					s3.delete(new FileObject(fn));
					s3.create(boxFile);

				} catch (Exception e) {
					log("Restoring of File \"" + fn + "\" on S3 failed" + e.getMessage());
				}

			} else if (boxFileMd5.equals(s3FileMd5)) {
				returnFile = boxFile;

				log("############ Inconsistency ############");
				log("Inconsistency! DropBox diffs to the other two hashes: DropBox: " + dboxFileMd5 + " Others: " + boxFileMd5 + " trying to restore...");
				log("#######################################");
				// restore dbox
				try {
					dbox.delete(new FileObject(fn));
					dbox.create(boxFile);

				} catch (Exception e) {
					log("Restoring of File \"" + fn + "\" on DropBox failed" + e.getMessage());
				}

			} else if (dboxFileMd5.equals(s3FileMd5)) {
				returnFile = dboxFile;

				log("############ Inconsistency ############");
				log("Inconsistency! Box diffs to the other two hashes: Box: " + s3FileMd5 + " Others: " + dboxFileMd5 + " trying to restore...");
				log("#######################################");
				// restore box
				try {
					box.delete(new FileObject(fn));
					box.create(dboxFile);

				} catch (Exception e) {
					log("Restoring of File \"" + fn + "\" on Box failed" + e.getMessage());
				}

			} else {
				log("Inconsistency! All three hashvalues are different! Box: " + boxFileMd5 + " DropBox: " + dboxFileMd5 + " S3: " + s3FileMd5);
				throw new IOException("Inconsistency! All three hashvalues are different! Box: " + boxFileMd5 + " DropBox: " + dboxFileMd5 + " S3: " + s3FileMd5);
			}

		} else {
			if (boxFileMd5 != null && dboxFileMd5 != null) {
				if (!boxFileMd5.equals(dboxFileMd5)) {
					log("File \"" + fn + "\" has inconsistency! Box MD5: " + boxFileMd5 + " DropBox MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! Box MD5: " + boxFileMd5 + " DropBox MD5: " + dboxFileMd5);
				}
			}

			if (boxFileMd5 != null && s3FileMd5 != null) {
				if (!boxFileMd5.equals(s3FileMd5)) {
					log("File \"" + fn + "\" has inconsistency! Box MD5: " + boxFileMd5 + " S3 MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! Box MD5: " + boxFileMd5 + " S3 MD5: " + s3FileMd5);
				}
			}

			if (dboxFileMd5 != null && s3FileMd5 != null) {
				if (!dboxFileMd5.equals(s3FileMd5)) {
					log("File \"" + fn + "\" has inconsistency! DropBox MD5: " + boxFileMd5 + " S3 MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! DropBox MD5: " + dboxFileMd5 + " S3 MD5: " + s3FileMd5);
				}
			}

		}

		log("Successfully read file \"" + fn + "\"");

		return returnFile;

	}

	/**
	 * Stores  the  file in atleast one connector
	 * 
	 * @param f
	 * @throws IOException if no write operation success 
	 *
	 */

	public synchronized void write(FileObject f) throws IOException {
		// TODO IMPLEMENT RAID1
		// LOGIK
		int baseErrors = 0;
		int historyErrors = 0;
		
		// generate a date_time_prefix	 
	    DateFormat dateFormat = new SimpleDateFormat( DATE_TIME_PREFIX_FORMAT );
	    Date date = new Date();
	    String dateAndTimePrefix = dateFormat.format(date); 
	    FileObject historyFile = new FileObject( f );
		// add date_time_prefix
	    historyFile.setName( dateAndTimePrefix + f.getName() );				
		

		// definition of connector interfaces
		ConnectorInterface[] cis = new ConnectorInterface[3];

		for (int ii = 0; ii < this.getMaxId(); ii++) {
			cis[ii] = this.getInterface(ii);
		}

		// simple implementation:
		// real implementation would run each interface in own thread
		// to parallelize the writing action and minimize the waiting time.
		for (ConnectorInterface ci : cis) {
			try {
				log("Write file: " + f.getName() + " to " + ci.getName());
				ci.create(f);
			} catch (Exception e) {
				baseErrors += 1;
				log("Error" + e.getMessage());
				e.printStackTrace();
			}

			log("Write " + f.getName() + " to " + ci.getName() + " ... OK.");
			
			if( writeHistory )
			{
				try {
					log("Write HISTORY file: " + historyFile.getName() + " to " + ci.getName());
					ci.create(historyFile);
				} catch (Exception e) {
					historyErrors += 1;
					log("Error" + e.getMessage());
					e.printStackTrace();
				}

				log("Write " + historyFile.getName() + " to " + ci.getName() + " ... OK.");				
			}
		}

		if (baseErrors == 3) {
			throw new IOException("Faild: The file is not stored in any of the connector!");
		}
		
		if (historyErrors == 3) {
			throw new IOException("Faild: The history file is not stored in any of the connector!");
		}
	}
	
	
	
	
	private void addTwoLinks( StringBuffer b, String file, 
			int from, boolean fromIsEmpty,
			int to, boolean toIsEmpty )
	{
		b.append("&nbsp;");	
		
log( "File: " + file + " FROM: " + from + " | " + fromIsEmpty 
		+ " TO: " + to + " | " + toIsEmpty );
		
		if( toIsEmpty == false )
			addLink( b, file, to, from, true );
		
		b.append("&nbsp;|&nbsp;");	
		
		if( fromIsEmpty == false )
			addLink( b, file, from, to, false );
		
		b.append("&nbsp;");	
	}
	
	private void addLink( StringBuffer b, String file, int from, int to, boolean isLeft )
	{
		b.append("<a target='_blank' href=\"raid1?task=copy&from=" + from  
				+ "&to=" + to 
				+ "&fileName=" + file + "\">" );
		
		if( isLeft == true )
			b.append( "<img src=\"/web/pic/copy_left.png\" alt=\"copy left\" />");
		else
			b.append( "<img src=\"/web/pic/copy_right.png\" alt=\"copy right\" />");
			
		b.append("</a>");			
	}
	
	private void generateCopyButtons( StringBuffer b, String fileName, String[] hashValues )
	{
		String firstHashValue = null;
		String actHashValue = null;
		String previousHashValue = null;
		String firstConnectorInterfaceName = null;
		int actId = 0;
		int firstId = -1;
		int previousId = -1;
		boolean firstIsEmpty = false;
		boolean actIsEmpty = false;
		boolean previousIsEmpty = false;

		
		b.append("<p>");
		
		for( ConnectorInterface ci : connectorInterface ) 
		{			
			actHashValue = hashValues[actId];
			
			if( actHashValue.compareTo( NON_EXISTENT ) == 0 )
				actIsEmpty = true;
			else
				actIsEmpty = false;
		
			
			if( firstHashValue == null )
			{
				firstHashValue = actHashValue;
				firstId = actId;
				firstConnectorInterfaceName = ci.getName();
				
				if( firstHashValue.compareTo( NON_EXISTENT ) == 0 )
				{
					firstIsEmpty = true;
				}
			}
			
			
			if( previousHashValue != null )
			{
				log(  "PRE: " + previousHashValue + " --> ACT: " + actHashValue );
				if( previousHashValue.compareTo( actHashValue ) != 0  )
				{
					// generate "<" and ">" button
					addTwoLinks( b, fileName, previousId, previousIsEmpty, actId, actIsEmpty );
				}
				else
				{
					b.append("&nbsp;|&nbsp;");							
				}
			}
			else
			{
				b.append("&nbsp;|&nbsp;");							
			}
			
			b.append( "<bold>" + ci.getName() + "</bold>" );
			
			previousHashValue = actHashValue;
			previousId = actId;
			
			if( previousHashValue.compareTo( NON_EXISTENT ) == 0 )
				previousIsEmpty = true;
			else
				previousIsEmpty = false;
			
			actId++;
		}
		
		if( firstHashValue != null )
		{
			if( firstHashValue.compareTo( actHashValue ) != 0  )
			{
				// generate "<" and ">" button
				addTwoLinks( b, fileName, previousId, previousIsEmpty, firstId, firstIsEmpty );
			}
			else
			{
				b.append("&nbsp;|&nbsp;");							
			}
		}
		else
		{
			b.append("&nbsp;|&nbsp;");							
		}
		
		b.append( "<bold>" + firstConnectorInterfaceName + "</bold>&nbsp;|" );
		
		b.append("</p>");		
	}
	
	
	public Raid1DTO getFileInfo( String fileName )
	{
		log( "getFileInfo( " + fileName + " )" );
		
		Raid1DTO ret = new Raid1DTO();
		ArrayList<FileViewObject> dataRow = new ArrayList<FileViewObject>();
		FileViewObject actRow = new FileViewObject();
		
		FileObject actFileObject = new FileObject( fileName );
		actFileObject.setHash( "--------------------------------" );
		actRow.setGlobalFo( actFileObject );
		
		FileObject[] interfaceInformationFos = new FileObject[3];
	
		try
		{
			// initialization of connector interfaces
			initConnectorInterface();

			int interfaceId = 0;

			for( ConnectorInterface ci : connectorInterface )
			{
				FileObject readFileObject = new FileObject( fileName ); 
				readFileObject.setHash( "--------------------------------" );
				
				try
				{		
					try
					{
						readFileObject = ci.read( actFileObject );
	
						if( readFileObject != null )
						{
							if( readFileObject.getMd5() != null )
							{
								String hashValue = readFileObject.getMd5();
								readFileObject.setHash( hashValue );
							}
							else
							{
								readFileObject.setHash( "--------------------------------" );
							}
						}
						else
						{
							readFileObject = actFileObject;
							readFileObject.setHash( "--------------------------------" );
						}

					}
					catch( FileNotFoundException fnfe )
					{
						readFileObject.setHash( "--------------------------------" );
						log( "getFileInfo(): FILE NOT FOUND: " + fnfe.getMessage() );
					}
				}
				catch( Exception e )
				{
					readFileObject.setHash( "--------------------------------" );
					log( "getFileInfo(): ERROR " + e.getMessage() );
				}

				log( "getFileInfo(): Id=" + interfaceId + " | " + readFileObject.getHash() );
				
				interfaceInformationFos[interfaceId] = new FileObject( readFileObject );
				
				interfaceId++;
			}
			
			actRow.setInterfaceInformationFos( interfaceInformationFos );
			dataRow.add( actRow );			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		ret.setFileViewObjects( dataRow );
		ret.setInterfaceNames( connectorNames );

		log( "getFileInfo(): returning " + dataRow.size() + " datasets.");
		
		// HERE WE DEBUGGING the whole dataset
/*		log( "getFileInfo(): " );
		String[] nameList = ret.getInterfaceNames();
		for( String name : nameList )
		{
			log( "name: " + name );
		}
		
		ArrayList<FileViewObject> fvos = ret.getFileViewObjects();
		for( FileViewObject fo : fvos )
		{
			FileObject gfo = fo.getGlobalFo();
			
			log( "name: " + gfo.getName() );
			log( "hash: " + gfo.getHash() );
			
			FileObject[] interfaceInfos = fo.getInterfaceInformationFos();
			int ii = 0;
			for( FileObject interfaceInfo : interfaceInfos )
			{
				log( "ID: " + ii + " name: " + interfaceInfo.getName() );
				log( "ID: " + ii + " hash: " + interfaceInfo.getHash() );		
				
				ii++;
			}
		}
		
		log( "getFileInfo(): === END ===" );	
 */	
		
		return ret;
	}

	/*
	public String getFileInfo(String fileName ) {
		try {
			StringBuffer b = new StringBuffer();
			
		    // initialization of connector interfaces
			initConnectorInterface();
			
			int ii = 0;
			String hashValues[] = new String[3];
			
			for( ConnectorInterface ci : connectorInterface ) {
				try {
					FileObject actFileObject = new FileObject( fileName );
					hashValues[ii] = NON_EXISTENT;
					
					try
					{
						FileObject readFileObject = ci.read( actFileObject );
						
						if( readFileObject != null )
						{
							if( readFileObject.getMd5() != null )
							{
								String hashValue = readFileObject.getMd5();
								b.append( "<tt>" + hashValue + "</tt>" );
								hashValues[ii] = hashValue;
							}
							else
							{
								b.append( "<tt>--------------------------------</tt>" );
							}
						}
						else
						{
							b.append( "<tt>--------------------------------</tt>" );
						}
					}
					catch( FileNotFoundException fnfe )
					{
						b.append( "<tt>--------------------------------</tt>" );
					}
					
					b.append(" ");
					b.append( ci.getName() );

				} catch (Exception e) {
					e.printStackTrace();
					b.append(ci.getName()+": error");
				}
				
				b.append("</br>");
				ii++;
			}
			
			boolean differentHashvalues = false;
			String preHash = null;
			
			for( String hashValue : hashValues )
			{
				if( preHash == null )
				{
					preHash = hashValue;
				}
				else
				{
					if( preHash.compareTo( hashValue ) != 0 )
					{
						differentHashvalues = true;
						break;
					}
				}
			}
				
			// now we are generating some exchange Buttons
			if( differentHashvalues )
			{
				generateCopyButtons( b, fileName, hashValues );
			}
			
			return	b.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "error:"+e.getMessage();
		}
		
	}
 */

	public Raid1DTO copyFile( String fn, String fromInterface, String toInterface )
	{
		log( "copyFile(): file: " + fn + ", fromInterface: " + fromInterface + ", toInterface: " + toInterface );
		
		initConnectorInterface();
		
		// convert InterfaceNames to Ids
		int fromId = Integer.parseInt( fromInterface );
		int toId = Integer.parseInt( toInterface );
		
		ConnectorInterface fromConnection = getInterface( fromId );
		ConnectorInterface toConnection = getInterface( toId );
		
		log( "copyFile(): file: " + fn + ", fromInterface: " + fromId + ", toInterface: " + toId );		
		log( "copyFile(): file: " + fn + ", fromInterface: " + fromConnection.getName() + ", toInterface: " + toConnection.getName() );	
		
		FileObject actFileObject = new FileObject( fn );
		
		try
		{
			// first read file
			actFileObject = fromConnection.read( actFileObject );
			
			// second write file
			toConnection.create( actFileObject );		
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return getFileInfo( fn );
	}
}
