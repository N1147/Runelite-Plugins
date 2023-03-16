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
	public void onGameTick(GameTick event) throws IOException, ClassNotFoundException {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (config.antivenom() && client.getVar(VarPlayer.IS_POISONED) > 0) {
			WidgetItem ven = utils.getInventoryWidgetItem(ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4, ItemID.SUPERANTIPOISON1, ItemID.SUPERANTIPOISON2, ItemID.SUPERANTIPOISON3, ItemID.SUPERANTIPOISON4,ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952, ItemID.ANTIVENOM1_12919 ,ItemID.ANTIVENOM2_12917,ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM4_12913, ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4, ItemID.ANTIPOISON_1, ItemID.ANTIPOISON_2, ItemID.ANTIPOISON_3, ItemID.ANTIPOISON_4);
			if (ven != null) {
				utils.useItem(ven.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", ven.getId(), MenuAction.CC_OP.getId(), ven.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (config.antifire() && client.getVarbitValue(6101) == 0){
			WidgetItem overload = utils.getInventoryWidgetItem(ItemID.SUPER_ANTIFIRE_POTION1, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION4, ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4,ItemID.EXTENDED_SUPER_ANTIFIRE1,ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4);
			if (overload != null) {
				utils.useItem(overload.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", overload.getId(), MenuAction.CC_OP.getId(), overload.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (config.antifire() && client.getVarbitValue(3981) == 0){
			WidgetItem overload = utils.getInventoryWidgetItem(ItemID.SUPER_ANTIFIRE_POTION1, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION4, ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4,ItemID.EXTENDED_SUPER_ANTIFIRE1,ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4);
			if (overload != null) {
				utils.useItem(overload.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", overload.getId(), MenuAction.CC_OP.getId(), overload.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill1().skill) <= config.level1()) {
			WidgetItem item1 = utils.getInventoryWidgetItem(config.pot1().ItemIDs);
			if (item1 != null) {
				utils.useItem(item1.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", item1.getId(), MenuAction.CC_OP.getId(), item1.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill2().skill) <= config.level2()) {
			WidgetItem item2 = utils.getInventoryWidgetItem(config.pot2().ItemIDs);
			if (item2 != null) {
				utils.useItem(item2.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", item2.getId(), MenuAction.CC_OP.getId(), item2.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(config.skill3().skill) <= config.level3()) {
			WidgetItem item3 = utils.getInventoryWidgetItem(config.pot3().ItemIDs);
			if (item3 != null) {
				utils.useItem(item3.getId(),"drink");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", item3.getId(), MenuAction.CC_OP.getId(), item3.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
	}

}