package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.Account;
import com.hotmail.shinyclef.shinybridge.CmdExecutor;
import com.hotmail.shinyclef.shinybridge.MCServer;
import com.hotmail.shinyclef.shinybridge.ShinyBridge;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Author: Shinyclef
 * Date: 26/08/13
 * Time: 4:48 AM
 */

public class PreProcessParser
{
    private static ShinyBaseAPI base;

    public static void initialize(ShinyBaseAPI base)
    {
        PreProcessParser.base = base;
    }

    /* Handles PlayerCommandPreProcess events forwarded by the event listener in standard
    * Command Executor format (sender, command, args) with the event itself included. */
    public static void parser(PlayerCommandPreprocessEvent e, final CommandSender sender,
                                        final String command, final String[] args)
    {
        switch (command.substring(1))
        {
            case "r+": case "rolydplus": case "rplus":
                CmdExecutor.rPlusParser(e, sender, args);
                break;

            case "ban": case "tempban":
            Bukkit.getScheduler().runTaskLater(ShinyBridge.getPlugin(), new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    banPostProcess(command, sender, args);
                }
            }, 0);
            break;
        }
    }

    /* Handles bans and tempbans. */
    public static void banPostProcess(String command, CommandSender sender, String[] args)
    {
        //do nothing if user doesn't have perm
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(CmdExecutor.NO_PERM);
            return;
        }

        //do nothing if insufficient parameters
        if (args.length < 1)
        {
            return;
        }

        //get sender name
        String userNameLc = args[0].toLowerCase();

        //if user is not actually banned, command was wrong, do nothing.
        if (!MCServer.isBanned(userNameLc))
        {
            return;
        }

        /* Note: From this point, we can assume a correct command! */

        //check if user has an r+ account
        Account account = Account.getAccountMap().get(userNameLc);
        if (account == null)
        {
            //no need to do anything, user is now banned with no r+ account
            return;
        }

        //if user is on r+, kick them
        if (account.isOnline())
        {
            //get type, tempBanLength (if applicable) and reason
            String type = command.substring(1).toLowerCase();
            String tempBanLength;
            String reason;

            //formuate tempBanLength and reason depending on whether it's a tempBan or not
            if (type.equals("tempban"))
            {
                tempBanLength = args[1];

                if (args.length > 2)
                {
                    reason = base.makeSentence(args, 2);
                }
                else
                {
                    reason = "No reason given.";
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
                    reason = "No reason given.";
                }
            }

            account.kick(type, tempBanLength, reason);
        }

        //unregister their account, they can only re-register from server after their ban
        Account.unregister(userNameLc);
    }
}
