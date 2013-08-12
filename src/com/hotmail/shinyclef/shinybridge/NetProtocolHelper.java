package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Server;

import java.util.logging.Logger;

/**
 * User: Shinyclef
 * Date: 4/08/13
 * Time: 8:04 PM
 */

public class NetProtocolHelper extends NetProtocol
{
    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Server s = p.getServer();
    private static Logger log = p.getLogger();

    public static void broadcastChat(String chatLine, boolean serverBroadcast)
    {
        //send to each client
        String clientChat = "*" + chatLine;
        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            try
            {
                //send out to all clients
                client.getOutQueue().put(clientChat);
            }
            catch (InterruptedException e)
            {
                log.info("Error processing chat message: " + e.getMessage());
            }
        }

        //broadcast on server if requested (no '*' for server)
        if(serverBroadcast)
        {
            s.broadcastMessage(chatLine);
        }
    }

    public static void loginRequest(int clientID, String[] args)
    {
        String username = args[1];
        String password = args[2];

        boolean isValidLogin = Account.validateLogin(clientID, username, password);
        String loginReply = "@Login:" + isValidLogin;
        sendToClient(clientID, loginReply);
    }
}
