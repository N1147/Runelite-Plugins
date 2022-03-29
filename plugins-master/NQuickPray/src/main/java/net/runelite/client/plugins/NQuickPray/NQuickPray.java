package net.runelite.client.plugins.NQuickPray;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NQuickPray",
	description = "Drinks prayer/restore pots.",
	tags = {"prayer","numb","pquickpray","ztd"},
	enabledByDefault = false
)
public class NQuickPray extends Plugin
{

	@Provides
	NQuickPrayConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NQuickPrayConfig.class);
	}

	@Inject
	private NQuickPrayConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;
	@Inject
	private PUtils utils;
	public WidgetItem getRestore() {
		return NQuickPrayType.PRAYER_POTION.getItemFromInventory(client);
	}

	public int randomInt(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
	private boolean started = false;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			return;
		}
		WidgetItem restoreItem = getRestore();
		int points = client.getBoostedSkillLevel(Skill.PRAYER);
		if (points <= randomInt(config.prayMin(), config.prayMax())) {
			clientThread.invoke(() ->
					client.invokeMenuAction(
							"Drink",
							"<col=ff9040>Potion",
							restoreItem.getId(),
							MenuAction.ITEM_FIRST_OPTION.getId(),
							restoreItem.getIndex(),
							WidgetInfo.INVENTORY.getId()
					)
			);
		}
	}
}