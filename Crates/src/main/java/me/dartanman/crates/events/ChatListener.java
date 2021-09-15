package me.dartanman.crates.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import me.dartanman.crates.Crates;
import me.dartanman.crates.crates.CrateManager;
import net.md_5.bungee.api.ChatColor;

/**
 * ChatListener - Listens to the PlayerChatEvent
 * @author Austin Dart (Dartanman)
 */
public class ChatListener implements Listener{
	
	private Crates plugin;
	
	/**
	 * Constructs a ChatListener object
	 * @param pl
	 *   Main class
	 */
	public ChatListener(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Listens to the PlayerChatEvent
	 * @param event
	 *   The PlayerChatEvent
	 */
	@EventHandler
	public void onChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if(CrateManager.chanceList.contains(player) && CrateManager.editMap.containsKey(player)) {
			String crateName = CrateManager.editMap.get(player);
			String message = event.getMessage();
			ItemStack item = CrateManager.itemMap.get(player);
			double chance = 0d;
			if(message.contains("%")) {
				message = message.replace("%", "");
			}
			try {
				chance = Double.parseDouble(message);
			}catch(Exception e) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.InvalidInputPercent")));
			}
			plugin.getCrateManager().editItem(crateName, item, chance);
			CrateManager.itemMap.remove(player);
			CrateManager.chanceList.remove(player);
			player.openInventory(plugin.getCrateManager().crateEditInventory(crateName));
			event.setCancelled(true);
		}
	}

}
