package com.rhetorical.cod.object;

import org.bukkit.inventory.ItemStack;

public class Scorestreak {
	
	private String name;
	private ScoreStreakCost type;
	private int cost;
	
	private ItemStack item;
	
	public Scorestreak(String name, ScoreStreakCost type, int cost, ItemStack item) {
		this.name = name;
		this.type = type;
		this.cost = cost;
		this.item = item;
	}
	
	public int getCost() {
		return this.cost;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ScoreStreakCost getType() {
		return this.type;
	}
	
	public ItemStack getItem() {
		return this.item;
	}
	
	public void setCost(int newCost) {
		this.cost = newCost;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void setItem(ItemStack newItem) {
		this.item = newItem;
	}
	
	public void setType(ScoreStreakCost newType) {
		this.type = newType;
	}
	
}
