import java.io.File;
import java.io.File;

public class FileIO {
	
	private String baseDirectory;
	
	public FileIO(String baseDir){
		this.baseDirectory = baseDir;	
	}
	
	public boolean checkBaseDirectoryValid() {
		boolean exists = false;		
		return exists;	
	}
	
	public boolean checkValidBaseDirectory(){
		//Check if base directory is valid
	    File tmpDir = new File(this.baseDirectory);
		return tmpDir.isDirectory() && tmpDir.exists();		
	}

	public void WriteToCSV(String data, String name) {
			
		
	}
	
	public void WriteToTxt(String data, String name) {
			
		
	}
}


