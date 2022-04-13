package net.runelite.client.plugins.NQuickFighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.runelite.client.plugins.NQuickFighter.NQuickFighterState.*;


@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
		name = "NQuickFighter",
		enabledByDefault = false,
		description = "Attacks and loots",
		tags = {"numb","pvm", "fighter"}
)
@Slf4j
public class NQuickFighter extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NQuickFighterConfig config;

	@Inject
	private PUtils utils;

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
	boolean startTeaks;
	List<TileItem> loot = new ArrayList<>();
	String[] values;
	String[] names;
	@Provides
	NQuickFighterConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NQuickFighterConfig.class);
	}


	@Override
	protected void startUp() throws IOException {
		resetVals();
	}

	@Override
	protected void shutDown() throws IOException {
		resetVals();
	}

	private void resetVals() throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
		loot.clear();
		lootableItems.clear();
		killableEnemies.clear();
		values = config.lootNames().toLowerCase().split("\\s*,\\s*");
		names = config.enemyNames().toLowerCase().split("\\s*,\\s*");
		if (!config.lootNames().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
			log.debug("Lootable items are: {}", lootableItems.toString());
		}
		if (!config.enemyNames().isBlank()){
			killableEnemies.clear();
			killableEnemies.addAll(Arrays.asList(names));
			log.debug("Enemies are: {}", killableEnemies.toString());
		}
		Firstime = true;
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NQuickFighter"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startTeaks)
			{
				startTeaks = true;
				Firstime = true;
				state = null;
				targetMenu = null;
				loot.clear();
				botTimer = Instant.now();
				setLocation();
			}
			else
			{
				startTeaks=false;
				resetVals();
			}
		}
	}

	public void setLocation() throws IOException {
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
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
	public NQuickFighterState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		else if (utils.isMoving(beforeLoc))
		{
			timeout = 2 + tickDelay();
			return MOVING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1){
			return ANIMATING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1 && client.getLocalPlayer().getAnimation() != 7202){
			return ANIMATING;
		}
		else {
			return getAirsState();
		}
	}
	boolean started = false;
	@Subscribe
	private void onGameTick(GameTick tick) throws IOException {
		if (!startTeaks)
		{
			return;
		}
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			startTeaks = false;
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && skillLocation != null)
		{
			state = getState();
			beforeLoc = player.getLocalLocation();
			utils.setMenuEntry(null);
			switch (state)
			{
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case MOVING:
					utils.handleRun(30, 20);
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
					lootItem(loot);
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
		loot.clear();
		if (event.getGameState() == GameState.LOGGED_IN && startTeaks)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}
	public void findEnemiesLoop(){
		NPC target = utils.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 20, config.enemyNames(), false);
		if (target == null)
		{
			return;
		}
		if (utils.findNearestNpcTargetingLocal(config.enemyNames(), false) != null){
			return;
		}
		utils.attackNPCDirect(target);
	}

	private void buryBones() {
		List<WidgetItem> bones = utils.getInventoryItems("bones");
		for (WidgetItem bone : bones) {
			if (bone != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "",bone.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), bone.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
	}

	private void lootItem(List<TileItem> itemList) {
		TileItem lootItem = utils.getNearestTileItem(itemList);
		if (lootItem != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "",lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
	}

	private NQuickFighterState getAirsState() {

		if (utils.inventoryContains("bones") && config.buryBones()) {
			return BURY_BONES;
		}
		if (loot.isEmpty() && !utils.inventoryFull()) {
			return ATTACK;
		}
		if (!loot.isEmpty() && !utils.inventoryFull()) {
			return LOOT_ITEMS;
		}
		return UNHANDLED_STATE;
	}

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		if (!startTeaks) {
			return;
		}
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {
			loot.add(item);
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		if (!startTeaks) {
			return;
		}
		loot.remove(event.getItem());
	}
}
