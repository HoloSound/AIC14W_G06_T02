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
			System.out.println("delete" + fn + "from box");
			box.delete(new FileObject(fn));
			
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		try {
			System.out.println("delete" + fn + "from s3");
			s3.delete(new FileObject(fn));
			
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		try {
			System.out.println("delete" + fn + "from dropbox");
			dbox.delete(new FileObject(fn));
			
		} catch (Exception e) {
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
		try {
			System.out.println("getFile" + fn);
			return box.read(new FileObject(fn));

		} catch (Exception e) {

			throw new IOException(e);
		}

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
