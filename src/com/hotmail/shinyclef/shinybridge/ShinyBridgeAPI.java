package com.hotmail.shinyclef.shinybridge;

/**
 * Author: Shinyclef
 * Date: 15/08/13
 * Time: 4:34 AM
 */

public class ShinyBridgeAPI
{
    public void broadcastMessage(String chatLine, String permission, boolean serverBroadcast)
    {
        NetProtocolHelper.broadcastChat(chatLine, permission, serverBroadcast);
    }

    public void sendToClient(String output)
    {

    }
}
