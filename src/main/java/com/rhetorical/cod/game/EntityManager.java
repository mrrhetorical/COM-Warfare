package com.rhetorical.cod.game;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle spawning &amp; despawning entities within a match.
 * */

class EntityManager {
	private List<Entity> spawned = new ArrayList<>();

	void registerEntity(Entity e) {
		spawned.add(e);
	}

	void clearEntities() {
		final List<Entity> entities = new ArrayList<>(spawned);
		spawned.clear();
		for (Entity e : entities) {
			if (e != null)
				e.remove();
		}
	}
}
