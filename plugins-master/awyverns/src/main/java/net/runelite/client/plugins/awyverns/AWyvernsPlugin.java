package net.runelite.client.plugins.awyverns;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "AWyverns",
	description = "Anarchise' Auto Wyverns",
	tags = {"anarchise","wyverns","aplugins"},
	enabledByDefault = false
)
public class AWyvernsPlugin extends Plugin
{

	private int nextRestoreVal = 0;

	@Inject
	private Client client;

	@Provides
	AWyvernsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AWyvernsConfig.class);
	}

	@Inject
	private AWyvernsConfig configph;

	@Inject
	private ClientThread clientThread;


	private Rectangle bounds;

	private int timeout;

	boolean FirstWalk = true;

	private final Map<LocalPoint, Integer> projectilesMap = new HashMap<LocalPoint, Integer>();
	private final Map<GameObject, Integer> toxicCloudsMap = new HashMap<GameObject, Integer>();

	private int lastAttackTick = -1;

	LocalPoint standPos;
	private WorldPoint lastLocation = new WorldPoint(0, 0, 0);

	@Getter(AccessLevel.PACKAGE)
	private final List<WorldPoint> obstacles = new ArrayList<>();

	@Getter
	private final Map<LocalPoint, Projectile> poisonProjectiles = new HashMap<>();

	@Nullable
	private NPC nm;
	@Inject
	private ItemManager itemManager;

	@Inject
	private PUtils utils;

	private boolean inFight;
	private boolean cursed;
	private Prayer prayerToClick;
	private Random r = new Random();
	public AWyvernsPlugin(){
		inFight = false;
	}

	List<TileItem> loot = new ArrayList<>();
	List<String> lootableItems = new ArrayList<>();
	List<String> withdrawList = new ArrayList<>();
	String[] list;
	String[] Loot;
	private Prayer prayer;
	@Inject
	private KeyManager keyManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private OverlayManager overlayManager;

	@Inject
	AWyvernsOverlay zulrahOverlay;
	Instant botTimer;
	private NPC zulrahNpc = null;
	public AWyvernsState state;
	private int stage = 0;
	private int phaseTicks = -1;
	private int attackTicks = -1;
	private int totalTicks = 0;
	private static boolean flipStandLocation = false;
	private static boolean flipPhasePrayer = false;
	private static boolean zulrahReset = false;
	private final Collection<NPC> snakelings = new ArrayList<NPC>();
	public static final BufferedImage[] ZULRAH_IMAGES = new BufferedImage[3];
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, ClassNotFoundException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("awyverns")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				overlayManager.add(zulrahOverlay);
				botTimer = Instant.now();
			}
			else {
				startTeaks = false;
				reset();
			}
		}
	}
	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	private void reset() throws IOException, ClassNotFoundException {
		if (!started) {
			if (utils.util() >=6) {
				started = true;
			}
		}
		loot.clear();
		lootableItems.clear();
		withdrawList.clear();
		Loot = configph.lootNames().toLowerCase().split("\\s*,\\s*");
		if (!configph.lootNames().isBlank()) {
			lootableItems.addAll(Arrays.asList(Loot));
		}
		banked = false;
		zulrahNpc = null;
		stage = 0;
		phaseTicks = -1;
		attackTicks = -1;
		totalTicks = 0;
		leavingcave = false;
		projectilesMap.clear();
		toxicCloudsMap.clear();
		flipStandLocation = false;
		flipPhasePrayer = false;
		zulrahReset = false;

		lastAttackTick = -1;
		inFight = false;
		prayerToClick = null;
		state = null;
		botTimer = null;
		overlayManager.remove(zulrahOverlay);
	}
	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	private void resetZul() {
		zulrahNpc = null;
		stage = 0;
		phaseTicks = -1;
		attackTicks = -1;
		totalTicks = 0;
		leavingcave = false;
		lastAttackTick = -1;
		inFight = false;
		prayerToClick = null;
		banked = false;
	}

	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);
	private static boolean isInPOH(Client client) {return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);}

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
	public AWyvernsState getState()
	{
		if (timeout > 0)
		{
			return AWyvernsState.TIMEOUT;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		else {
			return getStates();
		}
	}
	Player player;

	WorldArea ZULRAH_BOAT = new WorldArea(new WorldPoint(2192, 3045, 0), new WorldPoint(2221, 3068, 0));

	WorldArea LITHKREN_VAULT = new WorldArea(new WorldPoint(3535, 10438, 0), new WorldPoint(3566, 10472, 0));

	WorldArea LITHKREN_BUILDING = new WorldArea(new WorldPoint(3552, 3996, 0), new WorldPoint(3557, 4006, 0));
	WorldArea LITHKREN_BUILDING_UP = new WorldArea(new WorldPoint(3552, 3996, 1), new WorldPoint(3557, 4006, 1));
	WorldArea LITHKREN_OUTSIDE = new WorldArea(new WorldPoint(3560, 4000, 0), new WorldPoint(3566,4009,0));
	WorldArea LITHKREN_OUTSIDE_2 = new WorldArea(new WorldPoint(3569, 3981, 0), new WorldPoint(3588,4001,0));
	WorldPoint LITHKREN_POS = new WorldPoint(3579, 3991, 0);
	WorldArea FOSSIL_ISLAND = new WorldArea(new WorldPoint(3649, 3835, 0), new WorldPoint(3685, 3865, 0));
	WorldArea INSIDE_CAVE = new WorldArea(new WorldPoint(3604, 10288, 0), new WorldPoint(3609, 10295, 0));
	WorldArea INSIDE_CAVE2 = new WorldArea(new WorldPoint(3589, 10280, 0), new WorldPoint(3605, 10295, 0));
	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));
	LocalPoint standPos1;
	NPC beast;
	WorldArea FOSSIL_ISLAND_BANK = new WorldArea(new WorldPoint(3731, 3798, 0), new WorldPoint(3750, 3813, 0));
	WorldArea INSIDE_CAVE3 = new WorldArea(new WorldPoint(3597, 10224, 0), new WorldPoint(3611, 10238, 0));
	WorldArea OUTSIDE_CAVE = new WorldArea(new WorldPoint(3740, 3772, 0), new WorldPoint(3753, 3783, 0));
	boolean leavingcave = false;

	private AWyvernsState getStates(){
		NPC bs = utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, Collections.singleton(configph.type().npcID));

		if (!configph.taskCave()) {
			if (player.getWorldArea().intersectsWith(FOSSIL_ISLAND_BANK) && !banked) {
				return AWyvernsState.FIND_BANK2;
			}
			if (player.getWorldArea().intersectsWith(FOSSIL_ISLAND_BANK) && banked) {
				utils.useGameObject(30869, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldArea().intersectsWith(INSIDE_CAVE3) && !leavingcave) {
				banked = false;
				utils.walk(configph.type().worldPoint);
				if (client.getVar(Varbits.QUICK_PRAYER) == 0) {
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
				}
			}
			if (player.getWorldArea().intersectsWith(OUTSIDE_CAVE) && !banked) {
				return AWyvernsState.FIND_BANK2;
			}
			if (!loot.isEmpty() && !utils.inventoryFull() && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.LOOT_ITEMS;
			}
			if (utils.inventoryContains(configph.foodID()) && utils.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.EAT_FOOD;
			}
			if (player.getWorldArea().intersectsWith(INSIDE_CAVE3) && leavingcave) {
				return AWyvernsState.LEAVE_CAVE; //
			}
			if (getRestoreItem() == null && utils.findNearestNpc(configph.type().npcID) != null) {
				leavingcave = true;
				return AWyvernsState.LEAVE_CAVE;
			}
			if (!utils.inventoryContains(configph.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < configph.hptoLeave() && utils.findNearestNpc(configph.type().npcID) != null) {
				leavingcave = true;
				return AWyvernsState.LEAVE_CAVE;
			}
			if (client.getVar(Varbits.QUICK_PRAYER) != 0 && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 10, Collections.singleton(configph.type().npcID)) == null) {
				return AWyvernsState.DEACTIVATE_PRAY;
			}
			if (client.getVar(Varbits.QUICK_PRAYER) == 0 && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 10, Collections.singleton(configph.type().npcID)) != null) {
				clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE3) && !leavingcave && !utils.isMoving() && client.getLocalPlayer().getInteracting() == null && beast.getId() == configph.type().npcID && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, Collections.singleton(configph.type().npcID)) != null) {
				return AWyvernsState.ATTACK_RETAL; //
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE3) && !leavingcave && !utils.isMoving() && bs.getInteracting() == client.getLocalPlayer() && client.getLocalPlayer().getInteracting() == null) {
				return AWyvernsState.ATTACK; //
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE3) && !leavingcave && !utils.isMoving() && bs.getInteracting() == null && client.getLocalPlayer().getInteracting() == null) {
				return AWyvernsState.ATTACK; //
			}
		}

		if (configph.taskCave()) {
			if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && !banked) {
				return AWyvernsState.FIND_BANK;
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_VAULT)) {
				utils.useGameObject(32112, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
				banked = false;
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_BUILDING)) {
				utils.useGameObject(32083, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_BUILDING_UP)) {
				utils.useGameObject(32082, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_OUTSIDE) && utils.findNearestGameObject(32079) != null) {
				utils.useGameObject(32079, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_OUTSIDE_2) && utils.findNearestGameObject(32079) != null) {
				utils.useGameObject(32079, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldArea().intersectsWith(LITHKREN_OUTSIDE) && utils.findNearestGameObject(32079) == null) {
				utils.walk(LITHKREN_POS);
			}
			if (player.getWorldArea().intersectsWith(FOSSIL_ISLAND)) {
				utils.useGameObject(30842, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (player.getWorldLocation().equals(new WorldPoint(3595, 10291, 0))) { // Down the rope
				utils.useGameObject(31485, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}

			if (player.getWorldArea().intersectsWith(INSIDE_CAVE)) {
				utils.walk(configph.type().worldPoint);
				if (client.getVar(Varbits.QUICK_PRAYER) == 0) {
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
				}
			}
			if (player.getWorldLocation().equals(new WorldPoint(3633, 10264, 0))) { // up the steps
				utils.useGameObject(30847, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			}
			if (!loot.isEmpty() && !utils.inventoryFull() && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.LOOT_ITEMS;
			}
			if (utils.inventoryContains(configph.foodID()) && utils.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.EAT_FOOD;
			}
			if (getRestoreItem() == null && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.WALK_SECOND;
			}
			if (!utils.inventoryContains(configph.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < configph.hptoLeave() && utils.findNearestNpc(configph.type().npcID) != null) {
				return AWyvernsState.WALK_SECOND;
			}
			if (client.getVar(Varbits.QUICK_PRAYER) != 0 && isInPOH(client)) {
				return AWyvernsState.DEACTIVATE_PRAY;
			}
			if (client.getVar(Varbits.QUICK_PRAYER) == 0 && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 10, Collections.singleton(configph.type().npcID)) != null) {
				clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}
			if (isInPOH(client) && client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER) && configph.usePOHPool()) {
				return AWyvernsState.DRINK_POOL;
			}
			if (isInPOH(client) && !banked) {
				return AWyvernsState.TELE_EDGE;
			}
			if (isInPOH(client) && banked) {
				return AWyvernsState.TELE_LITHKREN;
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE2) && !utils.isMoving() && client.getLocalPlayer().getInteracting() == null && beast.getId() == configph.type().npcID && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, Collections.singleton(configph.type().npcID)) != null) {
				return AWyvernsState.ATTACK_RETAL; //
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE2) && !utils.isMoving() && bs.getInteracting() == client.getLocalPlayer() && client.getLocalPlayer().getInteracting() == null) {
				return AWyvernsState.ATTACK; //
			}
			if (!player.getWorldArea().intersectsWith(INSIDE_CAVE2) && !utils.isMoving() && bs.getInteracting() == null && client.getLocalPlayer().getInteracting() == null) {
				return AWyvernsState.ATTACK; //
			}
		}
		return AWyvernsState.TIMEOUT;
	}
	private boolean banked = false;

	private AWyvernsState getBankState()
	{

		if (!banked){
			utils.depositAll();
			leavingcave = false;
			banked = true;
			return AWyvernsState.DEPOSIT_ITEMS;
		}
		if (!utils.inventoryContains(8013) && configph.taskCave()){
			return AWyvernsState.WITHDRAW_HOUSE;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() &&configph.imbuedheart() && !utils.inventoryContains(20724)){
			return AWyvernsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() && configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(23745)){
			return AWyvernsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() && !configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(3040)){
			return AWyvernsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.RANGED && configph.supers() && !utils.inventoryContains(24635)){
			return AWyvernsState.WITHDRAW_RANGED;
		}
		if (configph.typecb() == CombatType.RANGED && !configph.supers() && !utils.inventoryContains(2444)){
			return AWyvernsState.WITHDRAW_RANGED;
		}
		if (configph.typecb() == CombatType.MELEE && !configph.supers() && !utils.inventoryContains(9739)){
			return AWyvernsState.WITHDRAW_COMBAT;
		}
		if (configph.typecb() == CombatType.MELEE && configph.supers() && !utils.inventoryContains(23685)){
			return AWyvernsState.WITHDRAW_COMBAT;
		}

		if (!configph.useRestores() && !utils.inventoryContains(2434)){
			return AWyvernsState.WITHDRAW_RESTORES;
		}
		if (configph.useRestores() && !utils.inventoryContains(3024)){
			return AWyvernsState.WITHDRAW_RESTORES;
		}
		if (!utils.inventoryContains(configph.foodID())){
			return AWyvernsState.WITHDRAW_FOOD1;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(configph.foodID()) && banked){
			return AWyvernsState.WALK_SECOND;
		}
		if (player.getWorldArea().intersectsWith(FOSSIL_ISLAND_BANK) && utils.inventoryContains(configph.foodID()) && banked){
			utils.useGameObject(30869, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}
		return AWyvernsState.TIMEOUT;
	}
	public boolean startTeaks = false;


	@Inject ConfigManager configManager;

	public Prayer currentPrayer;
	private boolean started = false;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		if (!startTeaks){
			return;
		}
		if (!started) {
			if (utils.util() >=6) {
				started = true;
			}
			return;
		}
		beast = utils.getFirstNPCWithLocalTarget();
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			if (!client.isResized()) {
				utils.sendGameMessage("client must be set to resizable mode");
				return;
			}
			state = getState();
			switch (state) {
				case TIMEOUT:
					///utils.handleRun(30, 20);
					timeout--;
					break;
				case CONTINUE2:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15007746));
					break;
				case FAIRY_RING:
					GameObject ring = utils.findNearestGameObject(40779);
					utils.useGameObjectDirect(ring, sleepDelay(), MenuAction.GAME_OBJECT_FOURTH_OPTION.getId());
					break;
				case ATTACK_RETAL:
					if (configph.typecb() == CombatType.MELEE && client.getBoostedSkillLevel(Skill.STRENGTH) <= client.getRealSkillLevel(Skill.STRENGTH)) {
						WidgetItem Cpot = GetCombatItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					if (configph.typecb() == CombatType.RANGED && client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED)) {
						WidgetItem Cpot = GetRangedItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					if (configph.typecb() == CombatType.MAGIC && client.getBoostedSkillLevel(Skill.MAGIC) <= client.getRealSkillLevel(Skill.MAGIC)) {
						WidgetItem Cpot = GetMagicItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					utils.attackNPCDirect(beast);
					timeout = tickDelay();
					break;
				case ATTACK:
					if (configph.typecb() == CombatType.MELEE && client.getBoostedSkillLevel(Skill.STRENGTH) <= client.getRealSkillLevel(Skill.STRENGTH)) {
						WidgetItem Cpot = GetCombatItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					if (configph.typecb() == CombatType.RANGED && client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED)) {
						WidgetItem Cpot = GetRangedItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					if (configph.typecb() == CombatType.MAGIC && client.getBoostedSkillLevel(Skill.MAGIC) <= client.getRealSkillLevel(Skill.MAGIC)) {
						WidgetItem Cpot = GetMagicItem();
						if (Cpot != null) {
							clientThread.invoke(() ->
									client.invokeMenuAction(
											"Drink",
											"<col=ff9040>Potion",
											Cpot.getId(),
											MenuAction.CC_OP.getId(),
											Cpot.getIndex(),
											WidgetInfo.INVENTORY.getId()
									)
							);
						}
					}
					NPC npc = utils.findNearestNpc(configph.type().npcID);
					utils.attackNPCDirect(npc);
					timeout = tickDelay();
					break;
				case TELE_LITHKREN:
					utils.useDecorativeObject(33418, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case CONTINUE:
					//utils.typeString("1");
					//utils.pressKey(VK_SPACE);
					clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 14352385));
					//timeout = tickDelay();
					break;
				case WALK_SAFE:
					//if (!utils.isMoving()) {
						utils.walk(standPos);
					//}
					timeout = 2;
					break;
				case DEACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					timeout = tickDelay();
					break;
				case ACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case WALK_FIRST:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 12938, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(12938)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					//resetZul();
					break;
				case LEAVE_CAVE:
					utils.useGameObject(30878, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					//resetZul();
					clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8013)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case TELE_EDGE:
					//resetZul();
					utils.useDecorativeObject(13523, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case DRINK_POOL:
					//resetZul();
					GameObject Pool = utils.findNearestGameObject(29240, 29241);
					utils.useGameObjectDirect(Pool, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case WITHDRAW_COMBAT:
					if (!configph.supers()){
						utils.withdrawItem(9739);
					}
					if (configph.supers()){
						utils.withdrawItem(23685);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_RANGED:
					if (!configph.supers()){
						utils.withdrawItem(2444);
					}
					if (configph.supers()){
						utils.withdrawItem(24635);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_MAGIC:
					if (!configph.supers()&& !configph.imbuedheart()){
						utils.withdrawItem(3040);
					}
					if (configph.supers() && !configph.imbuedheart()){
						utils.withdrawItem(23745);
					}
					if (configph.imbuedheart()){
						utils.withdrawItem(20724);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_RESTORES:
					if (!configph.useRestores()){
						utils.withdrawItemAmount(2434, configph.praypotAmount());
					}
					if (configph.useRestores()){
						utils.withdrawItemAmount(3024, configph.praypotAmount());
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_TELES:
					//utils.withdrawItemAmount(12938, 10); //zul andra tele
					utils.withdrawItem(12938);
					timeout = tickDelay();
					break;
				case WITHDRAW_HOUSE:
					utils.withdrawItemAmount(8013, 5); //house tabs TODO
					//utils.withdrawItem(8013);
					timeout = tickDelay();
					break;
				case WITHDRAW_FOOD1:
					utils.withdrawItemAmount(configph.foodID(), configph.foodAmount());
					timeout = tickDelay();
					break;
				//case MOVING:
					//utils.handleRun(30, 20);
					//timeout = tickDelay();
					//break;
				case DRINK_ANTIVENOM:
					WidgetItem ven = GetAntiVenomItem();
					if (ven != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										ven.getId(),
										MenuAction.CC_OP.getId(),
										ven.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					//timeout = tickDelay();
					break;
				case DRINK_MAGIC:
					WidgetItem Cpot = GetMagicItem();
					if (Cpot != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Cpot.getId(),
										MenuAction.CC_OP.getId(),
										Cpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case EAT_FOOD:
					WidgetItem food = GetFoodItem();
					if (food != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"",
										"",
										food.getId(),
										MenuAction.CC_OP.getId(),
										food.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case DRINK_RANGE:
					WidgetItem Rpot = GetRangedItem();
					if (Rpot != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Rpot.getId(),
										MenuAction.CC_OP.getId(),
										Rpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					//timeout = tickDelay();
					break;
				case USE_BOAT:
					GameObject boat = utils.findNearestGameObject(10068);
					utils.useGameObjectDirect(boat, 100, MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case FIND_BANK:
					//resetZul();
					openBank();
					timeout = tickDelay();
					break;
				case FIND_BANK2:
					leavingcave = false;
					utils.useGameObject(31427, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					//utils.depositAll();
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
					timeout = tickDelay();
					break;
				case LOOT_ITEMS:
					lootItem(loot);
					break;

			}
		}
	}

	public int getBoostAmount(WidgetItem restoreItem, int prayerLevel)
	{
		if (PrayerRestoreType.PRAYER_POTION.containsId(restoreItem.getId()))
		{
			return 7 + (int) Math.floor(prayerLevel * .25);
		}
		else if (PrayerRestoreType.SANFEW_SERUM.containsId(restoreItem.getId()))
		{
			return 4 + (int) Math.floor(prayerLevel * (double)(3 / 10));
		}
		else if (PrayerRestoreType.SUPER_RESTORE.containsId(restoreItem.getId()))
		{
			return 8 + (int) Math.floor(prayerLevel * .25);
		}

		return 0;
	}

	private int calculateHealth(NPC target, Integer maxHealth)
	{
		// Based on OpponentInfoOverlay HP calculation & taken from the default slayer plugin
		if (target == null || target.getName() == null)
		{
			return -1;
		}

		final int healthScale = target.getHealthScale();
		final int healthRatio = target.getHealthRatio();
		//maxHealth = 750;

		if (healthRatio < 0 || healthScale <= 0 || maxHealth == null)
		{
			return -1;
		}

		return (int)((maxHealth * healthRatio / healthScale) + 0.5f);
	}


	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();

		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains) && item.getId() != 1751) {             // || client.getItemDefinition(event.getItem().getId()).getName() == "Dragon bones" || client.getItemDefinition(event.getItem().getId()).getName() == "Draconic visage") {
			loot.add(item);
		}
	}

	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		loot.remove(event.getItem());
	}

	public WidgetItem getRestoreItem() {
		WidgetItem item;

		item = PrayerRestoreType.PRAYER_POTION.getItemFromInventory(client);

		if (item != null) {
			return item;
		}

		item = PrayerRestoreType.SANFEW_SERUM.getItemFromInventory(client);

		if (item != null) {
			return item;
		}

		item = PrayerRestoreType.SUPER_RESTORE.getItemFromInventory(client);

		return item;
	}

	int[] ItemIDs;

	public WidgetItem GetFoodItem() {
		WidgetItem item;

		item = utils.getInventoryWidgetItem(Collections.singletonList(configph.foodID()));

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public WidgetItem GetCombatItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.COMBAT.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public WidgetItem GetRangedItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.RANGED.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public WidgetItem GetMagicItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.MAGIC.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public WidgetItem GetAntifireItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.ANTIFIRE.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public WidgetItem GetAntiVenomItem() {

		WidgetItem item;

		item = PrayerRestoreType.ANTIVENOM.getItemFromInventory(client);

		if (item != null) {
			return item;
		}

		return item;
	}

	public String getTag(int itemId)
	{
		String tag = configManager.getConfiguration("inventorytags", "item_" + itemId);
		if (tag == null || tag.isEmpty())
		{
			return "";
		}

		return tag;
	}

	public void activatePrayer(WidgetInfo widgetInfo)
	{
		Widget prayer_widget = client.getWidget(widgetInfo);

		if (prayer_widget == null)
		{
			return;
		}

		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return;
		}

		clientThread.invoke(() ->
				client.invokeMenuAction(
				"Activate",
						prayer_widget.getName(),
						1,
						MenuAction.CC_OP.getId(),
						prayer_widget.getItemId(),
						prayer_widget.getId()
				)
		);
	}

	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(configph.sleepWeightedDistribution(), configph.sleepMin(), configph.sleepMax(), configph.sleepDeviation(), configph.sleepTarget());
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(configph.tickDelayWeightedDistribution(), configph.tickDelayMin(), configph.tickDelayMax(), configph.tickDelayDeviation(), configph.tickDelayTarget());
		return tickLength;
	}
}