package at.tuwien.aic.raid.data;

/**
 * @autor: rainer.schwarzinger@gmail.com aka holosound
 * 
 * 
 * The FileViewObject is used to show the information for the user
 * 
 * it consists of the (former) FileObject 
 * + 
 * 3 additional one to get information over each interface
 */


public class FileViewObject
{
	private FileObject globalFo;
	private FileObject[] interfaceInformationFos;
	private String[] interfaceNames;
	

	public FileObject getGlobalFo()
	{
		return globalFo;
	}

	public void setGlobalFo( FileObject globalFo )
	{
		this.globalFo = globalFo;
	}

	public FileObject[] getInterfaceInformationFos()
	{
		return interfaceInformationFos;
	}

	public void setInterfaceInformationFos( FileObject[] interfaceInformationFos )
	{
		this.interfaceInformationFos = interfaceInformationFos;
	}

	public String[] getInterfaceNames()
	{
		return interfaceNames;
	}

	public void setInterfaceNames( String[] interfaceNames )
	{
		this.interfaceNames = interfaceNames;
	}
	
	
	
	
}
