import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;

public class StockPrices {

	private String[] apiList = { "OYZUJQ7KPDJV69PS", "HNLXG9VCGGTK92YV", "U1IP4DNKQ4GO301G", "Q96186O1RQNJQFC7",
			"MGYZGVJEU1IRRDZ7", "07MRW7RDV23EB9DW", "FWKJB3QFARJEH5OI", "KZ59U7Z78IU6UXJW", "06OHTXWS1RCUVCFA",
			"3BW1YQ0UIVZ17XM4", "868CC591ASZDT30J", "WTCLECODPYSKTHJQ", "K01WGBTESTEBYTBK", "868CC591ASZDT30J",
			"EYW9JROENT30B8ON", "RTFY5W8YVE6DAF2O", "KREGHQ3FRYT09H4S", "SLN3EW4L0UXQ38F5", "DAB7RANDIUK2SSCX",
			"OW5A8ZQZSVBHJG5L" };
	private Connection con = null;
	private int consecutiveFails=0;
	// private Statement stm = null;
//	private String fileDelimiter;
	private int waitMilliSecs = 0;

	public static void main(String[] args) {

		StockPrices sp = new StockPrices();

		sp.updatePrices(new String[] { "ULTA" });

	}

	public StockPrices() {
		// create database connection
		try {
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/SM2019?autoReconnect=true&useSSL=false",
					"sm", "stockmate");

			// stm = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

//		if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
//			fileDelimiter = "\\";
//		} else {
//			fileDelimiter = "/";
//		}

	}

	public void updatePrices(String[] stocks) {

		PreparedStatement stmt = null;
		String ticker;
		ArrayList<String[]> priceRecentData = null;
		ArrayList<String[]> priceHistoricData = null;

		String api = null;
		int totalCount = 0;

		for (int i = 0; i < stocks.length; i++) {

			int entryCount = 0;
			ticker = stocks[i];
			System.out.print("Updating Prices for " + ticker + " (" + (i + 1) + " of " + (stocks.length) + ")");

//			Runtime r = Runtime.getRuntime();
//			Process p = null;

			api = apiList[new Random().nextInt(20)];
			System.out.print("; API call with: " + api);

//			try {

//			if (fileDelimiter.equals("/")) {
//				// linux
			priceRecentData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
					+ ticker + "&apikey=" + api + "&datatype=csv");
			priceHistoricData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol="
					+ ticker + "&apikey=" + api + "&datatype=csv");

//			} else {
//				// windows
//				priceRecentData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + ticker
//						+ "&apikey=" + api + "&datatype=csv");
//				priceHistoricData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol="
//						+ ticker + "&apikey=" + api + "&datatype=csv");
//			}
			if(consecutiveFails>=4) {
				System.out.print("; API calls failed 4 times, it is likely we exeeded the allottment for the day");
				
				try {
					stmt.close();
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("; ABORTING!");
				return;
				
			}else {
				System.out.print("; API calls complete");
			}
			

			// recent data
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.rprices(tkr, cdt, cpr) VALUES (?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE cpr=?, ldt=?");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			for (int j = 1; j < priceRecentData.size(); j++) {

				try {
					stmt.setString(1, ticker.toUpperCase()); // ticker
					stmt.setDate(2, Date.valueOf((priceRecentData.get(j)[0]))); // close date
					stmt.setDouble(3, Double.parseDouble(priceRecentData.get(j)[5].replace(",", ""))); // close price
					stmt.setDouble(4, Double.parseDouble(priceRecentData.get(j)[5].replace(",", ""))); // close pr if
																										// duped
					stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // load date if duped
					stmt.executeUpdate();
					entryCount++;
				} catch (Exception e) {
					System.out.print("ERROR " + e.getMessage());
				}
			}

			// historic data
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.hprices(tkr, mend, mhi, mlo) VALUES (?, ?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE mhi=?, mlo=?, ldt=?");
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int j = 2; j < Math.min(priceHistoricData.size(), 122); j++) { // 12*10yr = 120 + 2 skipped lines
				double high = Double.parseDouble(priceHistoricData.get(j)[2].replace(",", ""));
				double low = Double.parseDouble(priceHistoricData.get(j)[3].replace(",", ""));
				double close = Double.parseDouble(priceHistoricData.get(j)[4].replace(",", ""));
				double adj_close = Double.parseDouble(priceHistoricData.get(j)[5].replace(",", ""));
				double adj_high = adj_close / close * high;
				double adj_low = adj_close / close * low;

				try {
					stmt.setString(1, ticker.toUpperCase()); // ticker
					stmt.setDate(2, Date.valueOf((priceHistoricData.get(j)[0]))); // month end
					stmt.setDouble(3, adj_high); // month high
					stmt.setDouble(4, adj_low); // month low
					stmt.setDouble(5, adj_high); // month high
					stmt.setDouble(6, adj_low); // month low
					stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // load date
					stmt.executeUpdate();
					entryCount++;
				} catch (Exception e) {
					System.out.print("ERROR " + e.getMessage());
				}
			}

			System.out.print("; data insert complete. " + entryCount + " records inserted." + System.lineSeparator());
			totalCount = totalCount + entryCount;
		}
		System.out.println("Inserted records: " + totalCount);

