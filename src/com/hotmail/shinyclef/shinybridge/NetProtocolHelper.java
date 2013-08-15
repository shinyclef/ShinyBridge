package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.util.logging.Logger;

/**
 * User: Shinyclef
 * Date: 4/08/13
 * Time: 8:04 PM
 */

public class NetProtocolHelper extends NetProtocol
{
    private static final String COLOUR_CHAR = String.valueOf('\u00A7');
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

    public static void broadcastChat(String chatLine, String permission, boolean serverBroadcast)
    {
        //convert permission to a rank and required level
        Account.Rank requiredRank = MCServer.getRank(permission);

        //for each client...
        String clientChat = "*" + chatLine;
        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            //skip them if they are not logged in
            if (client.getAccount() == null)
            {
                continue;
            }

            //only send if they have the required rank
            if (client.getAccount().hasPermission(requiredRank))
            {
                try
                {
                    //send out to client
                    client.getOutQueue().put(clientChat);
                }
                catch (InterruptedException e)
                {
                    log.info("Error processing chat message: " + e.getMessage());
                }
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

    public static void clientQuit(int clientID, String[] args)
    {
        //check if client is logged in and get account
        Account account = NetClientConnection.getClientMap().get(clientID).getAccount();
        boolean wasLoggedIn;
        String quitMessage;

        if (account != null)
        {
            wasLoggedIn = true;
            String userName = account.getUserName();
            quitMessage = ChatColor.WHITE + userName + ChatColor.YELLOW + " left RolyDPlus!";
        }
        else
        {
            wasLoggedIn = false;
            quitMessage = "Disconnected: " + NetClientConnection.getClientMap().get(clientID).getIpAddress();
        }

        //finish disconnecting client
        NetClientConnection.getClientMap().get(clientID).disconnectClient();

        //broadcast the message to the appropriate place
        if (wasLoggedIn)
        {
            broadcastChat(quitMessage, true);
        }
        else
        {
            MCServer.pluginLog(quitMessage);
        }
    }

    public static void clientForceQuit(int clientID, String[] args)
    {
        //send disconnect message to client
        NetProtocol.sendToClient(clientID, NetProtocol.QUIT_MESSAGE + ":Forced");
    }
}
