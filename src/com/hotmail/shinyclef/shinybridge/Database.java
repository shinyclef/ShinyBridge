package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: Peter
 * Date: 8/08/13
 * Time: 7:41 PM
 */

public class Database
{
    private static ShinyBridge plugin;
    private static Logger log;
    private static Connection connection;
    private static Statement statement;

    private static String user;
    private static String password;
    private static String databaseName;
    private static String port;
    private static String hostname;

    //debug
    private static Map<String, String> debugMap;

    public static void setupDebug()
    {
        debugMap = new LinkedHashMap<String, String>();
        debugMap.put("SetupDatabase", "OK");
        debugMap.put("InstantiateAccounts", "OK");
        debugMap.put("InsertAccount", "OK");
        debugMap.put("DeleteAccount", "OK");
        debugMap.put("UpdatePasswordHash", "OK");
    }

    public static void printDebug(CommandSender sender)
    {
        sender.sendMessage(ChatColor.BLUE + "Debug Info:");
        for (Map.Entry<String, String> entry : debugMap.entrySet())
        {
            sender.sendMessage(ChatColor.AQUA + entry.getKey() + ": " + ChatColor.YELLOW + entry.getValue());
        }
        sender.sendMessage(ChatColor.BLUE + "Please notify shinyclef if any values are not 'OK'.");
    }

    /* Prepares the database driver for use with connections. */
    public static void prepareConnection(ShinyBridge thePlugin)
    {
        plugin = thePlugin;
        log = plugin.getLogger();

        //create the connection to the database
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) //problem with driver
        {
            log.info("SEVERE!!! UNABLE TO PREPARE DATABASE DRIVER. DISABLING PLUGIN.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        //get database settings
        Configuration config = plugin.getConfig();
        user = config.getString("Database.User");
        password = config.getString("Database.Password");
        databaseName = config.getString("Database.DatabaseName");
        port = config.getString("Database.Port");
        hostname = config.getString("Database.Hostname");
    }

    /* Establishes new connection and statement objects with the database.
    * This method should be called before accessing the database. */
    public static void connect()
    {
        //create the connection to the database
        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":"
                    + port + "/" + databaseName, user, password);
        }
        catch (SQLException ex) //problem with connection
        {
            System.out.println(ex.getMessage());
            log.info("An error has occurred while trying to establish a connection to the database: "
                    + ex.getMessage());
            return;
        }

        //create a statement object to use
        try
        {
            statement = connection.createStatement();
        }
        catch (SQLException ex)
        {
            log.info("An error has occurred while trying to establish a connection to the database: "
                    + ex.getMessage());
        }
    }

    /* Closes the connection and statement objects.
    * This method should be called after the database has been accessed. */
    public static void disconnect()
    {
        try
        {
            connection.close();
        }
        catch (SQLException ex)
        {
            log.info("An error has occurred while trying to close the connection to the database: "
                    + ex.getMessage());
        }
    }

    public static class onPluginLoad extends BukkitRunnable
    {
        @Override
        public void run()
        {
            //be careful of order
            setupDebug();
            connect();
            setupDatabase();
            instantiateAccounts();
            disconnect();
        }
    }

    private static void setupDatabase()
    {
        String createRafflePlayer =
                "Create Table if not exists RolyDPlusAccount (" +
                        "Username varchar(16) not null," +
                        "PasswordHash varchar(70) not null," +
                        "Rank varchar(10) not null," +
                        "Primary Key (Username));";

        //creating the tables if they don't exist
        try
        {
            statement.executeUpdate(createRafflePlayer);
        }
        catch (SQLException ex)
        {
            debugMap.put("SetupDatabase", ex.getMessage());
            return;
        }
    }

    private static void instantiateAccounts()
    {
        String selectAll = "Select * From RolyDPlusAccount";
        ResultSet rs;

        try
        {
            PreparedStatement prep = connection.prepareStatement(selectAll);
            rs = prep.executeQuery();
            while (rs.next())
            {
                //get the username, passwordHash and rank from db, and populate accountMap
                String username = rs.getString(1);
                String passwordHash = rs.getString(2);
                Account.Rank rank = Account.Rank.valueOf(rs.getString(3));
                Account.getAccountMap().put(username.toLowerCase(), new Account(username, passwordHash, rank));
            }
        }
        catch (SQLException ex)
        {
            debugMap.put("InstantiateAccounts", ex.getMessage());
            return;
        }
    }

    public static class InsertAccount extends BukkitRunnable
    {
        private String username;
        private String passwordHash;
        private String rank;

        public InsertAccount(String username, String passwordHash, String rank)
        {
            this.username = username;
            this.passwordHash = passwordHash;
            this.rank = rank;
        }

        @Override
        public void run()
        {
            String selectCount = "Select Count(Username) as Count From RolyDPlusAccount Where Username = ?";
            String insertStatement = "Insert Into RolyDPlusAccount Values (?, ?, ?);";
            ResultSet rs;

            connect();

            try
            {
                //if account exists, stop
                PreparedStatement prep = connection.prepareStatement(selectCount);
                prep.setString(1, username);
                rs = prep.executeQuery();
                int count = 0;

                //get the count
                while (rs.next())
                {
                    count = rs.getInt(1);
                }

                //if this account already exists, log in debug message and return
                if (count == 1)
                {
                    debugMap.put("InsertAccount", "Account for '" + username + "' already exists.");
                    return;
                }

                prep = connection.prepareStatement(insertStatement);
                prep.setString(1, username);
                prep.setString(2, passwordHash);
                prep.setString(3, rank);
                prep.executeUpdate();
            }
            catch (SQLException ex)
            {
                debugMap.put("InsertAccount", ex.getMessage());
                return;
            }
            finally
            {
                disconnect();
            }
        }
    }

    public static class DeleteAccount extends BukkitRunnable
    {
        String username;

        public DeleteAccount(String username)
        {
            this.username = username;
        }

        @Override
        public void run()
        {
            String deleteStatement = "Delete From RolyDPlusAccount Where Username = ?";

            connect();

            try
            {
                PreparedStatement prep = connection.prepareStatement(deleteStatement);
                prep.setString(1, username);
                prep.executeUpdate();
            }
            catch (SQLException e)
            {
                debugMap.put("DeleteAccount", e.getMessage());
                return;
            }
            finally
            {
                disconnect();
            }
        }
    }

    public static class UpdatePasswordHash extends BukkitRunnable
    {
        String username;
        String newHash;

        public UpdatePasswordHash(String username, String newHash)
        {
            this.username = username;
            this.newHash = newHash;
        }

        @Override
        public void run()
        {
            String updateStatement = "Update RolyDPlusAccount Set PasswordHash = ? Where Username = ?";

            connect();

            try
            {
                PreparedStatement prep = connection.prepareStatement(updateStatement);
                prep.setString(1, newHash);
                prep.setString(2, username);
                prep.executeUpdate();
            }
            catch (SQLException e)
            {
                debugMap.put("UpdatePasswordHash", e.getMessage());
            }
            finally
            {
                disconnect();
            }
        }
    }
}
