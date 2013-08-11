package com.hotmail.shinyclef.shinybridge;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 11:23 PM
 */

public class EventListener implements Listener
{
    private ShinyBridge plugin;

    public EventListener(ShinyBridge plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent e)
    {
        //construct player chat tag and add to map
        MCServer.addToChatTagMap(e.getPlayer());

        //reset rank if player has an account
        String playerName = e.getPlayer().getName();
        Account account = Account.getAccountMap().get(playerName);
        if (account != null)
        {
            account.setRank(MCServer.getPlayerRank(e.getPlayer()));
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e)
    {
        //remove player from chatTagMap
        MCServer.removeFromChatTagMap(e.getPlayer());
    }

    @EventHandler
    public void eventCommandPreprocess(PlayerCommandPreprocessEvent e)
    {
        //cancel early for any commands that don't start with /rolydplus register
        final String message = e.getMessage().trim();
        if (!message.toLowerCase().startsWith("/rolydplus register") &&
                !message.toLowerCase().startsWith("/rolydplus changepassword"))
        {
            return;
        }

        //setup sender and string for args
        CommandSender sender = e.getPlayer();
        String argsString;
        if (message.contains(" "))
        {
            argsString = message.substring(message.indexOf(" ") + 1);
        }
        else
        {
            argsString = "";
        }

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
        CmdExecutor.preProcessParser(sender, args);

        //cancel original command
        e.setCancelled(true);
    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent e)
    {
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



}
