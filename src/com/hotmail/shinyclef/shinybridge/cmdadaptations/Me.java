package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.MCServer;
import com.hotmail.shinyclef.shinybridge.NetProtocolHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * User: Shinyclef
 * Date: 7/10/13
 * Time: 2:54 PM
 */

public class Me extends AdaptedCommand
{
    public static void initialize()
    {
        registerCommand("/me");
    }

    public static void processMe(PlayerCommandPreprocessEvent e, CommandSender sender, String[] args)
    {
        /* we must forward on a successful /me message to all clients */

        //first make sure it's a valid /me by verifying a message is there
        if (args.length == 0)
        {
            return;
        }

        //ok, make the sentence and the message line
        String sentence = base.makeSentence(args, 0);
        String line = "* " + sender.getName() + " " + sentence;

        //if this command is coming from a client, we need to handle it ourselves to prevent errors
        if (sender instanceof MCServer.ClientPlayer)
        {
            e.setCancelled(true);
            NetProtocolHelper.broadcastChat(line, true);
        }
        else
        {
            //It's from the server, send it to all clients
            NetProtocolHelper.broadcastChat(line, false);
        }
    }
}
