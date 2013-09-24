package com.hotmail.shinyclef.shinybridge;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.logging.Level;

/**
 * Author: Shinyclef
 * Date: 12/07/13
 * Time: 1:10 AM
 */

public class CmdExecutor implements CommandExecutor
{
    public static final String NO_PERM = ChatColor.RED + "You do not have permission to do that.";
    private static ShinyBridge plugin;

    public CmdExecutor()
    {
        plugin = ShinyBridge.getPlugin();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (command.getName().equalsIgnoreCase("rolydplus"))
        {
            if (args.length < 1)
            {
                return false;
            }

            String subCommand = args[0].toLowerCase();

            /* Commands "register" and "changepassword" are in rPlusParser below. */
            switch (subCommand)
            {
                case "help":
                    return showHelp(sender, args);

                case "unregister":
                    return unregister(sender, args);

                case "inv":
                    return invisible(sender, args);

                case "debug":
                    return debug(sender);

                case "scoreboard":
                    return scoreboard(sender, args);

                case "stop":
                    return stop(sender, args);

                case "start":
                    return start(sender, args);

                case "reloadcommandwhitelist":
                    return reloadCommandWhiteList(sender);

                case "test":
                    //ScoreboardManager.addToScoreboard(args[1]);
                    return true;
            }

        }
        return false;
    }

    public static void rPlusParser(PlayerCommandPreprocessEvent e,
                                    CommandSender sender, String[] args)
    {
        switch (args[0])
        {
            case "register":
                register(e, sender, args);
                break;

            case "changepassword":
                changePassword(e, sender, args);
                break;
        }
    }

    /* Command logic */

    public static void register(PlayerCommandPreprocessEvent e,
                                CommandSender sender, String[] args)
    {
        //cancel original command
        e.setCancelled(true);

        //needs 2 args
        if (args.length != 2)
        {
            sender.sendMessage("/rolydplus help");
            return;
        }

        String username = sender.getName();

        //check if user already has an account
        if (Account.getAccountMap().containsKey(username.toLowerCase()))
        {
            sender.sendMessage(ChatColor.RED + "You already have a RolyDPlus account.");
            return;
        }

        String password = args[1];
        int lengthDifference = AccountPassword.isCorrectLength(password);

        //check if new pass is in correct range
        if (lengthDifference != 0)
        {
            String characterWord = "characters";
            String lengthWord = "long";
            if (lengthDifference < 0)
            {
                lengthWord = "short";
                lengthDifference = lengthDifference * -1; //remove sign
            }

            if (lengthDifference == 1)
            {
                characterWord = "character";
            }

            sender.sendMessage(ChatColor.RED + "Your chosen password is " + lengthDifference + " " +
                    characterWord + " too " + lengthWord + ".");
            return;
        }

        //password up to 25 chars
        if (password.length() > 25)
        {
            sender.sendMessage(ChatColor.RED + "Sorry, the maximum length for passwords is 25 characters.");
            return;
        }

        //create the new account
        Account.register(username, password);

        //user feedback
        sender.sendMessage(ChatColor.YELLOW + "RolyDPlus account successfully created.");

        //replacement log message
        Bukkit.getLogger().info(sender.getName() + " issued server command: /rolydplus register ********");
    }

    private boolean unregister(CommandSender sender, String[] args)
    {
        //args length of 1
        if (args.length != 1)
        {
            return false;
        }

        //check if user has an account
        if (!Account.getAccountMap().containsKey(sender.getName().toLowerCase()))
        {
            sender.sendMessage(ChatColor.RED + "You do not have an account.");
            return true;
        }

        //removal process
        Account.unregister(sender.getName());

        //user feedback
        sender.sendMessage(ChatColor.YELLOW + "RolyDPlus account successfully removed. You may re-register at any time.");

        return true;
    }

    public static void changePassword(PlayerCommandPreprocessEvent e,
                                      CommandSender sender, String[] args)
    {
        /* Format is /rolydplus changepassword (new password) */

        //args length of 2
        if (args.length != 2)
        {
            sender.sendMessage("/rolydplus help");
            return;
        }

        String senderName = sender.getName();

        //check if they have an account
        if (!Account.getAccountMap().containsKey(senderName))
        {
            sender.sendMessage(ChatColor.RED + "You do not have an account.");
            return;
        }

        Account account = Account.getAccountMap().get(senderName);
        String newPass = args[1];
        int lengthDifference = AccountPassword.isCorrectLength(newPass);

        //check if new pass is in correct range
        if (lengthDifference != 0)
        {
            String characterWord = "characters";
            String lengthWord = "long";
            if (lengthDifference < 0)
            {
                lengthWord = "short";
                lengthDifference = lengthDifference * -1; //remove sign
            }

            if (lengthDifference == 1)
            {
                characterWord = "character";
            }

            sender.sendMessage(ChatColor.RED + "Your chosen password is " + lengthDifference + " " +
                    characterWord + " too " + lengthWord + ".");
            return;
        }

        //it's fine, update with new password in memory and in database
        String newHash = AccountPassword.generateHash(newPass);
        account.setPasswordHash(newHash);
        new Database.UpdatePasswordHash(senderName, newHash).runTaskAsynchronously(plugin);

        //user feedback
        sender.sendMessage(ChatColor.YELLOW + "Your password has been successfully updated.");

        //replacement log message
        Bukkit.getLogger().info(sender.getName() + " issued server command: /rolydplus changepassword ********");

        //cancel original event
        e.setCancelled(true);

    }

