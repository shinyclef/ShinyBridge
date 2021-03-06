package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.logging.Level;
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
    public static final String PING = "P";

    public static final String SERVER_VERSION = CUSTOM_COMMAND_MARKER + "Ver";
    public static final String SERVER_VERSION_PLAIN = "Ver";
    public static final String QUIT_MESSAGE = CUSTOM_COMMAND_MARKER + "Disconnect";
    public static final String QUIT_MESSAGE_PLAIN = "Disconnect";
    public static final String LOGOUT_MESSAGE = CUSTOM_COMMAND_MARKER + "Logout";
    public static final String LOGOUT_MESSAGE_PLAIN = "Logout";
    public static final String QUIT_MESSAGE_UNEXPECTED = QUIT_MESSAGE + ":Unexpected";
    public static final String POISON_PILL_OUT = CUSTOM_COMMAND_MARKER + "PoisonPill";
    public static final String COMMAND_UNAVAILABLE_FROM_RPLUS = ChatColor.RED +
            "That command is not available from RolyDPlus.";


    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Server s = p.getServer();
    private static Logger log = p.getLogger();
    protected static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();


    /* ----- Sending to Client ----- */

    /* Sends output to the specified client. */
    public static synchronized void sendToClient(int clientID, String output, boolean isChat)
    {
        if (isChat)
        {
            output = CHAT_MARKER + output;
        }

        try
        {
            if (ShinyBridge.DEV_BUILD && NetClientConnection.getClientMap().get(clientID) == null)
            {
                MCServer.pluginLog("Caution: Sending to a null NetClientConnection in sendToClient.");
            }

            NetClientConnection.getClientMap().get(clientID).getOutQueue().put(output);
        }
        catch (InterruptedException e)
        {
            //nothing to do?
        }
        catch (NullPointerException e)
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog(e.getMessage());
            }
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
        switch (input.substring(0, 1))
        {
            case (PING):
                processPing(clientID);
                break;

            case CHAT_MARKER:
                processClientChat(input, clientID);
                break;

            case CUSTOM_COMMAND_MARKER:
                processCustomCommand(input, clientID);
                break;

            case MC_COMMAND_MARKER:
                processMCCommand(input, clientID);
                break;

            default:
                MCServer.bukkitLog(Level.WARNING, "Unexpected input in NetProtocol.ProcessInput");
                break;
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

        String fullChatTag = account.getClientPlayer().getChatTag();
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

        switch (args[0])
        {
            case "Login":
                NetProtocolHelper.loginRequest(clientID, args);
                break;

            case QUIT_MESSAGE_PLAIN:
                NetClientConnection.getClientMap().get(clientID).disconnectClient("Closing");
                break;

            case LOGOUT_MESSAGE_PLAIN:
                NetProtocolHelper.clientAccountLogout(clientID, "Logout");
                break;

            case "RequestPlayerList":
                NetProtocolHelper.processPlayerListRequest(clientID);
                break;

            case SERVER_VERSION_PLAIN:
                NetProtocolHelper.sendServerVersion(clientID);
                break;

            default:
                MCServer.bukkitLog(Level.WARNING, "Unknown data format received from client.");
                break;
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

        //get clientPlayer
        MCServer.ClientPlayer clientPlayer = NetClientConnection.getClientMap().get(clientID)
                .getAccount().getClientPlayer();

        //inform console
        MCServer.bukkitLog(Level.INFO, clientPlayer.getName() + " issued RolyDPlus command: /" + commandLine);

        if (MCServer.getCommandWhiteList().contains(baseCommand))
        {
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
            sendToClient(clientID, COMMAND_UNAVAILABLE_FROM_RPLUS, true);
        }
    }

    /* Echoes back pings. */
    private static void processPing(int clientID)
    {
        sendToClient(clientID, PING, false);
    }


    /* ----- Server-Originated ----- */

    /* Processes server chat, broadcasting to all chat clients */
    public static synchronized void processServerChat(String msg, Player player)
    {
        //get the full chat tag of the player
        String chatTag;
        if (player instanceof MCServer.ClientPlayer)
        {
            chatTag = ((MCServer.ClientPlayer) player).getChatTag();
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
