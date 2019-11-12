
public class EPSdata {
	// private double[] yrs;
	// private double[] vals;

	private double[] yrs3 = new double[3];
	private double[] vals3 = new double[3];
	public boolean valid3 = false;

	private double[] yrs5 = new double[5];
	private double[] vals5 = new double[5];
	public boolean valid5 = false;

	private double[] yrs9 = new double[9];
	private double[] vals9 = new double[9];
	public boolean valid9 = false;

	private double[] yrs10 = new double[10];
	private double[] vals10 = new double[10];
	public boolean valid10 = false;

	public EPSdata(double[] yrs, double[] vals) {
		// this.yrs = in_yrs;
		// this.vals = in_vals;

		if (yrs.length >= 3) {

			for (int i = 0; i < 10; i++) {

				if (i == 0) {

					yrs3[0] = yrs[0];
					vals3[0] = vals[0];
					if (yrs.length >= 3)
						valid3 = true;

					yrs5[0] = yrs[0];
					vals5[0] = vals[0];
					if (yrs.length >= 5)
						valid5 = true;

					yrs9[0] = yrs[0];
					vals9[0] = vals[0];
					if (yrs.length >= 9)
						valid9 = true;

					yrs10[0] = yrs[0];
					vals10[0] = vals[0];
					if (yrs.length >= 10)
						valid10 = true;

				} else {

					if (i < 3 && yrs.length >= 3) {
						if (yrs[i - 1] - 1 == yrs[i]) {
							yrs3[i] = yrs[i];
							vals3[i] = vals[i];
						} else {
							valid3 = false;
						}
					}

					if (i < 5 && yrs.length >= 5) {
						if (yrs[i - 1] - 1 == yrs[i]) {
							yrs5[i] = yrs[i];
							vals5[i] = vals[i];
						} else {
							valid5 = false;
						}
					}

					if (i < 9 && yrs.length >= 9) {
						if (yrs[i - 1] - 1 == yrs[i]) {
							yrs9[i] = yrs[i];
							vals9[i] = vals[i];
						} else {
							valid9 = false;
						}
					}

					if (i < 10 && yrs.length >= 10) {
						if (yrs[i - 1] - 1 == yrs[i]) {
							yrs10[i] = yrs[i];
							vals10[i] = vals[i];
						} else {
							valid10 = false;
						}
					}

				}
			}
		}
	}

	public double[] get3Yrs() {
		if (valid3) {
			return this.yrs3;
		} else {
			return null;
		}

	}

	public double[] get3Vals() {
		if (valid3) {
			return this.vals3;
		} else {
			return null;
		}

	}

	public double[] get5Yrs() {
		if (valid5) {
			return this.yrs5;
		} else {
			return null;
		}

	}

	public double[] get5Vals() {
		if (valid5) {
			return this.vals5;
		} else {
			return null;
		}

	}

	public double[] get9Yrs() {
		if (valid9) {
			return this.yrs9;
		} else {
			return null;
		}

	}

	public double[] get9Vals() {
		if (valid9) {
			return this.vals9;
		} else {
			return null;
		}

	}

	public double[] get10Yrs() {
		if (valid10) {
			return this.yrs10;
		} else {
			return null;
		}

	}

	public double[] get10Vals() {
		if (valid10) {
			return this.vals10;
		} else {
			return null;
		}

	}

}
