package me.dartanman.crates.events;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.dartanman.crates.Crates;

/**
 * JoinListener - Listens to the PlayerJoinEvent
 * @author Austin Dart (Dartanman)
 */
public class JoinListener implements Listener{
	
	private Crates plugin;
	
	/**
	 * Constructs a JoinListener object
	 * @param pl
	 *   Main class
	 */
	public JoinListener(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Listens to the PlayerJoinEvent
	 * @param event
	 *   The PlayerJoinEvent
	 */
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		plugin.getMySQLManager().addPlayerToDatabase(plugin.getMySQLInfo().getConnection(), uuid);
	}

}
