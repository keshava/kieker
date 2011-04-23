package kieker.test.monitoring.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andre van Hoorn
 */
public class JavaDBInitializer {
	private static final Log log = LogFactory.getLog(JavaDBInitializer.class);

	private static String dbDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String dbConnectionAddress = "jdbc:derby:tmp/KIEKER;user=DBUSER;password=DBPASS";
	private static String dbTableName = "APP.tpmondata";

	// TODO: needs to be read from file
	private static String strCreateAddressTable = 
		"CREATE table " + dbTableName + "(autoid INTEGER NOT NULL "
			+ "   PRIMARY KEY GENERATED ALWAYS AS IDENTITY " + "   (START WITH 0, INCREMENT BY 1),"
			+ "experimentid SMALLINT NOT NULL DEFAULT 0," + "operation VARCHAR(160) NOT NULL," + "sessionid VARCHAR(34),"
			+ "traceid BIGINT NOT NULL," + "tin BIGINT NOT NULL," + "tout BIGINT NOT NULL,"
			+ "vmname VARCHAR(40) NOT NULL DEFAULT ''," + "executionOrderIndex SMALLINT NOT NULL DEFAULT -1,"
			+ "executionStackSize SMALLINT NOT NULL DEFAULT -1" +
			// "INDEX (operation(16)), INDEX (traceid), INDEX (tin)" +
			")";

	public static void main(final String[] args) {
		try {
			Class.forName(dbDriverClassname).newInstance();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		Connection dbConnection = null;
		try {
			dbConnection = DriverManager.getConnection(JavaDBInitializer.dbConnectionAddress + ";create=true");
			JavaDBInitializer.createTables(dbConnection);
			dbConnection.close();
		} catch (final SQLException ex) {
			JavaDBInitializer.log.error(ex);
			System.exit(1);
		}
		JavaDBInitializer.log.info(JavaDBInitializer.class.getName() + ".main(..) done");
	}

	private static boolean createTables(final Connection dbConnection) {
		boolean bCreatedTables = false;
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			statement.execute(JavaDBInitializer.strCreateAddressTable);
			bCreatedTables = true;

			// TODO: remove:
			// statement = dbConnection.createStatement();
			// statement.execute("INSERT INTO APP.tpmondata (experimentid) VALUES (5)");
			// statement.execute("INSERT INTO APP.tpmondata (experimentid) VALUES (7)");
			//
			// statement = dbConnection.createStatement(); // TODO: FindBugs says this method may fail to close the database
			// resource
			// ResultSet res = statement.executeQuery("SELECT max(experimentid) FROM APP.tpmondata");
			// if (res.next()) {
			// System.out.println(res.getInt(1));
			// }
		} catch (final SQLException ex) {
			ex.printStackTrace();
		}

		return bCreatedTables;
	}
}
