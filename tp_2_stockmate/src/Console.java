import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Console {

	public static void main(String[] args) {
		Connection con = null;
		PreparedStatement stmt = null;
		int cnt = 0;

		if (args.length == 0) {
			args = new String[] { "BERY", "HOFT", "BIIB", "MHK", "WEN", "IVZ", "VLP", "MMP", "MAN", "LTC", "SBNY",
					"ASR", "OMAB", "PAC", "RHP", "CRI", "THO", "TROW", "EGBN", "NCLH", "MGA", "PKG", "SNA", "LABL",
					"PBCT", "WAL", "LUV", "OMC", "TOWN", "BLK", "TU", "SEDG", "BAP", "FB", "BJRI", "EGOV", "USAT",
					"MCK" };
			//args = new String[] { "MHK" };
		}

		try {
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/SM2019?user=sm&password=stockmate");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		try {
			stmt = con.prepareStatement(
					"REPLACE INTO SM2019.D(tkr, yr, prd, esb, esd, ern, shr, wsh, pft, ldt) VALUES (?, ?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		for (String t : args) {
			cnt++;
			System.out.println(cnt + " of " + args.length + "...(" + t + ")");
			FilingSummary fs = new FilingSummary(t, new String[] { "esb", "esd", "ern", "shb", "shd", "pft", "gpf" });
			fs.bufferAllFilings();

			for (String[] v : fs.getArrayFilings()) {

				try {
					if (v[0] == null || v[1] == null || v[2] == null || v[3] == null) {
						// do nothing
					} else {

						System.out.println(t.toUpperCase() + "," + v[0] + "," + v[1] + "," + v[2] + "," + v[3]);

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
