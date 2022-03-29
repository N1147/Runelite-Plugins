package net.runelite.client.plugins.NQuickPot;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NQuickPot",
	description = "Pots automatically",
	tags = {"spec","numb","nquickpot","ztd"},
	enabledByDefault = false
)
public class NQuickPot extends Plugin
{
	@Provides
	NQuickPotConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NQuickPotConfig.class);
	}

	@Inject
	private NQuickPotConfig config;
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
	Player enemy;
	Instant timer;
	public WidgetItem getInventoryItem(Collection<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					return item;
				}
			}
		}
		return null;
	}
	public void runTimer()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null) { return; }
		if (enemy == null)  { return; }
		if (localPlayer.getInteracting() == null)
		{
			if (Duration.between(timer, Instant.now()).compareTo(Duration.ofSeconds(5)) > 0)
			{
				enemy = null;
			}
		}
	}
	public WidgetItem getConfigItem1()
	{
		WidgetItem item;
		item = config.pot1().getItemFromInventory(client);
		return item;
	}
	public WidgetItem getConfigItem2()
	{
		WidgetItem item;
		item = config.pot2().getItemFromInventory(client);
		return item;
	}
	public WidgetItem getConfigItem3()
	{
		WidgetItem item;
		item = config.pot3().getItemFromInventory(client);
		return item;
	}
	public WidgetItem GetAntifireItem()
	{
		WidgetItem item;

		item = NQuickPotPot.ANTIFIRE.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntiVenomItem() {

		WidgetItem item;

		item = NQuickPotPot.ANTIVENOM.getItemFromInventory(client);

		if (item != null) {
			return item;
		}

		return item;
	}
	@Subscribe
	public void onInteractingChanged(final InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer()) { return; }
		if (event.getTarget() == null)
		{
			timer = Instant.now();
			return;
		}
		Player localPlayer = client.getLocalPlayer();
		final List<Player> players = client.getPlayers();
		for (final Player player : players)
		{
			if (localPlayer != null && player == localPlayer.getInteracting())
			{
				enemy = player;
			}
		}
	}
	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		runTimer();
	}
	boolean started = false;

	@Subscribe
	public void onGameTick(GameTick event) throws IOException {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			return;
		}
		if (config.antivenom() && client.getVar(VarPlayer.IS_POISONED) > 0) {
			WidgetItem ven = GetAntiVenomItem();
			if (ven != null) {
				clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", ven.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), ven.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (config.antifire() && client.getVarbitValue(6101) == 0){
			WidgetItem overload = GetAntifireItem();
			if (overload != null) {
				clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", overload.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), overload.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (config.antifire() && client.getVarbitValue(3981) == 0){
			WidgetItem overload = GetAntifireItem();
			if (overload != null) {
				clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", overload.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), overload.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill1().skill) <= config.level1()) {
			WidgetItem item1 = getConfigItem1();
			if (item1 != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", item1.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item1.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill2().skill) <= config.level2()) {
			WidgetItem item2 = getConfigItem2();
			if (item2 != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", item2.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item2.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill3().skill) <= config.level3()) {
			WidgetItem item3 = getConfigItem3();
			if (item3 != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", item3.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item3.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
	}

}