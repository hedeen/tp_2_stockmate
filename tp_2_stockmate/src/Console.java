import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Console {

	public static void main(String[] args) {

		Instant start = Instant.now();

		Console c = new Console();
		StockPrices sp = new StockPrices();
		//sp.updatePrices(c.getTickersFromTable("stocks"));
		sp.updatePrices(c.getTickersFromQuery("SELECT tkr FROM stocks WHERE tkr NOT IN (SELECT tkr FROM rprices)"));
		
		
		//c.loadCompanyDescriptions(); //puts company descriptions into stock table
		// c.loadQAdataInDB(new String[] { "HOFT" });// c.getTickers("D")); //
		//c.LoadAllSandP(false);
		//c.loadEPSCalcsInDB();
		//c.loadFilingTable();// populates filings table with filing type, filing dates for all tickers in the
		//					// EPS table/view
		// //
		// int[] latestQtr = c.getLatestQuarterly("MHK");
		// System.out.println(latestQtr[0] + "+" + latestQtr[1]);
		System.out.println("Total process took " + Math.floor(Duration.between(start, Instant.now()).getSeconds() / 60)
				+ " minutes.");
	}

	public void loadFilingTable() {

		// populates filings table with filing type, filing dates for all tickers in the
		// EPS table/view

		Connection con = connectToDB();
		PreparedStatement stmt = null;

		int cnt = 0;

		String[] tickers = getTickersFromTable("EPS");

		try {
			stmt = con.prepareStatement("REPLACE INTO SM2019.filings(tkr, ftp, fdt, ldt) VALUES (?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (String tkr : tickers) {
			cnt++;
			System.out.println(cnt + " of " + tickers.length + "...(" + tkr + ")");
			FilingSummary fs = new FilingSummary(tkr, new String[] { "esb" });
			fs.populateFilings("10-K");
			fs.populateFilings("10-Q");
			HashMap<LocalDate, String> filingDates = fs.getFilingDates();

			for (Entry<LocalDate, String> entry : filingDates.entrySet()) {
				LocalDate fdt = entry.getKey();
				String ftp = (String) entry.getValue();
				java.sql.Date dt = null;
				try {
					dt = new java.sql.Date(
							((java.util.Date) new SimpleDateFormat("yyyy-MM-dd").parse(fdt.toString())).getTime());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				try {
					stmt.setString(1, tkr);
					stmt.setString(2, ftp);
					stmt.setDate(3, dt);
					stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("\t" + ftp);
				}
			}

		}

	}

	public void LoadAllSandP(boolean OnlyMissing) {

		Console c = new Console();
		StockList sl = new StockList();

		List<String> sp500collection = Arrays.asList(sl.getSP500());
		List<String> currentInventory = Arrays.asList(getTickersFromTable("data"));
		List<String> diff = sp500collection.stream().filter(e -> !currentInventory.contains(e))
				.collect(Collectors.toList());
//		String[] missingTickers = sp500collection.toArray(new String[sp500collection.size()]);

		c.loadQAdataInDB(diff.toArray(new String[diff.size()]));
		// bad is AEE
		// last was DOV
		// "DOW","DTE","DUK","DRE","DD","DXC","ETFC","EMN","ETN","EBAY","ECL","EIX","EW","EA","EMR","ETR","EOG","EFX","EQIX","EQR","ESS","EL","EVRG","ES","RE","EXC","EXPE","EXPD","EXR","XOM","FFIV","FB","FAST","FRT","FDX","FIS","FITB","FE","FRC","FISV","FLT","FLIR","FLS","FMC","F","FTNT","FTV","FBHS","FOXA","FOX","BEN","FCX","GPS","GRMN","IT","GD","GE","GIS","GM","GPC","GILD","GL","GPN","GS","GWW","HAL","HBI","HOG","HIG","HAS","HCA","HCP","HP","HSIC","HSY","HES","HPE","HLT","HFC","HOLX","HD","HON","HRL","HST","HPQ","HUM","HBAN","HII","IEX","IDXX","INFO","ITW","ILMN","IR","INTC","ICE","IBM","INCY","IP","IPG","IFF","INTU","ISRG","IVZ","IPGP","IQV","IRM","JKHY","JEC","JBHT","SJM","JNJ","JCI","JPM","JNPR","KSU","K","KEY","KEYS","KMB","KIM","KMI","KLAC","KSS","KHC","KR","LB","LHX","LH","LRCX","LW","LVS","LEG","LDOS","LEN","LLY","LNC","LIN","LKQ","LMT","L","LOW","LYB","MTB","MAC","M","MRO","MPC","MKTX","MAR","MMC","MLM","MAS","MA","MKC","MXIM","MCD","MCK","MDT","MRK","MET","MTD","MGM","MCHP","MU","MSFT","MAA","MHK","TAP","MDLZ","MNST","MCO","MS","MOS","MSI","MSCI","MYL","NDAQ","NOV","NTAP","NFLX","NWL","NEM","NWSA","NWS","NEE","NLSN","NKE","NI","NBL","JWN","NSC","NTRS","NOC","NCLH","NRG","NUE","NVDA","NVR","ORLY","OXY","OMC","OKE","ORCL","PCAR","PKG","PH","PAYX","PYPL","PNR","PBCT","PEP","PKI","PRGO","PFE","PM","PSX","PNW","PXD","PNC","PPG","PPL","PFG","PG","PGR","PLD","PRU","PEG","PSA","PHM","PVH","QRVO","PWR","QCOM","DGX","RL","RJF","RTN","O","REG","REGN","RF","RSG","RMD","RHI","ROK","ROL","ROP","ROST","RCL","CRM","SBAC","SLB","STX","SEE","SRE","SHW","SPG","SWKS","SLG","SNA","SO","LUV","SPGI","SWK","SBUX","STT","SYK","STI","SIVB","SYMC","SYF","SNPS","SYY","TMUS","TROW","TTWO","TPR","TGT","TEL","FTI","TFX","TXN","TXT","TMO","TIF","TWTR","TJX","TSCO","TDG","TRV","TRIP","TSN","UDR","ULTA","USB","UAA","UA","UNP","UAL","UNH","UPS","URI","UTX","UHS","UNM","VFC","VLO","VAR","VTR","VRSN","VRSK","VZ","VRTX","VIAB","V","VNO","VMC","WAB","WMT","WBA","DIS","WM","WAT","WEC","WCG","WFC","WELL","WDC","WU","WRK","WY","WHR","WMB","WLTW","WYNN","XEL","XRX","XLNX","XYL","YUM","ZBH","ZION","ZTS"});
		// c.loadDataIntoDB(new String[]
		// {"AAL","AEP","AXP","AIG","AMT","AWK","AMP","ABC","AME","AMGN","APH","ADI","ANSS","ANTM","AON","AOS","APA","AIV","AAPL","AMAT","APTV","ADM","ARNC","ANET","AJG","AIZ","ATO","T","ADSK","ADP","AZO","AVB","AVY","BKR","BLL","BAC","BK","BAX","BBT","BDX","BRK.B","BBY","BIIB","BLK","HRB","BA","BKNG","BWA","BXP","BSX","BMY","AVGO","BR","BF.B","CHRW","COG","CDNS","CPB","COF","CPRI","CAH","KMX","CCL","CAT","CBOE","CBRE","CBS","CDW","CE","CELG","CNC","CNP","CTL","CERN","CF","SCHW","CHTR","CVX","CMG","CB","CHD","CI","XEC","CINF","CTAS","CSCO","C","CFG","CTXS","CLX","CME","CMS","KO","CTSH","CL","CMCSA","CMA","CAG","CXO","COP","ED","STZ","COO","CPRT","GLW","CTVA","COST","COTY","CCI","CSX","CMI","CVS","DHI","DHR","DRI","DVA","DE","DAL","XRAY","DVN","FANG","DLR","DFS","DISCA","DISCK","DISH","DG","DLTR","D","DOV","DOW","DTE","DUK","DRE","DD","DXC","ETFC","EMN","ETN","EBAY","ECL","EIX","EW","EA","EMR","ETR","EOG","EFX","EQIX","EQR","ESS","EL","EVRG","ES","RE","EXC","EXPE","EXPD","EXR","XOM","FFIV","FB","FAST","FRT","FDX","FIS","FITB","FE","FRC","FISV","FLT","FLIR","FLS","FMC","F","FTNT","FTV","FBHS","FOXA","FOX","BEN","FCX","GPS","GRMN","IT","GD","GE","GIS","GM","GPC","GILD","GL","GPN","GS","GWW","HAL","HBI","HOG","HIG","HAS","HCA","HCP","HP","HSIC","HSY","HES","HPE","HLT","HFC","HOLX","HD","HON","HRL","HST","HPQ","HUM","HBAN","HII","IEX","IDXX","INFO","ITW","ILMN","IR","INTC","ICE","IBM","INCY","IP","IPG","IFF","INTU","ISRG","IVZ","IPGP","IQV","IRM","JKHY","JEC","JBHT","SJM","JNJ","JCI","JPM","JNPR","KSU","K","KEY","KEYS","KMB","KIM","KMI","KLAC","KSS","KHC","KR","LB","LHX","LH","LRCX","LW","LVS","LEG","LDOS","LEN","LLY","LNC","LIN","LKQ","LMT","L","LOW","LYB","MTB","MAC","M","MRO","MPC","MKTX","MAR","MMC","MLM","MAS","MA","MKC","MXIM","MCD","MCK","MDT","MRK","MET","MTD","MGM","MCHP","MU","MSFT","MAA","MHK","TAP","MDLZ","MNST","MCO","MS","MOS","MSI","MSCI","MYL","NDAQ","NOV","NTAP","NFLX","NWL","NEM","NWSA","NWS","NEE","NLSN","NKE","NI","NBL","JWN","NSC","NTRS","NOC","NCLH","NRG","NUE","NVDA","NVR","ORLY","OXY","OMC","OKE","ORCL","PCAR","PKG","PH","PAYX","PYPL","PNR","PBCT","PEP","PKI","PRGO","PFE","PM","PSX","PNW","PXD","PNC","PPG","PPL","PFG","PG","PGR","PLD","PRU","PEG","PSA","PHM","PVH","QRVO","PWR","QCOM","DGX","RL","RJF","RTN","O","REG","REGN","RF","RSG","RMD","RHI","ROK","ROL","ROP","ROST","RCL","CRM","SBAC","SLB","STX","SEE","SRE","SHW","SPG","SWKS","SLG","SNA","SO","LUV","SPGI","SWK","SBUX","STT","SYK","STI","SIVB","SYMC","SYF","SNPS","SYY","TMUS","TROW","TTWO","TPR","TGT","TEL","FTI","TFX","TXN","TXT","TMO","TIF","TWTR","TJX","TSCO","TDG","TRV","TRIP","TSN","UDR","ULTA","USB","UAA","UA","UNP","UAL","UNH","UPS","URI","UTX","UHS","UNM","VFC","VLO","VAR","VTR","VRSN","VRSK","VZ","VRTX","VIAB","V","VNO","VMC","WAB","WMT","WBA","DIS","WM","WAT","WEC","WCG","WFC","WELL","WDC","WU","WRK","WY","WHR","WMB","WLTW","WYNN","XEL","XRX","XLNX","XYL","YUM","ZBH","ZION","ZTS"});
		// bad was "DOW"
		/*
		 * c.loadQAdataInDB(new String[] { "RCL", "CRM", "SBAC", "SLB", "STX", "SEE",
		 * "SRE", "SHW", "SPG", "SWKS", "SLG", "SNA", "SO", "LUV", "SPGI", "SWK",
		 * "SBUX", "STT", "SYK", "STI", "SIVB", "SYMC", "SYF", "SNPS", "SYY", "TMUS",
		 * "TROW", "TTWO", "TPR", "TGT", "TEL", "FTI", "TFX", "TXN", "TXT", "TMO",
		 * "TIF", "TWTR", "TJX", "TSCO", "TDG", "TRV", "TRIP", "TSN", "UDR", "ULTA",
		 * "USB", "UAA", "UA", "UNP", "UAL", "UNH", "UPS", "URI", "UTX", "UHS", "UNM",
		 * "VFC", "VLO", "VAR", "VTR", "VRSN", "VRSK", "VZ", "VRTX", "VIAB", "V", "VNO",
		 * "VMC", "WAB", "WMT", "WBA", "DIS", "WM", "WAT", "WEC", "WCG", "WFC", "WELL",
		 * "WDC", "WU", "WRK", "WY", "WHR", "WMB", "WLTW", "WYNN", "XEL", "XRX", "XLNX",
		 * "XYL", "YUM", "ZBH", "ZION", "ZTS" });
		 */
		// System.out.println(c.getLatestQuarterly("MHK")[0]);
		// System.out.println(c.getLatestQuarterly("MHK")[1]);
		// System.out.println(c.hasAnnualsForXyrs("MHK", 5));

	}

	public int[] getLatestQuarterly(String ticker) {
		Connection con = connectToDB();
		int maxQtr = -1;
		int maxYr = -1;
		ResultSet rs;
		try {
			rs = con.prepareStatement("SELECT MAX(D.prd), MAX(D.yr) " + "FROM SM2019.DATA "
					+ "JOIN (SELECT MAX(yr) mYr FROM SM2019.DATA WHERE prd > 0 AND tkr='" + ticker
					+ "') M ON M.mYr = D.yr;").executeQuery();

			if (rs.next()) {
				maxQtr = rs.getInt(1);
				maxYr = rs.getInt(2);
			}

		} catch (SQLException e) {

		}

		closeCommit(con);

		return new int[] { maxYr, maxQtr };
	}

	public String[] getTickersFromTable(String tablename) {
		Connection con = connectToDB();
		ResultSet rs;
		ArrayList<String> tkrs = new ArrayList<String>();

		try {
			rs = con.prepareStatement("SELECT DISTINCT tkr FROM SM2019." + tablename + " ORDER BY 1 DESC;")
					.executeQuery();

			while (rs.next()) {
				tkrs.add(rs.getString(1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeCommit(con);

		return tkrs.toArray(new String[tkrs.size()]);
	}
	
	public String[] getTickersFromQuery(String sql) {
		Connection con = connectToDB();
		ResultSet rs;
		ArrayList<String> tkrs = new ArrayList<String>();

		try {
			rs = con.prepareStatement(sql + " ORDER BY 1 DESC;")
					.executeQuery();

			while (rs.next()) {
				tkrs.add(rs.getString(1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeCommit(con);

		return tkrs.toArray(new String[tkrs.size()]);
	}

	public EPSdata getEPS(String ticker) {
		Connection con = connectToDB();
		ResultSet rs;
		ArrayList<Integer> yrs = new ArrayList<Integer>();
		ArrayList<Double> vals = new ArrayList<Double>();

		try {
			rs = con.prepareStatement(
					"SELECT yr, eps FROM SM2019.EPS " + "WHERE prd = 0 AND tkr='" + ticker + "' ORDER BY 1 DESC;")
					.executeQuery();

			while (rs.next()) {
				yrs.add(rs.getInt(1));
				vals.add(rs.getDouble(2));

			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println();
		}

		closeCommit(con);
		double[] valsArray = vals.stream().mapToDouble(i -> i).toArray();
		double[] yrsArray = yrs.stream().mapToDouble(i -> i).toArray();

		return new EPSdata(yrsArray, valsArray);
	}

	public int getLatestAnnual(String ticker) {
		Connection con = connectToDB();
		int maxYr = -1;
		ResultSet rs;
		try {
			rs = con.prepareStatement(
					"SELECT MAX(D.yr) " + "FROM SM2019.DATA " + "WHERE prd = 0 AND tkr='" + ticker + "';")
					.executeQuery();

			if (rs.next()) {
				maxYr = rs.getInt(1);
			}

		} catch (SQLException e) {

		}

		closeCommit(con);

		return maxYr;
	}

	public ArrayList<String> hasAnnualsForXyrs(String ticker, int x) {
		Connection con = connectToDB();
		int maxYr = getLatestAnnual(ticker);
		int minYr = maxYr - x;
		ArrayList<String> missingData = new ArrayList<String>();
		ResultSet rs;

		for (int yr = maxYr; yr > minYr; yr--) {
			try {
				String sql = "SELECT COUNT(*) " + "FROM SM2019.DATA " + "WHERE prd = 0 AND tkr='" + ticker
						+ "' AND yr = " + yr + ";";
				rs = con.prepareStatement(sql).executeQuery();

				if (rs.next()) {
					if (rs.getInt(1) != 1) {
						missingData.add(String.valueOf(yr));
					}
				} else {
					missingData.add(String.valueOf(yr));
				}

			} catch (SQLException e) {

			}
		}

		closeCommit(con);

		return missingData;
	}

//	public int[][] getXtags(String ticker){
//		
//	}

	public void closeCommit(Connection con) {
		try {
			con.commit();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection connectToDB() {

		Connection con = null;

		try {
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/SM2019?user=sm&password=stockmate");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		return con;
	}
	
	public void loadCompanyDescriptions() {

		Connection con = connectToDB();
		PreparedStatement stmt = null;

		Console c = new Console();
		StockList sl = new StockList();

		int cnt = 0;

		// if (tickers.length == 0)
		String[] tickers = sl.getSP500();

		try {
			stmt = con.prepareStatement(
					"INSERT IGNORE INTO SM2019.stocks(tkr, cpr) VALUES (?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (String t : tickers) {
			cnt++;
			System.out.println(cnt + " of " + tickers.length + "...(" + t + ")");
			try {
				stmt.setString(1,t.toUpperCase());
				stmt.setString(2,sl.getDesc(t.toUpperCase()));
				stmt.executeUpdate();
				TimeUnit.SECONDS.sleep(1);
			} catch (SQLException | InterruptedException e1) {
				//ignore
			}
			

		}
	}
	public void loadEPSCalcsInDB() {

		Connection con = connectToDB();
		PreparedStatement stmt = null;

		Console c = new Console();

		int cnt = 0;

		// if (tickers.length == 0)
		String[] tickers = getTickersFromTable("EPS");

		try {
			stmt = con.prepareStatement(
					"REPLACE INTO SM2019.CALCS(tkr, r3, m3, b3, r5, m5, b5, r9, m9, b9, r10, m10, b10, cdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (String t : tickers) {
			cnt++;
			System.out.println(cnt + " of " + tickers.length + "...(" + t + ")");
			EPSdata e = c.getEPS(t);

			try {
				stmt.setString(1, t.toUpperCase());
				double[] eps3 = epsPayload(3, e);
				double[] eps5 = epsPayload(5, e);
				double[] eps9 = epsPayload(9, e);
				double[] eps10 = epsPayload(10, e);

				if (eps3 != null) {
					stmt.setDouble(2, eps3[0]);
					stmt.setDouble(3, eps3[1]);
					stmt.setDouble(4, eps3[2]);
				} else {
					stmt.setNull(2, java.sql.Types.DOUBLE);
					stmt.setNull(3, java.sql.Types.DOUBLE);
					stmt.setNull(4, java.sql.Types.DOUBLE);
				}
				if (eps5 != null) {
					stmt.setDouble(5, eps5[0]);
					stmt.setDouble(6, eps5[1]);
					stmt.setDouble(7, eps5[2]);
				} else {
					stmt.setNull(5, java.sql.Types.DOUBLE);
					stmt.setNull(6, java.sql.Types.DOUBLE);
					stmt.setNull(7, java.sql.Types.DOUBLE);
				}
				if (eps9 != null) {
					stmt.setDouble(8, eps9[0]);
					stmt.setDouble(9, eps9[1]);
					stmt.setDouble(10, eps9[2]);
				} else {
					stmt.setNull(8, java.sql.Types.DOUBLE);
					stmt.setNull(9, java.sql.Types.DOUBLE);
					stmt.setNull(10, java.sql.Types.DOUBLE);
				}
				if (eps10 != null) {
					stmt.setDouble(11, eps10[0]);
					stmt.setDouble(12, eps10[1]);
					stmt.setDouble(13, eps10[2]);
				} else {
					stmt.setNull(11, java.sql.Types.DOUBLE);
					stmt.setNull(12, java.sql.Types.DOUBLE);
					stmt.setNull(13, java.sql.Types.DOUBLE);
				}
				stmt.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
				stmt.executeUpdate();
			} catch (SQLException exc) {
				// TODO Auto-generated catch block
				exc.printStackTrace();
			}

		}

		closeCommit(con);

	}

	public double[] epsPayload(int yr, EPSdata e) {

		LinearRegression lr = null;
		boolean valid = false;

		switch (yr) {
		case 3:
			try {
				lr = new LinearRegression(e.get3Yrs(), e.get3Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid3;
			break;
		case 5:
			try {
				lr = new LinearRegression(e.get5Yrs(), e.get5Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid5;
			break;
		case 9:
			try {
				lr = new LinearRegression(e.get9Yrs(), e.get9Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid9;
			break;
		case 10:
			try {
				lr = new LinearRegression(e.get10Yrs(), e.get10Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid10;
			break;
		}

		if (valid) {
			double slope = lr.slope();
			double r2 = lr.R2();
			double intercept = lr.intercept();

			if (!Double.isNaN(r2) && !Double.isNaN(slope) && !Double.isNaN(intercept)) {
				return new double[] { r2, slope, intercept };
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	public void loadQAdataInDB(String[] tickers) {

		Connection con = connectToDB();
		//PreparedStatement stmt = null;

		int cnt = 0;

		if (tickers.length == 0) {
//			args = new String[] { "BERY", "HOFT", "BIIB", "MHK", "WEN", "IVZ", "VLP", "MMP", "MAN", "LTC", "SBNY",
//					"ASR", "OMAB", "PAC", "RHP", "CRI", "THO", "TROW", "EGBN", "NCLH", "MGA", "PKG", "SNA", "LABL",
//					"PBCT", "WAL", "LUV", "OMC", "TOWN", "BLK", "TU", "SEDG", "BAP", "FB", "BJRI", "EGOV", "USAT",
//					"MCK" };
			tickers = new String[] { "PBCT" };
		}

//		try {
//			stmt = con.prepareStatement(
//					"REPLACE INTO SM2019.D(tkr, yr, prd, esb, esd, ern, shr, wsh, pft, ldt) VALUES (?, ?, ?, ?, ?)");
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
		Instant start = Instant.now();
		for (String t : tickers) {
			cnt++;

			if (cnt > 1) {
				System.out.println(cnt + " of " + tickers.length + "...(" + t + ") ["
						+ Math.floor(Duration.between(start, Instant.now()).getSeconds() / (cnt - 1)) + " secs/stock]");

			} else {
				System.out.println(cnt + " of " + tickers.length + "...(" + t + ")");
			}
			;

			FilingSummary fs = new FilingSummary(t, new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });
			fs.bufferAllFilings();

			for (String[] v : fs.getTagArray()) {

				if (v[0] == null || v[1] == null || v[2] == null || v[3] == null) {
					// do nothing
				} else {

					String tkr = t.toUpperCase();
					int yr = Integer.parseInt(v[1]);
					int prd = Integer.parseInt(v[2]);

					// start date
					String colName = "sdt";
					String colData = fs.getTagData(yr, prd, "startDate");
					String colType = "DATE";
					insertFilingData(con, tkr, yr, prd, colName, colData, colType);

					// end date
					colName = "edt";
					colData = fs.getTagData(yr, prd, "endDate");
					colType = "DATE";
					insertFilingData(con, tkr, yr, prd, colName, colData, colType);

//					// filing date
//					colName = "fdt";
//					colData = fs.getTagData(yr, prd, "filingDate");
//					colType = "DATE";
//					insertFilingData(con, tkr, yr, prd, colName, colData, colType);

					// rest of array data
					colName = v[0];
					colData = v[3];
					colType = "DBL";
					insertFilingData(con, tkr, yr, prd, colName, colData, colType);

//						try {
//							dt = new java.sql.Date(((java.util.Date) new SimpleDateFormat("yyyy-MM-dd")
//									.parse(fs.getTagData(yr, prd, "startDate"))).getTime());
//							stmt = dataTblUpdate(con, "sdt");
//							stmt.setString(1, t.toUpperCase()); // tkr
//							stmt.setInt(2, yr); // yr
//							stmt.setInt(3, prd); // prd
//							stmt.setDate(4, dt); // sdt
//							stmt.setTimestamp(5, ldt); // ldt
//							stmt.setDate(6, dt); // sdt (if dupe key)
//							stmt.setTimestamp(7, ldt); // (if dupe key)
//							stmt.executeUpdate();
//						} catch (NumberFormatException e2) {
//							// skip update
//							stmt.cancel();
//						} catch (ParseException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//							stmt.cancel();
//						}
//
//						// end date
//						try {
//							dt = new java.sql.Date(((java.util.Date) new SimpleDateFormat("yyyy-MM-dd")
//									.parse(fs.getTagData(yr, prd, "endDate"))).getTime());
//							stmt = dataTblUpdate(con, "edt");
//							stmt.setString(1, t.toUpperCase()); // tkr
//							stmt.setInt(2, yr); // yr
//							stmt.setInt(3, prd); // prd
//							stmt.setDate(4, dt); // sdt
//							stmt.setTimestamp(5, ldt); // ldt
//							stmt.setDate(6, dt); // sdt (if dupe key)
//							stmt.setTimestamp(7, ldt); // (if dupe key)
//							stmt.executeUpdate();
//						} catch (NumberFormatException e2) {
//							// skip update
//							stmt.cancel();
//						} catch (ParseException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//							stmt.cancel();
//						}
//
//						try {
//							stmt = dataTblUpdate(con, v[0]);
//							stmt.setString(1, t.toUpperCase()); // tkr
//							stmt.setInt(2, Integer.parseInt(v[1])); // yr
//							stmt.setInt(3, Integer.parseInt(v[2])); // prd
//							stmt.setDouble(4, Double.parseDouble(v[3])); // col
//							stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // ldt
//							stmt.setDouble(6, Double.parseDouble(v[3])); // col (if dupe key)
//							stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // ldt (if dupe key)
//							stmt.executeUpdate();
//						} catch (NumberFormatException e2) {
//							// skip update
//							stmt.cancel();
//						}

				}

			}

			System.out.println(fs.getFilingPreview(","));
		}

		closeCommit(con);

	}

	private static boolean insertFilingData(Connection con, String tkr, int yr, int prd, String colName, String colData,
			String colType) {
		PreparedStatement stmt = null;
		Timestamp ldt = new Timestamp(System.currentTimeMillis());
		try {
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.data (tkr, yr, prd, " + colName + ", ldt)"
						+ "VALUES (?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE " + colName + "=?," + "ldt=?;");

				stmt.setString(1, tkr.toUpperCase()); // tkr
				stmt.setInt(2, yr); // yr
				stmt.setInt(3, prd); // prd

				if (colType.equalsIgnoreCase("DATE")) {
					java.sql.Date dt = new java.sql.Date(
							((java.util.Date) new SimpleDateFormat("yyyy-MM-dd").parse(colData)).getTime());
					stmt.setDate(4, dt); // col dt
					stmt.setDate(6, dt); // col dt (if dupe key)
				} else if (colType.equalsIgnoreCase("DBL")) {
					stmt.setDouble(4, Double.parseDouble(colData)); // dbl col
					stmt.setDouble(6, Double.parseDouble(colData)); // dbl col (if dupe key)
				} else {
					System.out.println("fatal error, unknown data type");
					System.exit(-1);
				}

				stmt.setTimestamp(5, ldt); // ldt
				stmt.setTimestamp(7, ldt); // (if dupe key)
				stmt.executeUpdate();
			} catch (NumberFormatException e2) {
				stmt.cancel();
				return false;
			} catch (ParseException e) {
				e.printStackTrace();
				stmt.cancel();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
