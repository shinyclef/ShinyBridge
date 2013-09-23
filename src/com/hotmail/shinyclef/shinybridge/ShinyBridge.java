package com.hotmail.shinyclef.shinybridge;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.hotmail.shinyclef.shinybase.ShinyBase;
import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.cmdadaptations.PreProcessParser;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 12:14 AM
 */

public class ShinyBridge extends JavaPlugin
{
    public static final boolean DEV_BUILD = false;
    private static final String SERVER_VERSION = "1.0.1";
    private static final String SUPPORTED_CLIENT_VERSION = "1.0.1";
    private static int[] versionParts;

    private static ShinyBridge plugin;
    private static ShinyBridgeAPI shinyBridgeAPI;
    private static ShinyBaseAPI shinyBaseAPI;
    private static ProtocolManager protocolManager;
    private static Logger log;

    private ServerSocket serverSocket;
    private boolean acceptingConnections;

    @Override
    public void onEnable()
    {
        //assign variables
        versionParts = new int[3];
        String[] versionStrings = SUPPORTED_CLIENT_VERSION.split("\\.");
        versionParts[0] = Integer.parseInt(versionStrings[0]);
        versionParts[1] = Integer.parseInt(versionStrings[1]);
        versionParts[2] = Integer.parseInt(versionStrings[2]);

        plugin = this;
        log = this.getLogger();
        boolean scoreboardEnabled = true;

        //setup shinyBase
        Plugin base = Bukkit.getPluginManager().getPlugin("ShinyBase");
        if (base != null)
        {
            shinyBaseAPI = ((ShinyBase)base).getShinyBaseAPI();
        }

        //setup ProtocolLip
        try
        {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }
        catch (NoClassDefFoundError e)
        {
            log.info("WARNING! ProtocolLib not found! Scoreboard functionality disabled!");
            scoreboardEnabled = false;
        }

        //command executor and event listener
        new EventListener(this, shinyBaseAPI);
        CommandExecutor cmdExecutor = new CmdExecutor();
        getCommand("rolydplus").setExecutor(cmdExecutor);

        //make sure config and teams.txt exist
        saveDefaultConfig();
        File teamsFile = new File(plugin.getDataFolder(), "teams.txt");
        try
        {
            teamsFile.createNewFile(); //only create new if it doesn't exist
        }
        catch (IOException e)
        {
            log.info("WARNING! Error creating teams.txt. Scoreboard functionality disabled!");
            scoreboardEnabled = false;
        }

        //initialization components
        MCServer.initialize(this);
        Database.prepareConnection(this);
        new Database.onPluginLoad().runTaskAsynchronously(plugin);
        ScoreboardManager.initialise(this, protocolManager, teamsFile, scoreboardEnabled);
        PreProcessParser.initialize(shinyBaseAPI);
        startAcceptingClients();
        shinyBridgeAPI = new ShinyBridgeAPI();

        //add all online players to the chatTagMap
        for (Player player : getServer().getOnlinePlayers())
        {
            MCServer.addToPlayerChatTagMap(player);
        }
    }

    @Override
    public void onDisable()
    {
        //send disconnect to all clients
    }

    public void stopAcceptingClients()
    {
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            //already closed
        }
        acceptingConnections = false;
    }

    public void startAcceptingClients()
    {
        initializeConnDelegator();
        acceptingConnections = true;
    }

    private void initializeConnDelegator()
    {
        int port = plugin.getConfig().getInt("Server.Port");

        try
        {
            serverSocket = new ServerSocket(port);
            NetConnectionDelegator netConnectionDelegator = new NetConnectionDelegator(serverSocket);
            new Thread(netConnectionDelegator).start();
        }
        catch (IOException e)
        {
            log.info("SEVERE! Server could not listen on port: " + port + "! Plugin is shutting down.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static synchronized ShinyBridge getPlugin()
    {
        return plugin;
    }

    public ShinyBaseAPI getShinyBaseAPI()
    {
        return shinyBaseAPI;
    }

    public ShinyBridgeAPI getShinyBridgeAPI()
    {
        return shinyBridgeAPI;
    }

    public static int[] getVersion()
    {
        return versionParts;
    }

    public boolean isAcceptingConnections()
    {
        return acceptingConnections;
    }
}
