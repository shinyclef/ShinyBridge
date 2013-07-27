package com.hotmail.shinyclef.shinybridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Author: Shinyclef
 * Date: 28/07/13
 * Time: 4:11 AM
 */

public class NetClientIn implements Runnable
{
    private Socket socket;
    private int clientID;

    public NetClientIn(NetClientConnection clientConn)
    {
        this.socket = clientConn.getSocket();
        this.clientID = clientConn.getClientID();
    }

    @Override
    public void run()
    {
        try
        {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msgIn;

            //wait for input and pass it to handler
            do
            {
                msgIn = inFromClient.readLine();
                NetProtocol.processInput(msgIn, clientID);
            }
            while (!msgIn.startsWith("*QUIT"));

            //user disconnected close connection
            inFromClient.close();
            socket.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
