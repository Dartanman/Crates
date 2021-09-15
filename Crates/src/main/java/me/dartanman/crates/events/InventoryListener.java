package me.dartanman.crates.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import me.dartanman.crates.Crates;
import me.dartanman.crates.crates.CrateInfo;
import me.dartanman.crates.crates.CrateManager;

/**
 * InventoryListener - Listens to the InventoryClickEvent
 * @author Austin Dart (Dartanman)
 */
public class InventoryListener implements Listener{
	
	private Crates plugin;
	
	/**
	 * Constructs an InventoryListener object
	 * @param pl
	 *   Main class
	 */
	public InventoryListener(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Listens to the InventoryClickEvent
	 * @param event
	 *   The InventoryClickEvent
	 */
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			ItemStack item = event.getCurrentItem();
			if(item == null) {
				event.setCancelled(true);
				return;
			}
			InventoryView view = event.getView();
			String title = view.getTitle();
			if(title.startsWith(ChatColor.GREEN + "Create Crate: ")) {
				if(CrateInfo.createCrateMap.containsKey(player)) {
					if(item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Confirm")) {
						plugin.getCrateManager().createCrateInConfig(CrateInfo.createCrateMap.get(player));
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.CrateCreated")));
						player.closeInventory();
					}else if(item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel")) {
						CrateInfo.createCrateMap.remove(player);
						player.closeInventory();
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.CrateCreationCancelled")));
					}else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ConfirmOrCancel")));
					}
					event.setCancelled(true);
				}
			}else if(title.startsWith(ChatColor.GREEN + "Edit")) {
				if(CrateManager.editMap.containsKey(player)) {
					if(event.isLeftClick() && !event.isShiftClick()) {
						if(item != null && !item.getType().equals(Material.AIR)) {
							if(!plugin.getCrateManager().addItem(CrateManager.editMap.get(player), item, 0d)) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.AlreadyAdded")));
							}
							player.closeInventory();
							Inventory inv = plugin.getCrateManager().crateEditInventory(CrateManager.editMap.get(player));
							player.openInventory(inv);
						}
					}else if(event.isRightClick() && !event.isShiftClick()) {
						if(item != null && !item.getType().equals(Material.AIR)) {
							plugin.getCrateManager().removeItem(CrateManager.editMap.get(player), item);
							player.closeInventory();
							Inventory inv = plugin.getCrateManager().crateEditInventory(CrateManager.editMap.get(player));
							player.openInventory(inv);
						}
					}else if(event.isShiftClick()) {
						CrateManager.itemMap.put(player, item);
						CrateManager.chanceList.add(player);
						player.closeInventory();
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.GivePercent")));
					}
					event.setCancelled(true);
				}
			}else if(title.startsWith(ChatColor.RED + "Delete Crate: ")) {
				if(CrateManager.deleteMap.containsKey(player)) {
					if(item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Confirm")) {
						plugin.getConfig().set("Crates." + CrateManager.deleteMap.get(player), null);
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.CrateDeleted")));
						player.closeInventory();
						CrateManager.deleteMap.remove(player);
						plugin.saveConfig();
					}else if(item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel")) {
						CrateManager.deleteMap.remove(player);
						player.closeInventory();
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.CrateDeletionCancelled")));
					}else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ConfirmOrCancel")));
					}
					event.setCancelled(true);
				}
			}else if(title.startsWith(ChatColor.GREEN + "Open ")) {
				if(CrateManager.openMap.containsKey(player)) {
					if(item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Confirm")) {
						plugin.getCrateManager().openCrate(player, CrateManager.openMap.get(player));
						CrateManager.openMap.remove(player);
					}else if(item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel")) {
						player.closeInventory();
						CrateManager.openMap.remove(player);
					}else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.ConfirmOrCancel")));
					}
					event.setCancelled(true);
				}
			}else if(title.equals(ChatColor.GREEN + "Opening Crate...")) {
				event.setCancelled(true);
			}
		}
	}

}
