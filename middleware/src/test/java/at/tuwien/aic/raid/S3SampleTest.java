package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import at.tuwien.aic.raid.connector.S3Connector;
import at.tuwien.aic.raid.data.FileObject;

public class S3SampleTest {

    @Test
    public void createFileTest() {
    	
    	FileObject f;
    	f = new FileObject() ;
    	byte[] b = "AAAA".getBytes();


        S3Connector s = new S3Connector();
		f.setData(b);
		f.setName("Name7");
		
		s.create(f);
		//try {
			// This is a test both for create and read, to check if the returned file is the same as uploaded
			//FileObject fr = s.read(f);			
			//assertArrayEquals(b, fr.getData());
			
		//} catch (IOException e) {
		//	fail(e.getMessage());
		//	e.printStackTrace();
		//}		

    }
    
    @Test()
   public void readFileTest() throws IOException {
    	
    	FileObject f;
    	f = new FileObject() ;
    			f.setName("Name7");    	

    	
        S3Connector s = new S3Connector();

        
		
		s.read(f);

    }

    
    @Test
    public void updateFileTest() {
    	
    	FileObject f;
    	f = new FileObject() ;
    	byte[] b = "Test".getBytes();


        S3Connector s = new S3Connector();
		f.setData(b);
		f.setName("Name7");
		
		s.create(f);
		try {
			s.update(f);

			
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}		

    }
    
    @Test
    public void listFilesTest() throws Exception {
    	
        S3Connector s = new S3Connector();
		
		
	
		try {
			s.listFiles();

			
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}		

    }
    
    @Test
    public void deleteFileTest() throws IOException {
    	
    	FileObject f;
    	f = new FileObject() ;
		f.setName("Name7");
		
		S3Connector s = new S3Connector();
		s.delete(f);
		try {
			s.delete(f);
			
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}		

    }
    
    

//    @Test
//    public void bLaTest(){
//        String[] a=new String[0];
//        try {
//
//            S3Connector s = new S3Connector();
//            s.listFiles();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}