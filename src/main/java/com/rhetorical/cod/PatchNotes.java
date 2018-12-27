package com.rhetorical.cod;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class PatchNotes {
    private static String[] notes = {
            "Added:\n" + ChatColor.GREEN +
                    "+ Added the ability to vote for the next map. (Players click an item in their inventory to do so).\n" +
                    "+ Added \"lang.yml\" file but have yet to add much functionality. (Will be fully released in version 2.8.0).\n",
            ChatColor.RESET + "Changes & Fixes:\n" + ChatColor.GRAY +
                    "* Changed the layout of the main menus.\n" +
                    "* Fixed an issue where an arena's current map would be made available to other arenas.\n" +
                    "* Fixed an issue where the list of arenas would be refreshed every time a new arena instance started.\n" +
                    "* Fixed an issue where maps would show as \"IN USE\" when they were not.\n" +
                    "* Fixed an issue where sometimes players wouldn't take damage in certain instances.\n" +
                    "* Fixed an issue where players sometimes could take damage when in the lobby when they shouldn't.\n" +
                    "* Fixed an issue where not all players would be booted when using the boot command or when restarting the plugin.\n" +
                    "* Fixed a bug where the max player count in the game was always 12 regardless of the setting in the config.\n" +
                    "* Potion effects are now saved and cleared when joining a game."
    };
    static void getPatchNotes(CommandSender s) {
        s.sendMessage("=====[" + ChatColor.BOLD + ChatColor.RED + " COM-Warfare " + ChatColor.GOLD	+ Main.getPlugin().getDescription().getVersion() + ChatColor.RESET + "]=====");
        for (String note : notes) {
            s.sendMessage(note);
        }
    }
}