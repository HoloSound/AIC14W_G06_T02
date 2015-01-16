package at.tuwien.aic.raid.data;

import java.util.ArrayList;

/**
 * @autor: rainer.schwarzinger@gmail.com aka holosound
 * 
 * 
 * RIAD1 Data Transfer Object
 * 
 * holds list of interfaceNames
 * and
 * list of fileViewObject 
 */


public class Raid1DTO
{
	private String[] interfaceNames;
	private ArrayList<FileViewObject> fileViewObjects;

	public String[] getInterfaceNames()
	{
		return interfaceNames;
	}

	public void setInterfaceNames( String[] interfaceNames )
	{
		this.interfaceNames = interfaceNames;
	}

	public ArrayList<FileViewObject> getFileViewObjects()
	{
		return fileViewObjects;
	}

	public void setFileViewObjects( ArrayList<FileViewObject> fileViewObjects )
	{
		this.fileViewObjects = fileViewObjects;
	}
	
}
