package net.runelite.client.plugins.arunedragons;

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
import net.runelite.api.queries.NPCQuery;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "ARuneDragons",
	description = "Anarchise' Rune Dragons",
	tags = {"anarchise","dragons","aplugins"},
	enabledByDefault = false
)
public class ARuneDragonsPlugin extends Plugin
{

	private int nextRestoreVal = 0;

	@Inject
	private Client client;

	@Provides
	ARuneDragonsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ARuneDragonsConfig.class);
	}
	@Inject
	private ARuneDragonsConfig configph;

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
	public ARuneDragonsPlugin(){
		inFight = false;
	}

	@Nullable
	public NPC getFirstAttackable(int id)
	{
			assert client.isClientThread();

			if (client.getLocalPlayer() == null)
			{
				return null;
			}

			return new NPCQuery()
					.idEquals(id)
					.filter(npc -> npc.getInteracting() == null)
					.result(client)
					.nearestTo(client.getLocalPlayer());


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
	ARuneDragonsOverlay zulrahOverlay;
	Instant botTimer;
	private NPC zulrahNpc = null;
	public ARuneDragonsState state;
	private int stage = 0;
	private int phaseTicks = -1;
	private int attackTicks = -1;
	private int totalTicks = 0;
	private static boolean flipStandLocation = false;
	private static boolean flipPhasePrayer = false;
	private static boolean zulrahReset = false;
	private final Collection<NPC> snakelings = new ArrayList<NPC>();
	public static final BufferedImage[] ZULRAH_IMAGES = new BufferedImage[3];

	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	private void reset() throws IOException, ClassNotFoundException {
		if (!started) {
			if (utils.utilru() >=7) {
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
		startTeaks = false;
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

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, ClassNotFoundException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("arunedragons")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				botTimer = Instant.now();
				state = null;
				loot.clear();
				overlayManager.add(zulrahOverlay);
			} else {
				reset();
			}
		}
	}

	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);
	private static boolean isInPOH(Client client) {return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);}

	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
			/*targetMenu = new MenuEntry("", "", bankTarget.getId(),
					utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(),
					bankTarget.getSceneMinLocation().getY(), false);
			//utils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(bankTarget.getConvexHull().getBounds(), sleepDelay());*/
		}
	}
	private void lootItem(List<TileItem> itemList) {
		TileItem lootItem = getNearestTileItem(itemList);
		if (lootItem != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));

			/*targetMenu = new MenuEntry("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(),
					lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY(), false);
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(lootItem.getTile().getItemLayer().getCanvasTilePoly().getBounds(), 100);*/
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
	public ARuneDragonsState getState()
	{
		if (timeout > 0)
		{
			return ARuneDragonsState.TIMEOUT;
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
	WorldArea LITHKREN_BUILDING = new WorldArea(new WorldPoint(3547, 10470, 0), new WorldPoint(3552, 10476, 0));
	WorldArea OUTSIDE_DRAGONS = new WorldArea(new WorldPoint(1562, 5055, 0), new WorldPoint(1573, 5080, 0));
	WorldArea INSIDE_DRAGONS = new WorldArea(new WorldPoint(1575, 5058, 0), new WorldPoint(1601, 5087, 0));

	WorldArea DRAGON_SPOT = new WorldArea(new WorldPoint(1574, 5071, 0), new WorldPoint(1578, 5077, 0));

	WorldPoint DRAGON_WALK = new WorldPoint(1587, 5076, 0);

	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));
	LocalPoint standPos1;
	NPC beast;
	boolean leavingcave = false;
	int l = 999;
	private ARuneDragonsState getStates() {
		NPC bs = utils.findNearestNpc(8031);

		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && !banked) {
			return ARuneDragonsState.FIND_BANK;
		}
		if (getRestoreItem() == null && utils.findNearestNpc(8031) != null) {
			return ARuneDragonsState.WALK_SECOND;
		}
		if (!utils.inventoryContains(configph.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < configph.hptoLeave() && utils.findNearestNpc(8031) != null) {
			return ARuneDragonsState.WALK_SECOND;
		}
		if (player.getWorldArea().intersectsWith(LITHKREN_VAULT)) {
			utils.useGameObject(32113, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
			banked = false;
		}
		if (player.getWorldArea().intersectsWith(LITHKREN_BUILDING)) {
			utils.useGameObject(32117, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}
		if (player.getWorldArea().intersectsWith(OUTSIDE_DRAGONS)) {
			if (client.getVar(Varbits.QUICK_PRAYER) == 0) {
				clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}
			utils.useGameObject(32153, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}
		if (player.getWorldLocation().equals(new WorldPoint(3633, 10264, 0))) { // up the steps
			utils.useGameObject(30847, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && configph.superantifires() && client.getVarbitValue(6101) == 0){
			return ARuneDragonsState.DRINK_ANTIFIRE;
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && !configph.superantifires() && client.getVarbitValue(3981) == 0){
			return ARuneDragonsState.DRINK_ANTIFIRE;
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && !loot.isEmpty() && !utils.inventoryFull() && utils.findNearestNpc(8031) != null) {
			return ARuneDragonsState.LOOT_ITEMS;
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && utils.inventoryContains(configph.foodID()) && utils.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && utils.findNearestNpc(8031) != null) {
			return ARuneDragonsState.EAT_FOOD;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) != 0 && isInPOH(client)) {
			return ARuneDragonsState.DEACTIVATE_PRAY;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) == 0 && utils.findNearestNpc((8031)) != null) {
			clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
		}
		if (isInPOH(client) && client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER) && configph.usePOHPool()) {
			return ARuneDragonsState.DRINK_POOL;
		}
		if (isInPOH(client) && !banked) {
			return ARuneDragonsState.TELE_EDGE;
		}
		if (isInPOH(client) && banked) {
			return ARuneDragonsState.TELE_LITHKREN;
		}
		if (player.getWorldArea().intersectsWith(DRAGON_SPOT)) {
			utils.walk(DRAGON_WALK);
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && client.getLocalPlayer().getInteracting() == null && beast.getId() == 8031) {
			return ARuneDragonsState.ATTACK_RETAL; //
		}
		if (player.getWorldArea().intersectsWith(INSIDE_DRAGONS) && client.getLocalPlayer().getInteracting() == null && utils.getFirstNPCWithLocalTarget() == null) {
			return ARuneDragonsState.ATTACK; //
		}

		return ARuneDragonsState.TIMEOUT;
	}
	private boolean banked = false;

	private ARuneDragonsState getBankState()
	{

		if (!banked){
			utils.depositAll();
			leavingcave = false;
			banked = true;
			return ARuneDragonsState.DEPOSIT_ITEMS;
		}
		if (!utils.inventoryContains(8013)){
			return ARuneDragonsState.WITHDRAW_HOUSE;
		}
		if (!configph.superantifires() && !utils.inventoryContains(11951)) {
			return ARuneDragonsState.WITHDRAW_ANTIFIRE;
		}
		if (configph.superantifires() && !utils.inventoryContains(22209)) {
			return ARuneDragonsState.WITHDRAW_ANTIFIRE;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() &&configph.imbuedheart() && !utils.inventoryContains(20724)){
			return ARuneDragonsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() && configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(23745)){
			return ARuneDragonsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.MAGIC && !configph.nomagepots() && !configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(3040)){
			return ARuneDragonsState.WITHDRAW_MAGIC;
		}
		if (configph.typecb() == CombatType.RANGED && configph.supers() && !utils.inventoryContains(24635)){
			return ARuneDragonsState.WITHDRAW_RANGED;
		}
		if (configph.typecb() == CombatType.RANGED && !configph.supers() && !utils.inventoryContains(2444)){
			return ARuneDragonsState.WITHDRAW_RANGED;
		}
		if (configph.typecb() == CombatType.MELEE && !configph.supers() && !utils.inventoryContains(9739)){
			return ARuneDragonsState.WITHDRAW_COMBAT;
		}
		if (configph.typecb() == CombatType.MELEE && configph.supers() && !utils.inventoryContains(23685)){
			return ARuneDragonsState.WITHDRAW_COMBAT;
		}

		if (!configph.useRestores() && !utils.inventoryContains(2434)){
			return ARuneDragonsState.WITHDRAW_RESTORES;
		}
		if (configph.useRestores() && !utils.inventoryContains(3024)){
			return ARuneDragonsState.WITHDRAW_RESTORES;
		}
		if (!utils.inventoryContains(configph.foodID())){
			return ARuneDragonsState.WITHDRAW_FOOD1;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(configph.foodID()) && banked){
			return ARuneDragonsState.WALK_SECOND;
		}
		/*if (player.getWorldArea().intersectsWith(FOSSIL_ISLAND_BANK) && utils.inventoryContains(configph.foodID()) && banked){
			utils.useGameObject(30869, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}*/
		return ARuneDragonsState.TIMEOUT;
	}
	public boolean startTeaks = false;


	@Inject ConfigManager configManager;

	public Prayer currentPrayer;

	private boolean url(String key) throws IOException {
		String webSite = "http://51.195.151.145/lmiuyr.txt";
		URL url = new URL(webSite);
		List<String> strings = null;
		InputStream is = url.openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		String message = new String();
		final StringBuffer buffer = new StringBuffer(2048);
		while ((line = rd.readLine()) != null) {
			message += line;
		}
		if (message.contains(key) && key.length() > 15) {
			is.close();
			return true;
		} else {
			is.close();
			return false;
		}
	}
	private int kkk = 0;

	private boolean started = false;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, SQLException, ClassNotFoundException {
		if (!startTeaks){
			return;
		}
		if (!started) {
			if (utils.utilru() >=7) {
				started = true;
			}
			return;
		}
		beast = utils.getFirstNPCWithLocalTarget();
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					//utils.handleRun(30, 20);
					timeout--;
					break;
				case FAIRY_RING:
					GameObject ring = utils.findNearestGameObject(40779);
					utils.useGameObjectDirect(ring, sleepDelay(), MenuAction.GAME_OBJECT_FOURTH_OPTION.getId());
					break;
				case ATTACK_RETAL:
					if (configph.typecb() == CombatType.MELEE && client.getBoostedSkillLevel(Skill.STRENGTH) <= client.getRealSkillLevel(Skill.STRENGTH)) {
						WidgetItem Cpot = GetCombatItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					if (configph.typecb() == CombatType.RANGED && client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED)) {
						WidgetItem Cpot = GetRangedItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					if (configph.typecb() == CombatType.MAGIC && client.getBoostedSkillLevel(Skill.MAGIC) <= client.getRealSkillLevel(Skill.MAGIC)) {
						WidgetItem Cpot = GetMagicItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					utils.attackNPCDirect(beast);
					timeout = tickDelay();
					break;
				case ATTACK:
					//NPC bs = getFirstAttackable("rune dragon", true);

					if (configph.typecb() == CombatType.MELEE && client.getBoostedSkillLevel(Skill.STRENGTH) <= client.getRealSkillLevel(Skill.STRENGTH)) {
						WidgetItem Cpot = GetCombatItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					if (configph.typecb() == CombatType.RANGED && client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED)) {
						WidgetItem Cpot = GetRangedItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					if (configph.typecb() == CombatType.MAGIC && client.getBoostedSkillLevel(Skill.MAGIC) <= client.getRealSkillLevel(Skill.MAGIC)) {
						WidgetItem Cpot = GetMagicItem();
						if (Cpot != null) {
							utils.useItem(Cpot.getId(),"eat","drink");
						}
					}
					NPC npc = getFirstAttackable(8031);
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
					WidgetItem item = utils.getInventoryWidgetItem(Collections.singletonList(12938));
					utils.useItem(item.getId(), "teleport");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 12938, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(12938)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					//resetZul();
					break;
				case LEAVE_CAVE:
					utils.useGameObject(30878, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					//resetZul();
					loot.clear();
					WidgetItem item2 = utils.getInventoryWidgetItem(Collections.singletonList(8013));
					utils.useItem(item2.getId(), "break");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8013)).getIndex(), WidgetInfo.INVENTORY.getId()));
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
				case WITHDRAW_ANTIFIRE:
					if (!configph.superantifires()) {
						utils.withdrawItem(11951);
					}
					if (configph.superantifires()) {
						utils.withdrawItem(22209);
					}
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
				case DRINK_ANTIFIRE:
					WidgetItem ven = GetAntifireItem();
					if (ven != null) {
						utils.useItem(ven.getId(),"eat","drink");
					}
					//timeout = tickDelay();
					break;
				case DRINK_MAGIC:
					WidgetItem Cpot = GetMagicItem();
					if (Cpot != null) {
						utils.useItem(Cpot.getId(),"eat","drink");
					}
					break;
				case EAT_FOOD:
					WidgetItem food = GetFoodItem();
					if (food != null) {
						utils.useItem(food.getId(),"eat","drink");
					}
					break;
				case DRINK_RANGE:
					WidgetItem Rpot = GetRangedItem();
					if (Rpot != null) {
						utils.useItem(Rpot.getId(),"eat","drink");
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
			if (item.getTile().getWorldLocation().toWorldArea().intersectsWith(INSIDE_DRAGONS)) {
				loot.add(item);
			}
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
		long sleepLength = utils.randomDelay(false, 100, 350, 100, 150);
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(false, 0, 2, 1, 1);
		return tickLength;
	}
}