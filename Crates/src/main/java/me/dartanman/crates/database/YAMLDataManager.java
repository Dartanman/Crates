package me.dartanman.crates.database;

import java.sql.Connection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import me.dartanman.crates.Crates;

/**
 * YAMLDataManager - Manages Player Data in a file as opposed to MySQL
 * @author Austin Dart (Dartanman)
 */
public class YAMLDataManager extends DatabaseAndPlayerDataManager{
	
	private FileConfiguration pFile;

	/**
	 * Constructs a YAMLDataManager object
	 * @param pl
	 *   Main class
	 */
	public YAMLDataManager(Crates pl) {
		super(pl);
		pFile = plugin.getPlayerDataFile();
	}

	/**
	 * Misleading name - it isn't a table anymore. Makes sure the ConfigurationSection exists.
	 */
	@Override
	public boolean createCratesTable(Connection connection) {
		if(!pFile.contains("Players")) {
			pFile.set("Players.73eee2f0-4717-4a6a-89fd-b5f7430051b1.CratesOpenedToday", 0);
			pFile.set("Players.73eee2f0-4717-4a6a-89fd-b5f7430051b1.LastOpenTime", 0);
		}
		plugin.savePlayerDataFile();
		return true;
	}

	/**
	 * Add a player to the "database" which is a ConfigurationSection
	 */
	@Override
	public void addPlayerToDatabase(Connection connection, UUID playerUUID) {
		String uuidStr = playerUUID.toString();
		Set<String> players = pFile.getConfigurationSection("Players").getKeys(false);
		if(!players.contains(uuidStr)) {
			pFile.set("Players." + uuidStr + ".CratesOpenedToday", 0);
			pFile.set("Players." + uuidStr + ".LastOpenTime", 0);
		}
		plugin.savePlayerDataFile();
	}

	/**
	 * Get the amount of crates opened today by a specific player
	 */
	@Override
	public int getCratesToday(Connection connection, UUID playerUUID) {
		String uuidStr = playerUUID.toString();
		for(String uuidStrings : pFile.getConfigurationSection("Players").getKeys(false)) {
			if(uuidStr.equals(uuidStrings)) {
				return pFile.getInt("Players." + uuidStr + ".CratesOpenedToday");
			}
		}
		return 0;
	}

	/**
	 * Get the last time a given player opened a crate
	 */
	@Override
	public long getLastOpenTime(Connection connection, UUID playerUUID) {
		String uuidStr = playerUUID.toString();
		for(String uuidStrings : pFile.getConfigurationSection("Players").getKeys(false)) {
			if(uuidStr.equals(uuidStrings)) {
				return pFile.getLong("Players." + uuidStr + ".LastOpenTime");
			}
		}
		return 0;
	}

	/**
	 * Set the amount of crates opened by a specific player today
	 */
	@Override
	public void setCratesToday(Connection connection, int cratesToday, UUID playerUUID) {
		String uuidStr = playerUUID.toString();
		pFile.set("Players." + uuidStr + ".CratesOpenedToday", cratesToday);
		plugin.savePlayerDataFile();
	}

	/**
	 * Set the last time the player opened a crate
	 */
	@Override
	public void setLastOpenTime(Connection connection, long lastOpenTime, UUID playerUUID) {
		String uuidStr = playerUUID.toString();
		pFile.set("Players." + uuidStr + ".LastOpenTime", lastOpenTime);
		plugin.savePlayerDataFile();
	}

	/**
	 * Reset the amount of crates opened today to 0 for all players
	 */
	@Override
	public void resetCratesToday(Connection connection) {
		for(String uuidStrings : pFile.getConfigurationSection("Players").getKeys(false)) {
			pFile.set("Players." + uuidStrings + ".CratesOpenedToday", 0);
		}
		plugin.savePlayerDataFile();
	}

}
