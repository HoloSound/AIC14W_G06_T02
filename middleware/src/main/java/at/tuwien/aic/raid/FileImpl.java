package at.tuwien.aic.raid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import at.tuwien.aic.raid.data.FileObject;

/**
 * The FileImpl is a connector which works with files in a local diectory.
 * 
 * @author Schwarzinger Rainer aka Holosound
 *
 */

public class FileImpl 
		implements ConnectorInterface
{
	// directory is RAID level specific
	private File baseDirectory = new File( "c:/tmp/FILE/RAID1/" );

	
	public FileImpl( File baseDirectory )
	{
		super();
		this.baseDirectory = baseDirectory;
	}

	public FileImpl()
	{
		// TODO Auto-generated constructor stub
	}


	/**
	 * creates a FileObject
	 */
	@Override
	public void create( FileObject file )
	{
		/**
		 * check if baseDirecory exists
		 */
		if( !baseDirectory.isDirectory() )
		{
			baseDirectory.mkdirs();
		}

		/**
		 * check file existence
		 */
		File absPath = new File( baseDirectory, file.getName() );

		if( absPath.isFile() )
		{
			/**
			 * we delete it to be idempotent
			 */
			absPath.delete();
		}

		/**
		 * write data to file
		 */
		OutputStream os = null;

		try
		{
			os = new FileOutputStream( absPath );

			byte[] data = file.getData();
			os.write( data, 0, data.length );
		}
		catch( FileNotFoundException fnfe )
		{
			fnfe.printStackTrace();
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
		finally
		{
			if( os != null )
			{
				try
				{
					os.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public FileObject read( FileObject name )
	{
		// TODO Auto-generated method stub
		/**
		 * check file existence
		 */
		File absPath = new File( baseDirectory, name.getName() );

		if( absPath.isFile() )
		{
			InputStream is = null;
			byte[] buffer;

			try
			{
				is = new FileInputStream( absPath );
				buffer = IOUtils.toByteArray( is );

				name.setData( buffer );
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				if( is != null )
				{
					try
					{
						is.close();
					}
					catch( IOException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		// TODO else file not found exception?

		return null;
	}

	@Override
	public void update( FileObject file )
	{
		delete( file );		// return - dont care
		create( file );
	}

	@Override
	public void delete( FileObject file )
	{
		File absPath = new File( baseDirectory, file.getName() );

		if( absPath.isFile() )
		{
			/**
			 * we delete it to be idempotent
			 */
			absPath.delete();
		}
	}
	
	@Override
	public ArrayList<FileObject> listFiles() {
		return null;
	}

}
