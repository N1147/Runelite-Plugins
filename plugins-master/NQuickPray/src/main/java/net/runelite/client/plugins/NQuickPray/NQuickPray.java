package net.runelite.client.plugins.NQuickPray;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
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

	public int randomInt(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
	private boolean started = false;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		WidgetItem restoreItem = utils.getInventoryWidgetItem(ItemID.SANFEW_SERUM1, ItemID.SANFEW_SERUM2, ItemID.SANFEW_SERUM3, ItemID.SANFEW_SERUM4, ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4,
				ItemID.BLIGHTED_SUPER_RESTORE1, ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3,
				ItemID.BLIGHTED_SUPER_RESTORE4, ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4);
		int points = client.getBoostedSkillLevel(Skill.PRAYER);
		if (points <= randomInt(config.prayMin(), config.prayMax())) {
			utils.useItem(restoreItem.getId(),"drink");
		}
	}
}