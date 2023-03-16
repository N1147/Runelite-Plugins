package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

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

    @Override
    public void onGameTick(GameTick event) {
        Widget craftObjectWidget = client.getWidget(config.mode().getWidget().getGroupId(), config.mode().getWidget().getChildId());

        if (craftObjectWidget != null) {
            clientThread.invoke(() ->
                    client.invokeMenuAction("Continue", "",
                            1,
                            MenuAction.CC_OP.getId(),
                            craftObjectWidget.getIndex(),
                            craftObjectWidget.getId()
                    )
            );
        }
    }
}
