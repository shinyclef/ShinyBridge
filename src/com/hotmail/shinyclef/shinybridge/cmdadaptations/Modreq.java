package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.CmdExecutor;
import com.hotmail.shinyclef.shinybridge.MCServer;
import com.hotmail.shinyclef.shinybridge.NetProtocolHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * User: Shinyclef
 * Date: 8/10/13
 * Time: 10:03 PM
 */

public class Modreq extends AdaptedCommand
{
    public static void initialize()
    {
        registerCommand("/modreq");
        registerCommand("/claim");
        registerCommand("/done");
    }

    public static void processModreq(CommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            return;
        }

        String message = ChatColor.GREEN + "A new request has been filed by " + sender.getName() + ".";
        NetProtocolHelper.broadcastChat(message, CmdExecutor.MOD_PERM, false);
    }

    public static void processClaim(CommandSender sender, String[] args)
    {
        if (!(sender instanceof MCServer.ClientPlayer) ||
                !sender.hasPermission(CmdExecutor.MOD_PERM) ||
                args.length < 1)
        {
            return;
        }

        try
        {
            int number = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            return;
        }

        //simple feedback
        sender.sendMessage(ChatColor.GOLD + "Claim attempt on ticket #" + args[0] + ".");
    }

    public static void processDone(CommandSender sender, String[] args)
    {
        if (!(sender instanceof MCServer.ClientPlayer) ||
                !sender.hasPermission(CmdExecutor.MOD_PERM) ||
                args.length < 1)
        {
            return;
        }

        try
        {
            int number = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            return;
        }

        //simple feedback
        sender.sendMessage(ChatColor.GOLD + "Completion attempt on ticket #" + args[0] + ".");
    }
}
