package at.tuwien.aic.raid.sessionbean;

import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Stateless;

import at.tuwien.aic.raid.Raid1;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
import at.tuwien.aic.raid.data.Raid1DTO;

@Stateless()
public class Raid1SessionBean 
		implements RaidSessionBeanInterface 
{
	
	private Raid1 raid1;

	public Raid1SessionBean() 
				throws IOException 
	{
		raid1 = new Raid1();
	}

	@Override
	public Raid1DTO listFiles() throws IOException {
		
		return raid1.listFiles();
	}

	@Override
	public void delete(String fn) throws IOException {
		raid1.delete(fn);
		
	}

	@Override
	public void deleteHistory(String fn) throws IOException {
		raid1.deleteHistory(fn);
		
	}
	
	@Override
	public FileObject getFile(String fn) throws IOException {
		
		return raid1.getFile(fn);
	}
	
	@Override
	public FileObject getHistoryFile(String fn) throws IOException {
		
		return raid1.getHistoryFile(fn);
	}
	

	@Override
	public void write(FileObject f) throws IOException {
		raid1.write(f);
		
	}

	@Override
	public Raid1DTO getFileInfo(String fn) {
		
		return raid1.getFileInfo(fn);
	}

	@Override
	public Raid1DTO getFileHistory( String fn )
			 throws IOException 
	{
		return raid1.getFileHistory( fn );
	}

	@Override
	public Raid1DTO copyFile(String fn, String fromInterface, String toInterface ) 
	{
		return raid1.copyFile( fn, fromInterface, toInterface );
	}
}
