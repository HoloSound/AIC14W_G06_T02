package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;

import at.tuwien.aic.raid.data.FileObject;

/**
 * In the future we will need additional features.
 * 
 * mkDir( FileObject )
 * rmDir( FileObject )
 * lsContent( FileObject )
 *  
 * @author Schwarzinger Rainer aka Holosound
 *
 */
public interface ConnectorInterface
{
	public void create( FileObject file ) throws Exception;
	public FileObject read( FileObject name ) throws Exception;  // prototype pattern
	public void update( FileObject file ) throws Exception;
	public void delete( FileObject file ) throws Exception;
	public ArrayList<FileObject> listFiles() throws Exception;
	public String getName();

}
