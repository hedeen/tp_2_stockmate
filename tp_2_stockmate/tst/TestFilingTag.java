
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestFilingTag {

	public FilingTag ft = new FilingTag();


	@Test
	public void testTranslateTag() {
		assertTrue(ft.getXbrlTag("income").equalsIgnoreCase("us-gaap:NetIncomeLoss"));
		assertTrue(ft.getXbrlTag("asfdkljsadlkjfsdalkj")==null);
	}

	@Test
	public void testGetArrayOfSupportedTagDescriptions() {
		assertTrue(ft.getArrayOfSupportedTagDescriptions().length > 0);
	}

	@Test
	public void testGetArrayOfSupportedTags() {
		assertTrue(ft.getArrayOfSupportedTags().length > 0);
	}

	@Test
	public void testCheckTagSupported() {
		assertTrue(ft.checkTagSupported("eps") == true);
		assertTrue(ft.checkTagSupported("jfaafskldj") == false);
	}

	@Test
	public void testGetFullTagDescription() {
		assertTrue(ft.getFullTagDescription("income").equalsIgnoreCase("income"));
	}

	@Test
	public void testGetFormattedStringOfSupportedTags() {
		assertTrue(ft.getFormattedStringOfSupportedTags().length() > 0);
	}
}