		// delete 10yr old records
//		try {
//			stmt = con.prepareStatement("DELETE FROM SM2019.hprices WHERE mend < DATE_SUB(NOW(), INTERVAL 10 YEAR)");
//			stmt.executeUpdate();
//
//			System.out.println("Deleted records: " + stmt.getUpdateCount());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		try {
			stmt.close();
			con.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public void updateHistoricPrices(String[] stocks) {

		PreparedStatement stmt = null;
		String ticker;
		ArrayList<String[]> priceHistoricData = null;
		String api = null;
		int totalCount = 0;

		for (int i = 0; i < stocks.length; i++) {

			int entryCount = 0;
			ticker = stocks[i];
			System.out
					.print("Updating Historic Prices for " + ticker + " (" + (i + 1) + " of " + (stocks.length) + ")");

			api = apiList[new Random().nextInt(20)];
			System.out.print("; API call with: " + api);

			priceHistoricData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol="
					+ ticker + "&apikey=" + api + "&datatype=csv");

			if(consecutiveFails>=4) {
				System.out.print("; API calls failed 4 times, it is likely we exeeded the allottment for the day");
				
				try {
					stmt.close();
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("; ABORTING!");
				return;
				
			}else {
				System.out.print("; API calls complete");
			}

			// historic data
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.hprices(tkr, mend, mhi, mlo) VALUES (?, ?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE mhi=?, mlo=?, ldt=?");
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int j = 2; j < Math.min(priceHistoricData.size(), 122); j++) { // 12*10yr = 120 + 2 skipped lines
				double high = Double.parseDouble(priceHistoricData.get(j)[2].replace(",", ""));
				double low = Double.parseDouble(priceHistoricData.get(j)[3].replace(",", ""));
				double close = Double.parseDouble(priceHistoricData.get(j)[4].replace(",", ""));
				double adj_close = Double.parseDouble(priceHistoricData.get(j)[5].replace(",", ""));
				double adj_high = adj_close / close * high;
				double adj_low = adj_close / close * low;

				try {
					stmt.setString(1, ticker.toUpperCase()); // ticker
					stmt.setDate(2, Date.valueOf((priceHistoricData.get(j)[0]))); // month end
					stmt.setDouble(3, adj_high); // month high
					stmt.setDouble(4, adj_low); // month low
					stmt.setDouble(5, adj_high); // month high
					stmt.setDouble(6, adj_low); // month low
					stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // load date
					stmt.executeUpdate();
					entryCount++;
				} catch (Exception e) {
					System.out.print("ERROR " + e.getMessage());
				}
			}

			System.out.print("; data insert complete. " + entryCount + " records inserted." + System.lineSeparator());
			totalCount = totalCount + entryCount;
		}
		System.out.println("Inserted records: " + totalCount);

		// delete 10yr old records
//		try {
//			stmt = con.prepareStatement("DELETE FROM SM2019.hprices WHERE mend < DATE_SUB(NOW(), INTERVAL 10 YEAR)");
//			stmt.executeUpdate();
//
//			System.out.println("Deleted records: " + stmt.getUpdateCount());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		try {
			stmt.close();
			con.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public void updateRecentPrices(String[] stocks) {

		PreparedStatement stmt = null;

		String ticker;
		ArrayList<String[]> priceRecentData = null;
		String api = null;
		int totalCount = 0;

		for (int i = 0; i < stocks.length; i++) {

			int entryCount = 0;
			ticker = stocks[i];
			System.out.print("Updating Recent Prices for " + ticker + " (" + (i + 1) + " of " + (stocks.length) + ")");

			api = apiList[new Random().nextInt(20)];
			System.out.print("; API call with: " + api);

			priceRecentData = jget("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
					+ ticker + "&apikey=" + api + "&datatype=csv");

			if(consecutiveFails>=4) {
				System.out.print("; API calls failed 4 times, it is likely we exeeded the allottment for the day");
				
				try {
					stmt.close();
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("; ABORTING!");
				return;
				
			}else {
				System.out.print("; API calls complete");
			}

			// recent data
			try {
				stmt = con.prepareStatement("INSERT INTO SM2019.rprices(tkr, cdt, cpr) VALUES (?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE cpr=?, ldt=?");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			for (int j = 1; j < priceRecentData.size(); j++) {

				try {
					stmt.setString(1, ticker.toUpperCase()); // ticker
					stmt.setDate(2, Date.valueOf((priceRecentData.get(j)[0]))); // close date
					stmt.setDouble(3, Double.parseDouble(priceRecentData.get(j)[5].replace(",", ""))); // close price
					stmt.setDouble(4, Double.parseDouble(priceRecentData.get(j)[5].replace(",", ""))); // close price if
																										// duped
					stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // load date if duped
					stmt.executeUpdate();
					entryCount++;
				} catch (Exception e) {
					System.out.print("ERROR " + e.getMessage());
				}
			}

			System.out.print("; data insert complete. " + entryCount + " records inserted." + System.lineSeparator());
			totalCount = totalCount + entryCount;
		}
		System.out.println("Inserted records: " + totalCount);

		try {
			stmt.close();
			con.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Done");
	}

	private ArrayList<String[]> jget(String url) {
		{

			URL u;
			InputStream is = null;
			ArrayList<String[]> content = new ArrayList<>();
			BufferedReader d = null;

			try {
				String line = "";
				while (true) {

					try {
						TimeUnit.MILLISECONDS.sleep(waitMilliSecs); // 1 sec = 1000 millisecs
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					u = new URL(url);
					is = u.openStream();
					d = new BufferedReader(new InputStreamReader(is));
					line = d.readLine();

					if (waitMilliSecs > 20000) {
						System.out.print("[skipped]");
						waitMilliSecs = 0;
						consecutiveFails=consecutiveFails+1;
						break;
					} else if (line.split(",").length < 3) {
						waitMilliSecs = waitMilliSecs + 1000;
						System.out.print("+");
					} else {
						System.out.print("[" + waitMilliSecs / 1000 + "]");
						waitMilliSecs = waitMilliSecs - 250;
						consecutiveFails =0;
						break;
					}
				}

				while ((line = d.readLine()) != null) {
					content.add(line.split(","));
				}
			} catch (MalformedURLException mue) {
				System.err.println("- a MalformedURLException happened.");

			} catch (IOException ioe) {
				System.err.println("- an IOException happened.");
			}

			finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}

			return content;

		}
	}
}
