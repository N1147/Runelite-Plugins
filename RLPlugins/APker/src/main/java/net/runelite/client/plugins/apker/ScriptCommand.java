package net.runelite.client.plugins.apker;

import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;

public interface ScriptCommand {
    void execute(Client client, PkToolsConfig config, PkToolsPlugin plugin, ConfigManager configManager);
}
