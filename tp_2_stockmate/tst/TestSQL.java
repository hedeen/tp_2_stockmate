import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSQL {

	@SuppressWarnings("unused")
	private SQL s;
	
	@Test
	public void test_throws_exception_on_constructor() throws Exception {		
		
		// should throw exception since this class has not been developed
		Assertions.assertThrows(Exception.class, () -> s = new SQL());		
	}

}
