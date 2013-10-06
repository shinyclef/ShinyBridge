package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybase.ShinyBaseAPI;
import com.hotmail.shinyclef.shinybridge.CmdExecutor;
import com.hotmail.shinyclef.shinybridge.EventListener;
import com.hotmail.shinyclef.shinybridge.NetProtocolHelper;
import com.hotmail.shinyclef.shinybridge.ShinyBridge;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
        EventListener.registerCommand("/say");
    }

    public static void processSay(CommandSender sender, String[] args)
    {
        //do nothing if sender is not a mod
        if (!sender.hasPermission(CmdExecutor.MOD_PERM))
        {
            return;
        }

        //get the message and form the broadcast line
        String message = base.makeSentence(args, 0);
        String broadcastLine = SAY_COLOUR + "[Server] " + sender.getName() + ": " + message;

        //broadcast it to clients
        NetProtocolHelper.broadcastChat(broadcastLine, false);
    }
}
