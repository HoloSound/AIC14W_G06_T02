package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;

import at.tuwien.aic.raid.connector.BoxConnector;
import at.tuwien.aic.raid.data.FileObject;

public class Raid1 {
	 ConnectorInterface box = new DropBoxImpl();

	public Raid1() {
		System.out.println("NEW Raid1");
	}
	
	public ArrayList<FileObject> listFiles()  throws IOException{ //TODO IMPLEMENT  RAID1 LOGIK
		

		try {
			
			return  box.listFiles();
		
		} catch (Exception e) {
		
			
			throw new IOException(e);
		}
		
	}

	public void delete(String fn) throws IOException{//TODO IMPLEMENT  RAID1 LOGIK
		
		try {
			System.out.println("delete"+fn);
			box.delete(new FileObject(fn));
		
		} catch (Exception e) {
		
			
			throw new IOException(e);
		}
		
	}

	public FileObject getFile(String fn)  throws IOException {//TODO IMPLEMENT  RAID1 LOGIK
		try {
			System.out.println("getFile"+fn);
			return box.read(new FileObject(fn));
		
		} catch (Exception e) {
		
			
			throw new IOException(e);
		}
		
	}

	public void write(FileObject f) throws IOException {//TODO IMPLEMENT  RAID1 LOGIK
		try {
			System.out.println("write"+f.getName());
			box.create(f);
		} catch (Exception e) {
		
			throw new IOException(e);
		}
	}

}
