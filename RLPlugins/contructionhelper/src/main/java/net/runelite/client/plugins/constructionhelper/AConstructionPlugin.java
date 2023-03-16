package net.runelite.client.plugins.constructionhelper;

import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.constructionhelper.Tasks.*;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
@PluginDescriptor(
        name = "AConstruction (Auto)",
        description = "Trains construction. Unnotes planks using Phials.",
        tags = {"construction", "cons", "anarchise"},
        enabledByDefault = false
)

public class AConstructionPlugin extends Plugin {
    static List<Class<?>> taskClassList = new ArrayList<>();

    static {
        taskClassList.add(Reset.class);
       // taskClassList.add(Stop_Plugin.class);
        taskClassList.add(Run.class);
        taskClassList.add(Dialogue2.class);
        taskClassList.add(Craft.class);
        taskClassList.add(Remove.class);
        taskClassList.add(Build.class);
        taskClassList.add(Leave_House.class);
        taskClassList.add(Phials.class);
        taskClassList.add(Enter_House.class);
        taskClassList.add(Dialogue.class);
    }

    @Inject
    public Client client;

    @Inject
    public AConstructionConfig configch;

    @Inject
    public ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    boolean pluginStarted = false;

    @Provides
    AConstructionConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(AConstructionConfig.class);
    }

    public String status = "initializing...";

    private final TaskSet tasks = new TaskSet();
    Instant botTimer;
    public int delay = 0;

    @Override
    protected void startUp() throws Exception {
        if (!pluginStarted) {
            tasks.addAll(this, client, clientThread, configch, taskClassList);
            botTimer = Instant.now();
            pluginStarted = true;
        }
        else {
            tasks.clear();
            pluginStarted = false;
        }
    }

    @Override
    protected void shutDown() throws Exception {
        pluginStarted = false;
        tasks.clear();
        botTimer = null;
    }

    @Subscribe
    public void onGameTick(GameTick event) {

        if (!pluginStarted) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (delay > 0) {
            delay--;
            return;
        }

        Task task = tasks.getValidTask();

        if (task != null) {
            status = task.getTaskDescription();
            task.onGameTick(event);
            delay = task.getDelay() + getRandomWait();
        }
    }

    public int getRandomWait() {
        return (int) ((Math.random() * (configch.maxWaitTicks() - configch.minWaitTicks())) + configch.minWaitTicks());
    }

    private void sendGameMessage(String message) {
        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(
                                new ChatMessageBuilder()
                                        .append(ChatColorType.HIGHLIGHT)
                                        .append(message)
                                        .build())
                        .build());
    }

    public void stopPlugin() {
        stopPlugin("");
    }

    public void stopPlugin(String reason) {
        pluginStarted = false;
        if (reason != null && !reason.isEmpty())
            sendGameMessage("AConstruction Stopped: " + reason);
    }
}