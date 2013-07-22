package com.hotmail.shinyclef.shinybridge;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * User: Shinyclef
 * Date: 12/07/13
 * Time: 1:53 AM
 */

public class NetConnDelegator implements Runnable
{
    private ServerSocket serverSocket;
    private boolean isListening = true;

    public NetConnDelegator(ServerSocket serverSocket)
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
                NetClientChannel s = new NetClientChannel(serverSocket.accept());
                new Thread(s).start();
            }
        }
        catch (IOException ex)
        {

        }
    }
}
