
public class DataStore {
	private FileIO f;
	@SuppressWarnings("unused")
	private SQL sql;
	private WriteOption writeOpt;

	public enum WriteOption {
		CSV, TXT, SQL
	}

	public DataStore(WriteOption opt) {	
		writeOpt = opt;
	}

	 void setupFileIO(String baseDir) {
		f = new FileIO(baseDir);
	}

	public void setupSQL(String connString) {
		throw new UnsupportedOperationException("SQL interface not developed");
	}

	public boolean checkFileIOValid() {

		boolean valid = false;

		if (f != null) {
			valid = f.checkValidBaseDirectory();
		}

		return valid;
	}
	
	public int getWriteOptionInt() {
		return this.writeOpt.ordinal();
	}

	public void writeData(String data, String name) throws Exception {

		// select how to write the data based upon the available datastore options
		
		switch (writeOpt) {
		case CSV:
			if (f != null) {
				f.writeToCSV(data, name);
			} else {
				throw new Exception("Must initialize file io before writing data");
			}
			break;
		case TXT:
			if (f != null) {
				f.writeToTxt(data, name);
			} else {
				throw new Exception("Must initialize file io before writing data");
			}
			break;
		case SQL:
			throw new UnsupportedOperationException("SQL interface not developed");
		}
	}

	public String getLastWriteInfo() {

		switch (this.writeOpt) {
		case CSV:
			return "CSV file created: " + f.getFullFilePath();
		case TXT:
			return "TXT file created: " + f.getFullFilePath();
		default:
			throw new UnsupportedOperationException(this.writeOpt + " interface not developed");
		}
		
	}

}
