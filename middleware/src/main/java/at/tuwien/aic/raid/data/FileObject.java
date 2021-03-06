package at.tuwien.aic.raid.data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	private String hash = null;
	

	public FileObject( String name )
	{
		this.name = name;

	}

	public FileObject()
	{
		// TODO Auto-generated constructor stub
	}

	public FileObject( FileObject f )
	{
		// TODO Auto-generated constructor stub
		this.name = f.getName();
		this.data = f.getData();
		// this.isFile = f.getIsFile();
		// this.isDirectory = f.getIsDirectory();
		this.hash = f.getHash();
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
	
	private boolean compareData( byte[] byteArray )
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
	
	public void showHexData()
	{
		int ii = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append( String.format( "%08X: ", ii ) );
		
		for( byte b : data )
		{		
			if( ii != 0 && ii % 16 == 0 )
			{
				System.out.println( sb.toString() );
				
				sb = new StringBuilder();
				sb.append( String.format( "%08X: ", ii ) );
			}

			sb.append( String.format( "%02X ", b ) );
			
			ii++;
		}
		
		if( ! sb.toString().isEmpty() )
		{
			System.out.println( sb.toString() );
		}			
	}
	
	public boolean compare( FileObject secondFo )
	{
		boolean ret = false;
		
		if( this.name.compareTo( secondFo.getName() ) == 0 
				&& compareData( secondFo.getData() ) )
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
	
	public String getMd5() {
		if(data == null) {
			return null;
		}
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		
		byte[] digest = md.digest(data);
		
		BigInteger bigInt = new BigInteger(1,digest);
		String md5hash = bigInt.toString(16);
		while(md5hash.length() < 32 ){
		  md5hash = "0"+md5hash;
		}
		
		return md5hash;
	}

	public String getHash()
	{
		return hash;
	}

	public void setHash( String hash )
	{
		this.hash = hash;
	}
}
