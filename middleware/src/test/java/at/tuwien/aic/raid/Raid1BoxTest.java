package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.IOException;

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
	private static BoxImpl boxClient = new BoxImpl();
	private static String tmpfilename = "datei.txt";
	private static String tmpdata = "Testing data :)";
	
	/**
	 * Create Test
	 */
	@Test
	public void t00_test()
	{
		FileObject tmp = new FileObject();
		
		tmp.setName(tmpfilename);
		
		tmp.setData(tmpdata.getBytes());
		
		try {
			boxClient.create(tmp);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
		
		/*System.out.println("file created.");
		
		
		FileObject tmp2 = new FileObject();
		tmp2.setName("datei.txt");
		FileObject newFile = boxClient.read(tmp2);
		System.out.println(newFile.getName()+ "::" + new String(newFile.getData()));
		
		boxClient.delete(tmp);
		
		System.out.println("deleted. creating new");
		
		boxClient.create(tmp);
		
		System.out.println("new created");
		

		assertTrue(true);*/
	}
	
	/**
	 * Read Test
	 */
	@Test
	public void t01_test()
	{
		FileObject tmp2 = new FileObject();
		tmp2.setName(tmpfilename);
		FileObject newFile = null;
		try {
			newFile = boxClient.read(tmp2);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		//System.out.println(newFile.getName()+ "::" + new String(newFile.getData()));
		assertTrue(new String(newFile.getData()).equals(tmpdata));
	}
	
	
	/**
	 * Delete test
	 */
	@Test
	public void t02_test()
	{
		FileObject tmp = new FileObject();
		tmp.setName(tmpfilename);
		try {
			boxClient.delete(tmp);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	/**
	 * Exception when reading a not existing file
	 */
	@Test(expected=Exception.class)
	public void t03_test() throws IOException {
		FileObject tmp = new FileObject();
		tmp.setName(tmpfilename); 
		boxClient.read(tmp);
	}
	
	/**
	 * Exception when deleting a not existing file
	 */
	@Test(expected=Exception.class)
	public void t04_test() throws IOException {
		FileObject tmp = new FileObject();
		tmp.setName(tmpfilename); 
		boxClient.delete(tmp);
	}
}