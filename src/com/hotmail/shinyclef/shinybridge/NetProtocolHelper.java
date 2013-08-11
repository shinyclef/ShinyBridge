package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Server;

import java.util.logging.Logger;

/**
 * User: Shinyclef
 * Date: 4/08/13
 * Time: 8:04 PM
 */

public class NetProtocolHelper
{
    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Server s = p.getServer();
    private static Logger log = p.getLogger();

    public static void broadcastChat(String chatLine, boolean serverBroadcast)
    {
        //send to each client
        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            try
            {
                //send out to all clients
                client.getOutQueue().put(chatLine);
            }
            catch (InterruptedException e)
            {
                log.info("Error processing chat message: " + e.getMessage());
            }
        }

        //broadcast on server if requested
        if(serverBroadcast)
        {
            s.broadcastMessage(chatLine);
        }
    }
}
