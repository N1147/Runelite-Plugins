package net.runelite.client.plugins.NQuickEat;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
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
import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NQuickEat",
	description = "Eats food",
	tags = {"food","numb","pquickeat","ztd"},
	enabledByDefault = false
)
public class PQuickEat extends Plugin
{
	@Provides
	PQuickEatConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(PQuickEatConfig.class);
	}

	@Inject
	private PQuickEatConfig config;
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
	public WidgetItem getBrew() {
		return PQuickEatType.BREWS.getItemFromInventory(client);
	}

	public WidgetItem InventoryWidgetItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null) {
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
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
		int health = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
		if (health <= this.config.tripleHP()) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (config.brews()) {
				WidgetItem restoreItem = getBrew();
				clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
				clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!config.brews()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
				clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (health <= this.config.doubleHP() && health > this.config.tripleHP()) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (config.brews()) {
				WidgetItem restoreItem = getBrew();
				clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!config.brews()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (health < this.config.singleHP() && health > this.config.doubleHP()) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.ITEM_FIRST_OPTION.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
		}
	}
}