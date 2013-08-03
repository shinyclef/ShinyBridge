package com.hotmail.shinyclef.shinybridge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Author: Shinyclef
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
            for (NetClientConnection client : NetClientConnection.getClientMap().values())
            {
                try
                {
                    client.getOutQueue().put("Testing command 'bridge'");
                    commandSender.sendMessage("Command 'bridge' executed. Clients: " + client.getOutQueue().size());
                    return true;
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    return true;
                }
            }
        }

        return false;
    }
}
