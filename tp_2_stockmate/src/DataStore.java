
public class DataStore {
	private FileIO f;
	private SQL sql;
	
	public enum WriteOption {
	    CSV, TXT, SQL
	}
	
	public DataStore(){
		
	}
	
	public void setupFileIO(String baseDir) {		
		f = new FileIO(baseDir);		
	}
	
	
	
	public void writeData(String data, String name, WriteOption option) throws Exception {
		
		switch(option) {
			case CSV:
				if (f!=null) {
					f.WriteToCSV(data, name);						
				} else {	
					throw new Exception("Must initialize file io before writing data"); 
				}					
			case TXT:
				if (f!=null) {
					f.WriteToTxt(data, name);						
				} else {	
					throw new Exception("Must initialize file io before writing data"); 
				}					
			case SQL:
				throw new UnsupportedOperationException("SQL interface not developed");			
		}			
	}
	
	public void setupSQL(String connString) {
		throw new UnsupportedOperationException("SQL interface not developed"); 
	}
	
}

