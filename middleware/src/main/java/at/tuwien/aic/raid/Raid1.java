package at.tuwien.aic.raid;

import java.io.IOException;
import java.util.ArrayList;

import at.tuwien.aic.raid.connector.BoxConnector;
import at.tuwien.aic.raid.data.FileObject;

public class Raid1 {
	ConnectorInterface box = new BoxConnector();

	public ArrayList<FileObject> listFiles()  throws IOException{ //TODO IMPLEMENT  RAID1 LOGIK
		
		ArrayList<FileObject> ret = new ArrayList<FileObject>();
		ret.add(new FileObject("simpleFile1"));
		ret.add(new FileObject("simpleFile2"));
		FileObject d = new FileObject("IMPLEMENT DIRS ?.TXT");
		d.setIsDirectory();
		
		ret.add(d);
		return ret;

	}

	public void delete(String fn) throws IOException{//TODO IMPLEMENT  RAID1 LOGIK
		
		
	}

	public FileObject getFile(String fn)  throws IOException {//TODO IMPLEMENT  RAID1 LOGIK
		
		FileObject f=new FileObject(fn);
		f.setData((fn+"SOME DATA CONTENT").getBytes());
		return f;
	}

	public void write(FileObject f) throws IOException {//TODO IMPLEMENT  RAID1 LOGIK
			System.out.println(f.getName());
			System.out.println("len:"+f.getData().length);
			int i=0;
			for (byte b : f.getData()) {
				try {
					System.out.print(Character.toChars(b));
				} catch (IllegalArgumentException e) {
					System.out.print("?");
				}
				i++;
				if(i>600){
					return;
				}
			}
		
	}

}
