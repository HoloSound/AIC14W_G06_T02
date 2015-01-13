package at.tuwien.aic.raid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.tuwien.aic.raid.connector.ConnectorConstructor;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;

public class Raid1 {

	java.util.logging.Logger log = java.util.logging.Logger.getLogger("Raid1");

	ConnectorInterface dbox = ConnectorConstructor.dropBoxInstance();
	// static
	ConnectorInterface box = ConnectorConstructor.boxInstance();
	ConnectorInterface s3 = ConnectorConstructor.s3Instance();

	private ConnectorInterface[] connectorInterface = null;
	private String NON_EXISTENT = "========";
	
	
	public Raid1() {
		log.fine("NEW Raid1");
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

	/**
	 * Compares the content of all connectors and try to fix missing files If
	 * three is a file missing in a connector restore from other connectors
	 * 
	 * @return list of files which should be consistent on every connector
	 * @throws IOException
	 *             If ALL connectors fail. Log all other exception
	 * 
	 */

	public synchronized ArrayList<FileViewObject> listFiles() throws IOException {
		ArrayList<FileViewObject> ret = new ArrayList<FileViewObject>();

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
				log.fine("Querying files from " + ci.getName() + ".");

				fileObjectList = ci.listFiles();
			} catch (Exception e) {
				errorCount++;
				log.fine("Querying files from " + ci.getName() + " failed: " + e.getMessage());
				throw new IOException(e);
			}

			log.fine("Got " + fileObjectList.size() + " files from " + ci.getName() + ".");

			// Now we build up the matrix using fileObjectList
			for (FileObject aFO : fileObjectList) {
				// we take the filename
				String aFileName = aFO.getName();

				// in RAID1 we do NOT show RAID5 files!
				Pattern p = Pattern.compile( "[HLP][01]_.*" );
				Matcher m = p.matcher( aFileName );
				boolean b = m.matches();
				
				if( b == false )
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

		// Move it to return value
		for (String key : compareViewMap.keySet()) {
			FileViewObject toView = compareViewMap.get(key);

			// TODO here we have to distinguish if
			// History - or ACTUELL
			// and in both cases
			// RAID5 (else RAID5)

			// maybe we will update the hash - and
			// TODO delete the data - not necessary for viewing
			FileObject[] interfaceInformationFos = toView.getInterfaceInformationFos();

			toView.setInterfaceInformationFos(interfaceInformationFos);

			// TODO here we do another round if we want to duplicate files

			ret.add(toView);
		}

		/*
		 * // add an additional line for showing the Interfaces FileViewObject
		 * toView = new FileViewObject(); FileObject global = new FileObject();
		 * global.setName( " === INTERFACE ==="); toView.setGlobalFo( global );
		 * 
		 * FileObject[] interfaceInformationFos = new FileObject[3];
		 * 
		 * int ii = 0;
		 * 
		 * for( ConnectorInterface ci : cis ) { FileObject actFO = new
		 * FileObject();
		 * 
		 * actFO.setHash( ci.getName() ); interfaceInformationFos[ii] = actFO;
		 * 
		 * ii++; }
		 * 
		 * toView.setInterfaceInformationFos( interfaceInformationFos );
		 * 
		 * ret.add( toView );
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
	public synchronized void delete(String fn) throws IOException {
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
				log.fine("Deleting" + fn + "from " + ci.getName() + ".");
				ci.delete(new FileObject(fn));
			} catch (Exception e) {
				log.fine("Deleting from " + ci.getName() + " failed" + e.getMessage());
				throw new IOException(e);
			}

			log.fine("File " + fn + "deleted from " + ci.getName() + ".");
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
			// System.out.println("getFile" + fn);
			boxFile = box.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file " + fn + " from Box: " + e.toString());
		}

		try {
			dboxFile = dbox.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file \"" + fn + "\" from DropBox: " + e.toString());
		}

		try {
			s3File = s3.read(readFile);

		} catch (Exception e) {
			log.fine("An error occured while reading file " + fn + " from S3: " + e.toString());
		}

		if (boxFile == null && dboxFile == null && s3File == null) {
			log.fine("Couldn't read the file \"" + fn + "\" from all connectors.");
			throw new IOException("I/O Error");
		}

		String boxFileMd5 = null;
		String dboxFileMd5 = null;
		String s3FileMd5 = null;

		if (boxFile != null) {
			boxFileMd5 = boxFile.getMd5();
			log.fine("################### HASH VALUE FOR BOX ###################");
			log.fine("File \"" + fn + "\": BoxMD5: " + boxFileMd5);
			log.fine("##########################################################");
			returnFile = boxFile;
		}

		if (dboxFile != null) {
			dboxFileMd5 = dboxFile.getMd5();
			log.fine("################### HASH VALUE FOR DBOX ###################");
			log.fine("File \"" + fn + "\": DropBoxMD5: " + dboxFileMd5);
			log.fine("###########################################################");
			returnFile = dboxFile;
		}

		if (s3File != null) {
			s3FileMd5 = s3File.getMd5();
			log.fine("################### HASH VALUE FOR S3 ###################");
			log.fine("File \"" + fn + "\": S3MD5: " + s3FileMd5);
			log.fine("#########################################################");
			returnFile = s3File;
		}

		// if all hashvalues are available
		if (boxFileMd5 != null && dboxFileMd5 != null && s3FileMd5 != null) {
			// proof if all hashvalues are the same if not proof if two are the
			// same and restore and if all three are different raise exception
			if (boxFileMd5.equals(dboxFileMd5) && boxFileMd5.equals(s3FileMd5)) {
				log.fine("File: " + fn + ": All three hashvalues are the same: " + boxFileMd5);
			} else if (boxFileMd5.equals(dboxFileMd5)) {
				returnFile = boxFile;

				log.fine("############ Inconsistency ############");
				log.fine("Inconsistency! S3 diffs to the other two hashes: S3: " + s3FileMd5 + " Others: " + boxFileMd5 + " trying to restore...");
				log.fine("#######################################");
				// restore s3
				try {
					s3.delete(new FileObject(fn));
					s3.create(boxFile);

				} catch (Exception e) {
					log.fine("Restoring of File \"" + fn + "\" on S3 failed" + e.getMessage());
				}

			} else if (boxFileMd5.equals(s3FileMd5)) {
				returnFile = boxFile;

				log.fine("############ Inconsistency ############");
				log.fine("Inconsistency! DropBox diffs to the other two hashes: DropBox: " + dboxFileMd5 + " Others: " + boxFileMd5 + " trying to restore...");
				log.fine("#######################################");
				// restore dbox
				try {
					dbox.delete(new FileObject(fn));
					dbox.create(boxFile);

				} catch (Exception e) {
					log.fine("Restoring of File \"" + fn + "\" on DropBox failed" + e.getMessage());
				}

			} else if (dboxFileMd5.equals(s3FileMd5)) {
				returnFile = dboxFile;

				log.fine("############ Inconsistency ############");
				log.fine("Inconsistency! Box diffs to the other two hashes: Box: " + s3FileMd5 + " Others: " + dboxFileMd5 + " trying to restore...");
				log.fine("#######################################");
				// restore box
				try {
					box.delete(new FileObject(fn));
					box.create(dboxFile);

				} catch (Exception e) {
					log.fine("Restoring of File \"" + fn + "\" on Box failed" + e.getMessage());
				}

			} else {
				log.fine("Inconsistency! All three hashvalues are different! Box: " + boxFileMd5 + " DropBox: " + dboxFileMd5 + " S3: " + s3FileMd5);
				throw new IOException("Inconsistency! All three hashvalues are different! Box: " + boxFileMd5 + " DropBox: " + dboxFileMd5 + " S3: " + s3FileMd5);
			}

		} else {
			if (boxFileMd5 != null && dboxFileMd5 != null) {
				if (!boxFileMd5.equals(dboxFileMd5)) {
					log.fine("File \"" + fn + "\" has inconsistency! Box MD5: " + boxFileMd5 + " DropBox MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! Box MD5: " + boxFileMd5 + " DropBox MD5: " + dboxFileMd5);
				}
			}

			if (boxFileMd5 != null && s3FileMd5 != null) {
				if (!boxFileMd5.equals(s3FileMd5)) {
					log.fine("File \"" + fn + "\" has inconsistency! Box MD5: " + boxFileMd5 + " S3 MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! Box MD5: " + boxFileMd5 + " S3 MD5: " + s3FileMd5);
				}
			}

			if (dboxFileMd5 != null && s3FileMd5 != null) {
				if (!dboxFileMd5.equals(s3FileMd5)) {
					log.fine("File \"" + fn + "\" has inconsistency! DropBox MD5: " + boxFileMd5 + " S3 MD5: " + dboxFileMd5);
					throw new IOException("Inconsistency! DropBox MD5: " + dboxFileMd5 + " S3 MD5: " + s3FileMd5);
				}
			}

		}

		log.fine("Successfully read file \"" + fn + "\"");

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
		int b = 0;

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
				log.fine("Write" + f.getName() + " to " + ci.getName());
				ci.create(f);
			} catch (Exception e) {
				b = b + 1;
				log.fine("Error" + e.getMessage());
				e.printStackTrace();
			}

			log.fine("Write" + f.getName() + " to " + ci.getName() + " ... OK.");
		}

		if (b == 3) {
			throw new IOException("Faild: The file is not stored in any of the connector!");
		}
	}
	
	private void addTwoLinks( StringBuffer b, String file, 
			int from, boolean fromIsEmpty,
			int to, boolean toIsEmpty )
	{
		b.append("&nbsp;");	
		
System.out.println( "File: " + file + " FROM: " + from + " | " + fromIsEmpty 
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
				+ "&file=" + file + "\">" );
		
		if( isLeft == true )
			b.append( "<img src=\"/web/pic/copy_left.png\" alt=\"copy left\" />");
		else
			b.append( "<img src=\"/web/pic/copy_right.png\" alt=\"copy right\" />");
			
		b.append("</a>");			
	}
	

	public String getFileInfo(String fn) {
		try {
			StringBuffer b = new StringBuffer();
			
		    // initialization of connector interfaces
			initConnectorInterface();
			
			int ii = 0;
			String hashValues[] = new String[3];
			
			for( ConnectorInterface ci : connectorInterface ) {
				try {
					FileObject actFileObject = new FileObject(fn);
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
			
			// now we are generating some exchange Buttons

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
System.out.println(  "PRE: " + previousHashValue + " --> ACT: " + actHashValue );
					if( previousHashValue.compareTo( actHashValue ) != 0  )
					{
						// generate "<" and ">" button
						addTwoLinks( b, fn, previousId, previousIsEmpty, actId, actIsEmpty );
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
					addTwoLinks( b, fn, previousId, previousIsEmpty, firstId, firstIsEmpty );
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
			
			return	b.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "error:"+e.getMessage();
		}
		
	}
}
