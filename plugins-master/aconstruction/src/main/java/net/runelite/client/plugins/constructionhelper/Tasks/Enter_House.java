package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

public class Enter_House extends Task {
    public Enter_House(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 3;
    }

    @Override
    public boolean validate() {
        //if inside house
        if (MiscUtils.isInPOH(client)) {
            return false;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return false;
        }

        if (inventoryWidget.getWidgetItems().stream().filter(item -> item.getId() == config.mode().getPlankId()).count() < config.mode().getPlankCost()) {
            return false;
        }

        QueryResults<GameObject> objectQueryResults = new GameObjectQuery()
                .nameEquals("Portal")
                .result(client);

        if (objectQueryResults == null || objectQueryResults.isEmpty()) {
            return false;
        }

        GameObject portalObject = objectQueryResults.first();

        if (portalObject == null) {
            return false;
        }

        return true;
    }

    @Override
    public void onGameTick(GameTick event) {
        QueryResults<GameObject> objectQueryResults = new GameObjectQuery()
                .nameEquals("Portal")
                .result(client);

        if (objectQueryResults == null || objectQueryResults.isEmpty()) {
            return;
        }

        GameObject portalObject = objectQueryResults.first();

        if (portalObject == null) {
            return;
        }

        clientThread.invoke(() ->
                client.invokeMenuAction(
                        "Build mode",
                        "<col=ffff>Portal",
                        portalObject.getId(),
                        MenuAction.GAME_OBJECT_THIRD_OPTION.getId(),
                        portalObject.getSceneMinLocation().getX(),
                        portalObject.getSceneMinLocation().getY()
                )
        );
    }
}
