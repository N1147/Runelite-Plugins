package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

public class Phials extends Task {
    public Phials(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 4;
    }

    @Override
    public boolean validate() {
        //if inside house
        if (MiscUtils.isInPOH(client)) {
            return false;
        }

        QueryResults<NPC> results = new NPCQuery()
                .idEquals(NpcID.PHIALS)
                .result(client);

        if (results == null || results.isEmpty()) {
            return false;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return false;
        }

        if (inventoryWidget.getWidgetItems().stream().filter(item -> item.getId() == config.mode().getPlankId()).count() >= config.mode().getPlankCost()) {
            return false;
        }

        QueryResults<WidgetItem> notedPlanksQueryResults = new InventoryWidgetItemQuery()
                .idEquals(config.mode().getPlankId() + 1)
                .result(client);

        if (notedPlanksQueryResults == null || notedPlanksQueryResults.isEmpty()) {
            return false;
        }

        Widget dialogueWidget = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);

        if (dialogueWidget != null) {
            return false;
        }

        return true;
    }

    @Override
    public void onGameTick(GameTick event) {
        QueryResults<NPC> results = new NPCQuery()
                .idEquals(NpcID.PHIALS)
                .result(client);

        if (results == null || results.isEmpty()) {
            return;
        }

        NPC phials = results.first();

        if (phials == null) {
            return;
        }

        QueryResults<WidgetItem> widgetItemQueryResults = new InventoryWidgetItemQuery()
                .idEquals(config.mode().getPlankId() + 1)
                .result(client);

        if (widgetItemQueryResults == null || widgetItemQueryResults.isEmpty()) {
            return;
        }

        WidgetItem notedPlanks = widgetItemQueryResults.first();

        if (notedPlanks == null) {
            return;
        }

        clientThread.invoke(() -> {
            client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
            client.setSelectedItemSlot(notedPlanks.getIndex());
            client.setSelectedItemID(notedPlanks.getId());
            client.invokeMenuAction(
                    "Use",
                    "<col=00ffff>Oak plank -> Phials",
                    phials.getIndex(),
                    MenuAction.ITEM_USE_ON_NPC.getId(),
                    0,
                    0
            );
        });
    }
}
