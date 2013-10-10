package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.*;
import com.sun.javafx.sg.PGShape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Author: Shinyclef
 * Date: 26/08/13
 * Time: 4:48 AM
 */

public class PreProcessParser
{
    private static ShinyBridge p;
    private static ShinyBaseAPI base;
    private static final String MOD_PERM = "rolyd.mod";

    public static void initialize(ShinyBridge plugin, ShinyBaseAPI base)
    {
        p = plugin;
        PreProcessParser.base = base;
    }

    /* Handles PlayerCommandPreProcess events forwarded by the event listener in standard
    * Command Executor format (sender, command, args) with the event itself included. */
    public static void playerCommandParser(final PlayerCommandPreprocessEvent e, final CommandSender sender,
                                           final String command, final String[] args)
    {
        /* ATTENTION! Commands must be in EventListener.commandList in order to not be ignored. */

        switch (command.substring(1).toLowerCase())
        {
            case "r+": case "rolydplus": case "rplus":
                CmdExecutor.rPlusParser(e, sender, args);
                break;

            case "ban": case "tempban":
                Ban.banPreprocess(command, sender, args);
                break;

            case "unban":
                Ban.unbanPreprocess(sender, args);
                break;

            case "inv": case "invisible":
                Invisible.invPostProcess(sender, args);
                break;

            case "say":
                Say.processSay(e, sender, args);
                break;

            case "me":
                Me.processMe(e, sender, args);
                break;

            case "money":
                Money.processMoney(e, sender, args);
                break;

            case "modreq":
                Modreq.processModreq(sender, args);
                break;

            case "claim":
                Modreq.processClaim(sender, args);
                break;

            case "done":
                Modreq.processDone(sender, args);
                break;

            case "raffle": case "raf":
                Raffle.rafflePreprocess(e, sender, args);
                break;
        }
    }

    /* Handles ServerCommand events forwarded by the event listener in standard
    * Command Executor format (sender, command, args) with the event itself included. */
    public static void consoleCommandParser(final ServerCommandEvent e, final CommandSender sender,
                                            final String command, final String[] args)
    {
        /* ATTENTION! Commands must be in EventListener.commandList in order to not be ignored. */

        switch (command.substring(1).toLowerCase())
        {
            case "say":
                Say.processSay(null, sender, args);
                break;
        }
    }

    /* Handles refreshing of all permissions when an authorised player uses a /pex command. */
    private static void pexPostProcess(CommandSender sender)
    {
        if (!sender.hasPermission(CmdExecutor.MOD_PERM))
        {
            return;
        }

        MCServer.refreshAllPermissions();
    }
}
