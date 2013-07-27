package com.hotmail.shinyclef.shinybridge;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 1:53 AM
 * Description: Receives connection requests from clients and creates ClientConnection objects using the sockets.
 */

public class NetConnectionDelegator implements Runnable
{
    private ServerSocket serverSocket;
    private boolean isListening = true;

    public NetConnectionDelegator(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run()
    {
        listen();
    }

    private void listen()
    {
        try
        {
            while (isListening)
            {
                NetClientConnection s = new NetClientConnection(serverSocket.accept());
                s.startThreads();
                ShinyBridge.log("Received connection.");
            }
        }
        catch (IOException ex)
        {

        }
    }
}
