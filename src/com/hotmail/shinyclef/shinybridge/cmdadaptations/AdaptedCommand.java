package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.EventListener;
import com.hotmail.shinyclef.shinybridge.ShinyBridge;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;

/**
 * User: Shinyclef
 * Date: 6/10/13
 * Time: 4:57 PM
 */

public abstract class AdaptedCommand
{
    protected static ShinyBridge p = ShinyBridge.getPlugin();
    protected static Server s = p.getServer();
    protected static ShinyBaseAPI base = p.getShinyBaseAPI();
    protected static Configuration config = p.getConfig();

    public static void initialize()
    {
        PreProcessParser.initialize(p, p.getShinyBaseAPI());
        Invisible.initialise();
        Me.initialise();
        Modreq.initialize();
        Money.initialize(p.getEconomy());
        Say.initialize();
    }

    protected static void registerCommand(String command)
    {
        EventListener.registerCommand(command);
    }
}
