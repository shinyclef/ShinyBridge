package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Server;

import java.util.HashMap;
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
    private static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();

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
            processChatMessage(input, clientID);
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

    private static void processChatMessage(String input, int clientID)
    {
        String userName = clientID + "";
        String fullChatTag = "<" + clientID + "> ";


        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            try
            {
                //send out to all clients
                client.getOutQueue().put("Sender " + clientID + ": " + input);
            }
            catch (InterruptedException e)
            {
                log.info("Error processing chat message: " + e.getMessage());
            }
        }

        //broadcast chat in game
        server.broadcastMessage(fullChatTag + input);

    }

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


}
