package me.dartanman.crates.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import me.dartanman.crates.Crates;
import me.dartanman.crates.crates.CrateManager;

/**
 * InteractionListener - Listens to the PlayerInteractEvent
 * @author Austin Dart (Dartanman)
 */
public class InteractionListener implements Listener{
	
	private Crates plugin;
	
	/**
	 * Constructs an InteractionListener object
	 * @param pl
	 *   Main class
	 */
	public InteractionListener(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Listens to the PlayerInteractEvent
	 * @param event
	 *   The PlayerInteractEvent
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		Action action = event.getAction();
		if(action.equals(Action.RIGHT_CLICK_AIR)|| action.equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack item = event.getItem();
			for(String crates : plugin.getConfig().getConfigurationSection("Crates").getKeys(false)) {
				if(plugin.getConfig().getItemStack("Crates." + crates + ".Item").isSimilar(item)) {
					event.setCancelled(true);
					plugin.getMySQLManager().attemptBeginNextDay();
					if(plugin.getCrateManager().canOpenCrate(player)) {
						Inventory confirmationInv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Open " + ChatColor.BOLD + crates + ChatColor.RESET + "" + ChatColor.GREEN + "?");
						ItemStack confirm = new ItemStack(Material.WOOL);
						ItemMeta coMeta = confirm.getItemMeta();
						coMeta.setDisplayName(ChatColor.GREEN + "Confirm");
						confirm.setItemMeta(coMeta);
						confirm.setDurability((short) 5);
						ItemStack cancel = new ItemStack(Material.WOOL);
						ItemMeta caMeta = cancel.getItemMeta();
						caMeta.setDisplayName(ChatColor.RED + "Cancel");
						cancel.setItemMeta(caMeta);
						cancel.setDurability((short) 14);
						confirmationInv.setItem(12, confirm);
						confirmationInv.setItem(14, cancel);
						player.openInventory(confirmationInv);
						
						CrateManager.openMap.put(player, crates);
					}else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.CannotOpenCrate")));
					}
					break;
					
				}
			}
		}
	}

}
