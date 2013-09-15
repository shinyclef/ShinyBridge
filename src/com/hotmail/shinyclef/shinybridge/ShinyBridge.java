package com.hotmail.shinyclef.shinybridge;

import com.hotmail.shinyclef.shinybase.ShinyBase;
import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.NetConnectionDelegator;
import com.hotmail.shinyclef.shinybridge.cmdadaptations.PreProcessParser;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
    public static final boolean DEV_BUILD = true;

    private static ShinyBridge plugin;
    private static ShinyBridgeAPI shinyBridgeAPI;
    private static ShinyBaseAPI shinyBaseAPI;
    private static Logger log;

    @Override
    public void onEnable()
    {
        //assign variables
        plugin = this;
        log = this.getLogger();

        //setup shinyBase
        Plugin base = Bukkit.getPluginManager().getPlugin("ShinyBase");
        if (base != null)
        {
            shinyBaseAPI = ((ShinyBase)base).getShinyBaseAPI();
        }

        //command executor and event listener
        new EventListener(this, shinyBaseAPI);
        CommandExecutor cmdExecutor = new CmdExecutor();
        getCommand("rolydplus").setExecutor(cmdExecutor);

        //make sure config exists
        saveDefaultConfig();

        //initialization components
        Database.prepareConnection(this);
        new Database.onPluginLoad().runTaskAsynchronously(plugin);
        MCServer.initialize(this);
        PreProcessParser.initialize(shinyBaseAPI);
        initializeConnDelegator();
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

    private void initializeConnDelegator()
    {
        int port = plugin.getConfig().getInt("Server.Port");
        ServerSocket serverSocket;

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
}
