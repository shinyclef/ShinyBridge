package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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

            switch (subCommand)
            {
                case "help":
                    return showHelp(sender, args);

                case "unregister":
                    return unregister(sender, args);

                case "debug":
                    return debug(sender);

                case "reloadcommandwhitelist":
                    return reloadCommandWhiteList(sender);
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

    private boolean lockdown(CommandSender sender, String[] args)
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

        return true;
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

        Database.printDebug(sender);
        return true;
    }
}
