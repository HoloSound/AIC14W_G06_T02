package at.tuwien.aic.raid.sessionbean;

import java.io.IOException;

import javax.ejb.Local;

import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.Raid1DTO;

@Local
public interface RaidSessionBeanInterface {
	/**
	 * 
	 * @return file object list saved in the raid storage does not retrieve the
	 *         file content eg data is set to null
	 * 
	 */
	Raid1DTO listFiles() throws IOException;
	Raid1DTO getFileHistory( String fn ) throws IOException;

	Raid1DTO getFileInfo(String fn);
	Raid1DTO copyFile(String fn, String from2, String to2);
	
	/**
	 * 
	 * @param file name
	 * @return file with content
	 * @throws IOException
	 *             
	 */
	FileObject getFile(String fn) throws IOException;

	void write(FileObject f)throws IOException;
	void delete(String fn) throws IOException;

}
