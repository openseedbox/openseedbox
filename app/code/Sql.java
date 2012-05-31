package code;

import java.sql.ResultSet;
import java.sql.SQLException;
import play.Logger;
import play.db.DB;

public class Sql {
	
	public static int getScalar(String query) {
		ResultSet rs = DB.executeQuery(query);
		try {
			while (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException ex) {
			queryError(query, ex);
		} finally {
			try {
				rs.close();
			} catch (SQLException ex) {
				queryError(query, ex);
			}
		}
		return -1;
	}
	
	private static void queryError(String query, Exception ex) {
		Logger.error(ex, "SQL query failed: %s ", query);
	}
	
}
