package com.hotmail.shinyclef.shinybridge;

import com.hotmail.shinyclef.shinybase.PermissionListener;
import com.hotmail.shinyclef.shinybase.PermissionsChangeManager;
import com.hotmail.shinyclef.shinybridge.cmdadaptations.Invisible;
import com.hotmail.shinyclef.shinybridge.cmdadaptations.PreProcessParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 11:23 PM
 */

public class EventListener implements Listener, PermissionListener
{
    private static EventListener instance;
    private ShinyBridge plugin;
    private Set<String> commandList;

    public EventListener(ShinyBridge plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        instance = this;
        populateCommandList();

        //register for permission change events
        PermissionsChangeManager.registerListener(this);
    }

    /* The list of commands the commandPreProcess listener should not ignore. */
    private void populateCommandList()
    {
        commandList = new HashSet<String>();
        commandList.add("/rolydplus");
        commandList.add("/r+");
        commandList.add("/rplus");
        commandList.add("/ban");
        commandList.add("/tempban");
        commandList.add("/kick");
    }

    public static void registerCommand(String command)
    {
        if (instance.commandList == null)
        {
            instance.commandList = new HashSet<>();
        }
        instance.commandList.add(command);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e)
    {
        //construct player chat tag and add to map
        MCServer.addToPlayerChatTagMap(e.getPlayer());

        //reset rank if player has an account
        String playerName = e.getPlayer().getName();
        Account account = Account.getAccountMap().get(playerName.toLowerCase());
        if (account != null)
        {
            account.refreshPermissionSettings();
        }

        //inform ScoreBoard manager
        ScoreboardManager.processServerPlayerJoin(e.getPlayer());

        //inform clients
        NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
        if (!Invisible.isInvisibleServer(playerName))
        {
            if (e.getPlayer().hasPlayedBefore())
            {
                NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Server", "Join");
            }
            else
            {
                NetProtocolHelper.broadcastFirstTimeServerJoinMessageToClients(playerName);
            }
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e)
    {
        final Player player = e.getPlayer();

        //remove player from chatTagMap
        MCServer.removeFromPlayerChatTagMap(player);

        //inform ScoreBoard manager and clients with minimum delay to ensure quit has finalised
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            @Override
            public void run()
            {
                ScoreboardManager.processServerPlayerQuit(player);

                //inform clients
                String playerName = player.getName();
                NetProtocolHelper.informClientsOnPlayerStatusChange(playerName);
                if (!Invisible.isInvisibleServer(playerName))
                {
                    NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(playerName, "Server", "Quit");
                }
            }
        }, 0);
    }

    @EventHandler
    public void eventPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
    {
        final String message = e.getMessage().trim();

        //setup args string and command
        String command;
        String argsString;
        if (message.contains(" "))
        {
            command = message.substring(0, message.indexOf(" "));
            argsString = message.substring(message.indexOf(" ") + 1);
        }
        else
        {
            command = message;
            argsString = "";
        }

        //filter command and aliases
        boolean relevantCommandFound = false;
        for (String com : commandList)
        {
            if (command.toLowerCase().equals(com))
            {
                relevantCommandFound = true;
            }
        }

        if (!relevantCommandFound)
        {
            return;
        }

        //setup sender command and sender
        CommandSender sender = e.getPlayer();

        //convert the args string to args array
        String [] args;
        if (!argsString.equals(""))
        {
            args = argsString.split(" ");
        }
        else
        {
            args = new String[0];
        }

        //send all our data to be parsed
        PreProcessParser.playerCommandParser(e, sender, command, args);
    }

    @EventHandler
    public void eventServerCommand(ServerCommandEvent e)
    {
        final String message = "/" + e.getCommand().trim();

        //setup args string and command
        String command;
        String argsString;
        if (message.contains(" "))
        {
            command = message.substring(0, message.indexOf(" "));
            argsString = message.substring(message.indexOf(" ") + 1);
        }
        else
        {
            command = message;
            argsString = "";
        }

        //filter command and aliases
        boolean relevantCommandFound = false;
        for (String com : commandList)
        {
            if (command.toLowerCase().equals(com))
            {
                relevantCommandFound = true;
            }
        }

        if (!relevantCommandFound)
        {
            return;
        }

        //setup sender command and sender
        CommandSender sender = MCServer.getS().getConsoleSender();

        //convert the args string to args array
        String [] args;
        if (!argsString.equals(""))
        {
            args = argsString.split(" ");
        }
        else
        {
            args = new String[0];
        }

        //send all our data to be parsed
        PreProcessParser.consoleCommandParser(e, sender, command, args);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void playerChat(AsyncPlayerChatEvent e)
    {
        if (e.isCancelled())
        {
            return;
        }

        new PlayerChatSync(e.getMessage(), e.getPlayer()).runTask(plugin);
    }

    private class PlayerChatSync extends BukkitRunnable
    {
        String message;
        Player player;

        private PlayerChatSync(String message, Player player)
        {
            this.message = message;
            this.player = player;
        }

        @Override
        public void run()
        {
            NetProtocol.processServerChat(message, player);
        }
    }

    @Override
    public void permissionChangeEvent()
    {
        MCServer.refreshAllPermissions();
    }
}
