package com.rhetorical.cod.sql;

import com.rhetorical.cod.ComWarfare;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SQLDriver {

	private static SQLDriver instance;

	private boolean connected;

	private String ip,
			username,
			password;


	//load server data
	private SQLDriver() {

		setIp(ComWarfare.getInstance().getConfig().getString("mySql.ip", "none"));
		setUsername(ComWarfare.getInstance().getConfig().getString("mySql.user", "none"));
		setPassword(ComWarfare.getInstance().getConfig().getString("mySql.pass", "none"));

	}

	public static SQLDriver getInstance() {
		if (instance != null)
			instance = new SQLDriver();

		return instance;
	}

	public Connection getConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(getIp(), getUsername(), getPassword());
			if (connection != null) {
				System.out.println("Connected to MySQL database.");
				setConnected(true);
			} else {
				System.out.println("Failed to connect to MySQL database.");
				setConnected(false);
			}
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	private void createStatsTable() throws Exception {
		Connection c = getConnection();
		PreparedStatement ps = getConnection().prepareStatement("create table if not exists user_stats (uuid varchar(36) not null,\n" +
				"kills int unsigned not null,\n" +
				"deaths int unsigned not null,\n" +
				"playerLevel int unsigned not null,\n" +
				"prestige int unsigned not null,\n" +
				"experience double not null,\n" +
				"credits int unsigned not null,\n" +
				"purchasedGuns longtext not null,\n" +
				"purchasedWeapons longtext not null,\n" +
				"UNIQUE(uuid),\n" +
				"PRIMARY KEY(uuid));");
		ps.executeUpdate();
		ps.close();
		c.close();
	}

	public int getKills(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select kills from user_stats where uuid = \"%s\";", uuid.toString()));

		int k = rs.getInt("kills");


		statement.close();
		rs.close();
		c.close();

		return k;
	}

	public void setKills(UUID uuid, int kills) throws Exception {
		Connection c = getConnection();
		PreparedStatement ps = getConnection().prepareStatement("");
		ps.executeUpdate();
		ps.close();
		c.close();
	}

	public int getDeaths(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select deaths from user_stats where uuid = \"%s\";", uuid.toString()));

		int d = rs.getInt("deaths");


		statement.close();
		rs.close();
		c.close();

		return d;
	}

	public int getLevel(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select playerLevel from user_stats where uuid = \"%s\";", uuid.toString()));

		int l = rs.getInt("playerLevel");


		statement.close();
		rs.close();
		c.close();

		return l;
	}

	public int getPrestige(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select prestige from user_stats where uuid = \"%s\";", uuid.toString()));

		int l = rs.getInt("prestige");


		statement.close();
		rs.close();
		c.close();

		return l;
	}

	public int getCredits(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select credits from user_stats where uuid = \"%s\";", uuid.toString()));

		int credits = rs.getInt("credits");


		statement.close();
		rs.close();
		c.close();

		return credits;
	}

	public double getExperience(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select experience from user_stats where uuid = \"%s\";", uuid.toString()));

		double experience = rs.getDouble("experience");


		statement.close();
		rs.close();
		c.close();

		return experience;
	}

	public List<String> getPurchasedGuns(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select purchasedGuns from user_stats where uuid = \"%s\";", uuid.toString()));

		String purchased = rs.getString("purchasedGuns");

		List<String> purchasedGuns = Arrays.asList(purchased.split("::"));

		statement.close();
		rs.close();
		c.close();

		return purchasedGuns;
	}

	public List<String> getPurchasedWeapons(UUID uuid) throws Exception {
		Connection c = getConnection();
		Statement statement = c.createStatement();
		ResultSet rs = statement.executeQuery(String.format("select purchasedWeapons from user_stats where uuid = \"%s\";", uuid.toString()));

		String purchased = rs.getString("purchasedWeapons");

		List<String> purchasedWeapons = Arrays.asList(purchased.split("::"));

		statement.close();
		rs.close();
		c.close();

		return purchasedWeapons;
	}


}
