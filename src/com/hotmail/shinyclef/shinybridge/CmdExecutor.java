package com.hotmail.shinyclef.shinybridge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * User: Shinyclef
 * Date: 12/07/13
 * Time: 1:10 AM
 */

public class CmdExecutor implements CommandExecutor
{
    private static ShinyBridge plugin;

    public CmdExecutor()
    {
        plugin = ShinyBridge.getPlugin();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (command.getName().equalsIgnoreCase("bridge"))
        {

        }

        return false;
    }
}
