package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Bukkit;
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
    private static Logger bukkitLog;
    private static Logger pluginLog;
    private static Map<String, String> playerChatTagMap;

    public static void initialize(ShinyBridge thePlugin)
    {
        p = thePlugin;
        s = p.getServer();
        bukkitLog = Bukkit.getLogger();
        pluginLog = p.getLogger();
        playerChatTagMap = new HashMap<String, String>();
    }

    public static synchronized void pluginLog(String msg)
    {
        bukkitLog.info("R+: " + msg);
    }

    public static synchronized void bukkitLog(String msg)
    {
        pluginLog.info("R+: " + msg);
    }

    public static void addToPlayerChatTagMap(Player player)
    {
        String playerName = player.getName();
        Account.Rank rank = getRank(player);
        String rankTag = getColouredRankString(rank);

        //put it all together
        String fullChatTag = ChatColor.WHITE + "<" + rankTag + ChatColor.WHITE + playerName + "> ";

        //add it to the map
        playerChatTagMap.put(playerName, fullChatTag);
    }

    public static void removeFromPlayerChatTagMap(Player player)
    {
        if (!playerChatTagMap.containsKey(player.getName()))
        {
            return;
        }

        playerChatTagMap.remove(player.getName());
    }

    public static Account.Rank getRank(Player player)
    {
        //get highest rank
        Account.Rank rank;
        if (player.hasPermission("rolyd.admin"))
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

    public static Account.Rank getRank(String permission)
    {
        String perm = permission.toLowerCase();
        if (perm.equals("rolyd.admin"))
        {
            return Account.Rank.GM;
        }
        else if (perm.equals("rolyd.mod"))
        {
            return Account.Rank.MOD;
        }
        else if (perm.equals("rolyd.exp"))
        {
            return Account.Rank.EXPERT;
        }
        else if (perm.equals("rolyd.vip"))
        {
            return Account.Rank.VIP;
        }
        else
        {
            return Account.Rank.STANDARD;
        }
    }

    public static String getColouredRankString(Account.Rank rank)
    {
        String rankTag = "";
        if (rank.equals(Account.Rank.GM))
        {
            rankTag = ChatColor.RED + "[GM] ";
        }
        else if (rank.equals(Account.Rank.MOD))
        {
            rankTag = ChatColor.GREEN + "[Mod] ";
        }
        else if (rank.equals(Account.Rank.EXPERT))
        {
            rankTag = ChatColor.AQUA + "[Exp] ";
        }
        else if (rank.equals(Account.Rank.VIP))
        {
            rankTag = ChatColor.DARK_PURPLE + "[VIP] ";
        }
        return rankTag;
    }

    // ---------- getters ---------- //

    public static Map<String, String> getPlayerChatTagMap()
    {
        return playerChatTagMap;
    }


}
