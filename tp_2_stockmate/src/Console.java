import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Console {

	public static void main(String[] args) {

		Console c = new Console();
		// int[] latestQtr = c.getLatestQuarterly("MHK");
		// System.out.println(latestQtr[0] + "+" + latestQtr[1]);

		System.out.println(c.hasAnnualsForXyrs("THO", 5));

	}

	public int[] getLatestQuarterly(String ticker) {
		Connection con = connectToDB();
		int maxQtr = -1;
		int maxYr = -1;
		ResultSet rs;
		try {
			rs = con.prepareStatement("SELECT MAX(D.prd), MAX(D.yr) " + "FROM SM2019.D "
					+ "JOIN (SELECT MAX(yr) mYr FROM SM2019.D WHERE prd > 0 AND tkr='" + ticker
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

	public int getLatestAnnual(String ticker) {
		Connection con = connectToDB();
		int maxYr = -1;
		ResultSet rs;
		try {
			rs = con.prepareStatement(
					"SELECT MAX(D.yr) " + "FROM SM2019.D " + "WHERE prd = 0 AND tkr='" + ticker + "';").executeQuery();

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
				String sql = "SELECT COUNT(*) " + "FROM SM2019.D " + "WHERE prd = 0 AND tkr='" + ticker + "' AND yr = "
						+ yr + ";";
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

	public void loadDataIntoDB(String[] tickers) {

		Connection con = connectToDB();
		PreparedStatement stmt = null;

		int cnt = 0;

		if (tickers.length == 0) {
//			args = new String[] { "BERY", "HOFT", "BIIB", "MHK", "WEN", "IVZ", "VLP", "MMP", "MAN", "LTC", "SBNY",
//					"ASR", "OMAB", "PAC", "RHP", "CRI", "THO", "TROW", "EGBN", "NCLH", "MGA", "PKG", "SNA", "LABL",
//					"PBCT", "WAL", "LUV", "OMC", "TOWN", "BLK", "TU", "SEDG", "BAP", "FB", "BJRI", "EGOV", "USAT",
//					"MCK" };
			tickers = new String[] { "PBCT" };
		}

		try {
			stmt = con.prepareStatement(
					"REPLACE INTO SM2019.D(tkr, yr, prd, esb, esd, ern, shr, wsh, pft, ldt) VALUES (?, ?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (String t : tickers) {
			cnt++;
			System.out.println(cnt + " of " + tickers.length + "...(" + t + ")");
			FilingSummary fs = new FilingSummary(t, new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });
			fs.bufferAllFilings();

			for (String[] v : fs.getArrayFilings()) {

				try {
					if (v[0] == null || v[1] == null || v[2] == null || v[3] == null) {
						// do nothing
					} else {

						// System.out.println(t.toUpperCase() + "," + v[0] + "," + v[1] + "," + v[2] +
						// "," + v[3]);

						stmt = buildStatement(con, v[0]);

						stmt.setString(1, t.toUpperCase());
						stmt.setInt(2, Integer.parseInt(v[1]));
						stmt.setInt(3, Integer.parseInt(v[2]));
						stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
						stmt.setDouble(5, Double.parseDouble(v[3]));
						stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
						stmt.setDouble(7, Double.parseDouble(v[3]));
						stmt.executeUpdate();
						// System.out.println( t.toUpperCase()+","+v[0]+","+v[1]+","+v[2]+","+v[3]);
					}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			System.out.println(fs.getFilingPreview(","));
		}

		closeCommit(con);

	}

	private static PreparedStatement buildStatement(Connection con, String col) {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement("INSERT INTO d (tkr, yr, prd, ldt, " + col + ")" + "VALUES (?,?,?,?,?) "
					+ "ON DUPLICATE KEY UPDATE " + "ldt = ?, " + col + "=?;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stmt;

	}
}
