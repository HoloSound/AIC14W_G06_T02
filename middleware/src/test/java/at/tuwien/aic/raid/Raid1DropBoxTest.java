package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters; //Running test cases in order of method names in ascending order.

import at.tuwien.aic.raid.connector.DropBoxImpl;
import at.tuwien.aic.raid.data.FileObject;

/**
 * Testenvironment of two local directories
 * 
 * TODO: in every Impl and here there are to methods: toPrimitives, toObjects
 * HERE we should not copy these methods!
 * 
 * @author Schwarzinger Rainer aka Holosound
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Raid1DropBoxTest
{
	private DropBoxImpl dropBoxIF = null;


	@Before
	public void init()
	{
		dropBoxIF = new DropBoxImpl();
	}

	private FileObject createFileObject( String filename )
	{
		FileObject fo = new FileObject();
		
		fo.setName( filename );
		
		// TODO ... same in each *Impl object? --> REDESIGN
		File baseDirectory = new File( "src/test/data" );
		File absPath = new File( baseDirectory, filename );

		if( absPath.isFile() )
		{
			InputStream is = null;
			byte[] buffer;

			try
			{
				is = new FileInputStream( absPath );
				
				buffer = IOUtils.toByteArray( is );

				fo.setData( buffer );
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
		
		return fo;
	}

	@Test
	public void t00_listTest()
	{
		ArrayList<FileObject> list = dropBoxIF.listFiles();
		
		for( FileObject aFO : list )
		{
			System.out.println( " " + aFO.getName() );
		}

		System.out.println( "============================" );
	}
	
	
	@Test
	public void t00_createTest()
	{
		FileObject fo = createFileObject( "hex.bin" );
		
		dropBoxIF.create( fo );
	}

	
	// @Test
	public void t01_deleteAllFiles()
	{
		FileObject fo = new FileObject();
		
		ArrayList<FileObject> list = dropBoxIF.listFiles();
		
		for( FileObject aFO : list )
		{
			System.out.println( " " + aFO.getName() );
			fo.setName(  aFO.getName() );
				
			dropBoxIF.delete( fo );			
		}
	}	
	
	
	@Test
	public void t01_deleteTest()
	{
		FileObject fo = new FileObject();
		fo.setName(  "hex.bin" );
		
		dropBoxIF.delete( fo );
	}	
	
	@Test
	public void t02_readTest()
	{
		FileObject fo = createFileObject( "/hex.bin" );
		
		dropBoxIF.create( fo );
		
		FileObject readFo = new FileObject();
		readFo.setName( fo.getName() );
		
		
		// compare
		boolean ret = false;
		
		dropBoxIF.read( readFo );		
		
		// compare
		
		System.out.println( "Original file: " + fo.getName() );
		fo.showHexData();

		System.out.println( "Re-read file: " + readFo.getName() );
		readFo.showHexData();
		
		ret = fo.compare( readFo );
		assertEquals( "creation and read = document with same content and name", true, ret );
	}		
	
	@Test
	public void t03_idempotentCreateTest()
	{
		FileObject fo = createFileObject( "hex.bin" );
		
		dropBoxIF.create( fo );
		
		// and now a second time
		dropBoxIF.create( fo );
	}	
	
	@Test
	public void t04_updateTest()
	{
		FileObject fo = createFileObject( "hex_mirror.bin" );
		
		fo.setName( "hex.bin" );
		
		dropBoxIF.create( fo );
		
		// create palindrom
		byte[] data = fo.getData();
		byte tmp;
		
		for( int ii = 0 ; ii < (data.length / 2) ; ii++ )
		{
			tmp = data[ii];
			data[ii] = data[data.length-1-ii];
			data[data.length-1-ii] = tmp;
		}
		
		fo.setData( data );
		
		// and now a second time
		dropBoxIF.update( fo );
	}
	
	
	@Test
	public void t05_update2Test()
	{
		FileObject fo = createFileObject( "hex.bin" );
		
		dropBoxIF.create( fo );
		
		// create diagonal swap
		byte[] data = fo.getData();
		byte tmp;
		
		for( int ii = 0 ; ii < 16 ; ii++ )
		{
			for( int jj = 0 ; jj < ii ; jj ++ )
			{
					tmp = data[ii*16+jj];
					data[ii*16+jj] = data[jj*16+ii];
					data[jj*16+ii] = tmp;
			}
		}
		
		fo.setData( data );
		
		// and now a second time
		dropBoxIF.update( fo );
	}
	
}
