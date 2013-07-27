package com.hotmail.shinyclef.shinybridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

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
            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
            String msgOut = "";

            //read outPut queue
            while (!msgOut.startsWith("*QUIT"));
            {
                msgOut = outQueue.take();
                outToClient.println(msgOut);
            }

            //user disconnected close connection
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
    }
}
