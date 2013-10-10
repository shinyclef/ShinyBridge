package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.MCServer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * User: Shinyclef
 * Date: 9/10/13
 * Time: 9:22 PM
 */

public class Raffle extends AdaptedCommand
{
    public static void initialize()
    {
        registerCommand("/raffle");
        registerCommand("/raf");
    }

    public static void rafflePreprocess(PlayerCommandPreprocessEvent e, CommandSender sender, String[] args)
    {
        //don't interfere with any commands coming from server
        if (!(sender instanceof MCServer.ClientPlayer))
        {
            return;
        }

        if (!sender.hasPermission("rolyd.vip"))
        {
            sender.sendMessage(ChatColor.RED + "Participating in raffle from RolyDPlus is a feature reserved " +
                    "for VIPs. Sorry!");
            e.setCancelled(true);
        }
    }
}
