package net.runelite.client.plugins.ahotkeys;

import com.google.inject.Provides;
import net.runelite.api.widgets.WidgetItem;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import java.util.Random;

import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;


@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
		name = "AHotkeys",
		description = "Anarchise' Hotkeys",
		tags = {"Hotkeys","Anarchise","pvm"},
		enabledByDefault = false
)
public class AHotkeys extends Plugin
{
	@Inject PUtils utils;

	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	private AHotkeysConfig configpk;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;


	@Inject
	private AHotkeysListener AHotkeysListener;

	@Inject
	private KeyManager keyManager;

	@Getter(AccessLevel.PACKAGE)

	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	int timeout = 0;
	private Random r = new Random();
	@Provides
	AHotkeysConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(AHotkeysConfig.class);
	}

	@Override
	protected void startUp() throws IOException {
		keyManager.registerKeyListener(AHotkeysListener);

	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(AHotkeysListener);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) throws IOException {

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
	}

	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ahotkeys"))
		{
			return;
		}
	}
}
