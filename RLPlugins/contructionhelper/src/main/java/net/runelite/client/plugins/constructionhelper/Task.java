package net.runelite.client.plugins.constructionhelper;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;

public abstract class Task {
    public Task(AConstructionPlugin plugin, Client client, ClientThread clientThread, AConstructionConfig config) {
        this.plugin = plugin;
        this.client = client;
        this.clientThread = clientThread;
        this.config = config;
    }

    @Inject
    public AConstructionPlugin plugin;

    @Inject
    public Client client;

    @Inject
    public ClientThread clientThread;

    @Inject
    public AConstructionConfig config;

    public abstract int getDelay();

    public abstract boolean validate();

    public String getTaskDescription() {
        return this.getClass().getSimpleName();
    }

    public void onGameTick(GameTick event) { }
}
