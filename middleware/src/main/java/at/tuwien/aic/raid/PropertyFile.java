package at.tuwien.aic.raid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertyFile 
{
	private static String propertyFileName = null;
	
	public String getPropertyFileName() {
		return propertyFileName;
	}

	public void setPropertyFileName( String propertyFileName )
	{
		PropertyFile.propertyFileName = propertyFileName;
	}

	public String getProperty( String name )
	{
		Properties properties = new Properties();
		InputStream input = null;
		String value = null;
		
		try
		{
			input = new FileInputStream( propertyFileName );
			
			// load a properties file
			properties.load( input );
			
			// get the properties
			value = properties.getProperty( name );
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();			
		}
		finally
		{
			if( input != null )
			{
				try
				{
					input.close();
				}
				catch( IOException ioe )
				{
					ioe.printStackTrace();
				}
			}
		}
		
		return value;
	}
	
	public void addProperty( String name, String value )
	{
		Properties properties = new Properties();
		OutputStream output = null;
		
		try
		{
			output = new FileOutputStream( propertyFileName );
			
			// set the properties value
			properties.setProperty( name, value );
			
			// save properties to project root folder
			properties.store( output, null );
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
		finally
		{
			if( output != null )
			{
				try
				{
					output.close();
				}
				catch( IOException ioe )
				{
					ioe.printStackTrace();
				}
			}
		}
	}
}

