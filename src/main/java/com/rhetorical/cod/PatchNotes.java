package com.rhetorical.cod;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class PatchNotes {
    private static String[] notes = {
            "Added:\n" + ChatColor.GREEN +
                    "- '/cod notes' to check patch notes. Permission: 'com.patchNotes'\n" +
                    "- Message, experience, and credits when downing a player using final stand.",
            ChatColor.RESET + "Changes & Fixes:\n" + ChatColor.GRAY +
                    "* Fixed an issue where the player list wasn't updated after leaving a game or returning to the lobby.\n" +
                    "* Fixed an issue where the player list didn't update names correctly. \n" +
                    "* Fixed an issue where players spawned in the ground if the map was located in a different world from the lobby.\n" +
                    "* Fixed an issue where when players used final stand it would produce an error in the console when they got back up.\n" +
                    "* Fixed an issue with scavenger not working properly and producing an error.\n" +
                    "* Fixed an issue with leveling up producing an error.\n" +
                    "* When players level up they are now rewarded their guns when leaving the game or returning to the lobby.\n" +
                    "* Players states are saved before joining cod, and is reset upon leaving. \n" +
                    "* Fixed a bug when picking up items from within a game where it would produce an error to the console.\n" +
                    "* Fixed a bug where players in last stand would become invincible.\n" +
                    "* Fixed a bug where players would be able to use final stand more than once at a time.\n" +
                    "* Fixed a bug where players would have super speed when they got out of final stand.\n" +
                    "* Players in final stand now have their speed set to 0."
    };
    static void getPatchNotes(CommandSender s) {
        s.sendMessage("=====[" + ChatColor.BOLD + ChatColor.RED + " COM-Warfare " + ChatColor.GOLD	+ Main.getPlugin().getDescription().getVersion() + ChatColor.RESET + "]=====");
        for (String note : notes) {
            s.sendMessage(note);
        }
    }
}