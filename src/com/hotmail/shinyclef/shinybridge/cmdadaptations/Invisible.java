package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import be.Balor.Player.ACPlayer;
import be.Balor.Tools.Type;
import com.hotmail.shinyclef.shinybridge.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Shinyclef
 * Date: 25/09/13
 * Time: 6:02 AM
 */

public class Invisible extends AdaptedCommand
{

    private static final String MOD_PERM = "rolyd.mod";
    private static List<String> invisibleClientUsersLc;

    public static void initialise(ShinyBridge plugin)
    {
        invisibleClientUsersLc = new ArrayList<>();
        invisibleClientUsersLc = config.getStringList("InvisibleClientUsers");
        EventListener.registerCommand("/invisible");
        EventListener.registerCommand("/inv");
    }

    public static boolean isInvisibleClient(String username)
    {
        return invisibleClientUsersLc.contains(username.toLowerCase());
    }

    public static boolean isInvisibleServer(String playerName)
    {
        ACPlayer acPlayer = ACPlayer.getPlayer(playerName);
        return acPlayer.hasPower(Type.INVISIBLE);
    }

    public static void invPostProcess(final CommandSender sender, final String[] args)
    {
        Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //perm check
                if (!sender.hasPermission(MOD_PERM))
                {
                    return; //feedback handled by adminCmd
                }

                String targetPlayer;

                //if there is an args[0], that's the player we check, otherwise, it's the sender
                if (args.length > 0)
                {
                    targetPlayer = args[0];
                    targetPlayer = MCServer.getTargetNameFromShortcut(targetPlayer);
                }
                else
                {
                    targetPlayer = sender.getName();
                }

                //if target player is not online, return
                if (targetPlayer == null || MCServer.getCurrentPresence(targetPlayer).equals("None"))
                {
                    return; //feedback handled by adminCmd
                }

                //player list invisibility has been toggled for someone
                serverPlayerHasToggledInvisibility(targetPlayer, isInvisibleServer(targetPlayer));
            }
        }, 0);
    }

    /* Refer to note for below method regarding comparison of boolean logic. */
    public static void serverPlayerHasToggledInvisibility(String playerName, boolean isInvisible)
    {
        //if on server, add/remove invisibility
        if (MCServer.isServerOnline(playerName))
        {
            if (isInvisible) //make this person invisible
            {
                NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
                NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Server", "Quit");
                ScoreboardManager.processServerPlayerInvisible(playerName);
            }
            else //make this person visible
            {
                NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
                NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Server", "Join");
                ScoreboardManager.processServerPlayerVisible(playerName);
            }
        }
    }

    /* Not that this method's logic is opposite to the server invisibility method above. This method is
    * kind happens before invisibility status has changed, while above method happens after. */
    public static void clientPlayerHasToggledInvisibility(CommandSender sender)
    {
        String playerName = sender.getName();
        boolean isAlreadyInvisible = isInvisibleClient(playerName);

        if (!isAlreadyInvisible) //make this person invisible
        {
            //update invisible client list
            invisibleClientUsersLc.add(playerName.toLowerCase());
            config.set("InvisibleClientUsers", invisibleClientUsersLc);
            p.saveConfig();

            if (MCServer.isClientOnline(playerName))
            {
                //send these new values to clients and scoreboard
                NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
                NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Client", "Quit");
                ScoreboardManager.processClientPlayerInvisible(playerName);

                //broadcast logout
                Account.announceClientLogoutToServer(playerName);
            }
            else
            {
                sender.sendMessage(ChatColor.GOLD + "You will be invisible the next time you sign into RolyDPlus.");
            }
        }
        else //make this person visible
        {
            //update invisible client list
            invisibleClientUsersLc.remove(playerName.toLowerCase());
            config.set("InvisibleClientUsers", invisibleClientUsersLc);
            p.saveConfig();

            if (MCServer.isClientOnline(playerName))
            {
                //send these new values to clients and scoreboard
                NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
                NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Client", "Join");
                ScoreboardManager.processClientPlayerVisible(playerName);

                //broadcast login
                Account.announceClientLoginToServer(playerName);
            }
            else
            {
                sender.sendMessage(ChatColor.GOLD +
                        "You will be not be invisible the next time you sign into RolyDPlus.");
            }
        }
    }
}
