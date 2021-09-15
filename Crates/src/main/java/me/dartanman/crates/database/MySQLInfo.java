package me.dartanman.crates.database;

import java.sql.Connection;

/**
 * MySQLInfo - Stores some information about MySQL
 * @author Austin Dart (Dartanman)
 */
public class MySQLInfo {
	
	private Connection connection;
	private String username;
	private String password;
	
	/**
	 * Constructs a MySQLInfo object
	 * @param connection
	 *   A MySQL Connection
	 * @param username
	 *   Username for authentication
	 * @param password
	 *   Password for authentication
	 */
	public MySQLInfo(Connection connection, String username, String password) {
		this.connection = connection;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Returns the username for the MySQL server
	 * @return
	 *   The username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns the password for the MySQL server
	 * @return
	 *   The password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns the MySQL Connection
	 * @return
	 *   The MySQL Connection
	 */
	public Connection getConnection() {
		return connection;
	}

}
