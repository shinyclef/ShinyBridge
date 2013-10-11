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
    protected static final String MOD_PERM = "rolyd.mod";

    protected static ShinyBridge p = ShinyBridge.getPlugin();
    protected static Server s = p.getServer();
    protected static ShinyBaseAPI base = p.getShinyBaseAPI();
    protected static Configuration config = p.getConfig();


    public static void initializeCommands()
    {
        PreProcessParser.initialize(p, p.getShinyBaseAPI());
        Ban.initialize();
        GM.initialize();
        Invisible.initialize();
        Me.initialize();
        Modreq.initialize();
        Money.initialize(p.getEconomy());
        Raffle.initialize();
        Say.initialize();
    }

    protected static void registerCommand(String command)
    {
        EventListener.registerCommand(command);
    }
}
