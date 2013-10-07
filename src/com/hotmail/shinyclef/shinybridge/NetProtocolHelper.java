package com.hotmail.shinyclef.shinybridge;

import com.hotmail.shinyclef.shinybridge.cmdadaptations.Invisible;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import ru.tehkode.permissions.bukkit.PermissionsEx;

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

    public static final String CORRECT = "Correct:Standard";
    public static final String CORRECT_MOD = "Correct:Mod";
    public static final String NO_USER = "Incorrect:NoUser";
    public static final String BAD_PASSWORD = "Incorrect:UserPass";
    public static final String OUT_OF_DATE = "Incorrect:OutOfDate";
    public static final String DUPLICATE = "Duplicate";

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
            if (PermissionsEx.getUser(client.getAccount().getUserName()).has(permission))
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

    public static void sendToClientPlayerIfOnline(String playerName, String message)
    {
        if (MCServer.isClientOnline(playerName))
        {
            NetProtocol.sendToClient(Account.getOnlineLcUsersClientMap().get(playerName.toLowerCase()), message, true);
        }
    }

    public static void loginRequest(int clientID, String[] args)
    {
        String version = args[1];
        String username = args[2];
        String password = args[3];
        String loginResponse;

        if (!NetClientConnection.clientIsUpToDate(version))
        {
            loginResponse = OUT_OF_DATE;
        }
        else
        {
            loginResponse = Account.validateLogin(clientID, username, password);
        }

        sendToClient(clientID, "@Login:" + loginResponse, false);
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
            account.logout(!Invisible.isInvisibleClient(account.getUserName()));
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

    /* @location: Server/Client
    *  @type:     Join/Quit/Invisible/Visible */
    public static void informClientsOnPlayerStatusChange(String playerName)
    {
        broadcastRawToClients("@StatusChange:" + playerName + ":" +
                NetProtocolHelper.getOnlineLocationsCode(playerName) +
                NetProtocolHelper.getInvisibleLocationsCode(playerName), false);
    }

    public static void broadcastOnlineChangeMessageToClientsIfVisible(String playerName,
                                                                      String serverOrClient, String joinOrQuit)
    {
        String location = "";
        if (serverOrClient.equalsIgnoreCase("server"))
        {
            location = "the game";
        }
        else if (serverOrClient.equalsIgnoreCase("client"))
        {
            location = "RolyDPlus";
        }

        String direction = "";
        if (joinOrQuit.equalsIgnoreCase("join"))
        {
            direction = "joined";
        }
        else if (joinOrQuit.equalsIgnoreCase("quit"))
        {
            direction = "left";
        }


        broadcastChat(ChatColor.WHITE + playerName + ChatColor.YELLOW + " " + direction + " " + location + "!", false);
    }

    public static String getOnlineLocationsCode(String playerName)
    {
        String currentPresence = MCServer.getCurrentPresence(playerName);
        switch (currentPresence)
        {
            case "Server":
                return "S";

            case "Client":
                return "C";

            case "Both":
                return "B";

            case "None":
                return "N";

            default:
                MCServer.pluginLog("Warning! Default case triggered in NetProtocolHelper.getOnlineLocationsCode.");
                return "N";
        }
    }

    public static String getInvisibleLocationsCode(String playerName)
    {
        boolean serverInvisible = Invisible.isInvisibleServer(playerName);
        boolean clientInvisible = Invisible.isInvisibleClient(playerName);

        if (serverInvisible && clientInvisible)
        {
            return "b";
        }
        else if (serverInvisible)
        {
            return "s";
        }
        else if (clientInvisible)
        {
            return "c";
        }
        else
        {
            return "n";
        }
    }
}
