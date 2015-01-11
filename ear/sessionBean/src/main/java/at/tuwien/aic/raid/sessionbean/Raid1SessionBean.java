package at.tuwien.aic.raid.sessionbean;

import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Stateless;

import at.tuwien.aic.raid.Raid1;
import at.tuwien.aic.raid.data.FileObject;

@Stateless()
public class Raid1SessionBean implements RaidSessionBeanInterface {
	
	private Raid1 raid1;

	public Raid1SessionBean() throws IOException {
		raid1=new Raid1();
	}

	@Override
	public ArrayList<FileObject> listFiles() throws IOException {
		
		return raid1.listFiles();
	}

	@Override
	public void delete(String fn) throws IOException {
		raid1.delete(fn);
		
	}

	@Override
	public FileObject getFile(String fn) throws IOException {
		
		return raid1.getFile(fn);
	}

	@Override
	public void write(FileObject f) throws IOException {
		raid1.write(f);
		
	}

	
	
}