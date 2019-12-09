import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

public class UpdatePrices {
	public static void main(String[] args) {
		Connection con = null;
		Statement stm = null;
		ResultSet rst = null;
		ArrayList<String> stockList = new ArrayList<>();
		String ticker;
		ArrayList<String[]> priceRecentData = null;
		ArrayList<String[]> priceHistoricData = null;
		String fileDelimiter;
		
		if(System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
			fileDelimiter = "\\";
		}else {
			fileDelimiter = "/";
		}
		String recentLoc = System.getProperty("user.home") + fileDelimiter+ "Public" + fileDelimiter+ "recentPrices.csv";
		String historicLoc = System.getProperty("user.home") + fileDelimiter+ "Public" + fileDelimiter+ "historicPrices.csv";
		String[] apiList = { "OYZUJQ7KPDJV69PS", "HNLXG9VCGGTK92YV", "U1IP4DNKQ4GO301G", "Q96186O1RQNJQFC7",
				"MGYZGVJEU1IRRDZ7", "07MRW7RDV23EB9DW", "FWKJB3QFARJEH5OI", "KZ59U7Z78IU6UXJW", "06OHTXWS1RCUVCFA",
				"3BW1YQ0UIVZ17XM4", "868CC591ASZDT30J", "WTCLECODPYSKTHJQ", "K01WGBTESTEBYTBK", "868CC591ASZDT30J",
				"EYW9JROENT30B8ON", "RTFY5W8YVE6DAF2O", "KREGHQ3FRYT09H4S", "SLN3EW4L0UXQ38F5", "DAB7RANDIUK2SSCX",
				"OW5A8ZQZSVBHJG5L" };
		String api = null;
		int totalCount = 0;

		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/S?autoReconnect=true&useSSL=false", "root",
					"dh");

			stm = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				stockList.add(args[i].toUpperCase());
			}

		} else {
			try {

				rst = stm.executeQuery("SELECT DISTINCT TICKER FROM S.D");
				while (rst.next()) {

					stockList.add(rst.getString("ticker"));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//stockList.clear();
		//stockList.add("CMG");
		
		System.out.println(stockList);
		PreparedStatement stmt = null;

		for (int i = 0; i < stockList.size(); i++) {

			int entryCount = 0;
			ticker = stockList.get(i);
			System.out.print("Updating Prices for " + ticker + " (" + (i + 1) + " of " + (stockList.size()) + ")");

			Runtime r = Runtime.getRuntime();
			Process p = null;

			api = apiList[new Random().nextInt(20)];
			System.out.print("; API call with: " + api);

			try {
				p = r.exec(new String[] { "/bin/sh", "-c",
						"wget \"https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + ticker
								+ "&apikey=" + api + "&datatype=csv\" -O " + recentLoc });
				p.waitFor();
				priceRecentData = UpdatePrices.readData(recentLoc);

				p = r.exec(new String[] { "/bin/sh", "-c",
						"wget \"https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY&symbol=" + ticker
								+ "&apikey=" + api + "&datatype=csv\" -O " + historicLoc });
				p.waitFor();
				priceHistoricData = UpdatePrices.readData(historicLoc);
				System.out.print("; API calls successful");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// recent data
			try {
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/S?autoReconnect=true&useSSL=false",
						"root", "dh");
				stmt = con.prepareStatement("INSERT INTO S.R(TICKER, CLOSEDATE, PRICE, LOADDATE) VALUES (?, ?, ?, ?) ");
				// + "ON DUPLICATE KEY UPDATE PRICE=?, LOADDATE=?");
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int j = 1; j < priceRecentData.size(); j++) {
				try {
					stmt.setString(1, ticker.toUpperCase());
					stmt.setDate(2, Date.valueOf((priceRecentData.get(j)[0])));
					stmt.setDouble(3, Double.parseDouble(priceRecentData.get(j)[4].replace(",", "")));
					stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					// stmt.setDouble(5, Double.parseDouble(priceRecentData.get(j)[4].replace(",",
					// "")));
					// stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
					stmt.executeUpdate();
					System.out.print("; P" + priceRecentData.get(j)[0]);
					entryCount++;
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}

			// historic data
			try {
				stmt = con.prepareStatement(
						"INSERT INTO S.H(TICKER, MONTHEND, MONTHHIGH, MONTHLOW, LOADDATE) VALUES (?, ?, ?, ?, ?) ");
				// + "ON DUPLICATE KEY UPDATE MONTHHIGH=?, MONTHLOW=?, LOADDATE=?");
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int j = 2; j < Math.min(priceHistoricData.size(), 122); j++) { // 12*10yr = 120 + 2 skipped lines
				try { // (header and most recent month)
					stmt.setString(1, ticker.toUpperCase()); // ticker
					stmt.setDate(2, Date.valueOf((priceHistoricData.get(j)[0]))); // month end
					stmt.setDouble(3, Double.parseDouble(priceHistoricData.get(j)[2])); // month high
					stmt.setDouble(4, Double.parseDouble(priceHistoricData.get(j)[3])); // month low
					stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // load date
					// stmt.setDouble(6, Double.parseDouble(priceHistoricData.get(j)[2])); // month
					// high
					// stmt.setDouble(7, Double.parseDouble(priceHistoricData.get(j)[3])); // month
					// low
					// stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // load date
					stmt.executeUpdate();
					System.out.print("; M" + priceHistoricData.get(j)[0]);
					entryCount++;
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}

			System.out.print("; data insert complete. " + entryCount + " records inserted." + System.lineSeparator());
			totalCount = totalCount + entryCount;
		}
		System.out.println("Inserted records: " + totalCount);

		// delete 10yr old records
		try {
			stmt = con.prepareStatement("DELETE FROM S.H WHERE MONTHEND < DATE_SUB(NOW(), INTERVAL 10 YEAR)");
			stmt.executeUpdate();

			System.out.println("Deleted records: " + stmt.getUpdateCount());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			stmt.close();
			con.close();
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public static ArrayList<String[]> readData(String file) throws IOException {

		ArrayList<String[]> content = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = "";
			while ((line = br.readLine()) != null) {
				content.add(line.split(","));

			}
		} catch (Exception e) {
			// Some error logging
		}
		return content;
	}
}
