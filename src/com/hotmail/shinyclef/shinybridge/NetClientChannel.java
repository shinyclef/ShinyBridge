package com.hotmail.shinyclef.shinybridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * User: Shinyclef
 * Date: 12/07/13
 * Time: 8:09 PM
 */

public class NetClientChannel implements Runnable
{
    private Socket socket;

    public NetClientChannel(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            String outputLine;

            NetDataHandler data = new NetDataHandler();
            outputLine = "Connection Established";
            ShinyBridge.log(outputLine);

            while ((inputLine = in.readLine()) != null)
            {
                outputLine = data.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye"))
                {
                    break;
                }
            }

            out.close();
            in.close();
            socket.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
