package net.runelite.client.plugins.NGatherer;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
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
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NGatherer",
	description = "Gathers from various nodes and banks or drops.",
	tags = {"numb","skiller","thieving","woodcut","mining","hunter"},
	enabledByDefault = false
)

public class NGatherer extends Plugin
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
	private PUtils utils;
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	private Random r = new Random();
	private int timeout;
	public NGathererState state;
	Instant botTimer;
	WorldPoint ResetLocation = new WorldPoint(0, 0, 0);
	private final Set<Integer> itemIds = new HashSet<>();
	private GameObject bs2;
	public boolean startTeaks = false;
	public boolean started = false;
	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	private void reset() throws IOException, ClassNotFoundException {
		values = config.loot().toLowerCase().split("\\s*,\\s*");
		if (!config.loot().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
		}
		//FISHIES.clear();
		//FISHIES.add(ItemID.BLUEGILL, ItemID.COMMON_TENCH);
		//FISHIES.add(ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN);
		itemIds.clear();
		itemIds.addAll(utils.stringToIntList(config.items()));
		startTeaks = false;
		banked = false;
		state = null;
		botTimer = null;
	}
	private void handleDropItems() {
		if (utils.inventoryContains())
		utils.dropAllExcept(utils.stringToIntList(config.items()), true, 100, 350);
	}

	public WidgetItem getFood() {
		WidgetItem item;
		item = utils.getInventoryWidgetItem(Collections.singletonList(config.foodID()));
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
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, ClassNotFoundException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NGatherer")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				state = null;
				int[] customTemp = utils.stringToIntArray(config.returnLoc());
				ResetLocation = new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
				botTimer = Instant.now();
			} else {
				reset();
			}
		}
	}
	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		}
	}
	private void lootItem(List<TileItem> itemList) {
		TileItem lootItem = getNearestTileItem(itemList);
		if (lootItem != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
	}
	private TileItem getNearestTileItem(List<TileItem> tileItems) {
		int currentDistance;
		TileItem closestTileItem = tileItems.get(0);
		int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
		for (TileItem tileItem : tileItems) {
			currentDistance = tileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
			if (currentDistance < closestDistance) {
				closestTileItem = tileItem;
				closestDistance = currentDistance;
			}
		}
		return closestTileItem;
	}
	public NGathererState getState()
	{
		if (timeout > 0)
		{
			return NGathererState.TIMEOUT;
		}
		if (client.getLocalPlayer().getAnimation() != -1 && !config.thieving()){
			return NGathererState.ANIMATING;
		}
		if (utils.isMoving() && !config.aerial()) {
			return NGathererState.ANIMATING;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		else {
			return getStates();
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

	private NGathererState getStates() {
		if (config.thieving()) {
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
			if (utils.inventoryContains(1935)) {
				return NGathererState.DROP_JUG;
			}
			if (config.shadowVeil() && !isVeiled && client.getVarbitValue(12414) == 0 && client.getVarbitValue(12291) == 0 && client.getGameState() == GameState.LOGGED_IN){
				return NGathererState.CAST_SV;
			}
			if (config.bank() && config.dodgynecks() && !utils.inventoryContains(21143)) {
				banked = false;
				return NGathererState.FIND_BANK2;
			}
			if (config.bank() && !utils.inventoryContains(config.foodID())) {
				banked = false;
				return NGathererState.FIND_BANK2;
			}
			if (config.bank() && utils.inventoryFull()) {
				banked = false;
				return NGathererState.FIND_BANK2;
			}
			if (config.dodgynecks() && utils.inventoryContains(ItemID.DODGY_NECKLACE) && !utils.isItemEquipped(Collections.singleton(ItemID.DODGY_NECKLACE))) {
				return NGathererState.EQUIP_NECKLACE;
			}
			if (utils.inventoryItemContainsAmount(22521, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22522, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22523, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22524, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22525, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22526, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22527, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22528, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22529, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22530, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22531, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22532, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22533, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22534, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22535, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22536, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22537, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(22538, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (utils.inventoryItemContainsAmount(24703, config.maxPouches(), true, false)){
				return NGathererState.OPEN_POUCH;
			}
			if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.minHealth() && utils.inventoryContains(config.foodID())) {
				return NGathererState.EAT_FOOD;
			}
			if (!config.bank() && utils.inventoryFull()) {
				return NGathererState.DROP_INV;
			}
			if (!utils.inventoryFull() && bs != null) {
				return NGathererState.PICKPOCKET;
			}
			if (!utils.inventoryFull() && bs == null) {
				return NGathererState.WALK_SECOND;
			}
			if (!config.bank() && !utils.inventoryContains(config.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < 15) {
				utils.sendGameMessage("HP TOO LOW AND BANK DISABLED!");
				return NGathererState.UNHANDLED_STATE;
			}
		}
		if (!config.thieving()) {
			if (!loot.isEmpty()) {
				return NGathererState.LOOT_ITEMS;
			}
			if (config.bank() && utils.inventoryFull() && !config.aerial()) {
				banked = false;
				return NGathererState.FIND_BANK2;
			}
			if (!config.bank() && utils.inventoryFull() && !config.aerial()) {
				return NGathererState.DROP_INV;
			}
			if (utils.inventoryFull() && config.aerial()) {
				startCutting = true;
			}
			if (config.aerial() && startCutting && !utils.inventoryContains(FISH)) {
				startCutting = false;
			}
			if (config.aerial() && startCutting && utils.inventoryContains(FISH)) {
				return NGathererState.CUT_FISH;
			}
			if (!utils.inventoryFull() && bs != null && config.typethief() == NGathererTypeee.NPC) {
					return NGathererState.PICKPOCKET;
			}
			if (!utils.inventoryFull() && bs2 != null && config.typethief() == NGathererTypeee.OBJECT) {
					return NGathererState.PICKPOCKET;
			}
		}
		return NGathererState.TIMEOUT;
	}
	private final Set<Integer> FISH = Set.of(ItemID.BLUEGILL, ItemID.COMMON_TENCH, ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN);
	private boolean banked = false;
	private boolean startCutting = false;
	private NGathererState getBankState()
	{
		if (!config.thieving()) {
			if (!banked) {
				utils.depositAllExcept(utils.stringToIntList(config.items()));
				banked = true;
				return NGathererState.DEPOSIT_ITEMS;
			}
			if (banked) {
				return NGathererState.WALK_SECOND;
			}
		}
		if (config.thieving()) {
			if (!banked) {
				utils.depositAll();
				banked = true;
				return NGathererState.DEPOSIT_ITEMS;
			}
			if (config.shadowVeil() && !utils.inventoryContains(564)) {
				return NGathererState.WITHDRAW_COSMIC;
			}
			if (config.dodgynecks() && !utils.inventoryContains(21143)) {
				return NGathererState.WITHDRAW_NECKLACES;
			}
			if (!utils.inventoryContains(config.foodID())) {
				return NGathererState.WITHDRAW_FOOD1;
			}
			if (banked && utils.inventoryContains(config.foodID())) {
				return NGathererState.WALK_SECOND;
			}
		}
		return NGathererState.TIMEOUT;
	}

	public void useWallObject(WallObject targetObject, long sleepDelay, int opcode)
	{
		if(targetObject!=null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), opcode, targetObject.getLocalLocation().getSceneX(), targetObject.getLocalLocation().getSceneY()));
		}
	}

	public WidgetItem getFish() {
		//ItemID.BLUEGILL, ItemID.COMMON_TENCH, ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN
		return utils.getInventoryWidgetItem(utils.stringToIntList("22826,22829,22832,22835"));
	}

	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		if (!startTeaks){
			return;
		}
		bs = utils.findNearestNpc(config.npcID());
		bs2 = utils.findNearestGameObject(config.objID());
		beast = utils.getFirstNPCWithLocalTarget();
		player = client.getLocalPlayer();

		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (!started) {
			if (utils.utilga() >7) {
				started = true;
			}
			startTeaks = false;
			return;
		}
		if (client != null && player != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					//utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
					timeout = tickDelay();
					break;
				case LOOT_ITEMS:
					lootItem(loot);
					break;
				case CUT_FISH:
					utils.useItem(ItemID.KNIFE,"use");
					//utils.getInventoryWidgetItem()
					//clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.KNIFE, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(ItemID.KNIFE)).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", getFish().getId(), MenuAction.ITEM_USE_ON_ITEM.getId(), getFish().getIndex(), WidgetInfo.INVENTORY.getId()));
					break;
				case DROP_INV:
					handleDropItems();
					break;
				case CAST_SV:
					veilTimer = Instant.now();
					clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14287025));
					isVeiled = true;
					timeout = tickDelay();
					break;
				case DROP_JUG:
					utils.useItem(1935,"drop");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 1935, MenuAction.ITEM_FIFTH_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(1935)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case WALK_FIRST:
					utils.walk(walkzone);
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					utils.walk(ResetLocation);
					timeout = tickDelay();
					break;
				case OPEN_POUCH:
					if (utils.inventoryContains(22521)) {
						utils.useItem(22521,"open-all");
					}
					if (utils.inventoryContains(22522)) {
						utils.useItem(22522,"open-all");
					}
					if (utils.inventoryContains(22523)) {
						utils.useItem(22523,"open-all");
					}
					if (utils.inventoryContains(22524)) {
						utils.useItem(22524,"open-all");
					}
					if (utils.inventoryContains(22525)) {
						utils.useItem(22525,"open-all");
					}
					if (utils.inventoryContains(22526)) {
						utils.useItem(22526,"open-all");
					}
					if (utils.inventoryContains(22527)) {
						utils.useItem(22527,"open-all");
					}
					if (utils.inventoryContains(22528)) {
						utils.useItem(22528,"open-all");
					}
					if (utils.inventoryContains(22529)) {
						utils.useItem(22529,"open-all");
					}
					if (utils.inventoryContains(22530)) {
						utils.useItem(22530,"open-all");
					}
					if (utils.inventoryContains(22531)) {
						utils.useItem(22531,"open-all");
					}
					if (utils.inventoryContains(22532)) {
						utils.useItem(22532,"open-all");
					}
					if (utils.inventoryContains(22533)) {
						utils.useItem(22533,"open-all");
					}
					if (utils.inventoryContains(22534)) {
						utils.useItem(22534,"open-all");
					}
					if (utils.inventoryContains(22535)) {
						utils.useItem(22535,"open-all");
					}
					if (utils.inventoryContains(22536)) {
						utils.useItem(22536,"open-all");
					}
					if (utils.inventoryContains(22537)) {
						utils.useItem(22537,"open-all");
					}
					if (utils.inventoryContains(22538)) {
						utils.useItem(22538,"open-all");
					}
					if (utils.inventoryContains(24703)) {
						utils.useItem(24703,"open-all");
					}
					timeout = tickDelay();
					break;
				case OPEN_DOOR:
					banked = false;
					WallObject DOOR = utils.findNearestWallObject(36253);
					useWallObject(DOOR, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case PICKPOCKET:
					banked = false;
					if (config.typethief() == NGathererTypeee.NPC) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", bs.getIndex(), config.type().action.getId(), 0, 0));
					}
					if (config.typethief() == NGathererTypeee.OBJECT) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", bs2.getId(), config.type().action.getId(), bs2.getSceneMinLocation().getX(), bs2.getSceneMinLocation().getY()));
					}
					timeout = tickDelay();
					break;
				case EQUIP_NECKLACE:
					utils.useItem(ItemID.DODGY_NECKLACE,"equip","wear","wield");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 21143, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(21143)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case DEACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					timeout = tickDelay();
					break;
				case ACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case WITHDRAW_COSMIC:
					utils.withdrawAllItem(564);
					timeout = 4;
					break;
				case WITHDRAW_NECKLACES:
					utils.withdrawItemAmount(21143, config.dodgyNecks());
					timeout = 4;
					break;
				case WITHDRAW_HOUSE:
					utils.withdrawItemAmount(8013, 5);
					timeout = 4;
					break;
				case WITHDRAW_FOOD1:
					utils.withdrawItemAmount(config.foodID(), config.foodAmount());
					timeout = 4;
					break;
				case EAT_FOOD:
					WidgetItem food = getFood();
					if (food != null) {
						utils.useItem(food.getId(),"eat","drink");
					}
					break;
				case FIND_BANK:
					openBank();
					timeout = tickDelay();
					break;
				case FIND_BANK2:
					GameObject bank = utils.findNearestGameObject(config.bankID());
					utils.useGameObjectDirect(bank, sleepDelay(), config.type2().action.getId());
					timeout = tickDelay();
					break;
			}
		}
	}

	List<String> lootableItems = new ArrayList<>();

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		if (!startTeaks) {
			return;
		}
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {             // || client.getItemDefinition(event.getItem().getId()).getName() == "Dragon bones" || client.getItemDefinition(event.getItem().getId()).getName() == "Draconic visage") {
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
	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(false, 200, 300, 100, 250);
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}
}