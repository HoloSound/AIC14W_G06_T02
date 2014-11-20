package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import at.tuwien.aic.raid.data.FileObject;

/**
 * 
 * @author daniel
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Raid1BoxTest
{
	private BoxImpl boxClient = new BoxImpl();
	
	/*@Before
	public void init()
	{
		boxClient = new BoxImpl();
	}*/
	
	/**
	 * Create Test
	 */
	@Test
	public void t00_test()
	{
		FileObject tmp = new FileObject();
		
		String tmpdata = "Test gelungen :)";
		
		tmp.setName("datei.txt");
		
		tmp.setData(tmpdata.getBytes());
		boxClient.create(tmp);
		
		
		System.out.println("file created.");
		
		
		FileObject tmp2 = new FileObject();
		tmp2.setName("datei.txt");
		FileObject newFile = boxClient.read(tmp2);
		System.out.println(newFile.getName()+ "::" + new String(newFile.getData()));
		
		boxClient.delete(tmp);
		
		System.out.println("deleted.");

		assertTrue(true);
	}
	
	/**
	 * Read Test
	 */
	/*@Test
	public void t01_test()
	{
		FileObject tmp2 = new FileObject();
		tmp2.setName("datei.txt");
		FileObject newFile = boxClient.read(tmp2);
		System.out.println(newFile.getName()+ "::" + new String(newFile.getData()));
		assertTrue(true);
	}*/
	
	/**
	 * Delete test
	 */
	/*@Test
	public void t02_test()
	{
		FileObject tmp = new FileObject();
		tmp.setName("datei.txt");
		boxClient.delete(tmp);
		assertTrue(true);
	}*/
}