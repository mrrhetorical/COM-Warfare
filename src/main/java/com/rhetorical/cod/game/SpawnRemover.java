package com.rhetorical.cod.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SpawnRemover {

	private static List<String> mapsShowingSpawns = new ArrayList<>();

	private static List<Block> shownBlocks = new ArrayList<>();

	public static void showSpawns(CodMap map) {
		List<Location> spawns = new ArrayList<>(map.getBlueSpawns());
		spawns.addAll(map.getRedSpawns());
		spawns.addAll(map.getPinkSpawns());

		for (Location spawn : spawns) {
			Material blockMat;
			if (map.getPinkSpawns().contains(spawn))
				blockMat = Material.PINK_GLAZED_TERRACOTTA;
			else if (map.getBlueSpawns().contains(spawn))
				blockMat = Material.BLUE_GLAZED_TERRACOTTA;
			else
				blockMat = Material.RED_GLAZED_TERRACOTTA;

			shownBlocks.add(spawn.getBlock());
			spawn.getBlock().setType(blockMat);
		}

		mapsShowingSpawns.add(map.getName());
	}

	public static void clearSpawns(CodMap map) {
		List<Location> spawns = new ArrayList<>(map.getBlueSpawns());
		spawns.addAll(map.getRedSpawns());
		spawns.addAll(map.getPinkSpawns());

		for (Location spawn : spawns) {
			spawn.getBlock().setType(Material.AIR);
			shownBlocks.remove(spawn.getBlock());
		}

		mapsShowingSpawns.remove(map.getName());
	}

	public static List<Block> getShownBlocks() {
		return shownBlocks;
	}

	public static boolean isShowingSpawns(CodMap map) {
		return mapsShowingSpawns.contains(map.getName());
	}

	public static CodMap getMapWithSpawnBlock(Block block) {
		for (CodMap map : GameManager.getAddedMaps()) {
			List<Location> spawns = new ArrayList<>(map.getBlueSpawns());
			spawns.addAll(map.getRedSpawns());
			spawns.addAll(map.getPinkSpawns());

			for (Location loc : spawns) {
				if (loc.getBlock().equals(block))
					return map;
			}
		}

		return null;
	}

}
