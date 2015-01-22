package at.tuwien.aic.raid.sessionbean;

import java.io.IOException;

import javax.ejb.Stateless;

import at.tuwien.aic.raid.Raid5;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.Raid5DTO;

@Stateless()
public class Raid5SessionBean implements Raid5sessionBeanInterface {
	
	private Raid5 raid;

	public Raid5SessionBean() throws IOException {
		raid=new Raid5();
	}

	@Override
	public Raid5DTO listFiles() throws IOException {
		
		return raid.listFiles();
	}

	@Override
	public Raid5DTO getFileHistory( String fn )
			throws IOException 
	{	
		return raid.getFileHistory(fn);
	}
	
	@Override
	public void delete(String fn) throws IOException {
		
		raid.delete(fn);
	}
	
	@Override
	public void deleteHistory(String fn) throws IOException {
		
		raid.deleteHistory(fn);
	}

	@Override
	public FileObject getFile(String fn) throws IOException {
		
		return raid.getFile(fn);
	}
	
	@Override
	public FileObject getHistoryFile(String fn) throws IOException {
		
		return raid.getHistoryFile(fn);
	}

	@Override
	public void write(FileObject f) throws IOException {
		raid.write(f);
		
	}

	
	
}
