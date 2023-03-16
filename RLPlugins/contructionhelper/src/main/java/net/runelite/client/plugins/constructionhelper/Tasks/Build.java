package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.QueryResults;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

import javax.inject.Inject;

public class Build extends Task {
    public Build(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
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

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return false;
        }

        if (inventoryWidget.getWidgetItems()
                .stream()
                .filter(item -> item.getId() == config.mode().getPlankId())
                .count() < config.mode().getPlankCost()) {
            return false;
        }

        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.mode().getObjectSpaceId())
                .result(client);

        if (gameObjects == null || gameObjects.isEmpty()) {
            return false;
        }

        return true;
    }
    @Inject
    Core core;
    @Override
    public void onGameTick(GameTick event) {
        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.mode().getObjectSpaceId())
                .result(client);

        if (gameObjects == null || gameObjects.isEmpty()) {
            return;
        }

        GameObject larderSpaceObject = gameObjects.first();

        if (larderSpaceObject == null) {
            return;
        }
       core.useGameObjectDirect(larderSpaceObject);
    }
}
