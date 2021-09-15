package me.dartanman.crates.database;

import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import me.dartanman.crates.Crates;

/**
 * DatabaseAndPlayerDataManager - Provides the base to perform Database functions in a variety of different environments
 * @author Austin Dart (Dartanman)
 */
public abstract class DatabaseAndPlayerDataManager {
	
	protected Crates plugin;
	
	/**
	 * Constructs a DatabaseAndPlayerDataManager object
	 * @param pl
	 *   Main class
	 */
	public DatabaseAndPlayerDataManager(Crates pl) {
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
	
	public abstract boolean createCratesTable(Connection connection);
	
	public abstract void addPlayerToDatabase(Connection connection, UUID playerUUID);
	
	public abstract int getCratesToday(Connection connection, UUID playerUUID);
	
	public abstract long getLastOpenTime(Connection connection, UUID playerUUID);
	
	public abstract void setCratesToday(Connection connection, int cratesToday, UUID playerUUID);
	
	public abstract void setLastOpenTime(Connection connection, long lastOpenTime, UUID playerUUID);
	
	public abstract void resetCratesToday(Connection connection);

}
