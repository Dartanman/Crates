package me.dartanman.crates.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dartanman.crates.Crates;
import me.dartanman.crates.crates.CrateInfo;
import me.dartanman.crates.crates.CrateManager;

/**
 * CrateCmd - Executes the /crate command
 * 
 * @author Austin Dart (Dartanman)
 */
public class CrateCmd implements CommandExecutor {

	private Crates plugin;

	/**
	 * Constructs the CrateCmd Command Executor
	 * 
	 * @param pl Main class
	 */
	public CrateCmd(Crates pl) {
		plugin = pl;
	}

	/**
	 * Actually runs the command. Spigot cares if it returns true/false, so it's a
	 * boolean.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.MustBePlayer")));
					return true;
				}
				Player player = (Player) sender;
				if (!player.hasPermission("crates.admin.create")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.NoPermission")));
					return true;
				}
				String crateName = args[1];
				if (plugin.getCrateManager().crateExists(crateName)) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.CrateAlreadyExists")));
					return true;
				} else {
					if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("Messages.NeedItem")));
						return true;
					}
					CrateInfo crateInfo = new CrateInfo(crateName, 1, 1, player.getItemInHand());
					CrateInfo.createCrateMap.put(player, crateInfo);
					Inventory confirmationInv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Create Crate: "
							+ ChatColor.BOLD + crateName + ChatColor.RESET + "" + ChatColor.GREEN + "?");
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
					return true;
				}
			} else if (args[0].equalsIgnoreCase("edit")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.MustBePlayer")));
					return true;
				}
				Player player = (Player) sender;
				if (!player.hasPermission("crates.admin.edit")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.NoPermission")));
					return true;
				}
				String crateName = args[1];
				if (!plugin.getCrateManager().crateExists(crateName)) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.CrateDoesNotExist")));
					return true;
				} else {
					Inventory editInv = plugin.getCrateManager().crateEditInventory(crateName);
					CrateManager.editMap.put(player, crateName);
					player.openInventory(editInv);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.MustBePlayer")));
					return true;
				}
				Player player = (Player) sender;
				if (!player.hasPermission("crates.admin.delete")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.NoPermission")));
					return true;
				}
				String crateName = args[1];
				if (!plugin.getCrateManager().crateExists(crateName)) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.CrateDoesNotExist")));
					return true;
				} else {
					CrateManager.deleteMap.put(player, crateName);
					Inventory confirmationInv = Bukkit.createInventory(null, 27, ChatColor.RED + "Delete Crate: "
							+ ChatColor.BOLD + crateName + ChatColor.RESET + "" + ChatColor.RED + "?");
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
					return true;
				}
			}
		} else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("give")) {
				if (!sender.hasPermission("crates.admin.give")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.NoPermission")));
					return true;
				}
				String targetName = args[1];
				String crateName = args[2];
				String unparsedAmount = args[3];
				int amount = 0;
				Player target = Bukkit.getPlayer(targetName);
				if (target == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.PlayerNotOnline")));
					return true;
				}
				if (!plugin.getCrateManager().crateExists(crateName)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.CrateDoesNotExist")));
					return true;
				}
				try {
					amount = Integer.parseInt(unparsedAmount);
				} catch (Exception e) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("Messages.InvalidInput")));
					return true;
				}
				plugin.getCrateManager().giveCrate(target, crateName, amount);
				return true;
			}
		}
		return false;
	}

}
