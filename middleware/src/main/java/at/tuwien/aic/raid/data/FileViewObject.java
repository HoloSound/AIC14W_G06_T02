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
	// global basic name
	private FileObject globalFo;
	// RAID1 - gets the HASH values for each own file
	// RAID5 - gets the generated names of the files at the locations
	private FileObject[] interfaceInformationFos;
	

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
	
}
