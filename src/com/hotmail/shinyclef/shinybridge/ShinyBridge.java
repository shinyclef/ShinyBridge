package com.hotmail.shinyclef.shinybridge;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * User: Shinyclef
 * Date: 12/07/13
 * Time: 12:14 AM
 */

public class ShinyBridge extends JavaPlugin
{
    private static ShinyBridge plugin;
    private static Logger log;
    private NetConnDelegator netConnDelegator;

    @Override
    public void onEnable()
    {
        plugin = this;
        log = this.getLogger();
        CmdExecutor cmdExecutor = new CmdExecutor();
        initializeConnDelegator();
    }

    @Override
    public void onDisable()
    {

    }

    private void initializeConnDelegator()
    {
        int port = plugin.getConfig().getInt("ServerSettings.Port");
        ServerSocket serverSocket;

        try
        {
            serverSocket = new ServerSocket(port);
            netConnDelegator = new NetConnDelegator(serverSocket);
            new Thread(netConnDelegator).start();
            this.getLogger().info("Thread started");
        }
        catch (IOException e)
        {
            log.info("SEVERE! Server could not listen on port: " + port + "! Plugin is shutting down.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static void log(String msg)
    {
        new LogMessage(msg).runTask(plugin);
    }

    public static ShinyBridge getPlugin()
    {
        return plugin;
    }

    private static class LogMessage extends BukkitRunnable
    {
        private String msg;

        private LogMessage(String msg)
        {
            this.msg = msg;
        }

        @Override
        public void run()
        {
            log.info(msg);
        }
    }
}
