package net.runelite.client.plugins.ahotkeys;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.io.IOException;


@PluginDescriptor(
		name = "AHotkeys",
		description = "Anarchise' Hotkeys",
		tags = {"Hotkeys","Anarchise","pvm"},
		enabledByDefault = false
)
public class AHotkeys extends Plugin
{
	@Inject
	Core core;
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

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() == MenuAction.CC_OP && (event.getWidgetId() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getWidgetId() == 11927560 || event.getWidgetId() == 4522007 || event.getWidgetId() == 24772686))
		{
			//Either logging out or world-hopping which is handled by 3rd party plugins so let them have priority
			core.targetMenu = null;
			return;
		}
		if (event.getMenuOption().contains("Walk") || event.getMenuAction() == MenuAction.WALK) {
			event.consume();
		}
		if (core.targetMenu != null && event.getParam1() != WidgetInfo.INVENTORY.getId() && event.getParam1() != WidgetInfo.FIXED_VIEWPORT_PRAYER_TAB.getId() && event.getParam1() != WidgetInfo.RESIZABLE_VIEWPORT_PRAYER_TAB.getId()){
			if (event.getId() != core.targetMenu.getIdentifier() ||
					event.getParam0() != core.targetMenu.getParam0() ||
					event.getParam1() != core.targetMenu.getParam1()) {
				event.consume();
			}
			core.targetMenu = null;
		}
	}
}




