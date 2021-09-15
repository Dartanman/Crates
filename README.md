# Crates
Crates plugin.

Requirements:
  Java 8 or higher
  A MySQL Server and Database
  Bukkit/Spigot Server running Minecraft 1.8 or higher.

Configurable Settings:
  Every message is configurable.
  Cooldown between crates is configurable.
  Daily limit as to how many crates each player can open is configurable.

Commands:
  /crate <create/edit/delete> <crate> - Creates, edits, or deletes <crate> after a confirmation GUI
  /crate give <player> <crate> <amount> - Gives <player> <amount> of <crate>

Permissions:
  crates.admin.create - Allows /crate create
  crates.admin.delete - Allows /crate delete
  crates.admin.edit - Allows /crate edit
  crates.bypass - Bypass cooldown and daily limit
  
To Open A Crate:
  [1] Right-click while holding the crate in your hand.
  [2] Click "Confirm" in the GUI.
  [3] Enjoy watching the crate open in a fun little GUI.
  [4] Your item(s) get added to your inventory
