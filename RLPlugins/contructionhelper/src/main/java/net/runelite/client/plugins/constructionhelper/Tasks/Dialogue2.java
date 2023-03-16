package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.MiscUtils;
import net.runelite.client.plugins.constructionhelper.Task;

import javax.inject.Inject;
import java.util.Arrays;

public class Dialogue2 extends Task {
    public Dialogue2(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
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

        // check for remove option dialogue
        Widget remove_dialogue_widget = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);

        if (remove_dialogue_widget != null) {
            Widget[] children = remove_dialogue_widget.getChildren();

            if (children != null) {
                if (Arrays.stream(children).anyMatch(w -> w.getText().contains("Really remove it?"))) {
                    return true;
                }
            }
        }

        return false;
    }
    @Inject
    Core core;
    @Override
    public void onGameTick(GameTick event) {
        Widget remove_dialogue_widget = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);

        if (remove_dialogue_widget != null) {
            Widget[] children = remove_dialogue_widget.getChildren();

            if (children != null) {
                if (Arrays.stream(children).anyMatch(w -> w.getText().contains("Really remove it?"))) {
                    core.clickWidget(children[1]);
                }
            }
        }
    }
}
