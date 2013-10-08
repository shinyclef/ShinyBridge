package com.hotmail.shinyclef.shinybridge;

import com.hotmail.shinyclef.shinybridge.cmdadaptations.Invisible;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Author: Shinyclef
 * Date: 15/08/13
 * Time: 4:34 AM
 */

public class ShinyBridgeAPI
{
    private static Server server = ShinyBridge.getPlugin().getServer();

    public void broadcastMessage(String chatLine, boolean serverBroadcast)
    {
        NetProtocolHelper.broadcastChat(chatLine, serverBroadcast);
    }

    public void broadcastMessage(String chatLine, String permission, boolean serverBroadcast)
    {
        NetProtocolHelper.broadcastChat(chatLine, permission, serverBroadcast);
    }

    public void sendToClient(String playerName, String message)
    {
        NetProtocolHelper.sendToClientPlayerIfOnline(playerName, message);
    }

    public boolean isOnlineServerPlusClients(String playerName)
    {
        return server.getOfflinePlayer(playerName).isOnline() ||
                Account.getOnlineLcUsersAccountMap().containsKey(playerName.toLowerCase());

    }

    public boolean isVisiblyOnlineServerPlusClients(String playerName)
    {
        //true if they are online and not invisible on server, or, online and not invisible on client
        return (MCServer.isServerOnline(playerName) && !Invisible.isInvisibleServer(playerName)) ||
                (MCServer.isClientOnline(playerName) && !Invisible.isInvisibleClient(playerName));
    }

    public String onlineAnywherePlayerName(String playerNameLc)
    {
        if (server.getOfflinePlayer(playerNameLc).isOnline())
        {
            return server.getPlayer(playerNameLc).getName();
        }

        if (Account.getOnlineLcUsersAccountMap().containsKey(playerNameLc))
        {
            return Account.getOnlineLcUsersAccountMap().get(playerNameLc).getUserName();
        }

        return null;
    }

    public Set<Player> getOnlinePlayersEverywhereSet()
    {
        return MCServer.getOnlinePlayersEverywhereSet();
    }
}
