package me.dartanman.crates.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.dartanman.crates.Crates;

/**
 * MySQLAndPlayerDataManager - Manages everything to do with MySQL and Player Data as it relates to time and crates
 * @author Austin Dart (Dartanman)
 */
public class MySQLAndPlayerDataManager {
	
	private Crates plugin;
	
	/**
	 * Constructs a MySQLAndPlayerDataManager object
	 * @param pl
	 *   Main class
	 */
	public MySQLAndPlayerDataManager(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Returns an integer array of today's day of the year and the year
	 * @return
	 *   Integer array of today's day of the year and the year
	 */
	public int[] getTodayWithYear() {
		Date date = new Date();
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
		int[] dayAndYear = new int[2];
		dayAndYear[0] = localDate.getDayOfYear();
		dayAndYear[1] = localDate.getYear();
		return dayAndYear;
	}
	
	/**
	 * Attempts to start the next day and reset everyone's daily crates. Does nothing if it is not yet the next day.
	 */
	public void attemptBeginNextDay() {
		if(getTodayWithYear()[0] > getOldDayWithYear()[0]) {
			resetCratesToday(plugin.getMySQLInfo().getConnection());
			plugin.getConfig().set("Today", String.valueOf(getTodayWithYear()[0]) + "/" + String.valueOf(getTodayWithYear()[1]));
		}else if(getTodayWithYear()[1] > getOldDayWithYear()[1]) {
			resetCratesToday(plugin.getMySQLInfo().getConnection());
			plugin.getConfig().set("Today", String.valueOf(getTodayWithYear()[0]) + "/" + String.valueOf(getTodayWithYear()[1]));
		}
		plugin.saveConfig();
	}
	
	/**
	 * Gets what is saved as the current day and year, however it might be outdated (yesterday [or older in some circumstances])
	 * @return
	 *   What is saved as the current day and year
	 */
	public int[] getOldDayWithYear() {
		String oldDayStr = plugin.getConfig().getString("Today");
		String[] split = oldDayStr.split("/");
		String oldDay = split[0];
		String oldYear = split[1];
		int[] dayAndYear = new int[2];
		dayAndYear[0] = Integer.valueOf(oldDay);
		dayAndYear[1] = Integer.valueOf(oldYear);
		return dayAndYear;
	}
	
	/**
	 * Creates the table in the MySQL server used for Crates
	 * @param connection
	 *   The MySQL Connection to use
	 * @return
	 *   True if successful, false if there is an error
	 */
	public boolean createCratesTable(Connection connection) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"CREATE TABLE IF NOT EXISTS crates ("
					+ "PlayerUUID VARCHAR(37) NOT NULL, "
					+ "CratesOpenedToday INT NOT NULL, "
					+ "LastOpenTime BIGINT NOT NULL, "
					+ "PRIMARY KEY ( PlayerUUID )"
					+ ");");
			
			preparedStatement.executeUpdate();
			preparedStatement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
		
	/**
	 * Adds a player('s UUID) to the database
	 * @param connection
	 *   The MySQL Connection to use
	 * @param playerUUID
	 *   The player's UUID
	 * @return
	 *   True if successful, false if there is an error
	 */
		public boolean addPlayerToDatabase(Connection connection, UUID playerUUID) {
			try {
				String uuidStr = playerUUID.toString();
				
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT CratesOpenedToday FROM crates WHERE PlayerUUID='" + uuidStr + "';");
				ResultSet result = preparedStatement.executeQuery();
				
				boolean found = false;
				
				if(result != null) {
					while(result.next()) {
						found = true;
					}
				}
				
				if(!found) {
					PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO crates VALUES ('" + uuidStr + "', '0', '0')");
					preparedStatement2.executeUpdate();
					preparedStatement2.close();
				}
				preparedStatement.close();
				return true;
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Issue adding a player to database. Is your MySQL Server running properly?");
				return false;
			}
		}
		
