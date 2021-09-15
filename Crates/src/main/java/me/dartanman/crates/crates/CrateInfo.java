package me.dartanman.crates.crates;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * CrateInfo - Holds some information to crate Crates with
 * @author Austin Dart (Dartanman)
 */
public class CrateInfo {
	
	public static HashMap<Player, CrateInfo> createCrateMap = new HashMap();
	
	private String crateName;
	private int minItems;
	private int maxItems;
	private ItemStack crateItem;
	
	/**
	 * Constructs a new CrateInfo object
	 * @param crateName
	 *   Name of the Crate
	 * @param minItems
	 *   Minimum items to get from crate
	 * @param maxItems
	 *   Maximum items to get from crate
	 * @param crateItem
	 *   The physical crate
	 */
	public CrateInfo(String crateName, int minItems, int maxItems, ItemStack crateItem) {
		this.crateName = crateName;
		this.setMinItems(minItems);
		this.setMaxItems(maxItems);
		this.setCrateItem(crateItem);
	}
	
	/**
	 * Returns the crate's name
	 * @return
	 *   The crate's name
	 */
	public String getCrateName() {
		return crateName;
	}
	
	/**
	 * Sets the crate's name to the given String
	 * @param crateName
	 *   The new name
	 */
	public void setCrateName(String crateName) {
		this.crateName = crateName;
	}

	/**
	 * Returns the minimum number of items from the crate
	 * @return
	 *   The minimum number of items
	 */
	public int getMinItems() {
		return minItems;
	}

	/**
	 * Sets the minimum number of items from the crate
	 * @param minItems
	 *   The minimum number of items
	 */
	public void setMinItems(int minItems) {
		this.minItems = minItems;
	}

	/**
	 * Returns the maximum number of items from the crate
	 * @return
	 *   The maximum number of items
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * Sets the maximum number of items from the crate
	 * @param maxItems
	 *   The maximum number of items
	 */
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}

	/**
	 * Returns the ItemStack that is the physical crate
	 * @return
	 *   The physical crate item
	 */
	public ItemStack getCrateItem() {
		return crateItem;
	}

	/**
	 * Sets the ItemStack that is the physical crate
	 * @param crateItem
	 *   The physical crate item
	 */
	public void setCrateItem(ItemStack crateItem) {
		this.crateItem = crateItem;
	}

}
