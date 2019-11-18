public class FilingMap {

	private NestedMap<String, String> m = new NestedMap<>();
	private int rows = 0;
	private int maxYear = 2000;
	private int minYear = 3000;

	public void put(int year, int period, String key, String value) {
		String yr = String.valueOf(year);
		String prd = String.valueOf(period);
		String val = String.valueOf(value);

		// this will only set the value if it doesn't already exist!!
		if (!this.m.hasChild(yr)) {
			this.m.makeChild(yr);
			this.m.getChild(yr).makeChild(prd);
			//this.m.getChild(yr).getChild(prd).makeChild(tag);
			//this.m.getChild(yr).getChild(prd).getChild(tag).setValue(val);
			this.m.getChild(yr).getChild(prd).setValues(key, val);
			this.rows++;

			if (year > this.maxYear) {
				this.maxYear = year;
			}
			
			if (year < this.minYear) {
				this.minYear = year;
			}

		} else if (!this.m.getChild(yr).hasChild(prd)) {
			this.m.getChild(yr).makeChild(prd);
			this.m.getChild(yr).getChild(prd).setValues(key, val);
			//this.m.getChild(yr).getChild(prd).makeChild(tag);
			//this.m.getChild(yr).getChild(prd).getChild(tag).setValue(val);
			this.rows++;
		} else if (!this.m.getChild(yr).getChild(prd).hasChilds(key)) {
			this.m.getChild(yr).getChild(prd).setValues(key, val);
			//this.m.getChild(yr).getChild(prd).makeChild(tag);
			//this.m.getChild(yr).getChild(prd).getChild(tag).setValue(val);
			this.rows++;
		}

	}

	public String get(int year, int period, String key) {
		String yr = String.valueOf(year);
		String prd = String.valueOf(period);
		String val;

		try {
			//val = this.m.getChild(yr).getChild(prd).getChild(tag).getValue();
			val = this.m.getChild(yr).getChild(prd).getValues(key);
		} catch (NullPointerException e) {
			// value not found
			val = null;
		}

		return val;
	}

	public boolean hasPeriodData(int year, int period) {
		String yr = String.valueOf(year);
		String prd = String.valueOf(period);
		boolean hasData = false;

		try {
			hasData = this.m.getChild(yr).hasChild(prd);
		} catch (NullPointerException e) {
			// value not found, already false
		}

		return hasData;

	}

	public boolean hasData(int year, int period, String key) {
		String yr = String.valueOf(year);
		String prd = String.valueOf(period);
		boolean hasData = false;

		try {
			//hasData = this.m.getChild(yr).getChild(prd).hasChild(tag);
			hasData = this.m.getChild(yr).getChild(prd).hasChilds(key);
		} catch (NullPointerException e) {
			// value not found, already false
		}

		return hasData;

	}

	public int getRows() {
		return this.rows;
	}
	
	public int getMaxYear() {
		return this.maxYear;
	}
	
	public int getMinYear() {
		return this.minYear;
	}

}
