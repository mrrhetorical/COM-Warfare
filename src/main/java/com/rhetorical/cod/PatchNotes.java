package com.rhetorical.cod;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class PatchNotes {
    private static String[] notes = {
//            "Added:\n" + ChatColor.GREEN +
//                    "+ Added the ability to vote for the next map. (Players click an item in their inventory to do so).\n" +
//                    "+ Added \"lang.yml\" file but have yet to add much functionality. (Will be fully released in version 2.8.0).\n",
            ChatColor.RESET + "Changes & Fixes:\n" + ChatColor.GRAY +
					"* Instead of a close button, on screens where clicking where the close button would be that wouldn't actually close the menu, a back button now replaces it.\n" +
					"* Fixed some issues reading from the config some values.\n" +
					"* Revamped kill streaks selection menu. Now players will choose which streaks they want much more easily.\n" +
					"* Fixed a bug where kill streaks previously weren't saved to the 'killstreaks.yml' file.\n" +
					"* Shops now no longer work with material types, but now with the names of the items.\n" +
					"* Optimized utilizing default weapons/guns by reducing the amount of reading done from the config.\n" +
					"* '/cod createGun' and '/cod createWeapon' commands now support using item data. Do use it, after the material name, append ':(data value)'. For example 'iron_hoe:40'.\n" +
					"* Guns & Weapons are no longer stored raw in the 'guns.yml' file. They now only store the name of the item's material, and the data. WARNING: Please back up your 'guns.yml' file before updating this plugin. The file now uses a new format for storing guns.\n" +
					"* Optimized further loading of weapons from the 'guns.yml' file.\n" +
					"* When getting an error message when an item material doesn't exist, a more detailed error message will appear.\n" +
					"* Optimized most instances of duplicate code."
    };
    static void getPatchNotes(CommandSender s) {
        s.sendMessage("=====[" + ChatColor.BOLD + ChatColor.RED + " COM-Warfare " + ChatColor.GOLD	+ Main.getPlugin().getDescription().getVersion() + ChatColor.RESET + "]=====");
        for (String note : notes) {
            s.sendMessage(note);
        }
    }
}