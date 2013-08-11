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
    private static ShinyBridge plugin = ShinyBridge.getPlugin();
    private static Logger log = plugin.getLogger();
    private static Server server = plugin.getServer();
    protected static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();

    /* Processes raw input as received from clients, identifying work type and delegating to appropriate method. */
    public static synchronized void processInput(String input, int clientID)
    {
        if (clientMap == null)
        {
            log.info("Map is null.");
            return;
        }

        //check if it's a command or a chat message
        if (!input.startsWith("*"))
        {
            processClientChat(input, clientID);
        }
        else
        {
            processCommand(input, clientID);
        }

        String output = "";

        try
        {
            clientMap.get(clientID).getOutQueue().put(output);
        }
        catch (InterruptedException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /* Processes chat data as sent from clients, broadcasting to server and all clients. */
    private static void processClientChat(String input, int clientID)
    {
        String userName = clientID + "";
        String fullChatTag = "<" + clientID + "> ";

        //broadcast chat in game
        server.broadcastMessage(fullChatTag + input);

    }

    /* Processes command data as send from clients. */
    private static void processCommand(String input, int clientID)
    {
        if (input.length() < 2)
        {
            //invalid command code goes here

            return;
        }

        //remove the '*' from the string
        String command = input.substring(1);

        //do stuff with commands

    }


    // -------------------- Server-Originated -------------------- //

    /* Processes server chat, broadcasting to all chat clients */
    public static synchronized void processServerChat(String msg, Player player)
    {
        //get the full chat tag of the player
        String chatTag = MCServer.getChatTagMap().get(player.getName());

        //add the message
        String chatLine = chatTag + msg;

        //broadcast the message to all clients
        try
        {
            for (NetClientConnection client : NetClientConnection.getClientMap().values())
            {
                client.getOutQueue().put(chatLine);
            }
        }
        catch (InterruptedException e)
        {
            //dunno what to do with this
            plugin.getServer().broadcastMessage("Interrupted Exception");
        }
    }
}
