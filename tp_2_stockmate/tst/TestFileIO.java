import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

class TestFileIO {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	FileIO f;
	String baseDir = tempFolder.toString();


	@Test
	public void test_constructor() {

		// Assumes all computers running test case has a C: drive
		f = new FileIO("C:\\");
		assertTrue(f.checkValidBaseDirectory());
	}

	@Test
	public void test_base_directory_invalid() {

		// should not be a valid directory
		f = new FileIO("IJDK"); // garbage
		assertTrue(!f.checkValidBaseDirectory());
	}

	public void createTestDirectory() {
		// See if folder exists... If it does not then make it

		File tmpDir = new File(baseDir);
		if (tmpDir.isDirectory() && tmpDir.exists()) {

		} else {
			tmpDir.mkdir();
		}
	}

	@Test
	public void test_file_txt_created() throws Exception {

		createTestDirectory();
		// make a test file
		File testFile = new File(baseDir + "\\alphabet.txt");

		// delete it so we know it is gone.
		testFile.delete();

		// now see if our class can correctly create a file
		f = new FileIO(baseDir);
		f.writeToTxt("ABCDEFGHIJKLMNOPQRTUVWXYZ", "alphabet");

		// now it should always exist.
		assertTrue(testFile.exists());
	}

	@Test
	public void test_file_txt_not_created() throws Exception {
		// same test as above, but don't actually make the file - tests our test code a
		// little bit to make sure we aren't masking any errors

		createTestDirectory();

		// make a test file
		File testFile = new File(baseDir + "\\alphabet.txt");

		// delete it so we know it is gone.
		testFile.delete();

		// now see if our class can correctly create a file
		f = new FileIO(baseDir);

		// now it should always exist.
		assertTrue(!testFile.exists());
	}

	@Test
	public void test_file_csv_created() throws Exception {

		createTestDirectory();
		// make a test file
		File testFile = new File(baseDir + "\\alphabet.csv");

		// delete it so we know it is gone.
		testFile.delete();

		// now see if our class can correctly create a file
		f = new FileIO(baseDir);
		f.writeToCSV("ABCDEFGHIJKLMNOPQRTUVWXYZ", "alphabet");

		// now it should always exist.
		assertTrue(testFile.exists());
	}

	@Test
	public void test_file_csv_not_created() throws Exception {
		// same test as above, but don't actually make the file - tests our test code a
		// little bit to make sure we aren't masking any errors

		createTestDirectory();

		// make a test file
		File testFile = new File(baseDir + "\\alphabet.csv");

		// delete it so we know it is gone.
		testFile.delete();

		// now see if our class can correctly create a file
		f = new FileIO(baseDir);

		// now it should always exist.
		assertTrue(!testFile.exists());
	}

	@Test
	public void test_full_filepath() throws Exception {
		// same test as above, but don't actually make the file - tests our test code a
		// little bit to make sure we aren't masking any errors

		createTestDirectory();

		// make a test file
		String fullFile = baseDir + "\\alphabet.csv";
		File testFile = new File(baseDir + "\\alphabet.csv");

		// delete it so we know it is gone.
		testFile.delete();

		// now see if our class can correctly create a file
		f = new FileIO(baseDir);
		f.writeToCSV("ABCDEFGHIJKLMNOPQRTUVWXYZ", "alphabet"); // when it creates the file, it saves the full filepath

		// now it should always exist.
		assertTrue(f.getFullFilePath().equals(fullFile));
	}

	@Test
	public void test_throws_exception_on_invalid_file() throws Exception {
		// now see if our class can correctly create a file
		f = new FileIO("AFDFSDF"); // garbage

		// should throw exception since base folder if not valid
		Assertions.assertThrows(Exception.class, () -> f.writeToCSV("ABC", "DEF"));
	}

}
