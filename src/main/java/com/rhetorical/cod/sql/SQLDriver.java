package com.rhetorical.cod.sql;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rhetorical.cod.ComWarfare;
import net.md_5.bungee.api.ChatColor;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class SQLDriver {

    private static SQLDriver instance;
    public Thread startThread;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private int maxReconnects;
    private int initialTimeout;
    private boolean autoReconnect;
    private boolean useSSL;
    private boolean verifyServerCertificate;
    private Connection connection;

    //load server data
    public SQLDriver() {
        startThread = new Thread(() -> {
            ComWarfare.MySQL = true;
            host = ComWarfare.getInstance().getConfig().getString("MySQL.Host", "localhost");
            port = ComWarfare.getInstance().getConfig().getString("MySQL.Port", "3306");
            database = ComWarfare.getInstance().getConfig().getString("MySQL.Database", "COM-Warfare");
            username = ComWarfare.getInstance().getConfig().getString("MySQL.Username", "root");
            password = ComWarfare.getInstance().getConfig().getString("MySQL.Password", "");
            autoReconnect = ComWarfare.getInstance().getConfig().getBoolean("MySQL.autoReconnect", true);
            useSSL = ComWarfare.getInstance().getConfig().getBoolean("MySQL.useSSL", true);
            verifyServerCertificate = ComWarfare.getInstance().getConfig().getBoolean("MySQL.verifyServerCertificate", false);
            maxReconnects = ComWarfare.getInstance().getConfig().getInt("MySQL.maxReconnects", 5);
            initialTimeout = ComWarfare.getInstance().getConfig().getInt("MySQL.initialTimeout", 3);
            connect();
        });
        startThread.start();
    }

    public static SQLDriver getInstance() {
        if (instance == null)
            instance = new SQLDriver();

        return instance;
    }

    public void connect() {
        try {
            long startTime = System.currentTimeMillis();
            connection = DriverManager.getConnection("jdbc:mysql://" +
                            host + ":" +
                            port + "/" +
                            database +
                            "?useSSL=" + useSSL +
                            "&autoReconnect=" + autoReconnect +
                            "&maxReconnects=" + maxReconnects +
                            "&initialTimeout=" + initialTimeout +
                            "&verifyServerCertificate=" + verifyServerCertificate,
                    username,
                    password);
            if (isConnected()) {
                long timeTaken = System.currentTimeMillis() - startTime;
                createTables();
                ComWarfare.MySQL = true;
                ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Connected to MySQL database. " + ChatColor.GRAY + "(" + ChatColor.GOLD + timeTaken + ChatColor.YELLOW + " ms" + ChatColor.GRAY + ")");
            } else {
                ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.RED + "Failed to connect to MySQL database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ComWarfare.MySQL = false;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                // Wait 2 seconds before closing. This ain't perfect but it help avoid some issues
                // This is run on its own thread called from onDisable so it won't impact the servers shutdown time.
                Thread.sleep(2000);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return connection != null;
    }


    private void createTables() throws Exception {
        getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS stats (" +
                "uuid VARCHAR(36),\n" +
                "kills INT UNSIGNED,\n" +
                "deaths INT UNSIGNED,\n" +
                "level INT UNSIGNED,\n" +
                "prestige INT UNSIGNED,\n" +
                "experience DOUBLE,\n" +
                "credits INT UNSIGNED,\n" +
                "PRIMARY KEY(uuid));")
                .executeUpdate();

        getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS purchases (" +
                "uuid VARCHAR(36),\n" +
                "guns LONGTEXT,\n" +
                "weapons LONGTEXT,\n" +
                "perks LONGTEXT,\n" +
                "PRIMARY KEY(uuid));")
                .executeUpdate();

        getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS killstreaks (" +
                "uuid VARCHAR(36),\n" +
                "killstreaks LONGTEXT,\n" +
                "PRIMARY KEY(uuid));")
                .executeUpdate();

        getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS loadouts (" +
                "uuid VARCHAR(36),\n" +
                "loadouts LONGTEXT,\n" +
                "PRIMARY KEY(uuid));")
                .executeUpdate();
    }


//------------------------------------------------------------ Getters ------------------------------------------------------------\\


    public int getKills(UUID uuid) {
        return getInt(uuid, "stats", "kills");
    }

    public int getDeaths(UUID uuid) {
        return getInt(uuid, "stats", "deaths");
    }

    public int getLevel(UUID uuid) {
        return getInt(uuid, "stats", "level");
    }

    public int getPrestige(UUID uuid) {
        return getInt(uuid, "stats", "prestige");
    }

    public int getCredits(UUID uuid) {
        return getInt(uuid, "stats", "credits");
    }

    public double getExperience(UUID uuid) {
        return getDouble(uuid, "stats", "experience");
    }

    public List<String> getPurchasedGuns(UUID uuid) {
        return getList(uuid, "purchases", "guns");
    }

    public List<String> getPurchasedWeapons(UUID uuid) {
        return getList(uuid, "purchases", "weapons");
    }

    public List<String> getPurchasedPerks(UUID uuid) {
        return getList(uuid, "purchases", "perks");
    }

    public List<String> getKillstreaks(UUID uuid) {
        return getList(uuid, "killstreaks", "killstreaks");
    }

    public JsonObject getLoadout(UUID uuid) {
        return getJsonObject(uuid, "loadouts", "loadouts", "uuid", uuid.toString());
    }


//------------------------------------------------------------ Setters ------------------------------------------------------------\\


    public void setKills(UUID uuid, int kills) {
        setInt(uuid, "stats", kills, "kills");
    }

    public void setDeaths(UUID uuid, int deaths) {
        setInt(uuid, "stats", deaths, "deaths");
    }

    public void setLevel(UUID uuid, int level) {
        setInt(uuid, "stats", level, "level");
    }

    public void setPrestige(UUID uuid, int prestige) {
        setInt(uuid, "stats", prestige, "prestige");
    }

    public void setCredits(UUID uuid, int credits) {
        setInt(uuid, "stats", credits, "credits");
    }

    public void setExperience(UUID uuid, double experience) {
        setDouble(uuid, "stats", experience, "experience");
    }

    public void setPurchasedGuns(UUID uuid, List<String> guns) {
        setList(uuid, "purchases", guns, "guns");
    }

    public void setPurchasedWeapons(UUID uuid, List<String> weapons) {
        setList(uuid, "purchases", weapons, "weapons");
    }

    public void setPurchasedPerks(UUID uuid, List<String> perks) {
        setList(uuid, "purchases", perks, "perks");
    }

    public void setKillstreaks(UUID uuid, List<String> killstreaks) {
        setList(uuid, "killstreaks", killstreaks, "killstreaks");
    }

    public void setLoadouts(UUID uuid, JsonObject jsonObject) {
        setJsonObject(uuid, "loadouts", jsonObject, "loadouts", "uuid");
    }


//------------------------------------------------------------ Getter Methods ------------------------------------------------------------\\

    // To get integers
    public int getInt(UUID uuid, String table, String dataType) {
        int[] number = new int[1];
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT " + dataType + " FROM " + table + " WHERE uuid=?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getString(dataType) == null) {
                    number[0] = 0;
                    return;
                }
                number[0] = rs.getInt(dataType);
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while loading MySQL data!");
            }
        });
        thread.setName("COM-Warfare SQL - Get Int");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        return number[0];
    }

    // To get doubles
    public double getDouble(UUID uuid, String table, String dataType) {
        double[] amount = new double[1];
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT " + dataType + " FROM " + table + " WHERE uuid=?;");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getString(dataType) == null) {
                    amount[0] = 0.0;
                    return;
                }
                amount[0] = rs.getDouble(dataType);
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while loading MySQL data!");
            }
        });
        thread.setName("COM-Warfare SQL - Get Double");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        return amount[0];
    }

    // To get lists
    public List<String> getList(UUID uuid, String table, String dataType) {
        // AtomicReferenced list so it can be accessed in the async thread
        AtomicReference<List<String>> list = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT " + dataType + " FROM " + table + " WHERE uuid=?");
                // Set uuid in PreparedStatement to the actual uuid
                ps.setString(1, uuid.toString());
                // Set ResultSet to the result of the query
                ResultSet rs = ps.executeQuery();
                // return an empty list if the ResultSet is empty, or if the result of the dataType is null
                if (!rs.next() || rs.getString(dataType) == null) {
                    list.set(Collections.emptyList());
                    return;
                }
                // set list to the result of the dataType
                list.set(Collections.singletonList(rs.getString(dataType)));
                // Close Result Set to free up its resources and prevent memory leakage
                rs.close();
                // Close Prepared Statement to free up its resources and prevent memory leakage
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while loading MySQL data!");
            }
        });
        thread.setName("COM-Warfare SQL - Get List");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        return list.get();
    }

    // To get JSON objects
    public JsonObject getJsonObject(UUID uuid, String table, String dataType, String primaryKey, String primaryKeyValue) {
        AtomicReference<JsonObject> jsonObject = new AtomicReference<>(new JsonObject());
        Thread thread = new Thread(() -> {
            try {
                PreparedStatement ps = getConnection().prepareStatement("SELECT " + dataType + " FROM " + table + " WHERE " + primaryKey + "=?;");
                ps.setString(1, primaryKeyValue);
                                /*
                Hello :)
                - Insprill
                                */
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getString(dataType) == null) {
                    jsonObject.set(new JsonObject());
                    return;
                }
                jsonObject.set(new JsonParser().parse(rs.getString(dataType)).getAsJsonObject());
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while loading MySQL data!");
            }
        });
        thread.setName("COM-Warfare SQL - Get JsonObject");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        return jsonObject.get();
    }


