package com.rhetorical.cod;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

class PatchNotes {
    private static String[] notes = {
            "Added:\n" + ChatColor.GREEN +
                    "+ Added built-in support for CrackShot! Use the same name for COM-Warfare's guns as you use in CrackShot's config for CrackShot to have full functionality!",
            ChatColor.RESET + "Changes & Fixes:\n" + ChatColor.GRAY +
					"* Fixed a bug that would sometimes occur when players died, causing an exception.\n" +
					"* Fixed a bug where sometimes damage from firearms wouldn't be registered.\n" +
					"* Fixed a bug where sometimes games would end prematurely.\n" +
					"* Optimized code related to giving players guns.\n" +
					"* Players can no longer use the '/cod lobby' command while within a match."
    };
    static void getPatchNotes(CommandSender s) {
        s.sendMessage("=====[" + ChatColor.BOLD + ChatColor.RED + " COM-Warfare " + ChatColor.GOLD	+ Main.getPlugin().getDescription().getVersion() + ChatColor.RESET + "]=====");
        for (String note : notes) {
            s.sendMessage(note);
        }
    }
}