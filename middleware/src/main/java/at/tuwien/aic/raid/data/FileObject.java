package at.tuwien.aic.raid.data;

public class FileObject {
	private String name;
	private Byte[] data;
	public FileObject(String name) {
		this.name = name;
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Byte[] getData() {
		return data;
	}

	public void setData(Byte[] data) {
		this.data = data;
	}
}
