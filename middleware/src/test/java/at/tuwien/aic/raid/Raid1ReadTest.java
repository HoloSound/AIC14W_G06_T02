package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters; //Running test cases in order of method names in ascending order.

import at.tuwien.aic.raid.data.FileObject;

/**
 * @author daniel
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Raid1ReadTest
{
	private static Raid1 myraid1 = new Raid1();
	private static String tmpfilename = "datei.txt";
	private static String tmpdata = "Testing data :)";
	
	@Test
	public void t00_readTest()
	{

		FileObject tmp = new FileObject();
		
		tmp.setName(tmpfilename);
		
		tmp.setData(tmpdata.getBytes());
		
		try {
			myraid1.write(tmp);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
		FileObject tmp2 = null;
		try {
			tmp2 = myraid1.getFile(tmpfilename);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
		try {
			myraid1.delete(tmpfilename);
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
		//System.out.println(newFile.getName()+ "::" + new String(newFile.getData()));
		assertTrue(new String(tmp2.getData()).equals(tmpdata));
		
	}
	
}
