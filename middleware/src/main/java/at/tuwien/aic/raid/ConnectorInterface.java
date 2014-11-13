package at.tuwien.aic.raid;

import at.tuwien.aic.raid.data.FileObject;

/**
 * In the future we will need additional features.
 * 
 * mkDir( FileObject )
 * rmDir( FileObject )
 * lsContent( FileObject )
 *  
 * @author Holosound
 *
 */
public interface ConnectorInterface
{
	public void create( FileObject file );
	public FileObject read( FileObject name );  // prototype pattern
	public void update( FileObject file );
	public void delete( FileObject file );

}
