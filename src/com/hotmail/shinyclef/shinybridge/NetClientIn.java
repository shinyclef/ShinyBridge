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

    public NetClientIn(NetClientConnection clientConn)
    {
        this.socket = clientConn.getSocket();
        this.clientID = clientConn.getClientID();
    }

    @Override
    public void run()
    {
        BufferedReader inFromClient = null;
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
                    break;
                }
                NetProtocol.processInput(msgIn, clientID);
            }
            while (!msgIn.startsWith(NetProtocol.QUIT_MESSAGE));
        }
        catch (SocketException e)
        {
            MCServer.pluginLog("Unexpectedly lost connection: " + address);
        }
        catch (IOException e)
        {
            MCServer.pluginLog("IO Exception (not a big deal).");
        }
        finally
        {
            try
            {
                //user disconnected close connection
                if (inFromClient != null)
                {
                    inFromClient.close();
                }
                socket.close();
            }
            catch (IOException e)
            {
                //swallow
            }
        }

    }
}
