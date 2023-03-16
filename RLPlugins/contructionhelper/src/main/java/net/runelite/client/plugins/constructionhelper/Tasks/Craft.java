package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

import javax.inject.Inject;

public class Craft extends Task {
    public Craft(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 1;
    }

    @Override
    public boolean validate() {
        //if inside house
        if (!MiscUtils.isInPOH(client)) {
            return false;
        }

        Widget craftObjectWidget = client.getWidget(config.mode().getWidget().getGroupId(), config.mode().getWidget().getChildId());

        if (craftObjectWidget != null) {
            return true;
        }

        return false;
    }
    @Inject
    Core core;
    @Override
    public void onGameTick(GameTick event) {
        Widget craftObjectWidget = client.getWidget(config.mode().getWidget().getGroupId(), config.mode().getWidget().getChildId());
        if (craftObjectWidget != null) {
            core.clickWidget(craftObjectWidget);
        }
    }
}
