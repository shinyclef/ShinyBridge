package com.hotmail.shinyclef.shinybridge;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
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
    private static ShinyBridge plugin;
    private static Logger log;
    private NetConnectionDelegator netConnectionDelegator;

    @Override
    public void onEnable()
    {
        //assign variables
        plugin = this;
        log = this.getLogger();

        //command executor and event listener
        new EventListener(this);
        CommandExecutor cmdExecutor = new CmdExecutor();
        getCommand("rolydplus").setExecutor(cmdExecutor);

        //make sure config exists
        saveDefaultConfig();

        //initialization components
        Database.prepareConnection(this);
        new Database.onPluginLoad().runTaskAsynchronously(plugin);
        MCServer.initialize(this);
        initializeConnDelegator();

        //add all online players to the chatTagMap
        for (Player player : getServer().getOnlinePlayers())
        {
            MCServer.addToPlayerChatTagMap(player);
        }
    }

    @Override
    public void onDisable()
    {

    }

    private void initializeConnDelegator()
    {
        int port = plugin.getConfig().getInt("Server.Port");
        ServerSocket serverSocket;

        try
        {
            serverSocket = new ServerSocket(port);
            netConnectionDelegator = new NetConnectionDelegator(serverSocket);
            new Thread(netConnectionDelegator).start();
        }
        catch (IOException e)
        {
            log.info("SEVERE! Server could not listen on port: " + port + "! Plugin is shutting down.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static ShinyBridge getPlugin()
    {
        return plugin;
    }
}
