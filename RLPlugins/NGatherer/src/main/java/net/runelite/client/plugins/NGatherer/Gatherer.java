package net.runelite.client.plugins.Gatherer;

import com.google.inject.Provides;
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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.Utils.NewMenuEntry;
import net.runelite.client.plugins.Utils.Walking;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@PluginDescriptor(
	name = "NGatherer (Auto)",
	description = "Gathers from various nodes and banks or drops.",
	tags = {"anarchise","skiller","thieving","woodcut","mining","hunter"},
	enabledByDefault = false
)

public class Gatherer extends Plugin
{
	@Inject
	private Client client;
	@Provides
	NGathererConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NGathererConfig.class);
	}
	@Inject
	private NGathererConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Core core;
	@Inject
	private Walking walking;
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	private Random r = new Random();
	private int timeout;
	Instant botTimer;
	WorldPoint ResetLocation = new WorldPoint(0, 0, 0);
	private final Set<Integer> itemIds = new HashSet<>();
	private GameObject bs2;
	public boolean started = false;
	@Override
	protected void startUp() throws Exception
	{
		reset();
	}
	private final Set<WidgetItem> DIAMOND_SET = Set.of();
	private void reset() throws IOException, ClassNotFoundException {
		values = config.loot().toLowerCase().split("\\s*,\\s*");
		if (!config.loot().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
		}
		//FISHIES.clear();
		//FISHIES.add(ItemID.BLUEGILL, ItemID.COMMON_TENCH);
		//FISHIES.add(ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN);
		banked = true;
		botTimer = null;
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getType() == MenuAction.CC_OP.getId() && (event.getActionParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getActionParam1() == 11927560 || event.getActionParam1() == 4522007 || event.getActionParam1() == 24772686))
		{
			return;
		}

		/*if (core.targetMenu != null)
		{
			MenuEntry[] entries = client.getMenuEntries();
			entries[0] = client.createMenuEntry(-1).setOption(core.targetMenu.getOption()).setTarget(core.targetMenu.getTarget()).setIdentifier(core.targetMenu.getIdentifier()).setType(core.targetMenu.getType()).setParam0(core.targetMenu.getParam0()).setParam1(core.targetMenu.getParam1()).setForceLeftClick(true);
			client.setMenuEntries(entries);
		}*/
	}

	private void handleDropItems() {
		/*final Collection<WidgetItem> items = core.getAllInventoryItems();
		if (items.stream().findFirst() != null) {
			WidgetItem first = items.stream().filter(wi -> wi.getId() != ItemID.KNIFE && wi.getId() != ItemID.COINS_995 && wi.getId() != ItemID.GOLDEN_TENCH && wi.getId() != ItemID.RUNE_AXE && wi.getId() != ItemID.ADAMANT_AXE && wi.getId() != ItemID.MITHRIL_AXE && wi.getId() != ItemID.BLACK_AXE&& wi.getId() != ItemID.STEEL_AXE && wi.getId() != ItemID.DRAGON_AXE && wi.getId() != ItemID.DRAGON_PICKAXE && wi.getId() != ItemID.RUNE_PICKAXE && wi.getId() != ItemID.ADAMANT_PICKAXE && wi.getId() != ItemID.MITHRIL_PICKAXE && wi.getId() != ItemID.BLACK_PICKAXE && wi.getId() != ItemID.STEEL_PICKAXE && wi.getId() != ItemID.FISHING_BAIT && wi.getId() != ItemID.BARBARIAN_ROD && wi.getId() != ItemID.FISHING_ROD && wi.getId() != ItemID.FLY_FISHING_ROD && wi.getId() != ItemID.OILY_FISHING_ROD && wi.getId() != config.item1() && wi.getId() != config.item2()).findFirst().get();
			if (first != null) {
				startedDropping = true;
				core.dropItem(first);
			}
			else {
				startedDropping = false;
			}
		}
		else {
			startedDropping = false;
		}*/
		core.dropAllExcept(core.stringToIntList(config.item1()), true, 100, 350);
	}

	public WidgetItem getFood() {
		WidgetItem item;
		item = core.getInventoryWidgetItem(Collections.singletonList(config.foodID()));
		if (item != null)
		{
			return item;
		}
		return item;
	}
	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	private void openBank() {
		GameObject bankTarget = core.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			core.targetMenu = new NewMenuEntry("", "", bankTarget.getId(), core.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(),bankTarget.getSceneMinLocation().getY(), true);
			core.doInvoke(core.targetMenu, bankTarget.getConvexHull().getBounds());
			//clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), core.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		}
	}

	Player player;
	WorldPoint walkzone = new WorldPoint(0, 0, 0);
	Instant veilTimer;
	NPC beast;
	long timeRan;
	int timeRun;
	int resetTime = 61;
	int timeRuns;
	boolean isVeiled = false;
	NPC bs;
	List<TileItem> loot = new ArrayList<>();
	String[] values;
	String[] names;
	LocalPoint currentLoc;
	private boolean startedDropping = false;
	int timeout2 = 0;
	private void getStates() {

		if (veilTimer != null) {
			Duration duration = Duration.between(veilTimer, Instant.now());
			timeRan = duration.getSeconds();
			timeRun = (int) timeRan;
			timeRuns = (resetTime) - timeRun;
			if (timeRun > resetTime) {
				isVeiled = false;
				timeRan = 0;
				timeRun = 0;
				timeRuns = 0;
			}
		}
		if (client.getLocalPlayer().getAnimation() > 1 && !config.thieving()) {
			return;
		}
		if (client.getLocalPlayer().getInteracting() != null){
			return;
		}
		if (!startedDropping && core.inventoryFull() && !config.bank()) {
			startedDropping = true;
		}
		if (!core.inventoryContainsExcept(core.stringToIntList(config.item1())) && startedDropping) {
			startedDropping = false;
		}
		if (!config.bank() && startedDropping) {
			handleDropItems();
			return;
		}
		if (core.isBankOpen()) {
			if (!banked) {
				core.depositAll();
				banked = true;
				return;
			}
			/*if (config.shadowVeil() && !core.inventoryContains(564)) {
				core.withdrawAllItem(564);
				return;
			}*/
			if (config.dodgynecks() && !core.inventoryContains(21143)) {
				core.withdrawItemAmount(21143, config.dodgyNecks());
				return;
			}
			if (!core.inventoryContains(config.foodID()) && config.foodID() != 0) {
				core.withdrawItemAmount(config.foodID(), config.foodAmount());
				return;
			}
			if (banked) {
				walking.walkTileOnScreen(ResetLocation);
				return;
			}
		}


		if (!core.isBankOpen()) {
			if (!loot.isEmpty()) {
				lootItem(spawnedItems);
				return;
			}
			if (config.bank() && core.inventoryFull() && !config.aerial()) {
				banked = false;
				GameObject bank = core.findNearestBankNoDepositBoxes();
				core.useGameObjectDirect(bank);
				return;
			}
			if (!config.bank() && core.inventoryFull() && !config.aerial()) {
				handleDropItems();
				return;
			}
			/*if (config.shadowVeil() && !isVeiled && client.getVarbitValue(12414) == 0 && client.getVarbitValue(12291) == 0 && client.getGameState() == GameState.LOGGED_IN) {
				veilTimer = Instant.now();
				//TODO
				//clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14287025));
				isVeiled = true;
				return;//^veil
			}*/
			if (config.bank() && config.dodgynecks() && !core.inventoryContains(21143)) {
				banked = false;
				GameObject bank = core.findNearestBankNoDepositBoxes();
				core.useGameObjectDirect(bank);
				return;//&bank
			}
			/*if (config.bank() && !core.inventoryContains(config.foodID())) {
				banked = false;
				GameObject bank = core.findNearestBankNoDepositBoxes();
				core.useGameObjectDirect(bank);
				return;//bank
			}*/ //TODO REMOVED TO PREVENT BANK LOOP
			if (config.bank() && core.inventoryFull()) {
				banked = false;
				GameObject bank = core.findNearestBankNoDepositBoxes();
				core.useGameObjectDirect(bank);
				return;
			}

			if (config.dodgynecks() && core.inventoryContains(ItemID.DODGY_NECKLACE) && !core.isItemEquipped(Collections.singleton(ItemID.DODGY_NECKLACE))) {
				core.useItem(ItemID.DODGY_NECKLACE, MenuAction.ITEM_FIRST_OPTION);
				return;
			}
			if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.minHealth() && core.inventoryContains(config.foodID())) {
				WidgetItem food = getFood();
				if (food != null) {
					core.useItem(food.getId(), MenuAction.ITEM_FIRST_OPTION);
				}
				return;
			}
			if (core.inventoryFull() && config.aerial()) {
				startCutting = true;
			}
			if (config.aerial() && startCutting && !core.inventoryContains(FISH)) {
				startCutting = false;
			}
			if (config.aerial() && startCutting && core.inventoryContains(FISH)) {
				//core.useItem(ItemID.KNIFE, MenuAction.ITEM_USE);
				core.useItem(ItemID.KNIFE, MenuAction.ITEM_USE);
				core.useItem(getFish().getId(), MenuAction.ITEM_USE_ON_ITEM);
				return;
			}
			if (!core.inventoryFull() && bs != null && config.typethief()) {
				if (core.getRandomIntBetweenRange(0, 10) >= 8) {
					timeout2 = (int) core.randomDelay(false, 0, 5, 4, 2);
					return;
				}
 				if (config.typethief()) {//npc
					core.targetMenu = new NewMenuEntry("", "", bs.getIndex(), MenuAction.NPC_FIRST_OPTION, 0, 0, true);
					core.doInvoke(core.targetMenu, bs.getConvexHull().getBounds());
					//clientThread.invoke(() -> client.invokeMenuAction("", "", bs.getIndex(), config.type().action.getId(), 0, 0));
				}
				if (!config.thieving()) {
					return;
				}
			}
			if (!core.inventoryFull() && bs2 != null && !config.typethief()) {
				if (core.getRandomIntBetweenRange(0, 10) >= 8) {
					timeout2 = (int) core.randomDelay(false, 0, 5, 4, 2);
					return;
				}
				if (!config.typethief()) {//object
					core.targetMenu = new NewMenuEntry("", "", bs2.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION, bs2.getSceneMinLocation().getX(), bs2.getSceneMinLocation().getY(), true);
					core.doInvoke(core.targetMenu, bs2.getConvexHull().getBounds());
					//clientThread.invoke(() -> client.invokeMenuAction("", "", bs2.getId(), config.type().action.getId(), bs2.getSceneMinLocation().getX(), bs2.getSceneMinLocation().getY()));
				}
				return;
			}
		}
	}
	private final Set<Integer> FISH = Set.of(ItemID.BLUEGILL, ItemID.COMMON_TENCH, ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN);
	private boolean banked = true;
	private boolean startCutting = false;


	public void useWallObject(WallObject targetObject, long sleepDelay, int opcode)
	{
		if(targetObject!=null) {
			core.targetMenu = new NewMenuEntry("", "", targetObject.getId(), opcode, targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY(), true);
			core.doInvoke(core.targetMenu, targetObject.getConvexHull().getBounds());

			//clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), opcode, targetObject.getLocalLocation().getSceneX(), targetObject.getLocalLocation().getSceneY()));
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

	public WidgetItem getFish() {
		//ItemID.BLUEGILL, ItemID.COMMON_TENCH, ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN
		return core.getInventoryWidgetItem(core.stringToIntList("22826,22829,22832,22835"));
	}

	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		if (timeout2 > 0) {
			timeout2--;
			return;
		}
		currentLoc = client.getLocalPlayer().getLocalLocation();
		bs = core.findNearestNpc(config.npcID());
		bs2 = core.findNearestGameObject(config.objID());
		beast = core.getFirstNPCWithLocalTarget();
		player = client.getLocalPlayer();
		int[] customTemp2 = core.stringToIntArray(config.returnLoc());
		ResetLocation = new WorldPoint(customTemp2[0], customTemp2[1], customTemp2[2]);
		getStates();

	}

	List<String> lootableItems = new ArrayList<>();

	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged){
		spawnedItems.clear();
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
	private long sleepDelay()
	{
		long sleepLength = core.randomDelay(false, 200, 300, 100, 250);
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) core.randomDelay(false, 1, 2, 1, 1);
		return tickLength;
	}
}