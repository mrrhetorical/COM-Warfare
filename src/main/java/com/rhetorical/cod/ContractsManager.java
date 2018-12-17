package com.rhetorical.cod;


import org.bukkit.Bukkit;

import java.time.LocalDate;

public class ContractsManager {

	private LocalDate currentDate;

	public void setup() {
		currentDate = LocalDate.now();
		checkDate();
	}

	

}