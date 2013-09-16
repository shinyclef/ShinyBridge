package com.hotmail.shinyclef.shinybridge;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

/**
 * Author: Shinyclef
 * Date: 28/07/13
 * Time: 4:11 AM
 */

public class NetClientOut implements Runnable
{
    private Socket socket;
    private BlockingQueue<String> outQueue;

    public NetClientOut(NetClientConnection clientConn)
    {
        this.socket = clientConn.getSocket();
        this.outQueue = clientConn.getOutQueue();
    }

    @Override
    public void run()
    {
        try
        {
            //PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
            PrintWriter outToClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            String msgOut = "";

            //read outPut queue
            while (!msgOut.startsWith(NetProtocol.QUIT_MESSAGE))
            {
                msgOut = outQueue.take();
                if(msgOut.startsWith(NetProtocol.POISON_PILL_OUT))
                {
                    break;
                }

                outToClient.println(msgOut);
                outToClient.flush();

                if (ShinyBridge.DEV_BUILD)
                {
                    MCServer.bukkitLog(Level.INFO, msgOut);
                }
            }

            //disconnected message sent, close connection
            outToClient.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("NetClientOut closing.");
        }
    }
}
