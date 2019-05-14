import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.Before;
import org.junit.Test;

public class TestFilingSummary {
	public final String ticker = "GOOG";
	public final String[] tags = new String[] { "income", "eps" };
	public String cik;
	public FilingSummary fs;

	@Before
	public void setup() {
		
		fs = new FilingSummary(ticker,tags);
		fs.setTag("eps");
	}

	@Test
	public void testConstructor() {
		//test does not throw error
		assertDoesNotThrow(() -> new FilingSummary(ticker));
	}

	@Test
	public void testGetCik() {
		cik = fs.getCIK(ticker);
		assertTrue(cik.contentEquals("0001652044"));
		assertTrue(fs.getTicker().equalsIgnoreCase("GOOG"));
		assertTrue(fs.getCIK("fsakljsdfalkjsdalk")==null);
	}
	
	@Test
	public void testGetHTML() {
		assertTrue(fs.getHTML("fsakljsdfalkjsdalk")==null);
	}
	

	@Test
	public void testBufferAllFilings() {
		fs.bufferAllFilings();
		assertTrue(fs.getFilingCount()>0);
		assertTrue(fs.getFilingPreview(",").length()>0);
	}
	
	@Test
	public void testMostRecentFiling_GetTagData_GetFilingPreview_GetMostRecentFilingData() {
		fs.bufferMostRecentFiling();
		assertTrue(fs.getFilingCount()>0);
		assertTrue(fs.getTagData(2019, 1, "eps").length()>0);
		assertTrue(fs.getTagData(1984, 0, "eps")==null);
		assertTrue(fs.getFilingPreview(",").equalsIgnoreCase(fs.getCachedFilingPreview()));
		assertTrue(fs.getMostRecentFilingData("eps").length>0);
		assertTrue(fs.getMostRecentFilingData("asdlkjsadflkj")==null);
		assertTrue(fs.getFilingPreview(",").length()>0);
	}
	
	@Test
	public void testBadTicker() {
		FilingSummary fsBad;
		fsBad= new FilingSummary("asfdsafdfsad",tags);
		fsBad.bufferMostRecentFiling();
		assertTrue(fsBad.getFilingCount() == 0);
		assertTrue(fsBad.getFilingPreview(",")==null);
	}


	
	
}