		/**
		 * Gets the amount of crates opened today by the give player('s UUID)
		 * @param connection
		 *   The MySQL Connection to use
		 * @param playerUUID
		 *   The player's UUID
		 * @return
		 *   The amount of crates they've opened today. 0 if there is an error.
		 */
		public int getCratesToday(Connection connection, UUID playerUUID) {
			try {
				String uuidStr = playerUUID.toString();
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT CratesOpenedToday FROM crates WHERE PlayerUUID='" + uuidStr + "';");
				ResultSet result = preparedStatement.executeQuery();
				
				int resultsCount = 0;
				int openedToday = 0;
				
				if(result != null) {
					while(result.next()) {
						if(resultsCount < 1) {
							openedToday = result.getInt("CratesOpenedToday");
						}
						resultsCount++;
					}
				}
				
				preparedStatement.close();
				
				if(resultsCount != 1) {
					return 0;
				} else {
					return openedToday;
				}
				
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Failed to check Crates Opened Today. Is your MySQL Server running properly?");
				return 0;
			}
		}
		
		/**
		 * Gets the last time the give player('s UUID) opened a crate. It was gotten by System.currentTimeMillis()
		 * @param connection
		 *   The MySQL Connection to use
		 * @param playerUUID
		 *   The player's UUID
		 * @return
		 *   The last time, in millis, the player opened a crate
		 */
		public long getLastOpenTime(Connection connection, UUID playerUUID) {
			try {
				String uuidStr = playerUUID.toString();
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT LastOpenTime FROM crates WHERE PlayerUUID='" + uuidStr + "';");
				ResultSet result = preparedStatement.executeQuery();
				
				int resultsCount = 0;
				long lastOpenTime = 0L;
				
				if(result != null) {
					while(result.next()) {
						if(resultsCount < 1) {
							lastOpenTime = result.getLong("LastOpenTime");
						}
						resultsCount++;
					}
				}
				
				preparedStatement.close();
				
				if(resultsCount != 1) {
					return 0L;
				} else {
					return lastOpenTime;
				}
				
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Failed to check Last Open Time. Is your MySQL Server running properly?");
				return 0L;
			}
		}
		
		/**
		 * Sets the amount of crates opened by the given player('s UUID) today
		 * @param connection
		 *   The MySQL Connection to use
		 * @param cratesToday
		 *   The amount of crates opened
		 * @param playerUUID
		 *   The player's UUID
		 * @return
		 *   True if successful, false if there is an error
		 */
		public boolean setCratesToday(Connection connection, int cratesToday, UUID playerUUID) {
			try {
				String uuidStr = playerUUID.toString();
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET CratesOpenedToday=" + cratesToday + " WHERE PlayerUUID='" + uuidStr + "'");
				preparedStatement.executeUpdate();
				preparedStatement.close();
				return true;
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Failed to set Crates Opened Today. Is your MySQL Server running properly?");
				return false;
			}
			
		}
		
		/**
		 * Sets the most recent time the give player('s UUID) opened a crate, in millis
		 * @param connection
		 *   The MySQL Connection to use
		 * @param openTimeMillis
		 *   The time to set
		 * @param playerUUID
		 *   The player's UUID
		 * @return
		 *   True if successful, false if there is an error
		 */
		public boolean setLastOpenTime(Connection connection, long openTimeMillis, UUID playerUUID) {
			try {
				String uuidStr = playerUUID.toString();
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET LastOpenTime='" + openTimeMillis + "' WHERE PlayerUUID='" + uuidStr + "'");
				preparedStatement.executeUpdate();
				preparedStatement.close();
				return true;
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Failed to set Crates Opened Today. Is your MySQL Server running properly?");
				return false;
			}
			
		}
		
		/**
		 * Resets today's crates to 0 for everyone.
		 * @param connection
		 *   The MySQL Connection to use
		 * @return
		 *   True if successful, false if there is an error
		 */
		public boolean resetCratesToday(Connection connection) {
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET CratesOpenedToday='0'");
				preparedStatement.executeUpdate();
				preparedStatement.close();
				return true;
			} catch (SQLException e) {
				Bukkit.getLogger().severe("Failed to reset Crates Opened Today. Is your MySQL Server running properly?");
				return false;
			}
		}

}
