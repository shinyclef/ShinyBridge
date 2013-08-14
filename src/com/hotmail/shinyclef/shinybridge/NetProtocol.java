package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 11:05 PM
 */

public class NetProtocol
{
    public static final String QUIT_MESSAGE = "@Disconnect";
    public static final String POISON_PILL_OUT = "@PoisonPill";

    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Server s = p.getServer();
    private static Logger log = p.getLogger();
    protected static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();

    /* Sends output to the specified client. */
    protected static synchronized void sendToClient(int clientID, String output)
    {
        try
        {
            NetClientConnection.getClientMap().get(clientID).getOutQueue().put(output);
        }
        catch (InterruptedException e)
        {

        }
    }

    // -------------------- Client-Originated -------------------- //

    /* Processes raw input as received from clients, identifying work type and delegating to appropriate method. */
    public static synchronized void processInput(String input, int clientID)
    {
        if (clientMap == null)
        {
            log.info("Map is null.");
            return;
        }

        //parse message type
        if (input.startsWith("@")) //custom command
        {
            processCustomCommand(input, clientID);
        }
        else if (input.startsWith("/")) //mc command
        {
            processMCCommand(input, clientID);
        }
        else if (input.startsWith("*"))//chat message
        {
            processClientChat(input, clientID);
        }
    }

    /* Processes chat data as sent from clients, broadcasting to server and all clients. */
    private static void processClientChat(String input, int clientID)
    {
        if (input.length() < 2)
        {
            return;
        }

        //get the account
        Account account = NetClientConnection.getClientMap().get(clientID).getAccount();

        //remove first character
        String message = input.substring(1);

        String userName = account.getUserName();
        String fullChatTag = account.getChatTag();
        String chatLine = fullChatTag + message;

        //send chat to server and all clients
        NetProtocolHelper.broadcastChat(chatLine, true);
    }

    /* Processes command data as send from clients. */
    private static void processCustomCommand(String input, int clientID)
    {
        if (input.length() < 2)
        {
            return;
        }

        //remove first character and get args
        String command = input.substring(1);
        String[] args = command.split(":");

        if (args[0].equals("Login"))
        {
            NetProtocolHelper.loginRequest(clientID, args);
        }

        if (args[0].equals(QUIT_MESSAGE.substring(1)))
        {
            NetProtocolHelper.clientQuit(clientID, args);
        }
    }

    /* Processes command data as send from clients. */
    private static void processMCCommand(String input, int clientID)
    {
        if (input.length() < 3)
        {
            return;
        }

        //remove first character and get args
        String command = input.substring(1);
        String[] args = command.split(":");

    }


    // -------------------- Server-Originated -------------------- //

    /* Processes server chat, broadcasting to all chat clients */
    public static synchronized void processServerChat(String msg, Player player)
    {
        //get the full chat tag of the player
        String chatTag = MCServer.getPlayerChatTagMap().get(player.getName());

        //add the message
        String chatLine = chatTag + msg;

        //add chat marker and broadcast the message to all clients
        NetProtocolHelper.broadcastChat(chatLine, false);
    }
}
