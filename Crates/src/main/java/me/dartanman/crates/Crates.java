package me.dartanman.crates;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.dartanman.crates.commands.CrateCommand;
import me.dartanman.crates.crates.CrateManager;
import me.dartanman.crates.database.DatabaseAndPlayerDataManager;
import me.dartanman.crates.database.MySQLAndPlayerDataManager;
import me.dartanman.crates.database.MySQLInfo;
import me.dartanman.crates.database.YAMLDataManager;
import me.dartanman.crates.events.ChatListener;
import me.dartanman.crates.events.InteractionListener;
import me.dartanman.crates.events.InventoryListener;
import me.dartanman.crates.events.JoinListener;

/**
 * Crates - Main Class for the plugin.
 * @author Austin Dart (Dartanman)
 */
public class Crates extends JavaPlugin{
	
	private FileConfiguration playerDataFileConfig = new YamlConfiguration();
	private File playerDataFile;
	
	private MySQLInfo mySQLInfo;
	private CrateManager crateManager;
	private DatabaseAndPlayerDataManager databaseManager;
	
	private static Crates instance;
	
	/**
	 * Stuff to run when plugin is enabled by the server.
	 */
	public void onEnable() {
		instance = this;
		getConfig().options().copyDefaults(true);
		saveConfig();
		String dataStorageChoice = getConfig().getString("DataStorage");
		if(dataStorageChoice.equalsIgnoreCase("mysql")) {
			if(connectToMySQL()) {
				getLogger().info("Successfully connected to MySQL Server.");
				databaseManager = new MySQLAndPlayerDataManager(this);
				if(databaseManager.createCratesTable(mySQLInfo.getConnection())) {
					crateManager = new CrateManager(this);
					getCommand("crate").setExecutor(new CrateCommand(this));
					getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
					getServer().getPluginManager().registerEvents(new ChatListener(this), this);
					getServer().getPluginManager().registerEvents(new InteractionListener(this), this);	
					getServer().getPluginManager().registerEvents(new JoinListener(this), this);	
					databaseManager.attemptBeginNextDay();
				}else {
					getLogger().severe("Failed to create Crates table in MySQL server! Crates will not work!");
				}
			}else {
				getLogger().severe("SQL Authentication Failed!");
				getLogger().severe("Crates will not work without either a) switching to YAML or b) providing proper MySQL credentials. Please make sure your settings are correct in config.yml");
			}	
		}else if(dataStorageChoice.equalsIgnoreCase("yaml")){
			createFiles();
			mySQLInfo = new MySQLInfo(null, "username", "password");
			crateManager = new CrateManager(this);
			databaseManager = new YAMLDataManager(this);
			if(databaseManager.createCratesTable(mySQLInfo.getConnection())) {
				getCommand("crate").setExecutor(new CrateCommand(this));
				getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
				getServer().getPluginManager().registerEvents(new ChatListener(this), this);
				getServer().getPluginManager().registerEvents(new InteractionListener(this), this);	
				getServer().getPluginManager().registerEvents(new JoinListener(this), this);	
				databaseManager.attemptBeginNextDay();	
			}else {
				getLogger().severe("There was a problem loading Crates!");
			}
		}
	}
	
	public static Crates getInstance() {
		return instance;
	}
	
	/**
	 * Stuff to run when plugin is disabled by the server. In this case, I'm just closing the MySQL connection.
	 */
	public void onDisable() {
		if(mySQLInfo != null) {
			try {
				if(mySQLInfo.getConnection() != null) {
					mySQLInfo.getConnection().close();	
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Easy way to access MySQL stuff, primarily the connection
	 * @return
	 *   MySQLInfo object
	 */
	public MySQLInfo getMySQLInfo() {
		return mySQLInfo;
	}
	
	/**
	 * Manages almost everything to do with crates
	 * @return
	 *   CrateManager object
	 */
	public CrateManager getCrateManager() {
		return crateManager;
	}
	
	/**
	 * Manages everything to do with MySQL and PlayerData as it relates to time and crates
	 * @return
	 *   MySQLAndPlayerDataManager object
	 */
	public DatabaseAndPlayerDataManager getMySQLManager() {
		return databaseManager;
	}
	
	/**
	 * Connects to the MySQL server with the given information from the config file.
	 * @return
	 *   True if the connection is successful, false if it fails.
	 */
	private boolean connectToMySQL() {
		String address = getConfig().getString("MySQL.Address");
		String port = getConfig().getString("MySQL.Port");
		String databaseName = getConfig().getString("MySQL.Database");
		String username = getConfig().getString("MySQL.Username");
		String password = getConfig().getString("MySQL.Password");
		final String DB_URL = "jdbc:mysql://" + address + ":" + port + "/" + databaseName + "?characterEncoding=utf8";
		try {
			Connection connection = DriverManager.getConnection(DB_URL, username, password);
			mySQLInfo = new MySQLInfo(connection, username, password);
			return true;
		}catch(SQLException e) {
			getLogger().severe("It is expected for the plugin to fail epically if this is your first time loading the plugin." + 
					"Just make sure you add the correct MySQL information in the config.yml, then restart your server. " + 
					"If you still see errors like this one, then there is a problem.");
			return false;
		}
	}
	
	public FileConfiguration getPlayerDataFile() {
		return playerDataFileConfig;
	}
	
	public void savePlayerDataFile(){
        try {
            playerDataFileConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed to save playerData.yml!");
        }
    }
	
	private void createFiles() {
        playerDataFile = new File(getDataFolder(), "playerData.yml");

        saveRes(playerDataFile, "playerData.yml");
 

        playerDataFileConfig = new YamlConfiguration();
        try {
            playerDataFileConfig.load(playerDataFile);
        }catch(IOException | InvalidConfigurationException e) {
        	e.printStackTrace();
        }
    }
	
    public void saveRes(File file, String name) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource(name, false);
        }
    }
	

}
