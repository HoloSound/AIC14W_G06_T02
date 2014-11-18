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
	private byte[] data;
	private boolean isFile = false;
	private boolean isDirectory = false;
	

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

	public byte[] getData()
	{
		return data;
	}

	public void setData( byte[] data )
	{
		this.data = data;
	}
	
	private boolean compare( byte[] byteArray )
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
	
	/*
	 * 
	 * 	
	private byte[] toPrimitives( Byte[] oBytes )
	{

		byte[] bytes = new byte[oBytes.length];

		for( int i = 0 ; i < oBytes.length ; i++ )
		{
			bytes[i] = oBytes[i];
		}
		
		return bytes;

	}

	private Byte[] toObjects( byte[] bytesPrim )
	{

		Byte[] bytes = new Byte[bytesPrim.length];
		int i = 0;

		for( byte b : bytesPrim )
		{
			bytes[i++] = b; // Autoboxing
		}

		return bytes;

	}
	 * 
	 */
	
	public boolean isFile()
	{
		return this.isFile;
	}

	public boolean isDirectory()
	{
		return this.isDirectory;
	}

	public void setIsFile()
	{
		this.isFile = true;
		this.isDirectory = false;
	}

	public void setIsDirectory()
	{
		this.isDirectory = true;
		this.isFile = false;
	}
}
