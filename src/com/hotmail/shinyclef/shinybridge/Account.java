package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;

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

    private Date lastLogin;
    private Integer connectionID;

    public Account(String userName, String passwordHash, Rank rank)
    {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.rank = rank;
        lastLogin = null;
        connectionID = null;
    }

    public enum Rank
    {
        STANDARD, VIP, EXPERT, MOD, GM
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
        account.setChatTag(ChatColor.WHITE + "<" + rankTag + ChatColor.WHITE + " " + username + "> ");

        //attach account to the connection
        NetClientConnection.getClientMap().get(clientID).setAccount(account);

        //broadcast login
        NetProtocolHelper.broadcastChat(username + ChatColor.YELLOW + " joined RolyDPlus!", true);
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

    public String getChatTag()
    {
        return chatTag;
    }
}