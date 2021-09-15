package me.dartanman.crates.crates;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;

import me.dartanman.crates.Crates;
import me.dartanman.crates.utils.ItemStackSerialization;
import me.dartanman.crates.utils.WeightedRandomList;

/**
 * CrateManager - Manages almost everything that has to do with Crates
 * @author Austin Dart (Dartanman)
 */
public class CrateManager {
	
	private Crates plugin;
	
	public static HashMap<Player, String> editMap = new HashMap();
	public static HashMap<Player, String> deleteMap = new HashMap();
	public static HashMap<Player, ItemStack> itemMap = new HashMap();
	public static HashMap<Player, String> openMap = new HashMap();
	public static List<Player> chanceList = new ArrayList();
	
	/**
	 * Constructs a CrateManager object
	 * @param pl
	 *   Main class
	 */
	public CrateManager(Crates pl) {
		plugin = pl;
	}
	
	/**
	 * Checks if a crate exists with the given name
	 * @param crateName
	 *   The name of the crate
	 * @return
	 *   Whether the crate exists
	 */
	public boolean crateExists(String crateName) {
		if(plugin.getConfig().get("Crates") == null) {
			return false;
		}
		Set<String> crateNames = plugin.getConfig().getConfigurationSection("Crates").getKeys(false);
		return crateNames.contains(crateName);
	}
	
