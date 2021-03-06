package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * User: Shinyclef
 * Date: 2/10/13
 * Time: 5:44 PM
 */

public class Say extends AdaptedCommand
{
    private static final String SAY_COLOUR = "" + ChatColor.LIGHT_PURPLE;

    public static void initialize()
    {
       registerCommand("/say");
    }

    public static void processSay(PlayerCommandPreprocessEvent pc, CommandSender sender, String[] args)
    {
        //do nothing if sender is not a mod
        if (!sender.hasPermission(CmdExecutor.MOD_PERM))
        {
            return;
        }

        //get the message and form the broadcast line
        String message = base.makeSentence(args, 0);
        String broadcastLine = SAY_COLOUR + "[Server] " + sender.getName() + ": " + message;

        if (sender instanceof MCServer.ClientPlayer)
        {
            //cancel command and broadcast everywhere
            pc.setCancelled(true);
            NetProtocolHelper.broadcastChat(broadcastLine, true);
        }
        else
        {
            //broadcast it to clients only
            NetProtocolHelper.broadcastChat(broadcastLine, false);
        }


    }
}
