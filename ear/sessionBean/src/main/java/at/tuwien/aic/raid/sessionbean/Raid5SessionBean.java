package at.tuwien.aic.raid.sessionbean;

import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Stateless;

import at.tuwien.aic.raid.Raid5;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;

@Stateless()
public class Raid5SessionBean implements Raid5sessionBeanInterface {
	
	private Raid5 raid;

	public Raid5SessionBean() throws IOException {
		raid=new Raid5();
	}

	@Override
	public ArrayList<FileViewObject> listFiles() throws IOException {
		
		return raid.listFiles();
	}

	@Override
	public void delete(String fn) throws IOException {
		raid.delete(fn);
		
	}

	@Override
	public FileObject getFile(String fn) throws IOException {
		
		return raid.getFile(fn);
	}

	@Override
	public void write(FileObject f) throws IOException {
		raid.write(f);
		
	}

	
	
}