	/**
	 * Checks if the given player can open a crate under the current circumstances
	 * @param player
	 *   The player to check for
	 * @return
	 *   Whether they can open a crate right now
	 */
	public boolean canOpenCrate(Player player) {
		if(player.hasPermission("crates.bypass")) {
			return true;
		}
		long cooldownMillis = plugin.getConfig().getLong("Settings.CrateOpenCooldownSeconds");
		cooldownMillis *= 1000L;
		Connection connection = plugin.getMySQLInfo().getConnection();
		UUID uuid = player.getUniqueId();
		int maxCrates = plugin.getConfig().getInt("Settings.MaxCratesPerPlayerPerDay");
		int playerCrates = plugin.getMySQLManager().getCratesToday(connection, uuid);
		long lastOpenTime = plugin.getMySQLManager().getLastOpenTime(connection, uuid);
		long now = System.currentTimeMillis();
		if(playerCrates >= maxCrates || now - lastOpenTime < cooldownMillis) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Actually goes through the opening process of a crate for a player
	 * @param player
	 *   The player to open a crate
	 * @param crateName
	 *   The crate to open
	 */
	public void openCrate(final Player player, final String crateName) {
		Connection connection = plugin.getMySQLInfo().getConnection();
		UUID uuid = player.getUniqueId();
		plugin.getMySQLManager().setCratesToday(connection, plugin.getMySQLManager().getCratesToday(connection, uuid) + 1, uuid);
		plugin.getMySQLManager().setLastOpenTime(connection, System.currentTimeMillis(), uuid);
		ItemStack placeholder1 = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta meta = placeholder1.getItemMeta();
		meta.setDisplayName("...");
		placeholder1.setItemMeta(meta);
		final ItemStack placeholder = new ItemStack(placeholder1);
		placeholder.setDurability((short) 15);
		final Inventory inv = Bukkit.createInventory(null, 45, ChatColor.GREEN + "Opening Crate...");
		ItemStack item = player.getItemInHand();
		if(item.getAmount() == 1) {
			player.getInventory().removeItem(item);
		}else {
			item.setAmount(item.getAmount() - 1);
			player.updateInventory();
		}
		player.openInventory(inv);
		
		
		
		new BukkitRunnable() {
			int startA = 0;
			int startB = 44;
			
	           public void run() {
	        	   if(startA < startB) {
		        	   inv.setItem(startA, placeholder);
				    	inv.setItem(startB, placeholder);
				    	startA++;
				    	startB--;
	        	   }else {
			    		ItemStack[] toAdd = chooseRandomItems(crateName);
						int slot = 22;
						int amtCopy = toAdd.length;
						while(amtCopy > 2) {
							slot--;
							amtCopy -= 2;
						}
						for(int i = 0; i < toAdd.length; i++) {
							inv.setItem(slot, toAdd[i]);
							slot++;
							player.getInventory().addItem(toAdd[i]);
							  String message = plugin.getConfig().getString("Messages.ItemReceived");
							  String name = "item";
							  if(toAdd[i].getItemMeta().hasDisplayName()) {
								  name = toAdd[i].getItemMeta().getDisplayName();
							  }else {
								  name = toAdd[i].getType().toString();
							  }
							  message = message.replace("%item_name%", name);
							  message = message.replace("%item_chance%", String.valueOf(getChance(crateName, toAdd[0])));
							  player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
						}
						
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							  public void run() {
								  if(player.getOpenInventory() != null) {
									  if(player.getOpenInventory().getTitle().equals(ChatColor.GREEN + "Opening Crate...")) {
										  player.closeInventory();
									  }
								  }
								  }
							}, 40L);
						
						cancel();
			    	}
	           }
	         
	       }.runTaskTimer(plugin, 2L, 2L);
	}
	
	/**
	 * Gives a physical crate to a player
	 * @param player
	 *   The player to give a crate to
	 * @param crateName
	 *   The crate to give
	 */
	public void giveCrate(Player player, String crateName) {
		if(crateExists(crateName)) {
			ItemStack crate = plugin.getConfig().getItemStack("Crates." + crateName + ".Item");
			player.getInventory().addItem(crate);
		}
	}
	
	/**
	 * Gives any amount of crates to a player
	 * @param player
	 *   The player to give crates to
	 * @param crateName
	 *   The kind of crate to give
	 * @param amount
	 *   The amount of crates to give
	 */
	public void giveCrate(Player player, String crateName, int amount) {
		for(int i = 0; i < amount; i++) {
			giveCrate(player, crateName);
		}
	}
	
	/**
	 * Converts info from a CrateInfo object into usable information in the config
	 * @param crateInfo
	 *   The CrateInfo object to read from
	 */
	public void createCrateInConfig(CrateInfo crateInfo) {
		plugin.getConfig().set("Crates." + crateInfo.getCrateName() + ".Item", crateInfo.getCrateItem());
		plugin.getConfig().set("Crates." + crateInfo.getCrateName() + ".MinItems", crateInfo.getMinItems());
		plugin.getConfig().set("Crates." + crateInfo.getCrateName() + ".MaxItems", crateInfo.getMaxItems());
		plugin.saveConfig();
	}
	
	/**
	 * Adds an item to a crate
	 * @param crateName
	 *   The crate to add it to
	 * @param item
	 *   The item to add
	 * @param weight
	 *   The percentage chance to get that item from the crate
	 * @return
	 *   True if successful, false if not (usually means the crate doesn't exist, but could be that the item is already in the crate)
	 */
	public boolean addItem(String crateName, ItemStack item, double weight) {
		if(crateExists(crateName)) {
			if(containsItem(crateName, item)) {
				return false;
			}
			String itemToBase64 = ItemStackSerialization.itemStackArrayToBase64(new ItemStack[] {item});
			String itemAndWeight = itemToBase64  + "///:::///" + weight;
			List<String> list = new ArrayList();
			if(plugin.getConfig().get("Crates." + crateName + ".Rewards") == null) {
				list.add(itemAndWeight);
			}else {
				list = plugin.getConfig().getStringList("Crates." + crateName + ".Rewards");
				list.add(itemAndWeight);
			}
			plugin.getConfig().set("Crates." + crateName + ".Rewards", list);
			plugin.saveConfig();
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if an item is already in a crate
	 * @param crateName
	 *   The crate to check
	 * @param item
	 *   The item to check for
	 * @return
	 *   Whether the item is in the crate
	 */
	public boolean containsItem(String crateName, ItemStack item) {
		if(getItemsAndWeights(crateName) == null) {
			return false;
		}else {
			if(getItemsAndWeights(crateName).keySet().contains(item)) {
				return true;
			}else {
				return false;
			}
		}
	}
	
	/**
	 * Removes an item from a crate. Does nothing if the item is already not in the crate.
	 * @param crateName
	 *   The crate to remove it from
	 * @param item
	 *   The item to remove
	 */
	public void removeItem(String crateName, ItemStack item) {
		if (crateExists(crateName)) {
			List<String> list = plugin.getConfig().getStringList("Crates." + crateName + ".Rewards");
			for (int i = 0; i < list.size(); i++) {
				String itemAndWeight = list.get(i);
				String[] split = itemAndWeight.split("///:::///");
				String base64Item = split[0];
				ItemStack[] singleItemArray = ItemStackSerialization.itemStackArrayFromBase64(base64Item);
				ItemStack itemFromBase64 = singleItemArray[0];
				ItemMeta meta = itemFromBase64.getItemMeta();
				meta.setLore(null);
				itemFromBase64.setItemMeta(meta);
				ItemMeta oMeta = item.getItemMeta();
				oMeta.setLore(null);
				item.setItemMeta(oMeta);
				if (itemFromBase64.equals(item)) {
					list.remove(i);
					break;
				}
			}
			plugin.getConfig().set("Crates." + crateName + ".Rewards", list);
			plugin.saveConfig();
		}
	}
	
	/**
	 * Changes the weight/percentage of the item in the crate
	 * @param crateName
	 *   The crate to change the weight in
	 * @param item
	 *   The item to change the weight of
	 * @param weight
	 *   The new weight of the item
	 */
	public void editItem(String crateName, ItemStack item, double weight) {
		if(crateExists(crateName)) {
			List<String> list = plugin.getConfig().getStringList("Crates." + crateName + ".Rewards");
			for (int i = 0; i < list.size(); i++) {
				String itemAndWeight = list.get(i);
				String[] split = itemAndWeight.split("///:::///");
				String base64Item = split[0];
				ItemStack[] singleItemArray = ItemStackSerialization.itemStackArrayFromBase64(base64Item);
				ItemStack itemFromBase64 = singleItemArray[0];
				ItemMeta meta = itemFromBase64.getItemMeta();
				meta.setLore(null);
				itemFromBase64.setItemMeta(meta);
				ItemMeta oMeta = item.getItemMeta();
				oMeta.setLore(null);
				item.setItemMeta(oMeta);
				if (itemFromBase64.equals(item)) {
					String newItemAndWeight = base64Item + "///:::///" + weight;
					list.set(i, newItemAndWeight);
				}
			}
			plugin.getConfig().set("Crates." + crateName + ".Rewards", list);
			plugin.saveConfig();
		}
	}
	
	/**
	 * Weighted List of items in the crate. If the crate's weights do not get to 100%, adds air to get it to 100%.
	 * @param crateName
	 *   The crate to check for
	 * @return
	 *   A Weighted List of items
	 */
	public WeightedRandomList<ItemStack> getItems(String crateName) {
		double totalChance = 0;
		if(crateExists(crateName)) {
			if(getItemsAndWeights(crateName) != null) {
				WeightedRandomList<ItemStack> randomList = new WeightedRandomList<ItemStack>();
				for(ItemStack item : getItemsAndWeights(crateName).keySet()) {
					randomList.addEntry(item, getItemsAndWeights(crateName).get(item));
					totalChance += getItemsAndWeights(crateName).get(item);
				}
				if(totalChance < 100d) {
					ItemStack air = new ItemStack(Material.AIR);
					randomList.addEntry(air, 100d - totalChance);
				}
				return randomList;
			}
		}
		return null;
	}
	
	/**
	 * Chooses the correct amount of items. Items are semi-randomly chosen. Items with higher weights are chosen more frequently than those with lower weights.
	 * @param crateName
	 * @return
	 *   An ItemStack array of semi-randomly chosen items.
	 */
	public ItemStack[] chooseRandomItems(String crateName) {
		if(crateExists(crateName)) {
			WeightedRandomList<ItemStack> randomList = getItems(crateName);
			int min = plugin.getConfig().getInt("Crates." + crateName + ".MinItems");
			int max = plugin.getConfig().getInt("Crates." + crateName + ".MaxItems");
			int amt = min;
			if(min != max) {
				Random r = new Random();
				amt = r.nextInt(max + 1 - min) + min;
			}
			ItemStack[] items = new ItemStack[amt];
			for(int i = 0; i < amt; i++) {
				items[i] = randomList.getRandom();
			}
			return items;
		}
		return null;
	}
	
	/**
	 * Gets the chance of the given item appearing in the given crate
	 * @param crateName
	 *   The crate to check in
	 * @param item
	 *   The item to check
	 * @return
	 *   The percentage chance of the item being received from the crate
	 */
	public double getChance(String crateName, ItemStack item) {
		if(crateExists(crateName)) {
			if(getItemsAndWeights(crateName) != null) {
				if(getItemsAndWeights(crateName).containsKey(item)) {
					return getItemsAndWeights(crateName).get(item);
				}
			}
		}
		return 0d;
	}
	
	/**
	 * Gets a HashMap of all items and their respective weights from a crate
	 * @param crateName
	 *   The crate to check
	 * @return
	 *   All items and their respective weights
	 */
	public HashMap<ItemStack, Double> getItemsAndWeights(String crateName) {
		if(crateExists(crateName)) {
			if(plugin.getConfig().get("Crates." + crateName + ".Rewards") != null) {
				List<String> list = plugin.getConfig().getStringList("Crates." + crateName + ".Rewards");
				HashMap<ItemStack, Double> map = new HashMap();
				for(String itemAndWeight : list) {
					String[] split = itemAndWeight.split("///:::///");
					String base64Item = split[0];
					double weight = Double.parseDouble(split[1]);
					ItemStack[] singleItemArray = ItemStackSerialization.itemStackArrayFromBase64(base64Item);
					ItemStack item = singleItemArray[0];
					map.put(item, weight);
				}
				return map;
			}
		}
		return null;
	}
	
	/**
	 * Creates the Inventory used when performing /crate edit <crate>
	 * @param crateName
	 *   The crate to edit
	 * @return
	 *   The editing Inventory.
	 */
	public Inventory crateEditInventory(String crateName) {
		if(crateExists(crateName)) {
			if(getItemsAndWeights(crateName) == null) {
				Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Edit " + ChatColor.BOLD + crateName);
				return inv;
			}
			Set<ItemStack> items = getItemsAndWeights(crateName).keySet();
			List<ItemStack> itemList = new ArrayList();
			itemList.addAll(items);
			double itemCount = itemList.size();
			double rows = itemCount / 9;
			int rowsInt = (int) Math.ceil(rows);
			if(rowsInt == rows) {
				rowsInt += 1;
			}
			int slots = rowsInt * 9;
			// 54 is the max size for an inventory (6 * 9 = 54)
			if(slots > 54) {
				slots = 54;
			}
			Inventory inv = Bukkit.createInventory(null, slots, ChatColor.GREEN + "Edit " + ChatColor.BOLD + crateName);
			for(int i = 0; i < itemList.size(); i++) {
				ItemStack item = itemList.get(i);
				List<String> lore = new ArrayList();
				if(item.getItemMeta().hasLore()) {
					lore = item.getItemMeta().getLore();
					lore.add(" ");
				}
				lore.add(ChatColor.GREEN + "CHANCE: " + getItemsAndWeights(crateName).get(item) + "%");
				lore.add(ChatColor.YELLOW + "RIGHT-CLICK TO REMOVE");
				lore.add(ChatColor.YELLOW + "SHIFT-CLICK TO EDIT CHANCE");
				ItemMeta meta = item.getItemMeta();
				meta.setLore(lore);
				item.setItemMeta(meta);
				inv.setItem(i, item);
			}
			return inv;
		}
		return null;
	}

}
