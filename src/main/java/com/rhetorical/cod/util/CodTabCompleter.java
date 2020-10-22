package com.rhetorical.cod.util;


import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.game.CodMap;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class CodTabCompleter implements TabCompleter {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> Args = new ArrayList<>();
        if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
            return Collections.emptyList();
        if (args.length == 1) {
            if (ComWarfare.hasPerm(sender, "com.help")) {
                Args.add("help");
            }

            if (ComWarfare.hasPerm(sender, "com.join") && sender instanceof Player) {
                Args.add("menu");
                Args.add("join");
                Args.add("browser");
                Args.add("balance");
            }

            if (ComWarfare.hasPerm(sender, "com.leave") && sender instanceof Player) {
                Args.add("leave");
            }

            if (ComWarfare.hasPerm(sender, "com.lobby") && sender instanceof Player) {
                Args.add("lobby");
            }

            if (ComWarfare.hasPerm(sender, "com.openShop") && sender instanceof Player) {
                Args.add("shop");
            }

            if (ComWarfare.hasPerm(sender, "com.selectClass") && sender instanceof Player) {
                Args.add("class");
            }

            if (ComWarfare.hasPerm(sender, "com.map.list")) {
                Args.add("listMaps");
            }

            if (ComWarfare.hasPerm(sender, "com.forceStart") && sender instanceof Player) {
                Args.add("start");
            }

            if (ComWarfare.hasPerm(sender, "com.bootAll")) {
                Args.add("boot");
            }

            if (ComWarfare.hasPerm(sender, "com.changeMap") && sender instanceof Player) {
                Args.add("changeMap");
            }

            if (ComWarfare.hasPerm(sender, "com.modifyLevel")) {
                Args.add("setLevel");
            }

            if (ComWarfare.hasPerm(sender, "com.credits.give")) {
                Args.add("credits");
            }

            if (ComWarfare.hasPerm(sender, "com.createGun")) {
                Args.add("createGun");
            }

            if (ComWarfare.hasPerm(sender, "com.createWeapon")) {
                Args.add("createWeapon");
                Args.add("createGrenade");
            }

            if (ComWarfare.hasPerm(sender, "com.map.create")) {
                Args.add("createMap");
            }

            if (ComWarfare.hasPerm(sender, "com.map.remove")) {
                Args.add("removeMap");
            }

            if (ComWarfare.hasPerm(sender, "com.map.modify") && sender instanceof Player) {
                Args.add("set");
            }

            if (ComWarfare.hasPerm(sender, "com.map.modify")) {
                Args.add("reload");
            }

            if (ComWarfare.hasPerm(sender, "com.add")) {
                Args.add("add");
            }

            if (ComWarfare.hasPerm(sender, "com.blacklist")) {
                Args.add("blacklist");
            }

            if (ComWarfare.hasPerm(sender, "com.version")) {
                Args.add("version");
            }

            if (ComWarfare.hasPerm(sender, "com.removeSpawns")) {
                Args.add("removeSpawns");
            }

            if (ComWarfare.hasPerm(sender, "com.changeMode") && sender instanceof Player) {
                Args.add("changeMode");
            }

            /*if (ComWarfare.hasPerm(sender, "com.convertdata")) {
                Args.add("convertdata");
            }*/

            return matchingArgs(Args, args[0]);

        } else if (args.length == 2) {
            switch (args[0]) {
                case "help":
                    Args.add("1");
                    Args.add("2");
                    Args.add("3");
                    Args.add("4");
                    Args.add("5");
                    break;
                case "join":
                    GameManager.getAddedMaps().forEach(map -> Args.add(map.getName()));
                    break;

                case "convertdata":
                    Args.add("YAML->MySQL");
                    break;

                case "credits":
                    if (ComWarfare.hasPerm(sender, "com.credits.give"))
                        Args.add("give");
                    if (ComWarfare.hasPerm(sender, "com.credits.set"))
                        Args.add("set");
                    break;

                case "set":
                    if (ComWarfare.hasPerm(sender, "com.map.modify")) {
                        Args.add("flag");
                        Args.add("lobby");
                        Args.add("spawn");
                    }
                    break;

                case "setLevel":
                    Bukkit.getOnlinePlayers().forEach(p -> Args.add(p.getName()));
                    break;

                case "add":
                    Args.add("oitc");
                    Args.add("gun");
                    break;

                case "removeSpawns":
                case "removeMap":
                case "blacklist":
                case "reload":
                    GameManager.getAddedMaps().forEach(map -> Args.add(map.getName()));
                    break;

            }

            return matchingArgs(Args, args[1]);

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("credits") && args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("set")) {
                Bukkit.getOnlinePlayers().forEach(p -> Args.add(p.getName()));
            } else if (args[0].equalsIgnoreCase("blacklist")) {
                Arrays.asList(Gamemode.values()).forEach(gm -> Args.add(gm.toString()));
            }
            return matchingArgs(Args, args[2]);
        }
        return Args;
    }

    public List<String> matchingArgs(List<String> Args, String arg) {
        List<String> finalOne = new ArrayList<>();
        for (String s : Args) {
            if (!s.toLowerCase().startsWith(arg.toLowerCase())) continue;
            finalOne.add(s);
        }
        return finalOne;
    }


}
