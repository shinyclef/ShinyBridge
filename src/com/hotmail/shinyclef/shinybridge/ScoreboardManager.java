package com.hotmail.shinyclef.shinybridge;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.hotmail.shinyclef.shinybridge.cmdadaptations.Invisible;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * User: Shinyclef
 * Date: 19/09/13
 * Time: 2:58 AM
 */

public class ScoreboardManager
{
    private static boolean scoreboardFunctional = true;
    private static boolean scoreboardEnabled = true;
    private static ShinyBridge p;
    private static Server s;
    private static ProtocolManager protocolManager;
    private static Scoreboard scoreboard;
    private static File teamsFile;
    private static Charset encoding = StandardCharsets.UTF_8;

    private static Map<String, PacketContainer> onlineTeamAndPacketMap;
    //(String fullName, PacketContainer onlinePacket)

    public static void initialise(ShinyBridge plugin, ProtocolManager protocolManager,
                                  File teamsFile, boolean scoreboardEnabled)
    {
        p = plugin;
        s = plugin.getServer();
        ScoreboardManager.protocolManager = protocolManager;
        onlineTeamAndPacketMap = new LinkedHashMap<>();
        ScoreboardManager.scoreboardFunctional = scoreboardEnabled;
        ScoreboardManager.teamsFile = teamsFile;
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        removeTeamsOnTeamsFile();
    }

    public static void processServerPlayerJoin(Player player)
    {
        //remove r+ name if on list
        if (onlineTeamAndPacketMap.containsKey(player.getName()))
        {
            removeFromScoreboard(player.getName(), true);
        }

        //send r+ scoreboard online packets to player, delay to make sure they are added to end of list
        sendScoreboardListToNewPlayer(player);
    }

    public static void processServerPlayerQuit(Player player)
    {
        //check if player is logged into r+. If so, add to scoreboard.
        if (Account.getLoggedInClientUsernamesSet().contains(player.getName()))
        {
            addToScoreboard(player.getName(), false);
        }
    }

    public static void processServerPlayerInvisible(String playerName)
    {
        //we are ADDING r+ to scoreboard if that user is online
        if (MCServer.isClientOnline(playerName))
        {
            addToScoreboard(playerName, true);
        }
    }

    public static void processServerPlayerVisible(String playerName)
    {
        //ensure any potential scoreboard presence is removed
        removeFromScoreboard(playerName, true);
    }

    public static void processClientPlayerJoin(String playerName)
    {
        //add the player to scoreboard
        addToScoreboard(playerName, false);
    }

    public static void processClientPlayerQuit(String playerName)
    {
        //remove the player from scoreboard
        removeFromScoreboard(playerName, false);
    }

    public static void processClientPlayerInvisible(String playerName)
    {
        //if player is on server and visible, no change needed
        if (MCServer.isServerOnline(playerName) && !Invisible.isInvisibleServer(playerName))
        {
            return;
        }

        //we are REMOVING r+ from scoreboard if that user is online
        removeFromScoreboard(playerName, true);
    }

    public static void processClientPlayerVisible(String playerName)
    {
        //if player is on server and visible, no change needed
        if (MCServer.isServerOnline(playerName) && !Invisible.isInvisibleServer(playerName))
        {
            return;
        }

        //add to scoreboard as invisible toggle
        addToScoreboard(playerName, true);
    }

