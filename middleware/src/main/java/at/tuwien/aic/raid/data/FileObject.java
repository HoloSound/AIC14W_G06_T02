package at.tuwien.aic.raid.data;

/**
 * In the future there we will need additional parameter
 * 
 * isFile()
 * isDirectory()
 * 			... a directory holds no data!
 * ... determine the icon on UI
 * 
 * getSize()
 * getHash()
 * getModificationTime()
 * getCreationTime()
 * 
 * @author Holosound
 *
 */
public class FileObject
{
	private String name;
	private Byte[] data;

	public FileObject( String name )
	{
		this.name = name;

	}

	public FileObject()
	{
		// TODO Auto-generated constructor stub
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public Byte[] getData()
	{
		return data;
	}

	public void setData( Byte[] data )
	{
		this.data = data;
	}
	
	private boolean compare( Byte[] byteArray )
	{
		boolean ret = true;
		
		for( int ii = 0 ; ii < this.data.length ; ii ++ )
		{
			if( byteArray[ii] != this.data[ii] )
			{
				ret = false;
				break;
			}
		}
		
		return ret;
	}
	
	public boolean compare( FileObject secondFo )
	{
		boolean ret = false;
		
		if( this.name.compareTo( secondFo.getName() ) == 0 
				&& compare( secondFo.getData() ) == true )
		{
			ret = true;
		}
		
		return ret;
	}
}
