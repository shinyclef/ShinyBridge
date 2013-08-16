package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Shinyclef
 * Date: 13/07/13
 * Time: 4:23 AM
 */

public class Account
{
    private static Map<String, Account> accountMap = new HashMap<String, Account>();

    private final String userName;
    private String passwordHash;
    private Rank rank;
    private String chatTag;
    private CommandSender commandSender;

    private Date lastLogin;
    private Integer assignedClientID;

    public Account(String userName, String passwordHash, Rank rank)
    {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.rank = rank;
        lastLogin = null;
        assignedClientID = null;
        assignNewClientCommandSender();
    }

    public enum Rank
    {
        GM(5), MOD(4), EXPERT(3), VIP(2), STANDARD(1);

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

        //broadcast login
        NetProtocolHelper.broadcastChat(ChatColor.WHITE + username + ChatColor.YELLOW + " joined RolyDPlus!", true);

        //assign connection ID
        account.assignedClientID = clientID;
    }

    public void logout()
    {
        assignedClientID = null;
    }

    public boolean hasPermission(Rank requiredRank)
    {
        return rank.getLevel() >= requiredRank.getLevel();
    }

    public void assignNewClientCommandSender()
    {
        this.commandSender = new MCServer.ClientCommandSender(this);
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

    public String getUserName()
    {
        return userName;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public Rank getRank()
    {
        return rank;
    }

    public CommandSender getCommandSender()
    {
        return commandSender;
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