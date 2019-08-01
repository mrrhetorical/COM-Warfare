package com.rhetorical.cod.inventories;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.Gamemode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MatchBrowser implements Listener {

	private static MatchBrowser instance;

	private Inventory browser;

	public MatchBrowser() {
		if (instance != null)
			return;

		instance = this;

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());

		setupInventory();
		updateInventory();
	}

	public static MatchBrowser getInstance() {
		return instance != null ? instance : new MatchBrowser();
	}

	public Inventory getBrowser() {
		return browser;
	}

	private void setupInventory() {
		browser = Bukkit.createInventory(null, 45, "Match Browser");
		browser.setItem(44, InventoryManager.getInstance().closeInv);
	}

	private void updateInventory() {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				List<ItemStack> toAdd = new ArrayList<>();
				for (GameInstance game : GameManager.getRunningGames()) {
					ItemStack match = generateItem(game);
					toAdd.add(match);
				}

				browser.clear();
				for (ItemStack item : toAdd) {
					browser.addItem(item);
				}
			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 100L);
	}

	private ItemStack generateItem(GameInstance game) {
		ItemStack item = new ItemStack(Material.EMERALD);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(String.format("Join game - %s/%s", game.getPlayers(), Main.getMaxPlayers()));
		List<String> lore = new ArrayList<>();
		lore.add(String.format("%s", game.getId()));
		lore.add(String.format("Map: %s%s", ChatColor.GOLD, game.getMap()));
		lore.add(String.format("Mode: %s%s", ChatColor.GOLD, game.getGamemode()));
		lore.add(String.format("Status: %s%s", game.getState().getColor(), game.getState()));
		lore.add("================");
		for (Player p : game.getPlayers()) {
			if (game.isOnBlueTeam(p)) {
				lore.add(String.format("%s%s", ChatColor.BLUE, p.getName()));
			} else if (game.isOnRedTeam(p)) {
				ChatColor teamColor = game.getGamemode() == Gamemode.INFECT ? ChatColor.DARK_GREEN : ChatColor.RED;
				lore.add(String.format("%s%s", teamColor, p.getName()));
			} else {
				lore.add(String.format("%s%s", ChatColor.YELLOW, p.getName()));
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!e.getInventory().equals(browser))
			return;

		e.setCancelled(true);

		if (e.getCursor() == null || e.getCursor().getType() == Material.AIR)
			return;

		Player p = (Player) e.getWhoClicked();

		ItemStack clicked = e.getCursor();

		if (clicked.getItemMeta() == null || clicked.equals(InventoryManager.getInstance().closeInv)) {
			p.closeInventory();
			return;
		}

		List<String> lore = clicked.getItemMeta().getLore();
		if (lore != null && lore.size() < 1) {
			p.closeInventory();
			return;
		}

		String matchId = lore.get(0);
		long id;

		try {
			id = Long.parseLong(matchId);
		} catch (Exception npe) {
			p.closeInventory();
			return;
		}

		GameInstance found = null;

		for (GameInstance i : GameManager.getRunningGames()) {
			if (i.getId() == id) {
				found = i;
				break;
			}
		}

		if (found == null) {
			p.closeInventory();
			return;
		}

		GameManager.joinGame(p, found);
	}
}
