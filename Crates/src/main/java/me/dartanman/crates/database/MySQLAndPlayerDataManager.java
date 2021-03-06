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
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.dartanman.crates.Crates;

/**
 * MySQLAndPlayerDataManager - Manages everything to do with MySQL and Player Data as it relates to time and crates
 * @author Austin Dart (Dartanman)
 */
public class MySQLAndPlayerDataManager extends DatabaseAndPlayerDataManager{
	
	private Crates plugin;
	
	/**
	 * Constructs a MySQLAndPlayerDataManager object
	 * @param pl
	 *   Main class
	 */
	public MySQLAndPlayerDataManager(Crates pl) {
		super(pl);
	}
	
	/**
	 * Creates the table in the MySQL server used for Crates
	 * 
	 * @param connection The MySQL Connection to use
	 * @return True if successful, false if there is an error
	 */
	@Override
	public boolean createCratesTable(Connection connection) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS crates ("
					+ "PlayerUUID VARCHAR(37) NOT NULL, " + 
					"CratesOpenedToday INT NOT NULL, " + 
					"LastOpenTime BIGINT NOT NULL, " + 
					"PRIMARY KEY ( PlayerUUID )" + 
					");");

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
	 	*/
		@Override
		public void addPlayerToDatabase(final Connection connection, final UUID playerUUID) {
			new BukkitRunnable() {
				@Override
				public void run() {
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
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Issue adding a player to database. Is your MySQL Server running properly?");
					}
				}
		         
		       }.runTaskAsynchronously(Crates.getInstance());
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
		@Override
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
		@Override
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
		 */
		@Override
		public void setCratesToday(final Connection connection, final int cratesToday, final UUID playerUUID) {
			
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						String uuidStr = playerUUID.toString();
						PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET CratesOpenedToday=" + cratesToday + " WHERE PlayerUUID='" + uuidStr + "'");
						preparedStatement.executeUpdate();
						preparedStatement.close();
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Failed to set Crates Opened Today. Is your MySQL Server running properly?");
					}
				}
		         
		       }.runTaskAsynchronously(Crates.getInstance());
		}
		
		/**
		 * Sets the most recent time the give player('s UUID) opened a crate, in millis
		 * @param connection
		 *   The MySQL Connection to use
		 * @param openTimeMillis
		 *   The time to set
		 * @param playerUUID
		 *   The player's UUID
		 */
		@Override
		public void setLastOpenTime(final Connection connection, final long openTimeMillis, final UUID playerUUID) {
			
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						String uuidStr = playerUUID.toString();
						PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET LastOpenTime='" + openTimeMillis + "' WHERE PlayerUUID='" + uuidStr + "'");
						preparedStatement.executeUpdate();
						preparedStatement.close();
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Failed to set Crates Opened Today. Is your MySQL Server running properly?");
					}
				}
		         
		       }.runTaskAsynchronously(Crates.getInstance());
			
		}
		
		/**
		 * Resets today's crates to 0 for everyone.
		 * @param connection
		 *   The MySQL Connection to use
		 */
		@Override
		public void resetCratesToday(final Connection connection) {
			
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						PreparedStatement preparedStatement = connection.prepareStatement("UPDATE crates SET CratesOpenedToday='0'");
						preparedStatement.executeUpdate();
						preparedStatement.close();
					} catch (SQLException e) {
						Bukkit.getLogger().severe("Failed to reset Crates Opened Today. Is your MySQL Server running properly?");
					}
				}
		         
		       }.runTaskAsynchronously(Crates.getInstance());
			
		}

}