    public static void addToScoreboard(String playerName, boolean invisibilityToggle)
    {
        if (!scoreboardFunctional || !scoreboardEnabled)
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("Add to scoreboard triggered while scoreboard not enabled.");
            }
            return;
        }

        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("Add to scoreboard running with name: " + playerName);
        }

        //get name remainder
        String nameRemainder = playerName.substring(1);

        PacketContainer packet = createScoreboardAddPacket(nameRemainder);
        if (!invisibilityToggle) //legit login, so setup the team
        {
            createScoreboardTeam(playerName);
        }

        //add the fake player to the onlineTeamAndPacketMap
        onlineTeamAndPacketMap.put(playerName, packet);

        //add player to team
        Team team = scoreboard.getTeam(nameRemainder);
        team.addPlayer(s.getOfflinePlayer(nameRemainder));

        //send the packet to each online player who should get it
        for (Player player : s.getOnlinePlayers())
        {
            if (playerCanSeeRPlusPresence(player, playerName))
            {
                sendPacketToPlayer(player.getName(), packet);
            }
        }
    }

    public static void removeFromScoreboard(String playerName, boolean invisibilityToggle)
    {
        if (!scoreboardFunctional || !scoreboardEnabled)
        {
            return;
        }

        //check if player is on scoreboard
        String foundPlayer = null;
        for (String name : onlineTeamAndPacketMap.keySet())
        {
            if (playerName.equalsIgnoreCase(name))
            {
                foundPlayer = name;
                break;
            }
        }

        if (foundPlayer == null)
        {
            return;
        }

        //get the nameRemainder that's registered with scoreboard
        String nameRemainder = foundPlayer.substring(1);

        //get the packet
        PacketContainer packet = createScoreboardRemovePacket(nameRemainder);

        //send the packet to all online users who should get it
        for (Player player : s.getOnlinePlayers())
        {
            if (!playerCanSeeRPlusPresence(player, playerName))
            {
                sendPacketToPlayer(player.getName(), packet);
            }
        }

        if (!invisibilityToggle) //legit logout, clear the team
        {
            //remove this player from the onlineTeamAndPacketMap, and scoreboard
            onlineTeamAndPacketMap.remove(foundPlayer);
            removeScoreboardTeam(foundPlayer);
        }
    }

    private static void createScoreboardTeam(String playerName)
    {
        //split first character off the name for prefix
        String firstLetter = playerName.substring(0, 1);
        String nameRemainder = playerName.substring(1);

        //colour of the scoreboard team/name
        String rankColour = Account.getAccountMap().get(playerName.toLowerCase()).getRank().colour + "";

        //create prefix consisting of rankColour and firstLetter, and r+ suffix
        String prefix = rankColour + firstLetter;
        String suffix = (ChatColor.GRAY + "" + "(r+)");

        //prepare the the team
        Team team = scoreboard.getTeam(nameRemainder);
        if (team == null)
        {
            team = scoreboard.registerNewTeam(nameRemainder);
        }
        team.setPrefix(prefix);
        team.setSuffix(suffix);

        //update teams.txt
        updateTeamsListFile();
    }

    private static void removeScoreboardTeam(String playerName)
    {
        Team team = scoreboard.getTeam(playerName.substring(1));
        if (team == null)
        {
            return;
        }

        //remove the team from scoreboard and update team.txt
        team.unregister();
        updateTeamsListFile();
    }

    private static boolean playerCanSeeRPlusPresence(Player viewingPlayer, String targetPlayerName)
    {
        boolean serverOnline = MCServer.isServerOnline(targetPlayerName);
        boolean serverVisible = !Invisible.isInvisibleServer(targetPlayerName);
        boolean clientOnline = MCServer.isClientOnline(targetPlayerName);
        boolean clientVisible = !Invisible.isInvisibleClient(targetPlayerName);
        boolean viewerIsMod = viewingPlayer.hasPermission(CmdExecutor.MOD_PERM);
        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("sOnline: " + serverOnline + "  sVis: " + serverVisible + "  cOnline: " + clientOnline + "  cVis : " + clientVisible);
        }

        if (viewerIsMod)
        {
            if (serverOnline)
            {
                return false;
            }
            else
            {
                return clientOnline;
            }
        }

        //the viewer is not a mod
        if (serverOnline)
        {
            if (serverVisible)
            {
                return false;
            }
            else
            {
                return (clientOnline && clientVisible);
            }
        }
        else //not on server
        {
            return (clientOnline && clientVisible);
        }
    }

    public static void removeAllFromScoreboard()
    {
        if (!scoreboardFunctional || !scoreboardEnabled)
        {
            return;
        }

        for (String name : onlineTeamAndPacketMap.keySet())
        {
            removeFromScoreboard(name, false);
        }
    }

    public static void disableScoreboardFeature()
    {
        scoreboardEnabled = false;
        removeAllFromScoreboard();
    }

    public static void enableScoreboardFeature()
    {
        scoreboardEnabled = true;
        for (String name : Account.getLoggedInClientUsernamesSet())
        {
            if (!Invisible.isInvisibleClient(name))
            {
                addToScoreboard(name, false);
            }
        }
    }

    public static void sendScoreboardListToNewPlayer(final Player player)
    {
        if (!scoreboardFunctional || !scoreboardEnabled)
        {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
            @Override
            public void run()
            {
                for (Map.Entry<String, PacketContainer> entry : onlineTeamAndPacketMap.entrySet())
                {
                    if (playerCanSeeRPlusPresence(player, entry.getKey()))
                    {
                        sendPacketToPlayer(player.getName(), entry.getValue());
                    }
                }
            }
        }, 1);
    }

    private static PacketContainer createScoreboardAddPacket(String packetPlayerName)
    {
        PacketContainer playerInfo = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
        playerInfo.getStrings().write(0, packetPlayerName);
        playerInfo.getBooleans().write(0, true);
        playerInfo.getIntegers().write(0, 0);
        return playerInfo;
    }

    private static PacketContainer createScoreboardRemovePacket(String packetPlayerName)
    {
        PacketContainer playerInfo = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
        playerInfo.getStrings().write(0, packetPlayerName);
        playerInfo.getBooleans().write(0, false);
        playerInfo.getIntegers().write(0, 9999);
        return playerInfo;
    }

    private static void sendPacketToPlayer(String recipientPlayerName, PacketContainer packetContainer)
    {
        try
        {
            protocolManager.sendServerPacket(s.getPlayer(recipientPlayerName), packetContainer);
        }
        catch (InvocationTargetException e)
        {
            MCServer.pluginLog("WARNING! Could not send scoreboard packet to player in MCServer.addToScoreboard: " +
                    e.getMessage());
        }
    }

    private static void removeTeamsOnTeamsFile()
    {
        //get teamsList from file
        List<String> teamsList;
        try
        {
            teamsList = Files.readAllLines(Paths.get(teamsFile.getPath()), encoding);
        }
        catch (IOException e)
        {
            MCServer.pluginLog("WARNING! IOException in ScoreboardManager.removeTeamsOnTeamsFile. " +
                    "Scoreboard functionality disabled! Exception: " + e.getMessage());
            ScoreboardManager.scoreboardFunctional = false;
            return;
        }

        //remove all scoreboard teams on the list if they exist
        for (String teamName : teamsList)
        {
            Team team = scoreboard.getTeam(teamName);
            if (team != null)
            {
                scoreboard.getTeam(teamName).unregister();
            }
        }

        //clear file contents
        updateTeamsListFile();
    }

    private static void updateTeamsListFile()
    {
        List<String> teamsList = new ArrayList<>();
        for (String name : onlineTeamAndPacketMap.keySet())
        {
            teamsList.add(name.substring(1));
        }

        try
        {
            Files.write(Paths.get(teamsFile.getPath()), teamsList, encoding);
        }
        catch (IOException e)
        {
            MCServer.pluginLog("WARNING! IOException in ScoreboardManager.updateTeamsListFile: " + e.getMessage());
        }
    }

    public static void scoreboardDebug(CommandSender sender)
    {
        String teamsString = "";
        Set<Team> teams = scoreboard.getTeams();
        for (Team team : teams)
        {
            teamsString = teamsString + team.getName() + ", ";
        }

        //remove last ", "
        teamsString = teamsString.substring(0, teamsString.length() - 2);

        sender.sendMessage(ChatColor.AQUA + "Active Scoreboard Teams: " + ChatColor.YELLOW + teamsString);
    }
}
