package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import be.Balor.Player.ACPlayer;
import be.Balor.Tools.Type;
import com.hotmail.shinyclef.shinybridge.MCServer;
import com.hotmail.shinyclef.shinybridge.ShinyBridge;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * User: Shinyclef
 * Date: 25/09/13
 * Time: 6:02 AM
 */

public class Invisible
{
    private static ShinyBridge p;
    private static Server s;
    private static final String MOD_PERM = "rolyd.mod";

    public static void initialise(ShinyBridge plugin)
    {
        p = plugin;
        s = p.getServer();
    }

    public static void invOrFakeQuitPostProcess(final String command, final CommandSender sender, final String[] args)
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
                }
                else
                {
                    targetPlayer = sender.getName();
                }

                //if target player is not online, return
                {
                    if (!s.getOfflinePlayer(targetPlayer).isOnline())
                    {
                        return; //feedback handled by adminCmd
                    }
                }

                //player list invisibility has been toggled for someone
                playerHasToggledInvisibility(targetPlayer, isInvisible(targetPlayer));
            }
        }, 0);
    }

    public static boolean isInvisible(String playerName)
    {
        ACPlayer acPlayer = ACPlayer.getPlayer(playerName);
        return acPlayer.hasPower(Type.INVISIBLE) || acPlayer.hasPower(Type.FAKEQUIT);
    }

    public static void playerHasToggledInvisibility(String playerName, boolean isInvisible)
    {
        MCServer.pluginLog(playerName + " is now shown as: " + (isInvisible ? "offline" : "online"));
    }
}
