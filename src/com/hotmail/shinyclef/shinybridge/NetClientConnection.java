package com.hotmail.shinyclef.shinybridge;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 8:09 PM
 */

public class NetClientConnection
{
    //static class vars
    private static int latestConnectionID = 0;
    private static Map<Integer, NetClientConnection> clientMap = new HashMap<Integer, NetClientConnection>();

    //object vars
    private final Socket socket;
    private final int clientID;
    private BlockingQueue<String> outQueue;
    private final NetClientIn clientIn;
    private final NetClientOut clientOut;

    /* Constructor */
    public NetClientConnection(Socket socket)
    {
        //initialise variables
        this.socket = socket;
        latestConnectionID++;
        clientID = latestConnectionID;
        outQueue = new ArrayBlockingQueue<String>(50);
        clientIn = new NetClientIn(this);
        clientOut = new NetClientOut(this);

        //store client in map
        clientMap.put(clientID, this);
    }

    /* This must happen after the constructor finishes so the object can finish
    constructing before the threads have access to it. Thread safety! */
    public void startThreads()
    {
        new Thread(clientIn).start();
        new Thread(clientOut).start();
        ShinyBridge.log("Started in/out threads.");
    }

    /* Getters */
    public Socket getSocket()
    {
        return socket;
    }

    public static Map<Integer, NetClientConnection> getClientMap()
    {
        return clientMap;
    }

    public int getClientID()
    {
        return clientID;
    }

    public BlockingQueue<String> getOutQueue()
    {
        return outQueue;
    }
}
