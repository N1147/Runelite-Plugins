package net.runelite.client.plugins.NQuickFighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;


@PluginDescriptor(
		name = "QuickFighter (Auto)",
		enabledByDefault = false,
		description = "Attacks and loots.",
		tags = {"anarchise","pvm","fighter"}
)
@Slf4j
public class NQuickFighter extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NQuickFighterConfig config;

	@Inject
	private Core core;

	@Inject
	private ConfigManager configManager;

	@Inject
	ClientThread clientThread;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;
	List<String> lootableItems = new ArrayList<>();
	List<String> killableEnemies = new ArrayList<>();
	NQuickFighterState state;
	WorldPoint skillLocation;
	boolean Firstime;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	int timeout = 0;
	String[] values;

	@Provides
	NQuickFighterConfig getConfig(final ConfigManager configManager) {
		return configManager.getConfig(NQuickFighterConfig.class);
	}

	@Override
	protected void startUp() throws IOException, ClassNotFoundException {
		resetVals();
		//Resets values each time plugin is started or stopped
	}

	@Override
	protected void shutDown() throws IOException, ClassNotFoundException {
		resetVals();
		//Resets values each time plugin is started or stopped
	}

	private void resetVals() throws IOException, ClassNotFoundException {
		spawnedItems.clear();
		lootableItems.clear();
		killableEnemies.clear();
		values = config.lootNames().toLowerCase().split("\\s*,\\s*");
		if (!config.lootNames().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
		}
		Firstime = true;
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
	}

	private int tickDelay() {
		int tickLength = (int) core.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}

	public NQuickFighterState getState() {
		if (timeout > 0)
		{
			return NQuickFighterState.TIMEOUT;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1){
			return NQuickFighterState.ANIMATING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1 && client.getLocalPlayer().getAnimation() != 7202){
			return NQuickFighterState.ANIMATING;
		}
		else {
			return getFighterState();
		}
		//Basic animation checks so we don't attack enemies when we're already in combat
		//As well as delay if needed (add desired delay in ticks to 'timeout' within your function)
	}

	@Subscribe
	private void onGameTick(GameTick tick) throws IOException, ClassNotFoundException {
		player = client.getLocalPlayer();
		if (client != null && player != null)
		{
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case ANIMATING:
				case MOVING:
					timeout = tickDelay();
					break;
				case ATTACK:
					findEnemiesLoop();
					timeout = tickDelay();
					break;
				case LOOT_ITEMS:
					lootItem(spawnedItems);
					timeout = tickDelay();
					break;
				case BURY_BONES:
					buryBones();
					timeout = tickDelay();
					break;
			}
		}
		//Add functions for the conditions found in getFighterState()
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		spawnedItems.clear();
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			state = NQuickFighterState.TIMEOUT;
			timeout = 6;	//Waits for 6 ticks upon logging in, if the bot is started
		}
	}
	public void findEnemiesLoop(){
		NPC target = core.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 20, config.enemyNames(), false);
		if (core.findNearestNpcTargetingLocal() != null){
			target = core.findNearestNpcTargetingLocal();
			return;
		}	//Tries to find enemy targetting player before finding one that is not
		if (target == null)
		{
			return;
		}
		core.attackNPCDirect(target);
	}

	private void buryBones() {
		List<WidgetItem> bones = core.getInventoryItems("bones");
		for (WidgetItem bone : bones) {
			if (bone != null) {
				core.useItem(bone.getId(),"bury");
			}
		}
	}

	private NQuickFighterState getFighterState() {

		if (core.inventoryContains("bones") && config.buryBones()) {
			return NQuickFighterState.BURY_BONES;
		}
		if (!spawnedItems.isEmpty() && !core.inventoryFull()) {
			return NQuickFighterState.LOOT_ITEMS;
		}
		if (spawnedItems.isEmpty() && !core.inventoryFull()) {
			return NQuickFighterState.ATTACK;
		}
		return NQuickFighterState.UNHANDLED_STATE;
		//Iterate through conditions to decide what the bot should do
	}

	private void lootItem(List<WorldPoint> itemList) {
		if (itemList.get(0) != null) {
			core.walk(itemList.get(0));
		}
		//Clicks on the "tile" (point) of the item
	}

	public final List<WorldPoint> spawnedItems = new ArrayList<>();

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {
			spawnedItems.add(event.getTile().getWorldLocation());
			//Adds the location of item on the tile to our item map
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		spawnedItems.remove(event.getTile().getWorldLocation());
		//Resets the item map
	}
}
enum NQuickFighterState
{
	ANIMATING,
	MOVING,
	TIMEOUT,
	LOOT_ITEMS,
	BURY_BONES,
	ATTACK,
	UNHANDLED_STATE;
}
