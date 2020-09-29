package com.rhetorical.cod.util;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.game.CodMap;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tabcomplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> Args = new ArrayList<>();
        if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
            return Collections.emptyList();

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
        if (ComWarfare.hasPerm(sender, "com.lobby")  && sender instanceof Player) {
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
        if (ComWarfare.hasPerm(sender, "com.convertdata")) {
            Args.add("convertdata");
        }


        if (args.length == 2) {
            switch (args[0]) {
                case "help":
                    Args.clear();
                    Args.add("1");
                    Args.add("2");
                    Args.add("3");
                    Args.add("4");
                    Args.add("5");
                    break;
                case "convertdata":
                    Args.clear();
                    Args.add("YAML->MySQL");
                    break;
                case "credits":
                    Args.clear();
                    if (ComWarfare.hasPerm(sender, "com.credits.give"))
                        Args.add("give");
                    if (ComWarfare.hasPerm(sender, "com.credits.set"))
                        Args.add("set");
                    break;
                case "set":
                    Args.clear();
                    if (ComWarfare.hasPerm(sender, "com.map.modify")) {
                        Args.add("flag");
                        Args.add("lobby");
                        Args.add("spawn");
                    }
                    break;
                case "setLevel":
                    Args.clear();
                    for (Player p : Bukkit.getOnlinePlayers())
                        Args.add(p.getName());
                    break;
                case "add":
                    Args.clear();
                    Args.add("oitc");
                    Args.add("gun");
                    break;
                case "removeSpawns":
                case "removeMap":
                case "blacklist":
                case "reload":
                    Args.clear();
                    for (CodMap map : GameManager.getAddedMaps()) {
                        Args.add(map.getName());
                    }
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("credits") && args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("set")) {
                Args.clear();
                for (Player p : Bukkit.getOnlinePlayers())
                    Args.add(p.getName());
            } else if (args[0].equalsIgnoreCase("blacklist")) {
                Args.clear();
                for (Gamemode gm : Gamemode.values()) {
                    Args.add(gm.toString());
                }
            }
        } else if (args.length > 2) {
            Args.clear();
        }

        ArrayList<String> finalOne = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Args) {
                if (!s.toLowerCase().startsWith(args[0].toLowerCase())) continue;
                finalOne.add(s);
            }
            return finalOne;
        }
        if (args.length == 2) {
            for (String s : Args) {
                if (!s.toLowerCase().startsWith(args[1].toLowerCase())) continue;
                finalOne.add(s);
            }
            return finalOne;
        }
        if (args.length == 3) {
            for (String s : Args) {
                if (!s.toLowerCase().startsWith(args[2].toLowerCase())) continue;
                finalOne.add(s);
            }
            return finalOne;
        }
        return Args;
    }

}