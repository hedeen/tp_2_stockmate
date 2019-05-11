import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileIO {

	private String baseDirectory;
	private String fullFilePath;

	public FileIO(String baseDir) {
		this.baseDirectory = baseDir;
	}

	public boolean checkBaseDirectoryValid() {
		boolean exists = false;
		return exists;
	}

	public boolean checkValidBaseDirectory() {
		// Check if base directory is valid
		File tmpDir = new File(this.baseDirectory);
		return tmpDir.isDirectory() && tmpDir.exists();
	}

	public void writeToCSV(String stringData, String filename) throws Exception {
		this.fullFilePath = baseDirectory + "\\" + filename + ".csv";
		writeToFile(stringData);
	}

	public void writeToTxt(String stringData, String filename) throws Exception {
		this.fullFilePath = baseDirectory + "\\" + filename + ".txt";
		writeToFile(stringData);
	}

	private void writeToFile(String stringData) throws Exception {

		// setup out here so we can use in finally clause
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(new File(fullFilePath));
			StringBuilder sb = new StringBuilder();
			sb.append(stringData);
			writer.write(sb.toString());			

		} catch (FileNotFoundException e) {
			// throw a custom exception and append on the exception information
			throw new Exception("Error in writeToFile(). File/Directory not found /r /r" + e.getMessage());
		}
		finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
	
	public String getFullFilePath() {
		return this.fullFilePath;
	}
}
