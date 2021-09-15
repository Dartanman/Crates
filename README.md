# Crates
A simple Crates plugin to give your players crates! Perfect for any survival-based server.

Requirements:
- Java 8 or higher
- Bukkit/Spigot Server running Minecraft 1.8 or higher

Configurable Settings:
- Choose between YAML storage and MySQL storage
- Every message is configurable.
- Cooldown between crates is configurable.
- Daily limit as to how many crates each player can open is configurable.

Commands:
/crate <create/edit/delete> <crate/name> - Creates, edits, or deletes after a confirmation GUI
/crate give <player/name> <crate/name> <amount/of/crates> - Gives <player/name> <amount/of/crates> of <crate/name>

Permissions:
crates.admin.create - Allows /crate create
crates.admin.delete - Allows /crate delete
crates.admin.edit - Allows /crate edit
crates.bypass - Bypass cooldown and daily limit

To Create A Crate:
[1] Get the crate item (e.g. a Chest)
[2] Rename it to what you want the Crate to be called. You can use an Anvil, or another plugin.
[3] While holding the item in hand, do /crate create <crateName>
[4] Click "Confirm"
[5] Do /crate edit <crateName>
[6] Left-click items from your inventory to add them to the crate.
[7] Once added, shift-click them to edit their chance of being gotten.
[8] Right-click items to remove them from the crate.

To Open A Crate:
[1] Right-click while holding the crate in your hand.
[2] Click "Confirm" in the GUI.
[3] Enjoy watching the crate open in a fun little GUI.
[4] Your item(s) get added to your inventory
