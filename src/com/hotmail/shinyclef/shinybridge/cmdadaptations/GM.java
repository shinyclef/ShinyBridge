package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.CmdExecutor;
import com.hotmail.shinyclef.shinybridge.MCServer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * User: Shinyclef
 * Date: 12/10/13
 * Time: 2:43 AM
 */

public class GM extends AdaptedCommand
{
    public static void initialize()
    {
        registerCommand("/gm");
    }

    public static void processGM(PlayerCommandPreprocessEvent e, CommandSender sender, String[] args)
    {
        //we only care about clients, and those that have permission
        if (!(sender instanceof MCServer.ClientPlayer) || !sender.hasPermission(CmdExecutor.MOD_PERM))
        {
            return;
        }

        //we want to block all use of /gm that has no target (no args)
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "You cannot toggle creative for yourself from RolyDPlus. Please " +
                    "include a target player.");
            e.setCancelled(true);
        }
    }
}