//------------------------------------------------------------ Setter Methods ------------------------------------------------------------\\


    // To set integers
    public void setInt(UUID uuid, String table, int number, String dataType) {
        new Thread(() -> {
            Thread.currentThread().setName("COM-Warfare SQL - Set Int");
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " + table + " (uuid," + dataType + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + dataType + "=" + number);
                ps.setString(1, uuid.toString());
                ps.setInt(2, number);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while saving MySQL data!");
            }
        }).start();
    }

    // To set doubles
    public void setDouble(UUID uuid, String table, double amount, String dataType) {
        new Thread(() -> {
            Thread.currentThread().setName("COM-Warfare SQL - Set Double");
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " + table + " (uuid," + dataType + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + dataType + "=" + amount);
                ps.setString(1, uuid.toString());
                ps.setDouble(2, amount);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while saving MySQL data!");
            }
        }).start();
    }

    // To set lists
    public void setList(UUID uuid, String table, List<String> list, String dataType) {
        new Thread(() -> {
            Thread.currentThread().setName("COM-Warfare SQL - Set List");
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " + table + " (uuid," + dataType + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + dataType + "=?");
                // Set uuid in PreparedStatement to the actual uuid
                ps.setString(1, uuid.toString());
                // Create custom string from list
                StringBuilder sb = new StringBuilder();
                for (String element : list) {
                    sb.append(element).append("::");
                }
                // Set the 2 dataTypes in Prepared Statement to the StringBuilders string
                ps.setString(2, sb.toString());
                ps.setString(3, sb.toString());
                // Execute Prepared Statement.
                ps.executeUpdate();
                // Close Prepared Statement to free up its resources and prevent memory leakage
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while saving MySQL data!");
            }
        }).start();
    }

    // To set JSON objects
    public void setJsonObject(UUID uuid, String table, JsonObject jsonObject, String dataType, String primaryKey) {
        new Thread(() -> {
            Thread.currentThread().setName("COM-Warfare SQL - Set JsonObject");
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO " + table + " (" + primaryKey + "," + dataType + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + dataType + "=?");
                ps.setString(1, uuid.toString());
                ps.setString(2, jsonObject.toString());
                ps.setString(3, jsonObject.toString());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "An error has occurred while saving MySQL data!");
            }
        }).start();
    }


}
