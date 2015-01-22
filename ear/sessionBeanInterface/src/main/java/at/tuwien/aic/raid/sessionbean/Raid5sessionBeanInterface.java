
package at.tuwien.aic.raid.sessionbean;
import java.io.IOException;

import javax.ejb.Local;

import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.Raid5DTO;

@Local
public interface Raid5sessionBeanInterface {
	/**
	 * 
	 * @return file object list saved in the raid storage does not retrieve the
	 *         file content eg data is set to null
	 * 
	 */
	Raid5DTO listFiles() throws IOException;
	Raid5DTO getFileHistory(String fn) throws IOException;
	
	void delete(String fn) throws IOException;
	void deleteHistory(String fn) throws IOException;
	
	/**
	 * 
	 * @param file name
	 * @return file with content
	 * @throws IOException
	 *             
	 */
	FileObject getFile(String fn) throws IOException;
	FileObject getHistoryFile(String fn) throws IOException;

	void write(FileObject f)throws IOException;
}
