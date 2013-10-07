package com.hotmail.shinyclef.shinybridge.cmdadaptations;

import com.hotmail.shinyclef.shinybridge.EventListener;
import com.hotmail.shinyclef.shinybridge.NetProtocolHelper;
import org.bukkit.command.CommandSender;

/**
 * User: Shinyclef
 * Date: 7/10/13
 * Time: 2:54 PM
 */

public class Me extends AdaptedCommand
{
    public static void initialise()
    {
        EventListener.registerCommand("/me");
    }

    public static void processMe(CommandSender sender, String[] args)
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

        //send it to all clients
        NetProtocolHelper.broadcastChat(line, false);
    }
}
