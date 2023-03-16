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
		name = "AQuickFighter (Auto)",
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
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	boolean Firstime;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	Rectangle clickBounds;
	int timeout = 0;
	long sleepLength;
	//List<TileItem> loot = new ArrayList<>();
	String[] values;
	String[] names;
	@Provides
	NQuickFighterConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NQuickFighterConfig.class);
	}


	@Override
	protected void startUp() throws IOException, ClassNotFoundException {
		resetVals();
	}

	@Override
	protected void shutDown() throws IOException, ClassNotFoundException {
		resetVals();
	}

	private void resetVals() throws IOException, ClassNotFoundException {
		spawnedItems.clear();
		lootableItems.clear();
		killableEnemies.clear();
		values = config.lootNames().toLowerCase().split("\\s*,\\s*");
		//names = config.enemyNames().toLowerCase().split("\\s*,\\s*");
		if (!config.lootNames().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
			log.debug("Lootable items are: {}", lootableItems.toString());
		}
		/*if (!config.enemyNames().isBlank()){
			killableEnemies.clear();
			killableEnemies.addAll(Arrays.asList(names));
			log.debug("Enemies are: {}", killableEnemies.toString());
		}*/
		Firstime = true;
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
	}

	public void setLocation() throws IOException, ClassNotFoundException {
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
		{
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		}
		else
		{
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
	}

	private int tickDelay()
	{
		int tickLength = (int) core.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
	public NQuickFighterState getState()
	{
		if (timeout > 0)
		{
			return NQuickFighterState.TIMEOUT;
		}
		/*else if (core.isMoving(beforeLoc))
		{
			timeout = 2 + tickDelay();
			return NQuickFighterState.MOVING;
		}*/
		else if(client.getLocalPlayer().getAnimation()!=-1){
			return NQuickFighterState.ANIMATING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1 && client.getLocalPlayer().getAnimation() != 7202){
			return NQuickFighterState.ANIMATING;
		}
		else {
			return getAirsState();
		}
	}
	boolean started = false;
	@Subscribe
	private void onGameTick(GameTick tick) throws IOException, ClassNotFoundException {
		player = client.getLocalPlayer();
		if (client != null && player != null)
		{
			state = getState();
			beforeLoc = player.getLocalLocation();
			//core.setMenuEntry(null);
			switch (state)
			{
				case TIMEOUT:
					//core.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case MOVING:
					//core.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case ATTACK:
					findEnemiesLoop();
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
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
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		spawnedItems.clear();
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			state = NQuickFighterState.TIMEOUT;
			timeout = 2;
		}
	}
	public void findEnemiesLoop(){
		NPC target = core.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 20, config.enemyNames(), false);
		if (core.findNearestNpcTargetingLocal() != null){
			target = core.findNearestNpcTargetingLocal();
			return;
		}
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
				//clientThread.invoke(() -> client.invokeMenuAction("", "",bone.getId(), MenuAction.CC_OP.getId(), bone.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
	}

	private NQuickFighterState getAirsState() {

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
	}

	private void lootItem(List<WorldPoint> itemList) {
		if (itemList.get(0) != null) {
			core.walk(itemList.get(0));
			//clientThread.invoke(() -> client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
	}

	public final List<WorldPoint> spawnedItems = new ArrayList<>();

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {
			//Point point = Perspective.localToCanvas(client, new LocalPoint(event.getTile().getWorldLocation().getX(), event.getTile().getWorldLocation().getY()), client.getPlane());
			spawnedItems.add(event.getTile().getWorldLocation());
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		spawnedItems.remove(event.getTile().getWorldLocation());
	}
}
enum NQuickFighterState
{
	ANIMATING,
	MOVING,
	TIMEOUT,
	DEPOSIT_ITEMS,
	CLIMB_LADDER,
	CLIMB_LADDER2,
	LOOT_ITEMS,
	JUMP_OBSTACLE,
	JUMP_OBSTACLE2,
	BURY_BONES,
	ATTACK,
	WITHDRAW_ITEMS,
	UNHANDLED_STATE;
}
