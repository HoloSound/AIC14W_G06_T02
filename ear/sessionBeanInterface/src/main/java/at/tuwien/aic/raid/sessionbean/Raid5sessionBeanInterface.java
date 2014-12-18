
package at.tuwien.aic.raid.sessionbean;
import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Local;

import at.tuwien.aic.raid.data.FileObject;

@Local
public interface Raid5sessionBeanInterface {
	/**
	 * 
	 * @return file object list saved in the raid storage does not retrieve the
	 *         file content eg data is set to null
	 * 
	 */
	ArrayList<FileObject> listFiles() throws IOException;

	void delete(String fn) throws IOException;

	/**
	 * 
	 * @param file name
	 * @return file with content
	 * @throws IOException
	 *             
	 */
	FileObject getFile(String fn) throws IOException;

	void write(FileObject f)throws IOException;
}
