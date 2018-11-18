package com.rhetorical.cod;

public class ComVersion {

	private static boolean purchased;

	public static void setup(boolean p) {
		purchased = p;
	}

	public static boolean getPurchased() {
		return purchased;
	}
}