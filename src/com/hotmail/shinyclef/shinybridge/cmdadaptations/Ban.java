package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.Account;
import com.hotmail.shinyclef.shinybridge.MCServer;
import com.hotmail.shinyclef.shinybridge.NetProtocolHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * User: Shinyclef
 * Date: 9/10/13
 * Time: 10:02 PM
 */

public class Ban extends AdaptedCommand
{
    public static void initialize()
    {
        registerCommand("/ban");
        registerCommand("/tempban");
        registerCommand("/unban");
    }

    public static void banPreprocess(final String command, final CommandSender sender, final String[] args)
    {
        //do nothing if user doesn't have perm
        if (!sender.hasPermission(MOD_PERM))
        {
            //no perms message handled by ban plugin
            return;
        }

        //do nothing if insufficient parameters
        if (args.length < 1)
        {
            return;
        }

        String playerName = args[0];
        if (MCServer.isServerOnline(playerName))
        {
            playerName = s.getPlayer(playerName).getName();
        }

        final String targetPlayerName = playerName;

        //if user is already banned, do nothing.
        if (MCServer.isBanned(targetPlayerName.toLowerCase()))
        {
            return;
        }

        Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable()
        {
            @Override
            public void run()
            {
                banPostProcess(targetPlayerName, command, sender, args);
            }
        }, 0);
    }

    /* Handles bans and tempbans. Runs 'after' the command has been completed. */
    public static void banPostProcess(String targetPlayerName, String command, CommandSender sender, String[] args)
    {
        //get lc name
        String targetPlayerNameLC = targetPlayerName.toLowerCase();

        //if user is not actually banned, command was wrong, do nothing.
        if (!MCServer.isBanned(targetPlayerNameLC))
        {
            return;
        }

        /* Note: From this point, we can assume a correct command with a banned user. */


        //get type, tempBanLength (if applicable) and reason
        String type = command.substring(1).toLowerCase();
        String tempBanLength;
        String reason;

        //formulate tempBanLength and reason depending on whether it's a tempBan or not
        if (type.equals("tempban"))
        {
            tempBanLength = args[1];

            if (args.length > 2)
            {
                reason = base.makeSentence(args, 2);
            }
            else
            {
                reason = "undefined.";
            }
        }
        else
        {
            tempBanLength = null;
            if (args.length > 1)
            {
                reason = base.makeSentence(args, 1);
            }
            else
            {
                reason = "undefined.";
            }
        }


        //inform r+ users
        if (type.equalsIgnoreCase("ban"))
        {
            NetProtocolHelper.broadcastChat(ChatColor.GOLD + "Player " + ChatColor.YELLOW + targetPlayerName +
                    ChatColor.GOLD + " was banned by " + ChatColor.YELLOW + sender.getName() + ChatColor.GOLD + "!" +
                    " Reason: " + ChatColor.YELLOW + reason, false);
        }
        else if (type.equalsIgnoreCase("tempban"))
        {
            NetProtocolHelper.broadcastChat(ChatColor.GOLD + "Player " + ChatColor.YELLOW + targetPlayerName +
                    ChatColor.GOLD + " was temp-banned by " + ChatColor.YELLOW + sender.getName() + ChatColor.GOLD +
                    " for " + ChatColor.YELLOW + tempBanLength + ChatColor.GOLD + ". Reason: " + ChatColor.YELLOW +
                    reason, false);
        }

        //check if user has an r+ account
        Account account = Account.getAccountMap().get(targetPlayerNameLC);
        if (account == null)
        {
            //no need to do anything, user is now banned with no r+ account
            return;
        }

        //if user is on r+, kick them
        if (account.isOnline())
        {
            account.kick(type, tempBanLength, reason);
        }

        //unregister their account, they can only re-register from server after their ban
        Account.unregister(targetPlayerNameLC);
    }

    public static void unbanPreprocess(final CommandSender sender, String[] args)
    {
        //do nothing if user doesn't have perm
        if (!sender.hasPermission(MOD_PERM))
        {
            //no perms message handled by ban plugin
            return;
        }

        //do nothing if insufficient parameters
        if (args.length < 1)
        {
            return;
        }

        String playerName = args[0];
        if (MCServer.isServerOnline(playerName))
        {
            playerName = s.getPlayer(playerName).getName();
        }

        final String targetPlayerName = playerName;

        //do nothing if they're not banned
        if (!MCServer.isBanned(targetPlayerName.toLowerCase()))
        {
            return;
        }


        Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable()
        {
            @Override
            public void run()
            {
                unbanPostprocess(targetPlayerName, sender);
            }
        }, 30);
    }

    private static void unbanPostprocess(String targetPlayerName, CommandSender sender)
    {
        //get lc name
        String targetPlayerNameLC = targetPlayerName.toLowerCase();

        //if user is not actually unbanned, command was wrong, do nothing.
        if (MCServer.isBanned(targetPlayerNameLC))
        {
            return;
        }

        /* Note: From this point, we can assume a correct command with an unbanned user. */

        //inform r+ users
        NetProtocolHelper.broadcastChat(ChatColor.YELLOW + targetPlayerName + ChatColor.GOLD + " was unbanned by " +
                ChatColor.YELLOW + sender.getName() + ChatColor.GOLD + "!", false);
    }
}
