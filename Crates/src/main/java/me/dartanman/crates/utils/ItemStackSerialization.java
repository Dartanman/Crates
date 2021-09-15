package me.dartanman.crates.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * ItemStackSerialization - Based off of graywolf336's Inventory serialization. Used to store ItemStack arrays as Strings.
 * @author Austin Dart (Dartanman)
 * @author graywolf336 - I got the concept from him many, many years ago. It's still my go-to.
 */
public class ItemStackSerialization {
	
	/**
	 * Converts an ItemStack array to a Base64 String
	 * @param items
	 *   Array of items
	 * @return
	 *   Items as a Base64 String
	 */
    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (int i=0; i<items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
 
    /**
     * Converts a Base64 String to an ItemStack array
     * @param data
     *   Base64 String
     * @return
     *   Array of items
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i=0; i<items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            return null;
        }
    }

}
