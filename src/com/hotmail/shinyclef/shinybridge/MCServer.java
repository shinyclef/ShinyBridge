package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: Shinyclef
 * Date: 4/08/13
 * Time: 12:09 AM
 */

public class MCServer extends ShinyBridge
{
    private static ShinyBridge p;
    private static Server s;
    private static Logger log;
    private static Map<String, String> chatTagMap;

    public static void initialize(ShinyBridge thePlugin)
    {
        p = thePlugin;
        s = p.getServer();
        log = p.getLogger();
        chatTagMap = new HashMap<String, String>();
    }

    public static synchronized void log(String msg)
    {
        log.info(msg);
    }

    public static void addToChatTagMap(Player player)
    {
        String playerName = player.getName();
        String rankTag = "";

        if (player.hasPermission("simpleprefix.admin"))
        {
            rankTag = ChatColor.RED + "[GM]";
        }
        else if (player.hasPermission("simpleprefix.moderator"))
        {
            rankTag = ChatColor.GREEN + "[Mod]";
        }
        else if (player.hasPermission("simpleprefix.Exp"))
        {
            rankTag = ChatColor.AQUA + "[Exp]";
        }
        else if (player.hasPermission("simpleprefix.vip"))
        {
            rankTag = ChatColor.DARK_PURPLE + "[VIP]";
        }

        //put it all together
        String fullChatTag = ChatColor.WHITE + "<" + rankTag + ChatColor.WHITE + playerName + "> ";

        //add it to the map
        chatTagMap.put(playerName, fullChatTag);
    }

    public static void removeFromChatTagMap(Player player)
    {
        if (!chatTagMap.containsKey(player.getName()))
        {
            return;
        }

        chatTagMap.remove(player.getName());
    }

    public static Account.Rank getPlayerRank(Player player)
    {
        //get highest rank
        Account.Rank rank;
        if (player.hasPermission("rolyd.gm"))
        {
            rank = Account.Rank.GM;
        }
        else if (player.hasPermission("rolyd.mod"))
        {
            rank = Account.Rank.MOD;
        }
        else if (player.hasPermission("rolyd.exp"))
        {
            rank = Account.Rank.EXPERT;
        }
        else if (player.hasPermission("rolyd.vip"))
        {
            rank = Account.Rank.VIP;
        }
        else
        {
            rank = Account.Rank.STANDARD;
        }
        return rank;
    }

    // ---------- getters ---------- //

    public static Map<String, String> getChatTagMap()
    {
        return chatTagMap;
    }


}
