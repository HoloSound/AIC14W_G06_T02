package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;

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

	public ArrayList<FileObject> listFiles() throws IOException { // TODO
																	// IMPLEMENT
																	// RAID1


		try {

			return box.listFiles();

		} catch (Exception e) {

			throw new IOException(e);
		}

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
