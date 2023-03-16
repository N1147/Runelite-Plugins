package net.runelite.client.plugins.NFarming;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NFarming",
	description = "Does farming runs.",
	tags = {"numb","farming"},
	enabledByDefault = false
)

public class PFarming extends Plugin
{
	@Inject
	private Client client;
	@Provides
	PFarmingConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(PFarmingConfig.class);
	}
	@Inject
	private PFarmingConfig config;
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
	public PFarmingState state;
	Instant botTimer;

	public boolean startTeaks = false;
    Player player;
	WorldPoint Point = new WorldPoint(2515, 3161, 0);
	WorldPoint Elkoy = new WorldPoint(2515, 3160, 0);
	WallObject fence;
	NPC ElkoyNPC;
    WorldArea FALADOR = new WorldArea(new WorldPoint(2960, 3370, 0), new WorldPoint(2975, 3389, 0));
    WorldPoint TaverlyWalk = new WorldPoint(2948, 3426, 0);
    WorldArea TaverlyWalk1 = new WorldArea(new WorldPoint(2947, 3414, 0), new WorldPoint(2963, 3431, 0));
    WorldPoint TaverlyGate = new WorldPoint(2938, 3451, 0);
    boolean taverly = false;
    WorldArea TaverlyPatch = new WorldArea(new WorldPoint(2925, 3430, 0), new WorldPoint(2938, 3454, 0));
    WorldArea TaverlyGateArea = new WorldArea(new WorldPoint(2934, 3448, 0), new WorldPoint(2944, 3454, 0));
	boolean harvested = false;
    WorldPoint FallyWalk1 = new WorldPoint(3002, 3374, 0);
    WorldArea FaladorPatch = new WorldArea(new WorldPoint(2999, 3368, 0), new WorldPoint(3009, 3377, 0));
    boolean falador = false;
    WorldPoint VarrockWalk = new WorldPoint(3227, 3458, 0);
    WorldArea VARROCK = new WorldArea(new WorldPoint(3202, 3416, 0), new WorldPoint(3225, 3440, 0));
    WorldArea VarrockPatch = new WorldArea(new WorldPoint(3221, 3452, 0), new WorldPoint(3236, 3465, 0));
    boolean varrock = false;
    WorldPoint GEWalk = new WorldPoint(3196, 3476, 0);
    WorldArea GEArea = new WorldArea(new WorldPoint(3174, 3461, 0), new WorldPoint(3197, 3517, 0));
    WorldPoint GnomeWalk = new WorldPoint(2437, 3417 , 0);
    WorldArea GnomePatchT = new WorldArea(new WorldPoint(2430, 3407, 0), new WorldPoint(2444, 3424, 0));
    WorldArea STRONGHOLD = new WorldArea(new WorldPoint(2451, 3437, 0), new WorldPoint(2470, 3456, 0));
    boolean gnomet = false;
    WorldArea GnomePatchF = new WorldArea(new WorldPoint(2453, 3434, 0), new WorldPoint(2482, 3451, 0));
    boolean gnomef = false;
    WorldArea MAZE = new WorldArea(new WorldPoint(2517, 3163, 0), new WorldPoint(2544, 3174, 0));
    WorldArea MAZE_OUT = new WorldArea(new WorldPoint(2498, 3185, 0), new WorldPoint(2509, 3198, 0));
    WorldArea KHAZARDPATCH = new WorldArea(new WorldPoint(2486, 3176, 0), new WorldPoint(2495, 3185, 0));
	WorldArea ElkoyInside = new WorldArea(new WorldPoint(2512, 3161, 0), new WorldPoint(2517, 3164, 0));
	boolean khazard = false;
    WorldArea CAMELOT = new WorldArea(new WorldPoint(2753, 3472, 0), new WorldPoint(2764, 3481, 0));
    WorldPoint CATHERBYWALK1 = new WorldPoint(2802, 3449, 0);
    WorldArea CATHERBYWALK2 = new WorldArea(new WorldPoint(2796, 3442, 0), new WorldPoint(2810, 3454, 0));
    WorldPoint CATHERBYWALK3 = new WorldPoint(2842, 3437, 0);
    WorldArea CATHERBYPATCH = new WorldArea(new WorldPoint(2835, 3423, 0), new WorldPoint(2864, 3439, 0));
    boolean catherby = false;
    WorldArea CATHERBY_DOCKS = new WorldArea(new WorldPoint(2789, 3407, 0), new WorldPoint(2811, 3432, 0));
    WorldArea BRIMHAVEN_BOAT = new WorldArea(new WorldPoint(2762, 3234, 1), new WorldPoint(2766, 3244, 1));
    WorldArea BRIMHAVEN_DOCKS = new WorldArea(new WorldPoint(2755, 3227, 0), new WorldPoint(2770, 3245, 0));
    WorldPoint BRIMHAVEN_PATCH = new WorldPoint(2766, 3215, 0);
    WorldArea BRIMHAVEN_PATCH1 = new WorldArea(new WorldPoint(2755, 3204, 0), new WorldPoint(2772, 3218, 0));
    boolean brimhaven = false;
	WorldArea LUMBRIDGE = new WorldArea(new WorldPoint(3201, 3203, 0), new WorldPoint(3232, 3232, 0));
	WorldPoint LumbWalk = new WorldPoint(3195, 3231, 0);
	WorldArea LUMBRIDGE_PATCH = new WorldArea(new WorldPoint(3186, 3223, 0), new WorldPoint(3199, 3236, 0));
	boolean lumbridge = false;
	GameObject patch;
	GameObject fruitpatch;
	private boolean banked = false;
	boolean stumpcleared = false;

	@Override
	protected void startUp() throws Exception
	{
		reset();
	}
	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}
	private void reset() throws IOException, ClassNotFoundException {
		if (!started) {
			if (utils.util() >=5) {
				started = true;
			}
		}
		started = false;
        falador = false;
        varrock = false;
        taverly = false;
        gnomet = false;
        gnomef = false;
		lumbridge = false;
        harvested = false;
		teled =false;
		checkedHealth = false;
        brimhaven = false;
        stumpcleared = false;
		startTeaks = false;
		banked = false;
		state = null;
		botTimer = null;
	}
	private void handleFullInventory() {
		items = utils.stringToIntList(config.itemsToNote());
		WidgetItem InventoryItem = utils.getInventoryWidgetItem(items);
		if (InventoryItem != null) {
			//clientThread.invoke(() -> client.invokeMenuAction("", "", InventoryItem.getId(), MenuAction.ITEM_USE.getId(), InventoryItem.getIndex(), WidgetInfo.INVENTORY.getId()));
			utils.useItem(InventoryItem.getId(), "use");
			clientThread.invoke(() -> client.invokeMenuAction("", "", utils.findNearestNpc(0).getIndex(), MenuAction.ITEM_USE_ON_NPC.getId(), 0, 0));
			return;
		}
		if (InventoryItem == null) {
			utils.dropItems(utils.stringToIntList(config.items()), true, 100, 250);
			return;
		}
	}
	private List<Integer> items = new ArrayList<>();
	//Collection<Integer> items;

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException, ClassNotFoundException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("PFarming")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				state = null;
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
	public PFarmingState getState() throws IOException, ClassNotFoundException {
		if (timeout > 0)
		{
			return PFarmingState.TIMEOUT;
		}
		if(player.getAnimation()!=-1){
			return PFarmingState.IDLE_2;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		else {
			return getStates();
		}
	}
	boolean firstgate = false;
	boolean checkedHealth = false;
	boolean firstclear = true;
	boolean teled = false;
	private PFarmingState getStates() throws IOException, ClassNotFoundException {
		if (banked && !teled) {
			//clientThread.invoke(() -> client.invokeMenuAction("Break", "", 8009, MenuAction.CC_OP.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8009)).getIndex(), WidgetInfo.INVENTORY.getId()));
			utils.useItem(8009, "break");
			teled = true;
			return PFarmingState.IDLE;
		}
		if (utils.inventoryFull()) {
			handleFullInventory();
			return PFarmingState.IDLE;
		}
		/**
		 taverley
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(FALADOR) && !taverly) {
			utils.walk(TaverlyWalk);
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyWalk1) && !taverly) {
			utils.walk(TaverlyGate);
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyGateArea) && !firstgate) {
			if (utils.findNearestWallObject(1569, 1728, 1569).getWorldLocation().toWorldArea().intersectsWith(TaverlyGateArea)) {
				WallObject gate = utils.findNearestWallObject("gate");
				utils.useWallObjectDirect(gate, 0, MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			}
			firstgate = true;
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyGateArea) && firstgate) {
			utils.walk(new WorldPoint(2936, 3441, 0));
			firstgate = false;
			return PFarmingState.IDLE;
		}


		if (client.getWidget(231, 6) != null && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch)) {
			if (client.getWidget(231, 6).getText().contains("already looking after that patch for you")) {
				utils.useItem(ItemID.FALADOR_TELEPORT, "use");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.FALADOR_TELEPORT, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(ItemID.FALADOR_TELEPORT)).getIndex(), WidgetInfo.INVENTORY.getId()));
				taverly = true;
				stumpcleared = false;
				harvested = false;
				checkedHealth = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (!harvested && !checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch) && patch != null) {
			utils.useGameObjectDirect(patch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch) && patch != null) {
			NPC alain = utils.findNearestNpc("Alain");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch) && patch != null) {
			NPC alain = utils.findNearestNpc("Alain");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch) && patch != null) {
			utils.useItem(config.treeSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.treeSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.treeSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", patch.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), patch.getSceneMinLocation().getX(), patch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}




			/*if (client.getWidget(231, 4) != null) {
				if (client.getWidget(231, 4).getText().contains("Alain")) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", 8009, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(8009).getIndex(), WidgetInfo.INVENTORY.getId()));
					taverly = true;
					stumpcleared = false;
					harvested = false;
					checkedHealth = false;
					return PFarmingState.IDLE;
				}
			}
			if (stumpcleared && harvested && client.getLocalPlayer().getWorldArea().intersectsWith(TaverlyPatch)) {
				NPC alain = utils.findNearestNpc("Alain");
				clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
				return PFarmingState.IDLE;
			}*/
		/**
		 taverley
		 */
		/**
		 falador
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(FALADOR) && taverly) {
			utils.walk(FallyWalk1);
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 6) != null && client.getLocalPlayer().getWorldArea().intersectsWith(FaladorPatch)) {
			if (client.getWidget(231, 6).getText().contains("already looking after that patch for you")) {
				utils.useItem(ItemID.LUMBRIDGE_TELEPORT, "use");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.LUMBRIDGE_TELEPORT, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(ItemID.LUMBRIDGE_TELEPORT)).getIndex(), WidgetInfo.INVENTORY.getId()));
				falador = true;
				stumpcleared = false;
				harvested = false;
				checkedHealth = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (!harvested && !checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(FaladorPatch) && patch != null) {
			utils.useGameObjectDirect(patch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(FaladorPatch) && patch != null) {
			NPC alain = utils.findNearestNpc("Heskel");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(FaladorPatch) && patch != null) {
			NPC alain = utils.findNearestNpc("Heskel");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(FaladorPatch) && patch != null) {
			utils.useItem(config.treeSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.treeSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.treeSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", patch.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), patch.getSceneMinLocation().getX(), patch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		/**
		 falador
		 */
		/**
		 lumbridge
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE) && !lumbridge) {
			utils.walk(LumbWalk);
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 6) != null && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_PATCH)) {
			if (client.getWidget(231, 6).getText().contains("already looking after that patch for you")) {
				utils.useItem(ItemID.VARROCK_TELEPORT, "break");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.VARROCK_TELEPORT, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(ItemID.VARROCK_TELEPORT)).getIndex(), WidgetInfo.INVENTORY.getId()));
				lumbridge = true;
				stumpcleared = false;
				harvested = false;
				checkedHealth = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (!harvested && !checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_PATCH) && patch != null) {
			utils.useGameObjectDirect(patch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_PATCH) && patch != null) {
			NPC alain = utils.findNearestNpc("Fayeth");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_PATCH) && patch != null) {
			NPC alain = utils.findNearestNpc("Fayeth");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_PATCH) && patch != null) {
			utils.useItem(config.treeSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.treeSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.treeSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", patch.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), patch.getSceneMinLocation().getX(), patch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		/**
		 lumbridge
		 */
		/**
		 varrock
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(VARROCK) && !varrock) {
			utils.walk(VarrockWalk);
			return PFarmingState.IDLE;
		}

		if (client.getWidget(231, 6) != null && client.getLocalPlayer().getWorldArea().intersectsWith(VarrockPatch) && !varrock) {
			if (client.getWidget(231, 6).getText().contains("already looking after that patch for you")) {
				varrock = true;
				utils.walk(GEWalk);
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (!harvested && !checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(VarrockPatch) && patch != null && !varrock) {
			utils.useGameObjectDirect(patch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(VarrockPatch) && patch != null && !varrock) {
			NPC alain = utils.findNearestNpc("Treznor");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(VarrockPatch) && patch != null && !varrock) {
			NPC alain = utils.findNearestNpc("Treznor");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(VarrockPatch) && patch != null && !varrock) {
			utils.useItem(config.treeSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.treeSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.treeSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", patch.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), patch.getSceneMinLocation().getX(), patch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		/**
		 varrock
		 */
		/**
		 GnomeT
		 */
		if (client.getWidget(187, 3) != null && !gnomet) {
			if (!client.getWidget(187, 3).isHidden()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 12255235));
				stumpcleared = false;
				harvested = false;
				checkedHealth = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(GEArea) && !gnomet) {
			GameObject GTree = utils.findNearestGameObject(1295);
			utils.useGameObjectDirect(GTree, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = false;
			harvested = false;
			checkedHealth = false;
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT) && gnomet && !gnomef) {
			utils.walk(new WorldPoint(2474, 3444, 0));
			utils.sendGameMessage("Tree run completed! Starting fruit tree run");
			stumpcleared = false;
			harvested = false;
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(STRONGHOLD) && !gnomet && !gnomef) {
			utils.walk(GnomeWalk);
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 6) != null && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT)) {
			if (client.getWidget(231, 6).getText().contains("already looking after that patch for you")) {
				gnomet = true;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (!harvested && !checkedHealth && !stumpcleared && !gnomet && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT) && patch != null) {
			utils.useGameObjectDirect(patch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && !stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT) && patch != null) {
			NPC alain = utils.findNearestNpc("Prissy Scilla");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (harvested && checkedHealth && stumpcleared && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT) && patch != null) {
			NPC alain = utils.findNearestNpc("Prissy Scilla");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (!harvested && checkedHealth && stumpcleared && !gnomet && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT) && patch != null) {
			utils.useItem(config.treeSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.treeSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.treeSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", patch.getId(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), patch.getSceneMinLocation().getX(), patch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		//}
		/**
		 GnomeT
		 */
		//////////////////////////// END TREE RUN
		////////////////////////////
		////////////////////////////
		////////////////////////////
		//////////////////////////// FRUIT TREE RUN

		//if (config.RunType() == RunType.FRUIT_TREES) {
			/*if (client.getLocalPlayer().getWorldArea().intersectsWith(VARROCK) && !gnomef) {
				utils.walk(GEWalk);
				return PFarmingState.IDLE;
			}
			if (client.getLocalPlayer().getWorldArea().intersectsWith(GEArea) && !gnomef) {
				GameObject GTree = utils.findNearestGameObject(1295);
				utils.useGameObjectDirect(GTree, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				stumpcleared = false;
				harvested = false;
				return PFarmingState.IDLE;
			}*/
		/**
		 GnomeF
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF) && fruitpatch.getName().contains("stump") && !gnomef && gnomet) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF) && fruitpatch != null && !gnomef && gnomet) {
			utils.useItem(config.fruitSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.fruitSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.fruitSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", 7962, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), fruitpatch.getSceneMinLocation().getX(), fruitpatch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		if (!stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF) && fruitpatch != null && !gnomef && gnomet) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 4) != null) {
			if (client.getWidget(231, 4).getText().contains("Bolongo")) {
				GameObject STree = utils.findNearestGameObject(1294);
				utils.useGameObjectDirect(STree, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				gnomef = true;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			gnomef = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && harvested && client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF) && !gnomef && gnomet) {
			NPC alain = utils.findNearestNpc("Bolongo");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		if (client.getWidget(187, 3) != null && gnomef) {
			if (!client.getWidget(187, 3).isHidden()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 0, 12255235));
				stumpcleared = false;
				harvested = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF) && gnomef && gnomet) {
			GameObject STree = utils.findNearestGameObject(1294);
			utils.useGameObjectDirect(STree, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		/**
		 GnomeF
		 */
		/**
		 Khazard
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(ElkoyInside) && !khazard) {
			utils.useWallObjectDirect(fence, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldLocation().equals(Elkoy) && !khazard) {
			utils.interactNPC(MenuAction.NPC_THIRD_OPTION.getId(), sleepDelay(), ElkoyNPC.getId());
			stumpcleared = false;
			harvested = false;
			checkedHealth = false;
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(MAZE) && !khazard) {
			utils.walk(new WorldPoint(2515, 3161, 0));
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(MAZE_OUT) && !khazard) {
			utils.walk(new WorldPoint(2491, 3181, 0));
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(KHAZARDPATCH) && fruitpatch.getName().contains("stump") && !khazard) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(KHAZARDPATCH) && fruitpatch != null && !khazard) {
			utils.useItem(config.fruitSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.fruitSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.fruitSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", 7963, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), fruitpatch.getSceneMinLocation().getX(), fruitpatch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		if (!stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(KHAZARDPATCH) && fruitpatch != null && !khazard) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 4) != null) {
			if (client.getWidget(231, 4).getText().contains("Gileth")) {
				utils.useItem(8010, "break");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", 8010, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8010)).getIndex(), WidgetInfo.INVENTORY.getId()));
				khazard = true;
				stumpcleared = false;
				harvested = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			khazard = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && harvested && client.getLocalPlayer().getWorldArea().intersectsWith(KHAZARDPATCH) && !khazard) {
			NPC alain = utils.findNearestNpc("Gileth");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}

		/**
		 Khazard
		 */

		/**
		 * Catherby
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(CAMELOT) && !catherby) {
			utils.walk(CATHERBYWALK1);
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBYWALK2) && !catherby) {
			utils.walk(CATHERBYWALK3);
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBYPATCH) && fruitpatch.getName().contains("stump") && !catherby) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBYPATCH) && fruitpatch != null && !catherby) {
			utils.useItem(config.fruitSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.fruitSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.fruitSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", 7965, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), fruitpatch.getSceneMinLocation().getX(), fruitpatch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		if (!stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBYPATCH) && fruitpatch != null && !catherby) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 4) != null) {
			if (client.getWidget(231, 4).getText().contains("Ellena")) {
				catherby = true;
				utils.walk(new WorldPoint(2803, 3415, 0));
				stumpcleared = false;
				harvested = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			return PFarmingState.IDLE;
		}
		if (stumpcleared && harvested && client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBYPATCH) && !catherby) {
			NPC alain = utils.findNearestNpc("Ellena");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}


		if (client.getLocalPlayer().getWorldArea().intersectsWith(CATHERBY_DOCKS) && catherby) {
			NPC charter = utils.findNearestNpc("Trader Crewmember");
			utils.interactNPC(MenuAction.NPC_FIFTH_OPTION.getId(), sleepDelay(), charter.getId());
			return PFarmingState.IDLE;
		}
		/**
		 * Catherby
		 */
		/**
		 * Brimhaven
		 */
		if (client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_BOAT) && !brimhaven) {
			GameObject plank = utils.findNearestGameObject("Gangplank");
			utils.useGameObjectDirect(plank, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = false;
			harvested = false;
			checkedHealth = false;
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_DOCKS) && !brimhaven) {
			utils.walk(BRIMHAVEN_PATCH);
			return PFarmingState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_PATCH1) && fruitpatch.getName().contains("stump") && !brimhaven) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			stumpcleared = true;
			return PFarmingState.IDLE;
		}
		if (stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_PATCH1) && fruitpatch != null && !brimhaven) {
			utils.useItem(config.fruitSappling(), "use");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.fruitSappling(), MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(Collections.singletonList(config.fruitSappling())).getIndex(), WidgetInfo.INVENTORY.getId()));
			clientThread.invoke(() -> client.invokeMenuAction("", "", 7964, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), fruitpatch.getSceneMinLocation().getX(), fruitpatch.getSceneMinLocation().getY()));
			return PFarmingState.IDLE;
		}
		if (!stumpcleared && !harvested && client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_PATCH1) && fruitpatch != null && !brimhaven) {
			utils.useGameObjectDirect(fruitpatch, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return PFarmingState.IDLE;
		}
		if (client.getWidget(231, 4) != null) {
			if (client.getWidget(231, 4).getText().contains("Garth")) {
				brimhaven = true;
				reset();
				stumpcleared = false;
				harvested = false;
				return PFarmingState.IDLE;
			}
		}
		if (client.getWidget(219, 1) != null) {
			utils.typeString("1");
			return PFarmingState.IDLE;
		}
		if (stumpcleared && harvested && client.getLocalPlayer().getWorldArea().intersectsWith(BRIMHAVEN_PATCH1) && !brimhaven) {
			NPC alain = utils.findNearestNpc("Garth");
			clientThread.invoke(() -> client.invokeMenuAction("", "", alain.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
			return PFarmingState.IDLE;
		}
		/**
		 * Brimhaven
		 */
		return PFarmingState.TIMEOUT;
	}
	private final Set<Integer> AXES = Set.of(ItemID.BRONZE_AXE, ItemID.IRON_AXE, ItemID.STEEL_AXE, ItemID.BLACK_AXE, ItemID.MITHRIL_AXE, ItemID.ADAMANT_AXE, ItemID.RUNE_AXE, ItemID.GILDED_AXE, ItemID.DRAGON_AXE, ItemID.DRAGON_AXE_OR, ItemID.CRYSTAL_AXE, ItemID.CRYSTAL_AXE_23862);
	private final Set<Integer> AXEZ = Set.of(ItemID.BRONZE_AXE, ItemID.IRON_AXE, ItemID.STEEL_AXE, ItemID.BLACK_AXE, ItemID.MITHRIL_AXE, ItemID.ADAMANT_AXE, ItemID.RUNE_AXE, ItemID.GILDED_AXE, ItemID.DRAGON_AXE, ItemID.DRAGON_AXE_OR, ItemID.CRYSTAL_AXE, ItemID.CRYSTAL_AXE_23862);
	private PFarmingState getBankState() {
		if (!banked) {
			utils.depositAll();
			banked = true;
			return PFarmingState.DEPOSIT_ITEMS;
		}
		if (!utils.inventoryContains(ItemID.COINS_995)) {
			utils.withdrawAllItem(ItemID.COINS_995);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(ItemID.SPADE)) {
			utils.withdrawItem(ItemID.SPADE);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(ItemID.RAKE)) {
			utils.withdrawItem(ItemID.RAKE);
			return PFarmingState.IDLE;
		}
		if (!utils.isItemEquipped(AXES) && !utils.inventoryContains(AXEZ)) {
			utils.withdrawAnyOf(ItemID.BRONZE_AXE, ItemID.IRON_AXE, ItemID.STEEL_AXE, ItemID.BLACK_AXE, ItemID.MITHRIL_AXE, ItemID.ADAMANT_AXE, ItemID.RUNE_AXE, ItemID.GILDED_AXE, ItemID.DRAGON_AXE, ItemID.DRAGON_AXE_OR, ItemID.CRYSTAL_AXE, ItemID.CRYSTAL_AXE_23862);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(ItemID.VARROCK_TELEPORT)) {
			utils.withdrawAllItem(ItemID.VARROCK_TELEPORT);
			return PFarmingState.IDLE;
		}

		if (!utils.inventoryContains(ItemID.FALADOR_TELEPORT)) {
			utils.withdrawAllItem(ItemID.FALADOR_TELEPORT);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(ItemID.LUMBRIDGE_TELEPORT)) {
			utils.withdrawAllItem(ItemID.LUMBRIDGE_TELEPORT);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(ItemID.CAMELOT_TELEPORT)) {
			utils.withdrawAllItem(ItemID.CAMELOT_TELEPORT);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(config.treeSappling())) {
			utils.withdrawItemAmount(config.treeSappling(), 5);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(config.fruitSappling())) {
			utils.withdrawItemAmount(config.fruitSappling(), 5);
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(config.treePayment() + 1)) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 786456));
			utils.withdrawAllItem(config.treePayment());
			banked = true;
			return PFarmingState.IDLE;
		}
		if (!utils.inventoryContains(config.fruitPayment() + 1)) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 786456));
			utils.withdrawAllItem(config.fruitPayment());
			banked = true;
			return PFarmingState.IDLE;
		}
		if (banked && !teled) {
			//clientThread.invoke(() -> client.invokeMenuAction("", "", 8009, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(Collections.singletonList(8009)).getIndex(), WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()));
			//utils.closeBank();
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), 11, 786434));
			return PFarmingState.IDLE;
		}

		return PFarmingState.TIMEOUT;
	}

	public void useWallObject(WallObject targetObject, long sleepDelay, int opcode)
	{
		if(targetObject!=null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), opcode, targetObject.getLocalLocation().getSceneX(), targetObject.getLocalLocation().getSceneY()));
		}
	}
	boolean started = false;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		if (!startTeaks){
			return;
		}

		player = client.getLocalPlayer();
		patch = utils.findNearestGameObject(8388, 8389, 8390, 8391, 19147);
		fruitpatch = utils.findNearestGameObject(7963, 7962, 7964, 7965);

		fence = utils.findNearestWallObject(2186);
		ElkoyNPC = utils.findNearestNpc(4968);

		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (!started) {
			if (utils.util() >=5) {
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
				case IDLE:
					timeout = 1;
					break;
				case IDLE_2:
					timeout = 3;
					break;
				case DROP_INV:
					handleFullInventory();
					break;
				case OPEN_DOOR:
					banked = false;
					WallObject DOOR = utils.findNearestWallObject(36253);
					useWallObject(DOOR, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case FIND_BANK:
					openBank();
					timeout = tickDelay();
					break;
			}
		}
	}
	@Subscribe
	private void onChatMessage(ChatMessage event) {
		String message = ("You dig up the tree");
		String message2 = ("You have successfully cleared");
		String plantmessage = ("You plant the");
		String paygardener = ("You pay the gardener");
		String checkedPlant = ("You examine the tree for signs of disease");
		if (event.getMessage().equals(message) || event.getMessage().contains(message) || event.getMessage().contains(message2))
		{
			stumpcleared = true;
		}
		if (event.getMessage().contains(plantmessage))
		{
			harvested = true;
		}
		if (event.getMessage().contains(checkedPlant) || event.getMessage().equals(checkedPlant)) {
			checkedHealth = true;
		}
		if (event.getMessage().contains(paygardener))
		{
			if (client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchT)) {
				gnomet = true;
			}
			if (client.getLocalPlayer().getWorldArea().intersectsWith(GnomePatchF)) {
				gnomef = true;
			}
		}
	}

	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(false, 100, 600, 250, 300);
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return tickLength;
	}
}