package com.hotmail.shinyclef.shinybridge;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 11:05 PM
 */

public class NetProtocol
{
    private static Map<Integer, NetClientConnection> clientMap = NetClientConnection.getClientMap();

    public static synchronized void processInput(String input, int clientID)
    {
        if (clientMap == null)
        {
            ShinyBridge.log("Map is null.");
        }

        String output;
        ShinyBridge.log("Log: " + input);

        output = "Echo: " + input;
        try
        {
            clientMap.get(clientID).getOutQueue().put(output);
        }
        catch (InterruptedException e)
        {
            ShinyBridge.log(e.getMessage());
        }
    }
}
