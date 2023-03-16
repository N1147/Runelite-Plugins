package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

public class Remove extends Task {
    public Remove(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
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

        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.mode().getObjectId())
                .result(client);

        if (!gameObjects.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public void onGameTick(GameTick event) {
        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.mode().getObjectId())
                .result(client);

        if (gameObjects == null || gameObjects.isEmpty())
            return;

        GameObject builtObject = gameObjects.first();

        if (builtObject == null)
            return;

        clientThread.invoke(() ->
                client.invokeMenuAction(
                        "Remove",
                        "",
                        config.mode().getObjectId(),
                        MenuAction.GAME_OBJECT_FIFTH_OPTION.getId(),
                        builtObject.getSceneMinLocation().getX(),
                        builtObject.getSceneMinLocation().getY()
                )
        );
    }
}
