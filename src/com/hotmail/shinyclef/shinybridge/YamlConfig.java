package com.hotmail.shinyclef.shinybridge;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * User: Shinyclef
 * Date: 3/10/13
 * Time: 2:12 PM
 */

public class YamlConfig
{
    private static ShinyBridge p = ShinyBridge.getPlugin();
    private static Map<String, YamlConfig> yamlMap = new HashMap<>();

    private String configName;
    private File customConfigFile;
    private FileConfiguration customConfig;

    public YamlConfig(String configName)
    {
        if (!configName.endsWith(".yml"))
        {
            configName = configName + ".yml";
        }
        this.configName = configName;
        this.customConfigFile = new File(p.getDataFolder(), configName);
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        yamlMap.put(configName, this);
    }

    public void reload()
    {
        if (customConfigFile == null)
        {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("Warning! Custom config file could not be reloaded in YamlConfig.reload.");
            }
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = p.getResource(configName);
        if (defConfigStream != null)
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public void save()
    {
        if (customConfig == null || customConfigFile == null)
        {
            return;
        }

        try
        {
            customConfig.save(customConfigFile);
        }
        catch (IOException ex)
        {
            p.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    public void saveDefault()
    {
        if (customConfigFile == null)
        {
            customConfigFile = new File(p.getDataFolder(), configName);
        }

        if (!customConfigFile.exists())
        {
            p.saveResource(configName, false);
        }
    }

    public static YamlConfig getCustomConfig(String configName)
    {
        if (!configName.endsWith(".yml"))
        {
            configName = configName + ".yml";
        }

        if (yamlMap.containsKey(configName))
        {
            return yamlMap.get(configName);
        }
        else
        {
            return null;
        }
    }
}
