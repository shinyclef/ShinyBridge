package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Server;

import java.util.Set;
import java.util.logging.Level;
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

        String result = Account.validateLogin(clientID, username, password);
        String loginResponse = "@Login:" + result;
        sendToClient(clientID, loginResponse, false);
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
        sendToClient(clientID, "@PlayerList:" +  masterList, false);
    }

    public static void clientAccountLogout(int clientID, String[] args)
    {
        String type = args[1];
        clientAccountLogout(clientID, type);
    }

    public static void clientAccountLogout(int clientID, String type)
    {
        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("NetProtocolHelper.clientAccountLogout type: " + type);
        }

        //return if this client is not in map for some reason, perhaps already removed
        if (!NetClientConnection.getClientMap().containsKey(clientID))
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("CAUTION: NetClientConnection is null in NetProtocolHelper.clientAccountLogout,");
            }
            return;
        }

        //check if client is logged in and get account
        Account account = NetClientConnection.getClientMap().get(clientID).getAccount();
        boolean wasLoggedIn = false;

        if (account != null)
        {
            wasLoggedIn = true;
            account.logout(true);
        }

        //broadcast to console if it's a client that wasn't logged in
        if (!wasLoggedIn)
        {
            MCServer.pluginLog("Disconnected: " + NetClientConnection.getClientMap().get(clientID).getIpAddress());
        }
    }

    public static void clientForcedQuit(int clientID, String type, String tempBanLength, String reason)
    {
        String typeInfo;
        switch (type.toLowerCase())
        {
            case "kick":
                typeInfo = "Kick";
                break;

            case "ban":
                typeInfo = "Ban";
                break;

            case "tempban":
                typeInfo = "TempBan:" + tempBanLength;
                break;

            case "duplicatelogin":
                typeInfo = "DuplicateLogin";
                break;

            default:
                MCServer.bukkitLog(Level.WARNING,
                        "Unexpected message in NetProtocolHelper.clientForcedQuit: type == " + type);
                return;
        }

        //send disconnect message to client
        sendToClient(clientID, QUIT_MESSAGE + ":" + typeInfo + ":" + reason, false);
    }

    public static void broadcastServerJoin(String playerName)
    {
        //check if they're logged in client
        String currentPresence = "Server";
        if (Account.getOnlineLcUsersClientMap().keySet().contains(playerName.toLowerCase()))
        {
            currentPresence = "Both";
        }

        broadcastRawToClients("@" + "ServerJoin:" + playerName + ":" + currentPresence, false);

    }

    public static void broadcastServerQuit(String playerName)
    {
        //check if they're logged in client
        String currentPresence = "None";
        if (Account.getOnlineLcUsersClientMap().keySet().contains(playerName.toLowerCase()))
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
