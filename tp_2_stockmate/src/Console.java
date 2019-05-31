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
					"REPLACE INTO SM2019.D(ticker, yr, prd, tag, val, loaddate) VALUES (?, ?, ?, ?, ?, ?)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		for (String t : args) {
			FilingSummary fs = new FilingSummary(t, new String[] { "income", "eps", "epsd" });
			fs.bufferAllFilings();
			cnt++;
			System.out.println(cnt + " of " + args.length +"...(" + t + ")");
			for (String[] v : fs.getArrayFilings()) {

				try {
					if (v[0] == null || v[1] == null || v[2] == null || v[3] == null) {
						// do nothing
					} else {
						stmt.setString(1, t.toUpperCase());
						stmt.setInt(2, Integer.parseInt(v[0]));
						stmt.setInt(3, Integer.parseInt(v[1]));
						stmt.setString(4, v[2]);
						stmt.setDouble(5, Double.parseDouble(v[3]));
						stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
						stmt.executeUpdate();
					}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e1) {
					System.out.println(e1);
				}

			}
		}

	}
}
