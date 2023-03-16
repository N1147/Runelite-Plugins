package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.Task;

public class Break extends Task {
    public Break(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public boolean validate() {
        return plugin.chinBreakHandler.shouldBreak(plugin);
    }

    @Override
    public String getTaskDescription() {
        return "Taking a break";
    }

    @Override
    public void onGameTick(GameTick gameTick) {
        if (plugin.chinBreakHandler.shouldBreak(plugin)) {
            plugin.chinBreakHandler.startBreak(plugin);
        }
    }
}