    private boolean showHelp(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.AQUA + "Commands are accessible with /RolyDPlus, /rplus, or /r+. Eg. '/r+ help'.");
        sender.sendMessage(ChatColor.AQUA + "register [password]" + ChatColor.YELLOW +
                " - Registers your username for use with RolyDPlus with the given password.");
        sender.sendMessage(ChatColor.AQUA + "unregister" + ChatColor.YELLOW +
                " - Removes you RolyDPlus account. You can re-register at any time.");
        sender.sendMessage(ChatColor.AQUA + "changepassword [password]" + ChatColor.YELLOW +
                " - Changes your RolyDPlus password to the given password.");

        if (sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(ChatColor.DARK_RED + "Staff Commands:");
            sender.sendMessage(ChatColor.AQUA + "debug" + ChatColor.YELLOW +
                    " - Used to check for any errors with database operations. " +
                    "Check this if you think something is not working.");
            sender.sendMessage(ChatColor.AQUA + "reloadcommandwhitelist" + ChatColor.YELLOW +
                    " - Used after the command white list has been altered in the config file.");
        }
        return true;
    }

    private boolean invisible(CommandSender sender, String[] args)
    {

        return true;
    }

    private boolean scoreboard(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(NO_PERM);
        }

        if (args.length < 2)
        {
            return false;
        }

        switch (args[1].toLowerCase())
        {
            case "enable":
                ScoreboardManager.enableScoreboardFeature();
                sender.sendMessage(ChatColor.YELLOW + "RolyDPlus usernames will no longer appear in scoreboard.");
                break;

            case "disable":
                ScoreboardManager.disableScoreboardFeature();
                sender.sendMessage(ChatColor.YELLOW + "RolyDPlus usernames will now appear in scoreboard");
                break;
        }

        /* Commands with three args */
        if (args.length < 3)
        {
            return false;
        }

        switch (args[1].toLowerCase())
        {
            case "add":
                ScoreboardManager.addToScoreboard(args[2]);
                break;

            case "remove":
                ScoreboardManager.removeFromScoreboard(args[2].toLowerCase());
                break;

            default:
                return false;
        }

        return true;
    }

    private boolean stop(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(NO_PERM);
            return true;
        }

        if (args.length != 1)
        {
            return false;
        }

        //check if service is already stopped
        if (!ShinyBridge.isAcceptingConnections())
        {
            sender.sendMessage(ChatColor.RED + "RolyDPlus is already stopped.");
            return true;
        }

        //stop accepting clients
        plugin.stopAcceptingClients();

        //go through each client and disconnect
        for (NetClientConnection client : NetClientConnection.getClientMap().values())
        {
            client.disconnectClient("lockdown");
        }

        //user feedback
        sender.sendMessage(ChatColor.YELLOW +
                "The RolyDPlus service has stopped and is no longer accepting client connections. Type " +
                ChatColor.GOLD + "/r+ start" + ChatColor.YELLOW + " to restart.");

        //console feedback
        MCServer.bukkitLog(Level.INFO, "ATTENTION: RolyDPlus has been stopped by " + sender.getName() +
                ". Clients can no longer connect to the r+ service.");

        return true;
    }

    private boolean start(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(NO_PERM);
            return true;
        }

        if (args.length != 1)
        {
            return false;
        }

        //check if service is already running
        if (ShinyBridge.isAcceptingConnections())
        {
            sender.sendMessage(ChatColor.RED + "RolyDPlus is already accepting connections.");
            return true;
        }

        plugin.startAcceptingClients();

        //user feedback
        sender.sendMessage(ChatColor.YELLOW +
                "The RolyDPlus service has started and is now accepting client connections.");

        //console feedback
        MCServer.bukkitLog(Level.INFO, "ATTENTION: RolyDPlus has been started by " + sender.getName() +
                ". Clients can once again connect to the r+ service.");

        return true;
    }

    private boolean reloadCommandWhiteList(CommandSender sender)
    {
        if (!sender.hasPermission("roly.mod"))
        {
            sender.sendMessage(NO_PERM);
            return true;
        }
        MCServer.reloadCommandWhiteList();
        sender.sendMessage(ChatColor.YELLOW + "Command white list reloaded.");
        return true;
    }

    private boolean debug(CommandSender sender)
    {
        if (!sender.hasPermission("rolyd.mod"))
        {
            sender.sendMessage(NO_PERM);
            return true;
        }

        sender.sendMessage(ChatColor.BLUE + "General Debug Info:");
        sender.sendMessage(ChatColor.AQUA + "ClientConnections: " +
                ChatColor.YELLOW + NetClientConnection.getClientMap().size());
        sender.sendMessage(ChatColor.AQUA + "Online Accounts: " +
                ChatColor.YELLOW + Account.getOnlineLcUsersAccountMap().size());
        ScoreboardManager.scoreboardDebug(sender);
        Database.printDebug(sender);
        return true;
    }
}
