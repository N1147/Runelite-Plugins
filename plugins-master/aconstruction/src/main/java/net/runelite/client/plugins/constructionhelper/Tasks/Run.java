package net.runelite.client.plugins.constructionhelper.Tasks;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.constructionhelper.AConstructionConfig;
import net.runelite.client.plugins.constructionhelper.AConstructionPlugin;
import net.runelite.client.plugins.constructionhelper.Task;

import java.util.concurrent.ThreadLocalRandom;

public class Run extends Task {
    int nextRunEnergy;

    public Run(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        super(plugin, client, clientThread, config);
        nextRunEnergy = getRandomIntBetweenRange(20, 100);
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public boolean validate() {
        //check if run is already enabled
        if (client.getVarpValue(173) == 1) {
            return false;
        }

        if (client.getEnergy() <= nextRunEnergy) {
            return false;
        }

        return true;
    }

    @Override
    public void onGameTick(GameTick event) {
        boolean runEnabled = client.getVarpValue(173) == 1;

        if (client.getEnergy() > nextRunEnergy) {
            if (!runEnabled) {
                nextRunEnergy = getRandomIntBetweenRange(20, 100);
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);
                if (runOrb != null) {
                    clientThread.invoke(() ->
                            client.invokeMenuAction(
                                    "Toggle Run",
                                    "",
                                    1,
                                    MenuAction.CC_OP.getId(),
                                    -1,
                                    WidgetInfo.MINIMAP_TOGGLE_RUN_ORB.getId()
                            )
                    );
                }
            }
        }
    }

    public int getRandomIntBetweenRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
