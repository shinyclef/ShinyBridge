package com.hotmail.shinyclef.shinybridge;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    private final String userNameLC;
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
        this.userNameLC = userName.toLowerCase();
        this.passwordHash = passwordHash;
        this.rank = rank;
        isOnline = false;
        lastLogin = null;
        assignedClientID = null;
        accountListLCase.add(userName.toLowerCase());
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
        String lcUsername = username.toLowerCase();

        //check if user is registered
        if(!accountListLCase.contains(lcUsername))
        {
            return false;
        }

        //validate user's password
        String correctHash = accountMap.get(lcUsername).getPasswordHash();
        boolean isValidLogin = AccountPassword.validatePassword(password, correctHash);

        if (isValidLogin)
        {
            //set account to the connection and return true
            login(clientID, lcUsername);
            return true;

        }
        else
        {
            //invalid login
            return false;
        }
    }

    private static void login(int clientID, String lcUsername)
    {
        //get account
        Account account = accountMap.get(lcUsername);
        String username = account.getUserName();

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
        onlineAccountsMapLCase.put(lcUsername, clientID);
    }

    public void logout()
    {
        assignedClientID = null;
        isOnline = false;
        if (onlineAccountsMapLCase.containsKey(userNameLC.toLowerCase()))
        {
            onlineAccountsMapLCase.remove(userNameLC.toLowerCase());
        }
    }

    /* Creates a new r+ account. Command executor has taken care of validation. */
    public static void register(String username, String password)
    {
        //overwrite password with a hash
        password = AccountPassword.generateHash(password);

        //get rank
        Account.Rank rank = MCServer.getRank(MCServer.getPlugin().getServer().getPlayer(username));

        //create a new Account
        Account account = new Account(username, password, rank);
        Account.getAccountMap().put(username.toLowerCase(), account);

        //insert the new account data into the database
        new Database.InsertAccount(username, password, rank.toString()).runTaskAsynchronously(MCServer.getPlugin());
    }

    public static void unregister(String userName)
    {
        //remove from accountList
        if (accountListLCase.contains(userName.toLowerCase()))
        {
            accountListLCase.remove(userName.toLowerCase());
        }

        //remove from map (exists in map check completed in cmd executor)
        getAccountMap().remove(userName.toLowerCase());

        //remove from database
        new Database.DeleteAccount(userName).runTaskAsynchronously(ShinyBridge.getPlugin());
    }

    public boolean hasPermission(Rank requiredRank)
    {
        return rank.getLevel() >= requiredRank.getLevel();
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