package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 11:05 PM
 */

public class NetProtocol
{
    private static final String CHAT_MARKER = "*";
    private static final String CUSTOM_COMMAND_MARKER = "@";
    private static final String MC_COMMAND_MARKER = "/";
    public static final String QUIT_MESSAGE = CUSTOM_COMMAND_MARKER + "Disconnect";
    public static final String POISON_PILL_OUT = CUSTOM_COMMAND_MARKER + "PoisonPill";

    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Server s = p.getServer();
    private static Logger log = p.getLogger();
    protected static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();


    /* ----- Sending to Client ----- */

    /* Se9nds output to the specified client. */
    protected static synchronized void sendToClient(int clientID, String output, boolean isChat)
    {
        if (isChat)
        {
            output = CHAT_MARKER + output;
        }

        try
        {
            NetClientConnection.getClientMap().get(clientID).getOutQueue().put(output);
        }
        catch (InterruptedException e)
        {
            //nothing to do?
        }
    }


    /* ----- Client-Originated ----- */

    /* Processes raw input as received from clients, identifying work type and delegating to appropriate method. */
    public static synchronized void processInput(String input, int clientID)
    {
        if (clientMap == null)
        {
            log.info("Map is null.");
            return;
        }

        //parse message type
        if (input.startsWith(CUSTOM_COMMAND_MARKER))
        {
            processCustomCommand(input, clientID);
        }
        else if (input.startsWith(MC_COMMAND_MARKER))
        {
            processMCCommand(input, clientID);
        }
        else if (input.startsWith(CHAT_MARKER))
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
        account.getClientPlayer().chat(message);
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

        else if (args[0].equals(QUIT_MESSAGE.substring(1)))
        {
            NetProtocolHelper.clientQuit(clientID, args);
        }

        else if (args[0].equals("RequestPlayerList"))
        {
            NetProtocolHelper.processPlayerListRequest(clientID);
        }

    }

    /* Processes command data as send from clients. */
    private static void processMCCommand(String input, int clientID)
    {
        if (input.length() < 2)
        {
            return;
        }

        //remove first character and get args
        String commandLine = input.substring(1);
        String[] args = commandLine.split(" ");
        String baseCommand = args[0].toLowerCase();

        if (MCServer.getCommandWhiteList().contains(baseCommand))
        {
            MCServer.ClientPlayer clientPlayer = NetClientConnection.getClientMap().get(clientID)
                    .getAccount().getClientPlayer();

            PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(clientPlayer, "/" + commandLine);
            s.getPluginManager().callEvent(e);

            if (e.isCancelled())
            {
                return;
            }

            s.dispatchCommand(clientPlayer, commandLine);
        }
        else
        {
            sendToClient(clientID, ChatColor.RED + "That command is not available from RolyDPlus.", true);
        }
    }


    /* ----- Server-Originated ----- */

    /* Processes server chat, broadcasting to all chat clients */
    public static synchronized void processServerChat(String msg, Player player)
    {
        //get the full chat tag of the player
        String chatTag;
        if (player instanceof MCServer.ClientPlayer)
        {
            chatTag = player.getDisplayName();
        }
        else
        {
            chatTag = MCServer.getPlayerChatTagMap().get(player.getName());
        }

        //add the message
        String chatLine = chatTag + msg;

        //add chat marker and broadcast the message to all clients
        NetProtocolHelper.broadcastChat(chatLine, false);
    }
}
