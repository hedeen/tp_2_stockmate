import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.HashMap;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;
import java.sql.CallableStatement;
import java.time.Duration;
import java.time.Instant;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.Connection;

public class Console {
	Connection con;

	public Console() {
		this.con = null;
		try {
			this.con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/SM2019?user=sm&password=stockmate");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		
		displayConsole(args);
		//Console c = new Console();
		//String[] stocklist = new String[] {"ADS"};
		//c.popSecFilings(stocklist, true);
	}
	
	public static void displayConsole(String[] args) {
		Instant start = Instant.now();
		int logId = -1;
		String[] stocklist;
		String help = "Argument help: " + System.lineSeparator() + "-a\t update all" + System.lineSeparator()
				+ "-p\t update all recent/historic prices" + System.lineSeparator()
				+ "-p7\t update prices that are older than a week" + System.lineSeparator()
				+ "-fp\t update most recent filing and recent/historic prices" + System.lineSeparator()
				+ "-smfp\t update most recent filings and prices for best stockmate picks" + System.lineSeparator()
				+ "-smp\t update all prices for stockmate picks (NO FILINGS)" + System.lineSeparator()
				+ "-sma\t update all filings and prices for best stockmate picks" + System.lineSeparator()
				+ "-em\t email stockmate picks..." + System.lineSeparator()
				+ "+\t list of stocks to add to tracking, seperated by commas IE stockmate.jar +HOFT,MHK,USAT"
				+ System.lineSeparator();

		if (args.length == 1) {
			Console c = new Console();
			StockPrices sp = new StockPrices();

			switch (args[0]) {
			case "-a": {
				System.out.println("updating all filings and prices...");
				stocklist = c.getTickersFromTable("stocks");
				logId = c.LogStart(args[0], stocklist.length);
				c.popSecFilings(stocklist, true);
				sp.updatePrices(stocklist);
				break;
			}
			case "-p": {
				System.out.println("updating all prices...");
				stocklist = c.getTickersFromTable("stocks");
				logId = c.LogStart(args[0], stocklist.length);
				sp.updatePrices(stocklist);
				break;
			}
			case "-p7": {
				System.out.println("updating prices that haven't seen an update in 7 days...");
				stocklist = c.getTickersFromQuery("SELECT tkr FROM currentprice_v WHERE DATEDIFF(NOW(),cdt)>=7");
				logId = c.LogStart(args[0], stocklist.length);
				sp.updatePrices(stocklist);
				break;
			}
			// sp.updatePrices(c.getTickersFromQuery("SELECT tkr FROM stocks WHERE tkr NOT
			// IN (SELECT tkr FROM rprices)"));
			case "-fp": {
				System.out.println("updating most recent filings for everyone, update all prices...");
				stocklist = c.getTickersFromTable("stocks");
				logId = c.LogStart(args[0], stocklist.length);
				c.popSecFilings(stocklist, false);
				sp.updatePrices(stocklist);
				break;
			}
			case "-smfp": {
				System.out.println("updating most recent filings and prices for stockmate picks...");
				stocklist = c.getTickersFromTable("top_picks_v");
				logId = c.LogStart(args[0], stocklist.length);
				c.popSecFilings(stocklist, false);
				sp.updateRecentPrices(stocklist);
				break;
			}
			case "-smp": {
				System.out.println("updating all prices for stockmate picks (NO FILINGS)...");
				stocklist = c.getTickersFromTable("top_picks_v");
				logId = c.LogStart(args[0], stocklist.length);
				sp.updatePrices(stocklist);
				break;
			}
			case "-sma": {
				System.out.println("updating all filings and prices for best stockmate picks...");
				stocklist = c.getTickersFromTable("top_picks_v");
				logId = c.LogStart(args[0], stocklist.length);
				c.popSecFilings(stocklist, true);
				sp.updatePrices(stocklist);
				break;
			}
			case "-em": {
				System.out.println("emailing stockmate picks...");
				stocklist = c.getTickersFromTable("top_picks_v");
				logId = c.LogStart(args[0], stocklist.length);
				EmailStatus es = new EmailStatus(stocklist, "/home/pi/Shared/SM/");
				es.GetHTMLTable(c.StockMatePicksResultSet());
				c.popSecFilings(stocklist, true);
				sp.updatePrices(stocklist);
				break;
			}

			default:
				if (args[0].startsWith("+")) {
					stocklist = args[0].replace("+", "").split(",");
					logId = c.LogStart(args[0], stocklist.length);
					c.loadCompanyInfo(stocklist);
					c.popSecFilings(stocklist, true);
					sp.updatePrices(stocklist);
					break;
				} else {
					logId = c.LogStart("UNK: " + args[0], -1);
					System.out.println("UNKNOWN COMMAND! " + System.lineSeparator() + help);
					System.exit(-1);
					break;
				}
			}
			c.loadEPSCalcsInDB(c.getTickersFromTable("stocks"));
			c.refreshMVs();
			int elapsed_min = (int) Math.floor((double) (Duration.between(start, Instant.now()).getSeconds() / 60));
			c.LogEnd(logId, elapsed_min);
			System.out.println("Total process took " + elapsed_min + " minutes.");
		} else {
			System.out.println(help);
		}
		// sp.updatePrices(c.getTickersFromQuery("SELECT tkr FROM stocks WHERE tkr NOT
		// IN (SELECT tkr FROM rprices)"));
		// c.loadFilingTable(c.getTickersFromTable("stocks"));
		// sp.updatePrices(c.getTickersFromTable("stocks"));
		// c.loadCompanyInfo(new String[] { "HOFT" }); //puts company descriptions into
		// stock table
		// c.loadQAdataInDB(new String[] { "HOFT" });// c.getTickers("D")); //
		// c.LoadAllSandP(false);
		// sp.updatePrices(new String[] { "HOFT" });
		// c.loadEPSCalcsInDB();
		// c.loadFilingTable(new String[] { "HOFT" });// populates filings table with
		// filing type, filing dates
		// for all tickers in the
		// // EPS table/view
		// //
		// int[] latestQtr = c.getLatestQuarterly("MHK");
		// System.out.println(latestQtr[0] + "+" + latestQtr[1]);
	}

	public int LogStart(String command, int records) {
		// inserts issued command into log table and returns the primary key for update
		// later
		int logId = -1;
		try {
			PreparedStatement stmt = null;
			stmt = this.con.prepareStatement("INSERT INTO SM2019.runlog(cmd,num) VALUES (?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, command);
			stmt.setInt(2, records);
			stmt.execute();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			logId = rs.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return logId;
	}

	public void LogEnd(int logId, int minutes) {
		// inserts issued command into log table and returns the primary key for update
		// later
		try {
			PreparedStatement stmt = null;
			stmt = this.con.prepareStatement("UPDATE SM2019.runlog SET edt=SYSDATE(), mnt=? WHERE id=?");
			stmt.setInt(1, minutes);
			stmt.setInt(2, logId);
			stmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet StockMatePicksResultSet() {

		Statement stm = null;
		ResultSet rst = null;
		try {
			stm = con.createStatement();
			rst = stm.executeQuery("select * from SM2019.top_picks_v");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rst;
	}

	public void refreshMVs() {
		try {
			System.out.print("Refreshing materialized views...");
			CallableStatement cs = this.con.prepareCall("{call refresh_mvs}");
			cs.executeQuery();
			System.out.println("success!");
		} catch (SQLException ex) {
		}
	}

	public void loadFilingTable(String[] tickers) {
		// populates filings table with filing type, filing dates for all tickers in the
		// EPS table/view

		PreparedStatement stmt = null;
		int cnt = 0;
		try {
			stmt = this.con.prepareStatement("REPLACE INTO SM2019.filings(tkr, ftp, fdt, ldt) VALUES (?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		for (String tkr : tickers) {
			++cnt;
			System.out.println(String.valueOf(cnt) + " of " + tickers.length + "...(" + tkr + ")");
			FilingSummary fs = new FilingSummary(tkr, new String[] { "esb" });
			fs.populateFilings();
			HashMap<LocalDate, String> filingDates = (HashMap<LocalDate, String>) fs.getFilingDates();
			for (Map.Entry<LocalDate, String> entry : filingDates.entrySet()) {
				LocalDate fdt = entry.getKey();
				String ftp = entry.getValue();
				Date dt = null;
				try {
					dt = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(fdt.toString()).getTime());
				} catch (ParseException e2) {
					e2.printStackTrace();
				}
				try {
					stmt.setString(1, tkr);
					stmt.setString(2, ftp);
					stmt.setDate(3, dt);
					stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					stmt.executeUpdate();
				} catch (Exception e3) {
					System.out.println(e3.getMessage());
					System.out.println("\t" + ftp);
				}
			}
		}
		// delete odd filings
		try {
			stmt = this.con
					.prepareStatement("DELETE FROM SM2019.filings WHERE ftp NOT IN ('10-K','10-K/A','10-Q','10-Q/A')");
			stmt.executeUpdate();
			System.out.println("Deleted records: " + stmt.getUpdateCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public int[] getLatestQuarterly(String ticker) {
//		int maxQtr = -1;
//		int maxYr = -1;
//		try {
//			ResultSet rs = this.con.prepareStatement(
//					"SELECT MAX(D.prd), MAX(D.yr) FROM SM2019.DATA JOIN (SELECT MAX(yr) mYr FROM SM2019.DATA WHERE prd > 0 AND tkr='"
//							+ ticker + "') M ON M.mYr = D.yr;")
//					.executeQuery();
//			if (rs.next()) {
//				maxQtr = rs.getInt(1);
//				maxYr = rs.getInt(2);
//			}
//		} catch (SQLException ex) {
//		}
//		return new int[] { maxYr, maxQtr };
//	}

	public String[] getTickersFromTable(String tablename) {
		ArrayList<String> tkrs = new ArrayList<String>();
		try {
			ResultSet rs = this.con
					.prepareStatement("SELECT DISTINCT tkr FROM SM2019." + tablename + " ORDER BY 1 DESC;")
					.executeQuery();
			while (rs.next()) {
				tkrs.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tkrs.toArray(new String[tkrs.size()]);
	}

	public String[] tickersNotUpdated(String tablename) {
		ArrayList<String> tkrs = new ArrayList<String>();
		try {
			String sql = String.format(
					"SELECT DISTINCT tkr FROM SM2019.%s WHERE (tkr,DATE(NOW())) NOT IN (SELECT tkr, MAX(DATE(ldt)) FROM SM2019.%s GROUP BY tkr) ORDER BY 1 DESC;",
					tablename, tablename);
			ResultSet rs = this.con.prepareStatement(sql).executeQuery();
			while (rs.next()) {
				tkrs.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tkrs.toArray(new String[tkrs.size()]);
	}

	public String[] getTickersFromQuery(String sql) {
		ArrayList<String> tkrs = new ArrayList<String>();
		try {
			ResultSet rs = this.con.prepareStatement(String.valueOf(sql) + " ORDER BY 1 DESC;").executeQuery();
			while (rs.next()) {
				tkrs.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tkrs.toArray(new String[tkrs.size()]);
	}

	public EPSdata getEPS(String ticker) {
		ArrayList<Integer> yrs = new ArrayList<Integer>();
		ArrayList<Double> vals = new ArrayList<Double>();
		try {
			ResultSet rs = this.con
					.prepareStatement("SELECT YEAR(INTERVAL 15 DAY + edt) yr, eps FROM SM2019.eps_v WHERE prd = 12 AND tkr='" + ticker
							+ "' ORDER BY 1 DESC;")
					.executeQuery();
			while (rs.next()) {
				yrs.add(rs.getInt(1));
				vals.add(rs.getDouble(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println();
		}
		double[] valsArray = vals.stream().mapToDouble(i -> i).toArray();
		double[] yrsArray = yrs.stream().mapToDouble(i -> i).toArray();
		return new EPSdata(yrsArray, valsArray);
	}

	public int getLatestAnnual(String ticker) {
		int maxYr = -1;
		try {
			ResultSet rs = this.con
					.prepareStatement("SELECT MAX(D.yr) FROM SM2019.DATA WHERE prd = 0 AND tkr='" + ticker + "';")
					.executeQuery();
			if (rs.next()) {
				maxYr = rs.getInt(1);
			}
		} catch (SQLException ex) {
		}
		return maxYr;
	}

	public ArrayList<String> hasAnnualsForXyrs(String ticker, int x) {
		int maxYr = this.getLatestAnnual(ticker);
		int minYr = maxYr - x;
		ArrayList<String> missingData = new ArrayList<String>();
		for (int yr = maxYr; yr > minYr; --yr) {
			try {
				String sql = "SELECT COUNT(*) FROM SM2019.DATA WHERE prd = 0 AND tkr='" + ticker + "' AND yr = " + yr
						+ ";";
				ResultSet rs = this.con.prepareStatement(sql).executeQuery();
				if (rs.next()) {
					if (rs.getInt(1) != 1) {
						missingData.add(String.valueOf(yr));
					}
				} else {
					missingData.add(String.valueOf(yr));
				}
			} catch (SQLException ex) {
			}
		}
		return missingData;
	}

	public void loadCompanyInfo(String[] tickers) {
		PreparedStatement stmt = null;
		StockList sl = new StockList();

		try {
			stmt = this.con.prepareStatement("INSERT IGNORE INTO SM2019.stocks(tkr, nfo) VALUES (?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		for (String t : tickers) {

			System.out.print("Loading company info for " + t + "...");
			try {
				stmt.setString(1, t.toUpperCase());
				stmt.setString(2, sl.getDesc(t.toUpperCase()));
				stmt.executeUpdate();
				TimeUnit.SECONDS.sleep(1L);
				System.out.println("success!");
			} catch (SQLException ex) {
				System.out.println("SQL error!");
			} catch (InterruptedException ex2) {
				System.out.println("Interrupt error!");
			}

		}
	}

	public void loadEPSCalcsInDB(String[] tickers) {
		PreparedStatement stmt = null;
		Console c = new Console();

		try {
			PreparedStatement ps = con.prepareStatement("delete from SM2019.calcs");
			ps.executeUpdate();
			stmt = this.con.prepareStatement(
					"REPLACE INTO SM2019.calcs(tkr, r3, m3, b3, r5, m5, b5, r9, m9, b9, r10, m10, b10, cdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.print("Updating EPS calcs for " + tickers.length + " stocks: [");
		for (String t : tickers) {

			System.out.print(t + " ");
			EPSdata e2 = c.getEPS(t);
			try {
				stmt.setString(1, t.toUpperCase());
				double[] eps3 = this.epsPayload(3, e2);
				double[] eps4 = this.epsPayload(5, e2);
				double[] eps5 = this.epsPayload(9, e2);
				double[] eps6 = this.epsPayload(10, e2);
				if (eps3 != null) {
					stmt.setDouble(2, eps3[0]);
					stmt.setDouble(3, eps3[1]);
					stmt.setDouble(4, eps3[2]);
				} else {
					stmt.setNull(2, 8);
					stmt.setNull(3, 8);
					stmt.setNull(4, 8);
				}
				if (eps4 != null) {
					stmt.setDouble(5, eps4[0]);
					stmt.setDouble(6, eps4[1]);
					stmt.setDouble(7, eps4[2]);
				} else {
					stmt.setNull(5, 8);
					stmt.setNull(6, 8);
					stmt.setNull(7, 8);
				}
				if (eps5 != null) {
					stmt.setDouble(8, eps5[0]);
					stmt.setDouble(9, eps5[1]);
					stmt.setDouble(10, eps5[2]);
				} else {
					stmt.setNull(8, 8);
					stmt.setNull(9, 8);
					stmt.setNull(10, 8);
				}
				if (eps6 != null) {
					stmt.setDouble(11, eps6[0]);
					stmt.setDouble(12, eps6[1]);
					stmt.setDouble(13, eps6[2]);
				} else {
					stmt.setNull(11, 8);
					stmt.setNull(12, 8);
					stmt.setNull(13, 8);
				}
				stmt.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
				stmt.executeUpdate();
			} catch (SQLException exc) {
				exc.printStackTrace();
			}
		}
		System.out.println("] DONE!");
	}

	public double[] epsPayload(int yr, EPSdata e) {
		LinearRegression lr = null;
		boolean valid = false;
		switch (yr) {
		case 3: {
			try {
				lr = new LinearRegression(e.get3Yrs(), e.get3Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid3;
			break;
		}
		case 5: {
			try {
				lr = new LinearRegression(e.get5Yrs(), e.get5Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid5;
			break;
		}
		case 9: {
			try {
				lr = new LinearRegression(e.get9Yrs(), e.get9Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid9;
			break;
		}
		case 10: {
			try {
				lr = new LinearRegression(e.get10Yrs(), e.get10Vals());
			} catch (IllegalArgumentException iae) {
				valid = false;
				break;
			}
			valid = e.valid10;
			break;
		}
		}
		if (!valid) {
			return null;
		}
		double slope = lr.slope();
		double r2 = lr.R2();
		double intercept = lr.intercept();
		if (!Double.isNaN(r2) && !Double.isNaN(slope) && !Double.isNaN(intercept)) {
			return new double[] { r2, slope, intercept };
		}
		return null;
	}

	public void popSecFilings(String[] tickers, boolean allFilings) {
		int cnt = 0;
		Instant start = Instant.now();
		for (String t : tickers) {
			if (++cnt > 1) {
				System.out.println(
						"Obtaining SEC Filings for " + t + "..." + String.valueOf(cnt) + " of " + tickers.length + " ["
								+ Math.floor((double) (Duration.between(start, Instant.now()).getSeconds() / (cnt - 1)))
								+ " secs/stock]");
			} else {
				System.out.println(
						"Obtaining SEC Filings for " + t + "..." + String.valueOf(cnt) + " of " + tickers.length);
			}
			FilingSummary fs = new FilingSummary(t, new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });

			if (allFilings) {
				fs.bufferAllFilings();
			} else {
				fs.bufferMostRecentFiling();
			}

			for (String[] v : fs.getTagArray()) {
				if (v[0] != null && v[1] != null && v[2] != null && v[3] != null) {
					String tkr = t.toUpperCase();
					LocalDate endDate = LocalDate.parse(v[1]);
					int periodLength = Integer.parseInt(v[2]);
					String colName = "sdt";
					String colData = fs.getTagData(endDate, periodLength, "startDate");
					String colType = "DATE";
					insertSecFiling(this.con, tkr, endDate, periodLength, colName, colData, colType);
					colName = v[0];
					colData = v[3];
					colType = "DBL";
					insertSecFiling(this.con, tkr, endDate, periodLength, colName, colData, colType);
				}
			}
			System.out.println(fs.getFilingPreview(","));
		}
	}

	private static boolean insertSecFiling(Connection con, String tkr, LocalDate endDate, int prd, String colName,
			String colData, String colType) {
		if (colData == "''" || colData.isBlank() || colData.isEmpty())
			return false;
		PreparedStatement stmt = null;
		Timestamp ldt = new Timestamp(System.currentTimeMillis());
		try {
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.secfilings (tkr, edt, prd, " + colName + ")"
						+ "VALUES (?,?,?,?) " + "ON DUPLICATE KEY UPDATE " + colName + "=?," + "ldt=?;");
				stmt.setString(1, tkr.toUpperCase());
				stmt.setObject(2, endDate);
				stmt.setInt(3, prd);
				stmt.setObject(4, colData);
				stmt.setObject(5, colData);
				stmt.setTimestamp(6, ldt);
				stmt.executeUpdate();
			} catch (NumberFormatException e2) {
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
