package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Author: Shinyclef
 * Date: 13/07/13
 * Time: 4:23 AM
 */

public class Account
{
    private static Map<String, Account> accountMap = new HashMap<String, Account>();
    private static List<String> accountListLCase = new ArrayList<String>();
    private static Map<String, Integer> onlineAccountsMapLCase = new HashMap<String, Integer>();

    private final String userName;
    private String passwordHash;
    private Rank rank;
    private String chatTag;
    private CommandSender commandSender;
    private MCServer.ClientPlayer clientPlayer;

    private boolean isOnline;
    private Date lastLogin;
    private Integer assignedClientID;

    public Account(String userName, String passwordHash, Rank rank)
    {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.rank = rank;
        isOnline = false;
        lastLogin = null;
        assignedClientID = null;
        accountListLCase.add(userName.toLowerCase());
        assignNewClientCommandSender();
        assignNewClientPlayer();
    }

    public enum Rank
    {
        GM(5),
        MOD(4),
        EXPERT(3),
        VIP(2),
        STANDARD(1);

        private final int level;

        Rank(int level)
        {
            this.level = level;
        }

        public int getLevel()
        {
            return level;
        }
    }

    public static boolean validateLogin(int clientID, String username, String password)
    {
        //check if user is registered
        if(!accountMap.containsKey(username))
        {
            return false;
        }

        //validate user's password
        String correctHash = accountMap.get(username).getPasswordHash();
        boolean isValidLogin = AccountPassword.validatePassword(password, correctHash);

        if (isValidLogin)
        {
            //set account to the connection and return true
            login(clientID, username);
            return true;

        }
        else
        {
            //invalid login
            return false;
        }
    }

    private static void login(int clientID, String username)
    {
        //get account
        Account account = accountMap.get(username);

        //set chat tag
        String rankTag = MCServer.getColouredRankString(account.rank);
        account.setChatTag(ChatColor.WHITE + "<" + rankTag + ChatColor.WHITE + username + "> ");

        //attach account to the connection
        NetClientConnection.getClientMap().get(clientID).setAccount(account);

        //broadcast login on server
        MCServer.getPlugin().getServer().broadcastMessage(ChatColor.WHITE + username +
                ChatColor.YELLOW + " joined RolyDPlus!");

        //inform clients
        NetProtocolHelper.broadcastClientJoin(username);

        //set logged in values
        account.assignedClientID = clientID;
        account.isOnline = true;
        onlineAccountsMapLCase.put(account.userName.toLowerCase(), clientID);
    }

    public void logout()
    {
        assignedClientID = null;
        isOnline = false;
        if (onlineAccountsMapLCase.containsKey(userName.toLowerCase()))
        {
            onlineAccountsMapLCase.remove(userName.toLowerCase());
        }
    }

    public static void unregister(String userName)
    {
        //remove from accountList
        if (accountListLCase.contains(userName.toLowerCase()))
        {
            accountListLCase.remove(userName.toLowerCase());
        }

        //remove from map (exists in map check completed in cmd executor)
        getAccountMap().remove(userName);

        //remove from database
        new Database.DeleteAccount(userName).runTaskAsynchronously(ShinyBridge.getPlugin());
    }

    public boolean hasPermission(Rank requiredRank)
    {
        return rank.getLevel() >= requiredRank.getLevel();
    }

    public void assignNewClientCommandSender()
    {
        this.commandSender = new MCServer.ClientCommandSender(this);
    }

    public void assignNewClientPlayer()
    {
        this.clientPlayer = new MCServer.ClientPlayer(this);
    }

    public static Set<String> getLoggedInClientUsernamesSet()
    {
        Set<String> set = new HashSet<String>();
        for (int clientID : onlineAccountsMapLCase.values())
        {
            set.add(NetClientConnection.getClientMap().get(clientID).getAccount().getUserName());
        }

        return set;
    }


    /* Setters */

    public void setPasswordHash(String newPasswordHash)
    {
        passwordHash = newPasswordHash;
    }

    public void setRank(Rank rank)
    {
        this.rank = rank;
    }

    public void setChatTag(String chatTag)
    {
        this.chatTag = chatTag;
    }

    /* Getters */

    public static Map<String, Account> getAccountMap()
    {
        return accountMap;
    }

    public static List<String> getAccountListLCase()
    {
        return accountListLCase;
    }

    public static Map<String, Integer> getOnlineAccountsMapLCase()
    {
        return onlineAccountsMapLCase;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public boolean isOnline()
    {
        return isOnline;
    }

    public Rank getRank()
    {
        return rank;
    }

    public CommandSender getCommandSender()
    {
        return commandSender;
    }

    public MCServer.ClientPlayer getClientPlayer()
    {
        return clientPlayer;
    }

    public String getChatTag()
    {
        return chatTag;
    }

    public Integer getAssignedClientID()
    {
        return assignedClientID;
    }
}