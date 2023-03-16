package net.runelite.client.plugins.azulrah;

import com.google.common.base.Preconditions;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "AZulrah",
	description = "Anarchise' Auto Zulrah",
	tags = {"anarchise","zulrah","aplugins"},
	enabledByDefault = false
)
public class AZulrahPlugin extends Plugin
{
	int jjj = 91911;

	private int nextRestoreVal = 0;

	@Inject
	private Client client;

	private static final String MESSAGE_STUN = "The Alchemical Hydra temporarily stuns you.";

	@Provides
	AZulrahConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AZulrahConfig.class);
	}

	@Inject
	private AZulrahConfig configph;

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
	public AZulrahPlugin(){
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

	ZulrahAttributes zulrahAttributes;
	ZulrahData zulrahData;
	ZulrahPhase zulrahPhase;


	@Inject AZulrahOverlay zulrahOverlay;
	Instant botTimer;
	boolean noBomb = true;
	boolean noBomb2 = true;
	private NPC zulrahNpc = null;
	public AZulrahState state;
	private NPC zulrah = null;
	private int stage = 0;
	private int phaseTicks = -1;
	private int attackTicks = -1;
	private int acidFreePathLength = 3;
	private int totalTicks = 0;
	private RotationType currentRotation = null;
	private List<RotationType> potentialRotations = new ArrayList<RotationType>();
	private static boolean flipStandLocation = false;
	private static boolean flipPhasePrayer = false;
	private static boolean zulrahReset = false;
	private final Collection<NPC> snakelings = new ArrayList<NPC>();
	private boolean holdingSnakelingHotkey = false;
	private Counter zulrahTotalTicksInfoBox;
	public static final BufferedImage[] ZULRAH_IMAGES = new BufferedImage[3];
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, ClassNotFoundException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("azulrah")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				overlayManager.add(zulrahOverlay);
				botTimer = Instant.now();
			}
			else {
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
		startTeaks = false;
		zulrahNpc = null;
		stage = 0;
		phaseTicks = -1;
		attackTicks = -1;
		totalTicks = 0;
		currentRotation = null;
		potentialRotations.clear();
		projectilesMap.clear();
		toxicCloudsMap.clear();
		flipStandLocation = false;
		flipPhasePrayer = false;
		zulrahReset = false;
		//clearSnakelingCollection();
		holdingSnakelingHotkey = false;
		lastAttackTick = -1;
		inFight = false;
		prayerToClick = null;
		alreadyBanked = false;
		zulrah = null;
		state = null;
		botTimer = null;
		/*overlayManager.remove(phaseOverlay);
		overlayManager.remove(prayerHelperOverlay);
		overlayManager.remove(prayerMarkerOverlay);
		overlayManager.remove(sceneOverlay);*/
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
		currentRotation = null;
		potentialRotations.clear();
		projectilesMap.clear();
		toxicCloudsMap.clear();
		flipStandLocation = false;
		flipPhasePrayer = false;
		zulrahReset = false;
		//clearSnakelingCollection();
		holdingSnakelingHotkey = false;
		lastAttackTick = -1;
		inFight = false;
		prayerToClick = null;
		zulrah = null;
		banked = false;
		//zulrahReset = true;
	}


	//private static final BufferedImage CLOCK_ICON = ImageUtil.getResourceStreamFromClass(AZulrahPlugin.class, "clock.png");
	private final BiConsumer<RotationType, RotationType> phaseTicksHandler = (current, potential) -> {
		if (zulrahReset)
		{
			phaseTicks = 38;
		}
		else
		{
			ZulrahPhase p = current != null ? getCurrentPhase((RotationType)((Object)current)) : getCurrentPhase((RotationType)((Object)potential));
			Preconditions.checkNotNull(p, "Attempted to set phase ticks but current Zulrah phase was somehow null. Stage: " + stage);
			phaseTicks = p.getAttributes().getPhaseTicks();
		}
	};

	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);
	private static final List<Integer> regionz = Arrays.asList(9007, 9008);
	private static boolean isInPOH(Client client) {return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);}
	private static boolean isInZulrah(Client client)
	{
		return Arrays.stream(client.getMapRegions()).anyMatch(regionz::contains);
	}
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
	public AZulrahState getState()
	{
		if (timeout > 0)
		{
			return AZulrahState.TIMEOUT;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		//else if(client.getLocalPlayer().getAnimation()!=-1){
		//	return AVorkathState.ANIMATING;
		//}
		else {
			return getStates();
		}
	}
	Player player;
	WorldArea ZULRAH_BOAT = new WorldArea(new WorldPoint(2192, 3045, 0), new WorldPoint(2221, 3068, 0));
	WorldArea ZULRAH_ISLAND = new WorldArea(new WorldPoint(2145, 3065, 0), new WorldPoint(2156, 3076, 0));
	WorldArea ZULRAH_ISLAND2 = new WorldArea(new WorldPoint(2158, 3066, 0), new WorldPoint(2193, 3086, 0));
	WorldPoint ZULRAHPOINT = new WorldPoint(2178, 3068, 0);
	WorldArea ZULRAH_ISLAND3 = new WorldArea(new WorldPoint(2172, 3063, 0), new WorldPoint(2182, 3075, 0));
	WorldPoint ZULRAHPOINT2 = new WorldPoint(2195, 3059, 0);
	WorldArea ZULRAH_ISLAND4 = new WorldArea(new WorldPoint(2157, 3068, 0), new WorldPoint(2166, 3078, 0));

	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));
	LocalPoint standPos1;
	ZulrahData data;

	private AZulrahState getStates(){
		NPC bs = utils.findNearestNpc(2042,2043,2044);

		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && !banked){
			loot.clear();
			return AZulrahState.FIND_BANK;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && banked && !utils.isBankOpen()){
			banked = false;
			loot.clear();
			return AZulrahState.FIND_BANK;
		}
		if (player.getWorldArea().intersectsWith(ZULRAH_BOAT) && utils.findNearestGameObject(10068) != null){
			return AZulrahState.USE_BOAT;
		}
		if (player.getWorldArea().intersectsWith(ZULRAH_ISLAND)) {
			alreadyBanked = false;
			utils.useGroundObject(10663, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
		}
		if (player.getWorldArea().intersectsWith(ZULRAH_ISLAND3)) {
			return AZulrahState.WALK_FOURTH;
		}
		if (player.getWorldArea().intersectsWith(ZULRAH_ISLAND4)) {
			alreadyBanked = false;
			return AZulrahState.WALK_THIRD;
		}
		if (!loot.isEmpty() && !utils.inventoryFull() && isInZulrah(client)){
			return AZulrahState.LOOT_ITEMS;
		}
		if (utils.inventoryContains(configph.foodID()) && utils.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && isInZulrah(client)){
			return AZulrahState.EAT_FOOD;
		}
		if (utils.inventoryContains(12934) && loot.isEmpty() && !isInPOH(client) && isInZulrah(client)){
			//Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : utils.getAllInventoryItems()) {
				if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			return AZulrahState.TELE_TAB;
		}
		if (getRestoreItem() == null && isInZulrah(client)){
			//Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : utils.getAllInventoryItems()) {
				if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
					//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			return AZulrahState.TELE_TAB;
		}
		if (!utils.inventoryContains(configph.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < 50 && isInZulrah(client)){
			//Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : utils.getAllInventoryItems()) {
				if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
					//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			return AZulrahState.TELE_TAB;
		}
		if (client.getVar(Varbits.PRAYER_PROTECT_FROM_MAGIC) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		}
		if (client.getVar(Varbits.PRAYER_PROTECT_FROM_MISSILES) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		}
		if (client.getVar(Varbits.PRAYER_EAGLE_EYE) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_EAGLE_EYE);
		}
		if (client.getVar(Varbits.PRAYER_MYSTIC_MIGHT) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
		}
		if (client.getVar(Varbits.PRAYER_AUGURY) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_AUGURY);
		}
		if (client.getVar(Varbits.PRAYER_RIGOUR) != 0 && isInPOH(client)) {
			activatePrayer(WidgetInfo.PRAYER_RIGOUR);
		}


		if (client.getVar(Prayer.RIGOUR.getVarbit()) == 0 && configph.Rigour() && isInZulrah(client) && configph.RangedOnly() && !configph.MageOnly()) {
			activatePrayer(WidgetInfo.PRAYER_RIGOUR);
		}
		if (client.getVar(Prayer.EAGLE_EYE.getVarbit()) == 0 && !configph.Rigour() && isInZulrah(client) && configph.RangedOnly() && !configph.MageOnly()) {
			activatePrayer(WidgetInfo.PRAYER_EAGLE_EYE);
		}

		if (client.getVar(Prayer.AUGURY.getVarbit()) == 0 && configph.Augury() && isInZulrah(client) && !configph.RangedOnly() && configph.MageOnly()) {
			activatePrayer(WidgetInfo.PRAYER_AUGURY);
		}
		if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 0 && !configph.Augury() && isInZulrah(client) && !configph.RangedOnly() && configph.MageOnly()) {
			activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
		}


		if (isInPOH(client) && client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER) && configph.usePOHPool()){
			return AZulrahState.DRINK_POOL;
		}
		if (isInPOH(client) && !alreadyBanked && configph.fairyRings()){
			return AZulrahState.TELE_EDGE;
		}
		if (isInPOH(client) && alreadyBanked && configph.fairyRings()){
			return AZulrahState.FAIRY_RING;
		}
		if (isInPOH(client) && !configph.fairyRings()){
			return AZulrahState.TELE_EDGE;
		}
		if (!configph.MageOnly() &&client.getBoostedSkillLevel(Skill.RANGED) <= client.getRealSkillLevel(Skill.RANGED) && isInZulrah(client)){
			return AZulrahState.DRINK_RANGE;
		}
		if (!configph.RangedOnly() && !configph.nomagepots() && client.getBoostedSkillLevel(Skill.MAGIC) <= client.getRealSkillLevel(Skill.MAGIC) && isInZulrah(client)){
			return AZulrahState.DRINK_MAGIC;
		}
		if (configph.antivenomplus() && client.getVar(VarPlayer.IS_POISONED) > 0 && isInZulrah(client)) {
			return AZulrahState.DRINK_ANTIVENOM;
		}
		if (client.getLocalPlayer().getLocalLocation() != standPos && isInZulrah(client)) {
			return AZulrahState.WALK_SAFE; // = utils.walk(standPos);
		}
		if (client.getLocalPlayer().getInteracting() != bs && isInZulrah(client)){
			return AZulrahState.ATTACK_ZULRAH; //
		}
		else return AZulrahState.TIMEOUT;
	}
	private boolean banked = false;

	private AZulrahState getBankState()
	{
		if (!banked){
			utils.depositAll();
			banked = true;
			return AZulrahState.DEPOSIT_ITEMS;
		}
		if (!configph.fairyRings() && !utils.inventoryContains(12938)){
			return AZulrahState.WITHDRAW_TELES;
		}
		if (configph.fairyRings() && !utils.inventoryContains(772)){
			utils.withdrawItem(772);
		}
		if (!utils.inventoryContains(8013)){
			return AZulrahState.WITHDRAW_HOUSE;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID1() != 0 && !utils.inventoryContains(configph.mageID1())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() &&configph.mageID2() != 0 && !utils.inventoryContains(configph.mageID2())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID3() != 0 && !utils.inventoryContains(configph.mageID3())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID4() != 0 && !utils.inventoryContains(configph.mageID4())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID5() != 0 && !utils.inventoryContains(configph.mageID5())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID6() != 0 && !utils.inventoryContains(configph.mageID6())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID7() != 0 && !utils.inventoryContains(configph.mageID7())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.MageOnly() && configph.mageID8() != 0 && !utils.inventoryContains(configph.mageID8())){
			return AZulrahState.WITHDRAW_GEAR;
		}
		if (!configph.RangedOnly() && !configph.nomagepots() &&configph.imbuedheart() && !utils.inventoryContains(20724)){
			return AZulrahState.WITHDRAW_MAGIC;
		}
		if (!configph.RangedOnly() && !configph.nomagepots() && configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(23745)){
			return AZulrahState.WITHDRAW_MAGIC;
		}
		if (!configph.RangedOnly() && !configph.nomagepots() && !configph.supers() && !configph.imbuedheart() && !utils.inventoryContains(3040)){
			return AZulrahState.WITHDRAW_MAGIC;
		}
		if (!configph.MageOnly() && configph.supers() && !utils.inventoryContains(24635)){
			return AZulrahState.WITHDRAW_RANGED;
		}
		if (!configph.MageOnly() && !configph.supers() && !utils.inventoryContains(2444)){
			return AZulrahState.WITHDRAW_RANGED;
		}
		if (!utils.inventoryContains(12913) && configph.antivenomplus() && !configph.serphelm() && !configph.superantipoison()){
			return AZulrahState.WITHDRAW_VENOM;	//
		}
		if (!utils.inventoryContains(5952) && !configph.antivenomplus() && !configph.serphelm() && !configph.superantipoison()){
			return AZulrahState.WITHDRAW_VENOM;	//
		}
		if (!utils.inventoryContains(2448) && !configph.antivenomplus() && !configph.serphelm() && configph.superantipoison()){
			return AZulrahState.WITHDRAW_VENOM;	//
		}
		if (!configph.useRestores() && !utils.inventoryContains(2434)){
			return AZulrahState.WITHDRAW_RESTORES;
		}
		if (configph.useRestores() && !utils.inventoryContains(3024)){
			return AZulrahState.WITHDRAW_RESTORES;
		}
		if (!utils.inventoryContains(configph.foodID())){
			return AZulrahState.WITHDRAW_FOOD1;
		}
		if (!utils.inventoryContains(configph.foodID2())){
			return AZulrahState.WITHDRAW_FOOD2;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(configph.foodID2()) && banked){
			alreadyBanked = true;
			return AZulrahState.WALK_SECOND;
		}
		else return AZulrahState.TIMEOUT;
	}
	public boolean startTeaks = false;


	@Inject ConfigManager configManager;

	public Prayer currentPrayer;

	boolean alreadyBanked = false;

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
		if (client.getWidget(219, 1) != null) {
			if (player.getWorldArea().intersectsWith(ZULRAH_BOAT) && !utils.isMoving() && client.getWidget(219, 1).getChildren()[0].getText().equals("Return to Zulrah's shrine?")) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", 0, 30, 1, 14352385));
			}
			if (player.getWorldArea().intersectsWith(ZULRAH_BOAT) && !utils.isMoving() && client.getWidget(219, 1).getText().equals("The priestess rows you to Zulrah's shrine, then hurriedly paddles away.")) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15007746));
			}
		}
		if (getZulrahNpc() != null) {
			if (client.getLocalPlayer().getInteracting() != zulrahNpc && isInZulrah(client) && !utils.isMoving()){
				utils.attackNPCDirect(zulrahNpc);
			}
			++totalTicks;
			if (attackTicks >= 0) {
				--attackTicks;
			}
			if (phaseTicks >= 0) {
				--phaseTicks;
			}
			if (projectilesMap.size() > 0) {
				projectilesMap.values().removeIf(v -> v <= 0);
				projectilesMap.replaceAll((k, v) -> v - 1);
			}
			if (toxicCloudsMap.size() > 0) {
				toxicCloudsMap.values().removeIf(v -> v <= 0);
				toxicCloudsMap.replaceAll((k, v) -> v - 1);
			}
		}
			for (ZulrahData data : getZulrahData()) {
				if (data.getCurrentPhase().isPresent()) {
					standPos = data.getCurrentDynamicStandLocation().get().toLocalPoint();
					if (data.getCurrentPhasePrayer().isPresent()) {
						currentPrayer = data.getCurrentPhasePrayer().get();
						if (currentPrayer != null) {
							if (client.getVar(currentPrayer.getVarbit()) == 0) {
								activatePrayer(currentPrayer.getWidgetInfo());
							}
						}
					}
					//if (client.getLocalPlayer().getLocalLocation() != standPos) {
						//if (!utils.isMoving()) {
						//utils.walk(standPos);
						//}
					//}
				}
			}

		//getZulrahNpc();
		//getZulrahData();
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			state = getState();
			//beforeLoc = player.getLocalLocation();
			//utils.setMenuEntry(null);
			switch (state) {
				case TIMEOUT:
					//utils.handleRun(30, 20);
					timeout--;
					break;
				case CONTINUE2:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15007746));
					break;
				case FAIRY_RING:
					if (!utils.isItemEquipped(Collections.singleton(772))){
						WidgetItem ITEM = utils.getInventoryWidgetItem(Collections.singletonList(ItemID.DRAMEN_STAFF));
						utils.useItem(ITEM.getId(), "wield");
						//clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + 772, 772, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(772)).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					GameObject ring = utils.findNearestGameObject(29228);
					utils.useGameObjectDirect(ring, sleepDelay(), MenuAction.GAME_OBJECT_THIRD_OPTION.getId());
					break;
				case WALK_FOURTH:
					utils.walk(ZULRAHPOINT2);
					timeout = tickDelay();
					break;
				case WALK_THIRD:
					//Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
					if (configph.RangedOnly()) {
						for (WidgetItem item : utils.getAllInventoryItems()) {
							if (("Group 2").equalsIgnoreCase(getTag(item.getId()))) {
								//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
								utils.useItem(item.getId(), "wield");
								//clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
							}
						}
					}
					if (configph.MageOnly()) {
						for (WidgetItem item : utils.getAllInventoryItems()) {
							if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
								//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
								//clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
								utils.useItem(item.getId(), "wield");
							}
						}
					}
					if (!configph.RangedOnly()) {
						for (WidgetItem item : utils.getAllInventoryItems()) {
							if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
								//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
								//clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
								utils.useItem(item.getId(), "wield");
							}
						}
					}
					utils.walk(ZULRAHPOINT);
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
				case WITHDRAW_VENOM:
					if (configph.antivenomplus() && !configph.superantipoison()) {
						utils.withdrawItem(12913); //anti venom+
					}
					if (!configph.antivenomplus() && !configph.superantipoison()){
						utils.withdrawItem(5952); // antidote++
					}
					if (configph.superantipoison()) {
						utils.withdrawItem(2448);	//superantipoison
					}
					timeout = tickDelay();
					break;
				case ATTACK_ZULRAH:
				//	NPC zulrah = utils.findNearestNpc(2042,2043,2044);
					utils.attackNPCDirect(zulrahNpc);
					timeout = tickDelay();
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
					resetZul();
					break;
				case TELE_TAB:
					WidgetItem ITEM = utils.getInventoryWidgetItem(Collections.singletonList(8013));
					utils.useItem(ITEM.getId(), "break");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8013)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					resetZul();
					if (configph.fairyRings()) {
						WidgetItem ITEM2 = utils.getInventoryWidgetItem(Collections.singletonList(8013));
						utils.useItem(ITEM2.getId(), "break");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8013)).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (!configph.fairyRings()){
						WidgetItem ITEM2 = utils.getInventoryWidgetItem(Collections.singletonList(12938));
						utils.useItem(ITEM2.getId(), "teleport");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", 12938, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(12938)).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					timeout = tickDelay();
					break;
				case TELE_EDGE:
					resetZul();
					utils.useDecorativeObject(13523, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case DRINK_POOL:
					resetZul();
					GameObject Pool = utils.findNearestGameObject(29240, 29241);
					utils.useGameObjectDirect(Pool, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case WITHDRAW_GEAR:
					if (!utils.inventoryContains(configph.mageID1())) {
						utils.withdrawItem(configph.mageID1());
					}
					if (!utils.inventoryContains(configph.mageID2())) {
						utils.withdrawItem(configph.mageID2());
					}
					if (!utils.inventoryContains(configph.mageID3())) {
						utils.withdrawItem(configph.mageID3());
					}
					if (!utils.inventoryContains(configph.mageID4())) {
						utils.withdrawItem(configph.mageID4());
					}
					if (!utils.inventoryContains(configph.mageID5())) {
						utils.withdrawItem(configph.mageID5());
					}
					if (!utils.inventoryContains(configph.mageID6())) {
						utils.withdrawItem(configph.mageID6());
					}
					if (!utils.inventoryContains(configph.mageID7())) {
						utils.withdrawItem(configph.mageID7());
					}
					if (!utils.inventoryContains(configph.mageID8())) {
						utils.withdrawItem(configph.mageID8());
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
				case WITHDRAW_FOOD2:
					utils.withdrawItemAmount(configph.foodID2(), configph.foodAmount2());
					timeout = tickDelay();
					break;
				case MOVING:
					//utils.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case DRINK_ANTIVENOM:
					WidgetItem ven = GetAntiVenomItem();
					if (ven != null) {
						/*clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										ven.getId(),
										MenuAction.CC_OP.getId(),
										ven.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);*/
						//WidgetItem ITEM2 = utils.getInventoryWidgetItem(Collections.singletonList(8013));
						utils.useItem(ven.getId(), "drink");
					}
					//timeout = tickDelay();
					break;
				case DRINK_MAGIC:
					WidgetItem Cpot = GetMagicItem();
					if (Cpot != null) {
						/*clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Cpot.getId(),
										MenuAction.CC_OP.getId(),
										Cpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);*/
						utils.useItem(Cpot.getId(), "drink");
					}
					break;
				case EAT_FOOD:
					WidgetItem food = GetFoodItem();
					if (food != null) {
						/*clientThread.invoke(() ->
								client.invokeMenuAction(
										"",
										"",
										food.getId(),
										MenuAction.CC_OP.getId(),
										food.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);*/
						utils.useItem(food.getId(), "eat");
					}
					break;
				case DRINK_RANGE:
					WidgetItem Rpot = GetRangedItem();
					if (Rpot != null) {
						/*clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Rpot.getId(),
										MenuAction.CC_OP.getId(),
										Rpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);*/
						utils.useItem(Rpot.getId(), "drink");
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


		/*if (!utils.isMoving() && client.getLocalPlayer().getInteracting() != zulrahNpc){
			//NPC npc = utils.findNearestNpc(2042,2043,2044);
			utils.attackNPCDirect(zulrahNpc);
		}*/


		/*if (timeout > 0)
		{
			timeout--;
		}*/

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
	public void onAnimationChanged(AnimationChanged event)
	{
			if (!(event.getActor() instanceof NPC))
			{
				return;
			}
			NPC npc = (NPC)((Object)event.getActor());
			if (npc.getName() != null && !npc.getName().equalsIgnoreCase("zulrah"))
			{
				return;
			}
		//if (npc.getName().equalsIgnoreCase("zulrah")){
		//	zulrahNpc = npc;
		//}
			switch (npc.getAnimation())
			{
				case 5071:
				{
					zulrahNpc = npc;
					potentialRotations = RotationType.findPotentialRotations(npc, stage);
					phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));
					//log.debug("New Zulrah Encounter Started");
					break;
				}
				case 5073:
				{
					++stage;
					if (currentRotation == null)
					{
						potentialRotations = RotationType.findPotentialRotations(npc, stage);
						currentRotation = potentialRotations.size() == 1 ? potentialRotations.get(0) : null;
					}
					phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));
					break;
				}
				case 5072:
				{
					if (zulrahReset)
					{
						zulrahReset = false;
					}
					if (currentRotation == null || !isLastPhase(currentRotation)) break;
					stage = -1;
					currentRotation = null;
					potentialRotations.clear();
					snakelings.clear();
					flipStandLocation = false;
					flipPhasePrayer = false;
					zulrahReset = true;
					//log.debug("Resetting Zulrah");
					break;
				}
				case 5069:
				{
					attackTicks = 4;
					if (currentRotation == null || !getCurrentPhase(currentRotation).getZulrahNpc().isJad() || zulrahNpc.getInteracting() != client.getLocalPlayer()) break;
					flipPhasePrayer = !flipPhasePrayer;
					break;
				}
				case 5806:
				case 5807:
				{
					attackTicks = 8;
					flipStandLocation = !flipStandLocation;
					break;
				}
				case 5804:
				{
					resetZul();
				}
			}
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
	public WidgetItem getRestoreItem()
			{
				WidgetItem item;

				item = PrayerRestoreType.PRAYER_POTION.getItemFromInventory(client);

				if (item != null)
				{
					return item;
				}

				item = PrayerRestoreType.SANFEW_SERUM.getItemFromInventory(client);

				if (item != null)
				{
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



	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
			if (zulrahNpc == null)
			{
				return;
			}
			Projectile p = event.getProjectile();
			switch (p.getId())
			{
				case 1045:
				case 1047:
				{
					projectilesMap.put(event.getPosition(), p.getRemainingCycles() / 30);
				}
			}
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
	@Subscribe
	private void onNpcChanged(NpcChanged event)
	{

			final int npcId = event.getNpc().getId();

			if (npcId == 2043 && client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) != 0){
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);	//Melee Form Range prayer enabled
			}
			if (npcId == 2043 && client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) != 0){
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);	//Melee Form Mage prayer enabled
			}
			if (npcId == 2042 && client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 0){
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);	//Ranged form

			}
			else if (npcId == 2044 && client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 0){
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);	//Magic form
			}



			if (event.getNpc().getName().equalsIgnoreCase("zulrah")){
				zulrahNpc = event.getNpc();
			}


			//final int npcId = event.getNpc().getId();
			if (npcId == 2042 && !configph.RangedOnly()){		//	RANGED FORM
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
				if (inventory == null) {
					return;
				}
				for (WidgetItem item : utils.getAllInventoryItems()) {
					if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
						//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
						clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
				}
				if (client.getVar(Prayer.AUGURY.getVarbit()) == 0 && configph.Augury()) {
					activatePrayer(WidgetInfo.PRAYER_AUGURY);
				}
				if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 0 && !configph.Augury()) {
					activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
				}
				//utils.attackNPCDirect(zulrahNpc);
			}
			else if (npcId == 2043 && !configph.RangedOnly()){	//	MELEE FORM
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
				if (inventory == null) {
					return;
				}
				for (WidgetItem item : utils.getAllInventoryItems()) {
					if (("Group 3").equalsIgnoreCase(getTag(item.getId()))) {
						//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
						clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
				}
				if (client.getVar(Prayer.AUGURY.getVarbit()) == 0 && configph.Augury()) {
					activatePrayer(WidgetInfo.PRAYER_AUGURY);
				}
				if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 0 && !configph.Augury()) {
					activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
				}
				//utils.attackNPCDirect(zulrahNpc);
			}
			else if (npcId == 2044 && !configph.MageOnly()) {	//	MAGIC FORM
				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
				if (inventory == null) {
					return;
				}
				for (WidgetItem item : utils.getAllInventoryItems()) {
					if (("Group 2").equalsIgnoreCase(getTag(item.getId()))) {
						//plugin.entryList.add(new MenuEntry("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false));
						clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.CC_OP.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
				}
				if (client.getVar(Prayer.RIGOUR.getVarbit()) == 0 && configph.Rigour()) {
					activatePrayer(WidgetInfo.PRAYER_RIGOUR);
				}
				if (client.getVar(Prayer.RIGOUR.getVarbit()) == 0 && !configph.Rigour()) {
					activatePrayer(WidgetInfo.PRAYER_EAGLE_EYE);
				}
				//utils.attackNPCDirect(zulrahNpc);
			}

	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {

		NPC npc = event.getNpc();
		//if (npc.getName().toLowerCase().contains("zulrah")) {
		//	zulrah = npc;
		//}

		final int npcId = event.getNpc().getId();

		//if (event.getNpc().getName().equalsIgnoreCase("zulrah")){
		//	zulrahNpc = event.getNpc();
		//}
		if (npcId == 2042 && client.getLocalPlayer().getOverheadIcon() != HeadIcon.RANGED) {
			zulrah = npc;
			zulrahNpc = npc;
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);    //Ranged form

			if (client.getVar(Prayer.AUGURY.getVarbit()) == 0 && configph.Augury() && !configph.RangedOnly()) {
				activatePrayer(WidgetInfo.PRAYER_AUGURY);
			}
			if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 0 && !configph.Augury() && !configph.RangedOnly()) {
				activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
			}
		} else if (npcId == 2044 && client.getLocalPlayer().getOverheadIcon() != HeadIcon.MAGIC) {
			zulrah = npc;
			zulrahNpc = npc;
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);    //Magic form
			//activatePrayer(WidgetInfo.PRAYER_EAGLE_EYE);
		}
	}


	private static void setHidden(Renderable renderable, boolean hidden)
	{
		Method setHidden = null;
		try
		{
			setHidden = renderable.getClass().getMethod("setHidden", Boolean.TYPE);
		}
		catch (NoSuchMethodException e)
		{
			return;
		}
		try
		{
			setHidden.invoke(renderable, hidden);
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{

		}
	}


	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event) {
		if (zulrahNpc == null) {
			return;
		}
		GameObject obj = event.getGameObject();
		if (obj.getId() == 11700) {
			toxicCloudsMap.put(obj, 30);
		}
	}
	int lll = 999990;

	@Nullable
	private ZulrahPhase getCurrentPhase(RotationType type)
	{
		return stage >= type.getZulrahPhases().size() ? null : type.getZulrahPhases().get(stage);
	}

	@Nullable
	private ZulrahPhase getNextPhase(RotationType type)
	{
		return isLastPhase(type) ? null : type.getZulrahPhases().get(stage + 1);
	}

	private boolean isLastPhase(RotationType type)
	{
		return stage == type.getZulrahPhases().size() - 1;
	}

	public Set<ZulrahData> getZulrahData()
	{
		LinkedHashSet<ZulrahData> zulrahDataSet = new LinkedHashSet<ZulrahData>();
		if (currentRotation == null)
		{
			potentialRotations.forEach(type -> zulrahDataSet.add(new ZulrahData(getCurrentPhase((RotationType)((Object)type)), getNextPhase((RotationType)((Object)type)))));
		}
		else
		{
			zulrahDataSet.add(new ZulrahData(getCurrentPhase(currentRotation), getNextPhase(currentRotation)));
		}
		return zulrahDataSet.size() > 0 ? zulrahDataSet : Collections.emptySet();
	}

	public NPC getZulrahNpc()
	{
		return zulrahNpc;
	}

	public int getPhaseTicks()
	{
		return phaseTicks;
	}

	public int getAttackTicks()
	{
		return attackTicks;
	}

	public RotationType getCurrentRotation()
	{
		return currentRotation;
	}

	public Map<LocalPoint, Integer> getProjectilesMap()
	{
		return projectilesMap;
	}

	public Map<GameObject, Integer> getToxicCloudsMap()
	{
		return toxicCloudsMap;
	}

	public static boolean isFlipStandLocation()
	{
		return flipStandLocation;
	}

	public static boolean isFlipPhasePrayer()
	{
		return flipPhasePrayer;
	}

	public static boolean isZulrahReset()
	{
		return zulrahReset;
	}

	static
	{
		AZulrahPlugin.ZULRAH_IMAGES[0] = ImageUtil.getResourceStreamFromClass(AZulrahPlugin.class, "zulrah_range.png");
		AZulrahPlugin.ZULRAH_IMAGES[1] = ImageUtil.getResourceStreamFromClass(AZulrahPlugin.class, "zulrah_melee.png");
		AZulrahPlugin.ZULRAH_IMAGES[2] = ImageUtil.getResourceStreamFromClass(AZulrahPlugin.class, "zulrah_magic.png");
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