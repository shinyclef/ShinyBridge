package com.hotmail.shinyclef.shinybridge;

import java.util.Date;
import java.util.Map;

/**
 * User: Shinyclef
 * Date: 13/07/13
 * Time: 4:23 AM
 */

public class Account
{
    private static Map<String, Account> accountMap;

    private final String userName;
    private int pin;
    private Rank rank;
    private Date lastLogin;

    public Account(String userName, int pin, Rank rank)
    {
        this.userName = userName;
        this.pin = pin;
        this.rank = rank;
    }

    private enum Rank
    {
        STANDARD, VIP, EXPERT, MOD, GM
    }
}