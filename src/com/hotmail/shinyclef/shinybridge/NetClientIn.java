package com.hotmail.shinyclef.shinybridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * Author: Shinyclef
 * Date: 28/07/13
 * Time: 4:11 AM
 */

public class NetClientIn implements Runnable
{
    private Socket socket;
    private int clientID;
    private String address = "0.0.0.0";
    private volatile boolean disconnectAlreadyTriggered = false;

    public NetClientIn(NetClientConnection clientConn)
    {
        this.socket = clientConn.getSocket();
        this.clientID = clientConn.getClientID();
    }

    @Override
    public void run()
    {
        BufferedReader inFromClient;
        try
        {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String msgIn;
            address = socket.getRemoteSocketAddress().toString();

            //wait for input and pass it to handler
            do
            {
                msgIn = inFromClient.readLine();
                if (msgIn == null)
                {
                    if (ShinyBridge.DEV_BUILD)
                    {
                        MCServer.pluginLog("CAUTION! MsgIn == null in NetClientIn");
                    }
                    break;
                }
                if (ShinyBridge.DEV_BUILD)
                {
                    MCServer.pluginLog("In: " + msgIn);
                }
                NetProtocol.processInput(msgIn, clientID);
            }
            while (!msgIn.startsWith(NetProtocol.QUIT_MESSAGE));
            if (msgIn != null && msgIn.startsWith(NetProtocol.QUIT_MESSAGE))
            {
                disconnectTriggered(); //do this now because other thread may be too slow
            }
        }
        catch (SocketException e)
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("NetClientIn closing with disconnectAlreadyTriggered. " + clientID);
            }

            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("NetClientIn (SocketException). " + clientID);
            }
        }
        catch (IOException e)
        {
            if (e.getMessage().equals("Read timed out"))
            {
                NetClientConnection.getClientMap().get(clientID).timeOutConsoleNotification();

                if (ShinyBridge.DEV_BUILD)
                {
                    MCServer.pluginLog("NetClientIn (IOException Timeout). " + clientID);
                }
            }
            else
            {
                MCServer.pluginLog("IO Exception: " + e.getMessage());
            }
        }

        //after everything else, disconnect client (if it hasn't already happened)
        if (!disconnectAlreadyTriggered)
        {
            NetClientConnection.getClientMap().get(clientID).disconnectClient("Unexpected");
        }

        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("NetClientIn closing (End of class). " + clientID);
        }
    }

    public synchronized void disconnectTriggered()
    {
        disconnectAlreadyTriggered = true;
    }
}
