package com.hotmail.shinyclef.shinybridge;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

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
    private static final int TIMEOUT_SECONDS = 30;

    //object vars
    private final Socket socket;
    private Thread netClientInThread;
    private Thread netClientOutThread;
    private final NetClientIn netClientIn;
    private final NetClientOut netClientOut;

    private final int clientID;
    private String ipAddress;
    private BlockingQueue<String> outQueue;

    private Account account;
    private boolean readyToCloseSockets;

    /* Constructor */
    public NetClientConnection(Socket socket)
    {
        //initialise variables
        this.socket = socket;
        latestConnectionID++;
        clientID = latestConnectionID;
        ipAddress = socket.getRemoteSocketAddress().toString();
        outQueue = new ArrayBlockingQueue<String>(50);
        netClientIn = new NetClientIn(this);
        netClientOut = new NetClientOut(this);
        account = null;
        readyToCloseSockets = false;

        //set socket timeout
        try
        {
            socket.setSoTimeout(TIMEOUT_SECONDS * 1000);
        }
        catch (SocketException e)
        {
            MCServer.bukkitLog(Level.SEVERE, "Unable to set socket timeout for client.");
        }

        //store client in map
        clientMap.put(clientID, this);

        //announce
        MCServer.pluginLog("Connection established: " + socket.getRemoteSocketAddress());
    }

    /* This must happen after the constructor finishes so the object can finish
    constructing before the threads have access to it. Thread safety! */
    public void startThreads()
    {
        netClientInThread = new Thread(netClientIn);
        netClientOutThread = new Thread(netClientOut);
        netClientInThread.start();
        netClientOutThread.start();
    }

    //this stops all client interaction threads,
    public void disconnectClient(String type)
    {
        //shut down in/out and remove client from map
        //NetProtocolHelper.sendToClient(clientID, NetProtocol.POISON_PILL_OUT, false);
        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("NetClientConnection.disconnectClient has started. " + clientID);
        }

        netClientIn.disconnectTriggered();

        netClientOutThread.interrupt();
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            //already closed
        }

        NetProtocolHelper.clientAccountLogout(clientID, type);
        NetClientConnection.getClientMap().remove(clientID);

        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("NetClientConnection.disconnectClient has finished. " + clientID);
        }
    }

    public void timeOutNotification()
    {
        //get user ID (usually name)
        String ID;
        if (account == null)
        {
            ID = ipAddress;
        }
        else
        {
            ID = account.getUserName();
        }

        //broadcast timeout
        MCServer.pluginLog(ID + " timed out.");
        if (account != null)
        {
            account.logout(true);
        }
        else
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("Caution: NetClientConnection.account == null. " + clientID);
            }
        }
    }


    /* Setters */

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public synchronized void setReadyToCloseSockets(boolean readyToCloseSockets)
    {
        this.readyToCloseSockets = readyToCloseSockets;
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

    public String getIpAddress()
    {
        return ipAddress;
    }

    public Account getAccount()
    {
        return account;
    }

    public BlockingQueue<String> getOutQueue()
    {
        return outQueue;
    }

    public synchronized boolean isReadyToCloseSockets()
    {
        return readyToCloseSockets;
    }


}
