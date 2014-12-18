package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters; //Running test cases in order of method names in ascending order.

import at.tuwien.aic.raid.connector.DropBoxImpl;
import at.tuwien.aic.raid.connector.FileImpl;
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
public class Raid5Test
{
	private Raid5 raid5 = null;
	private FileImpl fileIF = null;

	
	@Before
	public void init()
	{
		raid5 = new Raid5();
		fileIF = new FileImpl();
	}


	private FileObject createFileObject( String filename )
	{
		FileObject fo = new FileObject();
		
		fo.setName( filename );
		
		// TODO ... same in each *Impl object? --> REDESIGN
		File baseDirectory = new File( "c:/tmp/" );
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
	public void t00_createTest()
	{
		FileObject fo = createFileObject( "hex.bin" );
		// we read local from Directory: ...
		
		// and write local to directory: ...
		fileIF.create( fo );
		
		FileObject[] raid5Files = raid5.generateFiles( fo );
		
		// this fileObject - we split into 3 files
		for( int ii = 0 ; ii < raid5Files.length ; ii++ )
		{
			// and store each alone
			FileObject actFo = raid5Files[ii];
			
			fileIF.create( actFo );
		}
		
		// now try to analyse the files in different configurations
		// all files given:
		try
		{
			FileObject newTarget = raid5.reconstructFile( raid5Files );
			
			boolean ret = fo.compare( newTarget );
			assertEquals( "creation and read = document with same content and name", true, ret );
			
			newTarget.setName(  "recover_" + newTarget.getName() );
			
			fileIF.create( newTarget );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void t01_createTest()
	{
		FileObject fo = createFileObject( "hex_odd.bin" );
		// we read local from Directory: ...
		
		// and write local to directory: ...
		fileIF.create( fo );
		
		FileObject[] raid5Files = raid5.generateFiles( fo );
		
		// this fileObject - we split into 3 files
		for( int ii = 0 ; ii < raid5Files.length ; ii++ )
		{
			// and store each alone
			FileObject actFo = raid5Files[ii];
			
			fileIF.create( actFo );
		}
		
		// now try to analyse the files in different configurations
		// all files given:
		try
		{
			FileObject newTarget = raid5.reconstructFile( raid5Files );
			
			boolean ret = fo.compare( newTarget );
			assertEquals( "creation and read = document with same content and name", true, ret );
			
			newTarget.setName(  "recover_" + newTarget.getName() );
			
			fileIF.create( newTarget );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void t02_part1Test()
	{
		FileObject fo = createFileObject( "hex.bin" );
		// we read local from Directory: ...
		
		// and write local to directory: ...
		fileIF.create( fo );
		
		FileObject[] raid5Files = raid5.generateFiles( fo );
		
		// this fileObject - we split into 3 files
		for( int ii = 0 ; ii < raid5Files.length ; ii++ )
		{
			// and store each alone
			FileObject actFo = raid5Files[ii];
			
			fileIF.create( actFo );
		}
		
		// we delete one generated file 
		raid5Files = Arrays.copyOf( raid5Files, raid5Files.length -1 );
		// last one should now only have LON and HIN! (Parity deleted)
		
		// now try to analyse the files in different configurations
		// all files given:
		try
		{
			FileObject newTarget = raid5.reconstructFile( raid5Files );
			
			boolean ret = fo.compare( newTarget );
			assertEquals( "creation and read = document with same content and name", true, ret );
			
			newTarget.setName(  "recover_" + newTarget.getName() );
			
			fileIF.create( newTarget );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void t02_part2Test()
	{
		FileObject fo = createFileObject( "hex.bin" );
		// we read local from Directory: ...
		
		// and write local to directory: ...
		fileIF.create( fo );
		
		FileObject[] raid5Files = raid5.generateFiles( fo );
		
		// this fileObject - we split into 3 files
		for( int ii = 0 ; ii < raid5Files.length ; ii++ )
		{
			// and store each alone
			FileObject actFo = raid5Files[ii];
			
			fileIF.create( actFo );
		}
		
		// we delete one generated file 
		FileObject[] raid5FilesTmp = Arrays.copyOf( raid5Files, raid5Files.length -1 );
		// last one should now only have LON and HIN! (Parity deleted)
		raid5FilesTmp[0] = raid5Files[0];
		raid5FilesTmp[1] = raid5Files[2];
		
		raid5Files = raid5FilesTmp;
		
		// now try to analyse the files in different configurations
		// all files given:
		try
		{
			FileObject newTarget = raid5.reconstructFile( raid5Files );
			
			boolean ret = fo.compare( newTarget );
			assertEquals( "creation and read = document with same content and name", true, ret );
			
			newTarget.setName(  "recover_" + newTarget.getName() );
			
			fileIF.create( newTarget );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void t02_part3Test()
	{
		FileObject fo = createFileObject( "hex.bin" );
		// we read local from Directory: ...
		
		// and write local to directory: ...
		fileIF.create( fo );
		
		FileObject[] raid5Files = raid5.generateFiles( fo );
		
		// this fileObject - we split into 3 files
		for( int ii = 0 ; ii < raid5Files.length ; ii++ )
		{
			// and store each alone
			FileObject actFo = raid5Files[ii];
			
			fileIF.create( actFo );
		}
		
		// we delete one generated file 
		FileObject[] raid5FilesTmp = Arrays.copyOf( raid5Files, raid5Files.length -1 );
		// last one should now only have LON and HIN! (Parity deleted)
		raid5FilesTmp[0] = raid5Files[1];
		raid5FilesTmp[1] = raid5Files[2];
		
		raid5Files = raid5FilesTmp;
		
		// now try to analyse the files in different configurations
		// all files given:
		try
		{
			FileObject newTarget = raid5.reconstructFile( raid5Files );
			
			boolean ret = fo.compare( newTarget );
			assertEquals( "creation and read = document with same content and name", true, ret );
			
			newTarget.setName(  "recover_" + newTarget.getName() );
			
			fileIF.create( newTarget );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

