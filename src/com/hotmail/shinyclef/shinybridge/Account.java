package com.hotmail.shinyclef.shinybridge;

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

    public void setPasswordHash(String newPasswordHash)
    {
        passwordHash = newPasswordHash;
    }

    public void setRank(Rank rank)
    {
        this.rank = rank;
    }

    /* ---------- Getters ---------- */

    public static Map<String, Account> getAccountMap()
    {
        return accountMap;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public Rank getRank()
    {
        return rank;
    }
}