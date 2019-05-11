import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataStoreTest {
	
	DataStore ds;

	@Before
	public void setup() {		

	}

	@Test
	public void test_constructor() {
		ds = new DataStore(DataStore.WriteOption.CSV);
		assertTrue(ds.getWriteOptionInt()==DataStore.WriteOption.CSV.ordinal());
		
		ds = new DataStore(DataStore.WriteOption.TXT);
		assertTrue(ds.getWriteOptionInt()==DataStore.WriteOption.TXT.ordinal());		
	}
	
	@Test
	public void test_invalid_write_option_throws() {
		//test does throw an error
		ds = new DataStore(DataStore.WriteOption.SQL);
		Assertions.assertThrows(RuntimeException.class, () -> ds.writeData("TestData", "TestFilename"));
	}
	
	@Test
	public void test_get_last_write_data_correct() throws Exception {
		ds = new DataStore(DataStore.WriteOption.TXT);
		ds.setupFileIO("C:\\Stock Mate");
		ds.writeData("TESTING", "TestFile");
		assertTrue(ds.getLastWriteInfo().contains(".txt"));
		
		ds = new DataStore(DataStore.WriteOption.CSV);
		ds.setupFileIO("C:\\Stock Mate");
		ds.writeData("TESTING", "TestFile");
		assertTrue(ds.getLastWriteInfo().contains(".csv"));
	}
	
	@Test
	public void test_get_last_write_data_incorrect() throws Exception {
		ds = new DataStore(DataStore.WriteOption.TXT);
		ds.setupFileIO("C:\\Stock Mate");
		ds.writeData("TESTING", "TestFile");
		assertTrue(!ds.getLastWriteInfo().contains(".abc"));
	}
	
	@Test
	public void test_unsupported_sql() {
		//test does throw an error
		ds = new DataStore(DataStore.WriteOption.SQL);
		Assertions.assertThrows(RuntimeException.class, () -> ds.setupSQL("ConnString"));
	}
	
}
