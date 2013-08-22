package com.hotmail.shinyclef.shinybridge;

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

    public void broadcastMessage(String chatLine, String permission, boolean serverBroadcast)
    {
        NetProtocolHelper.broadcastChat(chatLine, permission, serverBroadcast);
    }

    public void sendToClient(String playerName, String message)
    {
        if (Account.getOnlineAccountsMapLCase().containsKey(playerName.toLowerCase()))
        {
            NetProtocol.sendToClient(Account.getOnlineAccountsMapLCase().get(playerName.toLowerCase()), message, true);
        }
    }

    public boolean isOnlineServerPlusClients(String playerName)
    {
        return server.getOfflinePlayer(playerName).isOnline() ||
                Account.getAccountListLCase().contains(playerName.toLowerCase());

    }

    public Set<Player> getOnlinePlayersEverywhereSet()
    {
        return MCServer.getOnlinePlayersEverywhereSet();
    }
}
