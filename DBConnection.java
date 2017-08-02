import java.sql.*;

/**
 * Connection to a specific database by using user name and password.
 * A object of <code>DBConnection</code> is able to active the Postgresql driver,
 * connect to a specific database, which is defined by three variables in the class,
 * and close the connection to the database.
 * @author fisheryzhq
 *
 */

public class DBConnection {
	/**
	 * Three required parameters for accessing postgresql Database: usr_name:
	 * Login user name pwd: Login password url: The path and port for postgresql
	 * Database
	 */
	String usr_name = "postgres";
	String pwd = "yobdc1314";
	String url = "jdbc:postgresql://localhost:5432/postgres";

	/**
	 * Try to Load the Postgresql Driver.
	 * @return <code>true</code> if loading the driver successfully;
	 *         <code>false</code> if loading the driver fails. And throw an
	 *         exception.
	 */
	public boolean getSqlDriver() {
		try {
			Class.forName("org.postgresql.Driver"); // Load the required driver
			return true;
		} catch (Exception e) {
			System.out.println("Fail loading Driver!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Use pre-defined user name, password and connection URL to connect a database.
	 * @return A object of <class>Connection</class>, which references to a database, if connect
	 * to the database successfully; <code>null</code>, if connecting to the database fails.
	 */
	public Connection connectDB() {
		try {
			return DriverManager.getConnection(url, usr_name, pwd); //connect to the database using the password and username
		} catch (SQLException e) {
			System.out.println("Connection URL or username or password errors!");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * When all data are gotten, close the connection to the database.
	 * @param conn A object of <class>Connection</class> which should be close.
	 */
	public void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();  // Close the connection
			} catch (SQLException e) {
				System.out.println("Fail to close Connection to Database!");
				e.printStackTrace();
			}
		}
	}
}
