import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.Before;
import org.junit.Test;

public class FilingProcessorTest {
	public final String ticker = "GOOG";
	public final String[] tags = new String[] { "income", "eps" };
	public String cik;
	public FilingProcessor fp;

	@Before
	public void setup() {
		
		fp = new FilingProcessor(ticker,tags);
		fp.setTag("eps");
	}

	@Test
	public void test_constructor() {
		//test does not throw error
		assertDoesNotThrow(() -> new FilingProcessor(ticker));
	}

	@Test
	public void test_getCik() {
		cik = fp.getCIK(ticker);
		assertTrue(cik.contentEquals("0001652044"));
		assertTrue(fp.getTicker().equalsIgnoreCase("GOOG"));
	}
	

	@Test
	public void testBufferAllFilings() {
		fp.bufferAllFilings();
		assertTrue(fp.getFilingCount()>0);
	}
	
	@Test
	public void testMostRecentFiling_GetTagData_GetFilingPreview_GetMostRecentFilingData() {
		fp.bufferMostRecentFiling();
		assertTrue(fp.getFilingCount()>0);
		assertTrue(fp.getTagData(2018, 0, "eps").length()>0);
		assertTrue(fp.getTagData(1984, 0, "eps")==null);
		assertTrue(fp.getFilingPreview().length()>0);
		assertTrue(fp.getMostRecentFilingData("eps").length>0);
	}

	@Test
	public void testTranslateTag() {
		assertTrue(fp.translateTag("income").equalsIgnoreCase("us-gaap:NetIncomeLoss"));
	}

	@Test
	public void GetArrayOfSupportedTagDescriptions() {
		assertTrue(fp.getArrayOfSupportedTagDescriptions().length>0);
	}

	@Test
	public void testGetArrayOfSupportedTags() {
		assertTrue(fp.getArrayOfSupportedTags().length>0);
	}

	@Test
	public void testCheckTagSupported() {
		assertTrue(fp.checkTagSupported("eps")==true);
		assertTrue(fp.checkTagSupported("jfaafskldj")==false);
	}

	@Test
	public void testGetFullTagDescription() {
		assertTrue(fp.getFullTagDescription("income").equalsIgnoreCase("income"));
	}


	@Test
	public void testGetFormattedStringOfSupportedTags() {
		assertTrue(fp.getFormattedStringOfSupportedTags().length()>0);
	}

}
