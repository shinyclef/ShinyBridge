package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.util.Set;
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

    public static void broadcastRawToClients(String message, boolean isChat)
    {
        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            if (isChat)
            {
                message = "*" + message;
            }

            try
            {
                //send out to all clients
                client.getOutQueue().put(message);
            }
            catch (InterruptedException e)
            {
                log.info("Error processing client broadcast: " + e.getMessage());
            }
        }
    }

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
            s.broadcast(chatLine, permission);
        }
    }

    public static void loginRequest(int clientID, String[] args)
    {
        String username = args[1];
        String password = args[2];

        boolean isValidLogin = Account.validateLogin(clientID, username, password);
        String loginReply = "@Login:" + isValidLogin;
        sendToClient(clientID, loginReply, false);
    }

    public static void processPlayerListRequest(int clientID)
    {
        //Prefix '+' for logged in both, '-' for client only, and no prefix for server only.
        Set<String> formattedSet = MCServer.getAllOnlinePlayerFormattedNamesSet();

        //build the string
        String masterList = "";
        for (String s : formattedSet)
        {
            masterList = masterList + s + ",";
        }

        //remove the last ','
        masterList = masterList.substring(0, masterList.length() - 1);

        //send it
        NetProtocol.sendToClient(clientID, "@PlayerList:" +  masterList, false);
    }

    public static void clientQuit(int clientID, String[] args)
    {
        //check if client is logged in and get account
        Account account = NetClientConnection.getClientMap().get(clientID).getAccount();
        boolean wasLoggedIn;
        String serverQuitMessage;

        if (account != null)
        {
            wasLoggedIn = true;
            account.logout();
            String userName = account.getUserName();
            serverQuitMessage = ChatColor.WHITE + userName + ChatColor.YELLOW + " left RolyDPlus!";
        }
        else
        {
            wasLoggedIn = false;
            serverQuitMessage = "Disconnected: " + NetClientConnection.getClientMap().get(clientID).getIpAddress();
        }

        //finish disconnecting client
        NetClientConnection.getClientMap().get(clientID).disconnectClient();

        //broadcast the message to the appropriate place
        if (wasLoggedIn)
        {
            MCServer.getPlugin().getServer().broadcastMessage(serverQuitMessage);

            //inform clients
            broadcastClientQuit(account.getUserName());
        }
        else
        {
            MCServer.bukkitLog(serverQuitMessage);
        }
    }

    public static void clientKick(int clientID, String[] args)
    {
        //send disconnect message to client
        NetProtocol.sendToClient(clientID, NetProtocol.QUIT_MESSAGE + ":Forced", false);
    }

    public static void broadcastServerJoin(String playerName)
    {
        //check if they're logged in client
        String currentPresence = "Server";
        if (Account.getOnlineAccountsMapLCase().keySet().contains(playerName.toLowerCase()))
        {
            currentPresence = "Both";
        }

        broadcastRawToClients("@" + "ServerJoin:" + playerName + ":" + currentPresence, false);

    }

    public static void broadcastServerQuit(String playerName)
    {
        //check if they're logged in client
        String currentPresence = "None";
        if (Account.getOnlineAccountsMapLCase().keySet().contains(playerName.toLowerCase()))
        {
            currentPresence = "Client";
        }
        broadcastRawToClients("@" + "ServerQuit:" + playerName + ":" + currentPresence, false);
    }

    public static void broadcastClientJoin(String playerName)
    {
        //check if they are logged in server
        String currentPresence = "Client";
        if (MCServer.isServerOnline(playerName))
        {
            currentPresence = "Both";
        }
        broadcastRawToClients("@" + "ClientJoin:" + playerName + ":" + currentPresence, false);
    }

    public static void broadcastClientQuit(String playerName)
    {
        //check if they are logged in server
        String currentPresence = "None";
        if (MCServer.isServerOnline(playerName))
        {
            currentPresence = "Server";
        }
        broadcastRawToClients("@" + "ClientQuit:" + playerName + ":" + currentPresence, false);
    }
}
