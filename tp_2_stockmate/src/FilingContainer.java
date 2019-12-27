import java.time.LocalDate;
import java.util.ArrayList;

public class FilingContainer {

	private NestedMap<String, String> m = new NestedMap<>();
	private int rows = 0;
	private LocalDate newestFiling = LocalDate.now().minusYears(100);
	private LocalDate oldestFiling = LocalDate.now().plusYears(100);
	private ArrayList<LocalDate> filingDates = new ArrayList<LocalDate>();

	public void put(LocalDate periodEndDate, int periodLength, String key, String value) {
		String prdEnd = String.valueOf(periodEndDate);
		String prdLen = String.valueOf(periodLength);

		// this will only set the value if it doesn't already exist!!
		if (!this.m.hasChild(prdEnd)) {
			this.m.makeChild(prdEnd);
			this.m.getChild(prdEnd).makeChild(prdLen);
			this.m.getChild(prdEnd).getChild(prdLen).setValues(key, value);
			this.rows++;
			
			this.filingDates.add(periodEndDate);
		
			if(periodEndDate.isAfter(this.newestFiling)) {
				periodEndDate = this.newestFiling;
			}
			
			if(periodEndDate.isBefore(this.oldestFiling)) {
				periodEndDate = this.oldestFiling;
			}

		} else if (!this.m.getChild(prdEnd).hasChild(prdLen)) {
			this.m.getChild(prdEnd).makeChild(prdLen);
			this.m.getChild(prdEnd).getChild(prdLen).setValues(key, value);
			this.rows++;
		} else if (!this.m.getChild(prdEnd).getChild(prdLen).hasChilds(key)) {
			this.m.getChild(prdEnd).getChild(prdLen).setValues(key, value);
			this.rows++;
		}

	}
	
	public String get(LocalDate periodEndDate, int periodLength,  String key) {
		String prdEnd = String.valueOf(periodEndDate);
		String prdLen = String.valueOf(periodLength);
		String val;

		try {
			val = this.m.getChild(prdEnd).getChild(prdLen).getValues(key);
		} catch (NullPointerException e) {
			// value not found
			val = null;
		}

		return val;
	}

	public boolean hasPeriodData(LocalDate periodEndDate, int periodLength) {
		String prdEnd = String.valueOf(periodEndDate);
		String prdLen = String.valueOf(periodLength);
		boolean hasData = false;

		try {
			hasData = this.m.getChild(prdEnd).hasChild(prdLen);
		} catch (NullPointerException e) {
			// value not found, already false
		}

		return hasData;

	}

	public boolean hasData(LocalDate periodEndDate, int periodLength, String key) {
		String prdEnd = String.valueOf(periodEndDate);
		String prdLen = String.valueOf(periodLength);
		boolean hasData = false;

		try {
			hasData = this.m.getChild(prdEnd).getChild(prdLen).hasChilds(key);
		} catch (NullPointerException e) {
			// value not found, already false
		}

		return hasData;

	}

	public int getRows() {
		return this.rows;
	}
	
	public int getMaxYear() {
		return this.newestFiling.getYear();
	}
	
	public int getMinYear() {
		return this.oldestFiling.getYear();
	}
	
	public ArrayList<LocalDate> getFilingDates() {
		return this.filingDates;
	}
	

}
