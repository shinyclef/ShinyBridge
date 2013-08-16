package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.*;
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

    private static List<String> commandWhiteList;

    public static void initialize(ShinyBridge thePlugin)
    {
        p = thePlugin;
        s = p.getServer();
        bukkitLog = Bukkit.getLogger();
        pluginLog = p.getLogger();
        playerChatTagMap = new HashMap<String, String>();
        commandWhiteList = new ArrayList<String>();
        reloadCommandWhiteList();
    }

    public static void reloadCommandWhiteList()
    {
        //generate a temp list from config, then convert to lowercase for final list
        commandWhiteList.clear();
        List<String> temp = p.getConfig().getStringList("CommandWhiteList");
        for (String s : temp)
        {
            commandWhiteList.add(s.toLowerCase());
        }
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

    public static String playerList()
    {
        String onlineList = s.getOnlinePlayers().toString();
        return onlineList;
    }

    public static class ClientCommandSender implements CommandSender
    {
        private Account account;

        public ClientCommandSender(Account account)
        {
            this.account = account;
        }

        @Override
        public void sendMessage(String s)
        {
            NetProtocol.sendToClient(account.getAssignedClientID(), "*" + ChatColor.WHITE + s);
        }

        @Override
        public void sendMessage(String[] strings)
        {
        }

        @Override
        public Server getServer()
        {
            return p.getServer();
        }

        @Override
        public String getName()
        {
            return account.getUserName();
        }

        @Override
        public boolean isPermissionSet(String s)
        {
            return false;
        }

        @Override
        public boolean isPermissionSet(Permission permission)
        {
            return false;
        }

        @Override
        public boolean hasPermission(String s)
        {
            return account.hasPermission(MCServer.getRank(s));
        }

        @Override
        public boolean hasPermission(Permission permission)
        {
            return false;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i)
        {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i)
        {
            return null;
        }

        @Override
        public void removeAttachment(PermissionAttachment permissionAttachment)
        {
        }

        @Override
        public void recalculatePermissions()
        {
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions()
        {
            return null;
        }

        @Override
        public boolean isOp()
        {
            return false;
        }

        @Override
        public void setOp(boolean b)
        {
        }
    }

    // ---------- getters ---------- //

    public static Map<String, String> getPlayerChatTagMap()
    {
        return playerChatTagMap;
    }

    public static List<String> getCommandWhiteList()
    {
        return commandWhiteList;
    }
}
