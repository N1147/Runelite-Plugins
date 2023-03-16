package net.runelite.client.plugins.avorkath;

import com.google.inject.Provides;
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
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Utils.Walking;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.apache.commons.lang3.ArrayUtils;


import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;



@PluginDescriptor(
	name = "Auto Vorkath",
	description = "Auto Vorkath. Optional helper mode to handle key boss mechanics only. Banks in Edgeville. Mounted glory required. House in Relekka required.",
	tags = {"vorkath","anarchise","runeleet"},
	enabledByDefault = false
)
public class AVorkathPlugin extends Plugin
{
	@Inject
	private Client client;
	@Provides
	AVorkathConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AVorkathConfig.class);
	}
	@Inject
	private AVorkathConfig configvk;
	@Inject
	private ClientThread clientThread;
	@Inject
	private Core core;
	@Inject
	Walking walking;
	@Inject
	private KeyManager keyManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private OverlayManager overlayManager;

	private Rectangle bounds;
	private int timeout;
	private NPC vorkath;
	private List<WorldPoint> acidSpots = new ArrayList<>();
	boolean FirstWalk = true;
	private List<WorldPoint> acidFreePath = new ArrayList<>();
	private int lastAcidSpotsSize = 0;
	//private final Set<Integer> DIAMOND_SET = Set.of(ItemID.DIAMOND_DRAGON_BOLTS_E, ItemID.DIAMOND_BOLTS_E);

	AVorkathState state;
	LocalPoint beforeLoc;
	Player player;
	MenuEntry targetMenu;
	WorldPoint dodgeRight;
	WorldPoint dodgeLeft;
	Instant botTimer;
	private boolean inFight;
	private Prayer prayerToClick;
	private Random r = new Random();
	public AVorkathPlugin(){
		inFight = false;
	}
	List<String> lootableItems = new ArrayList<>();
	List<TileItem> loot = new ArrayList<>();
	private Prayer prayer;
	boolean startTeaks = false;
	boolean killedvorkath = false;
	boolean noBomb = true;
	boolean noBomb2 = true;

	private int acidFreePathLength = 3;
	boolean banked = false;
	String[] values;
	private void reset() throws IOException, ClassNotFoundException {
		loot.clear();
		lootableItems.clear();
		values = configvk.lootNames().toLowerCase().split("\\s*,\\s*");
		if (!configvk.lootNames().isBlank()) {
			lootableItems.addAll(Arrays.asList(values));
		}
		startTeaks = false;

		state = null;
		killedvorkath = false;
		timeout = 0;

		inFight = false;
		prayerToClick = null;
		noBomb = true;
		noBomb2 = true;
		dodgeRight = null;
		dodgeLeft = null;
		banked = false;
		botTimer = null;
	}

	private boolean started = false;
	private long sleepDelay()
	{
		long sleepLength = core.randomDelay(false, 100, 350, 100, 150);
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) core.randomDelay(false, 0, 2, 1, 1);
		return tickLength;
	}
	@Override
	protected void startUp() throws Exception
	{
		if (!startTeaks) {
			startTeaks = true;
			loot.clear();
			lootableItems.clear();
			values = configvk.lootNames().toLowerCase().split("\\s*,\\s*");
			if (!configvk.lootNames().isBlank()) {
				lootableItems.addAll(Arrays.asList(values));
			}
			noBomb = true;
			noBomb2 = true;
			banked = false;
			botTimer = Instant.now();
			//RUBY_SET.add(ItemID.RUBY_DRAGON_BOLTS_E, ItemID.RUBY_BOLTS_E);
			//DIAMOND_SET.add(ItemID.DIAMOND_DRAGON_BOLTS_E, ItemID.DIAMOND_BOLTS_E);
		} else {
			reset();
		}
	}

	@Inject ConfigManager configManager;


	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	private void openBank() {
		/*GameObject bankTarget = core.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), core.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		}
		 */
		GameObject bank = core.findNearestBank();
		core.useGameObjectDirect(bank);
	}


	int[] ItemIDs;
	public WidgetItem GetFoodItem() {
		WidgetItem item;

		item = core.getInventoryWidgetItem(Collections.singletonList(configvk.foodID()));

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetRangedItem()
	{
		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.DIVINE_RANGING_POTION1, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION4,ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4, ItemID.BASTION_POTION1, ItemID.BASTION_POTION2, ItemID.BASTION_POTION3, ItemID.BASTION_POTION4,ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetCombatItem()
	{
		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4, ItemID.COMBAT_POTION1, ItemID.COMBAT_POTION2, ItemID.COMBAT_POTION3, ItemID.COMBAT_POTION4);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntifireItem()
	{
		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4,ItemID.EXTENDED_SUPER_ANTIFIRE1,ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntiVenomItem() {

		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952, ItemID.ANTIVENOM1_12919 ,ItemID.ANTIVENOM2_12917,ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM4_12913);

		if (item != null) {
			return item;
		}

		return item;
	}
	public AVorkathState getState()
	{
		if (timeout > 0)
		{
			return AVorkathState.TIMEOUT;
		}
		else {
			return getStates();
		}
	}

	private boolean isVorkathAwake() {
		return !core.findNearestNpc(8059).isDead();
	}

	private boolean isInVorkath()
	{
		return ArrayUtils.contains(client.getMapRegions(), 9023);
	}

	private AVorkathState getStates(){
		if (!noBomb2 && !isInVorkath()){
			noBomb2 = true;
			attacked = false;
		}
		if (!noBomb2 && core.findNearestNpc("Zombified Spawn") == null) {
			noBomb2 = true;
		}
		if (!noBomb && !isInVorkath()){
			noBomb = true;
			attacked = false;
		}
		if (core.findNearestNpc(8059) != null && isInVorkath()){
			acidFreePath.clear();
			acidSpots.clear();
			noBomb = true;
			noBomb2 = true;
			attacked = false;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) == 1 && !isInVorkath())
		{
			return AVorkathState.DEACTIVATE_PRAY;
		}
		if (!configvk.helperMode()) {
			if (isInPOH(client) && core.inventoryContains(ItemID.RUBY_BOLTS_E, ItemID.RUBY_DRAGON_BOLTS_E) && core.isItemEquipped(Collections.singleton(ItemID.RUBY_DRAGON_BOLTS_E)) && !core.isItemEquipped(Collections.singleton(ItemID.RUBY_BOLTS_E)) && configvk.useRanged() && !configvk.useBlowpipe()) {
				return AVorkathState.EQUIP_RUBIES;
			}
			if (!isInVorkath() &&  core.inventoryContains(ItemID.RUBY_BOLTS_E, ItemID.RUBY_DRAGON_BOLTS_E) && !core.isItemEquipped(Collections.singleton(ItemID.RUBY_BOLTS_E)) && !core.isItemEquipped(Collections.singleton(ItemID.RUBY_DRAGON_BOLTS_E)) && configvk.useRanged() && !configvk.useBlowpipe()) {
				return AVorkathState.EQUIP_RUBIES;
			}
			if (isInVorkath() && calculateHealth(vorkath, 750) > 265 && calculateHealth(vorkath, 750) <= 750 && core.inventoryContains(ItemID.RUBY_BOLTS_E, ItemID.RUBY_DRAGON_BOLTS_E) && !core.isItemEquipped(Collections.singleton(ItemID.RUBY_BOLTS_E)) && !core.isItemEquipped(Collections.singleton(ItemID.RUBY_DRAGON_BOLTS_E)) && acidSpots.isEmpty() && configvk.useRanged() && !configvk.useBlowpipe()) {
				return AVorkathState.EQUIP_RUBIES;
			}
			if (isInVorkath() && calculateHealth(vorkath, 750) < 265 && core.inventoryContains(ItemID.DIAMOND_DRAGON_BOLTS_E, ItemID.DIAMOND_BOLTS_E) && !core.isItemEquipped(Collections.singleton(ItemID.DIAMOND_BOLTS_E)) && !core.isItemEquipped(Collections.singleton(ItemID.DIAMOND_DRAGON_BOLTS_E)) && acidSpots.isEmpty() && configvk.useRanged() && !configvk.useBlowpipe()) {
				return AVorkathState.EQUIP_DIAMONDS;
			}
			if (core.isItemEquipped(Collections.singleton(configvk.specWeapon())) && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) < configvk.specThreshold() * 10) {
				WidgetItem weapon = core.getInventoryWidgetItem(Collections.singletonList(configvk.normalWeapon()));
				WidgetItem offhand = core.getInventoryWidgetItem(Collections.singletonList(configvk.normalOffhand()));
				if (weapon != null) {
					core.useItem(weapon.getId(), "wield");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.CC_OP.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
				if (offhand != null) {
					core.useItem(offhand.getId(), "wield");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", offhand.getId(), MenuAction.CC_OP.getId(), offhand.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			if (core.isItemEquipped(Collections.singleton(configvk.specWeapon())) && !isInVorkath()) {
				WidgetItem weapon = core.getInventoryWidgetItem(Collections.singletonList(configvk.normalWeapon()));
				WidgetItem offhand = core.getInventoryWidgetItem(Collections.singletonList(configvk.normalOffhand()));
				if (weapon != null) {
					core.useItem(weapon.getId(), "wield");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.CC_OP.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
				if (offhand != null) {
					core.useItem(offhand.getId(), "wield");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", offhand.getId(), MenuAction.CC_OP.getId(), offhand.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			if (!loot.isEmpty() && !core.inventoryFull() && isInVorkath()) {
				return AVorkathState.LOOT_ITEMS;
			}
			if (core.inventoryContains(configvk.foodID()) && core.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && isInVorkath()) {
				return AVorkathState.EAT_FOOD;
			}
		}
		if (!acidSpots.isEmpty() && isInVorkath()){
			return AVorkathState.ACID_WALK;
		}
		if (!noBomb && isInVorkath()){
			return AVorkathState.HANDLE_BOMB;
		}
		if (!noBomb2 && isInVorkath()){
			return AVorkathState.HANDLE_ICE;
		}
		if (noBomb2 && isInVorkath() && core.isItemEquipped(Collections.singleton(ItemID.SLAYERS_STAFF))) {
			core.useItem(configvk.normalWeapon(), "wield");
			return AVorkathState.TIMEOUT;
		}
		if (!configvk.helperMode()) {
			if (configvk.antivenomplus() && client.getVar(VarPlayer.POISON) > 0 && isInVorkath()) {
				return AVorkathState.DRINK_ANTIVENOM;
			}
			if (client.getBoostedSkillLevel(Skill.RANGED) <= configvk.potThreshold() && isInVorkath() && configvk.useRanged()) {
				return AVorkathState.DRINK_RANGE;
			}

			if (client.getBoostedSkillLevel(Skill.STRENGTH) <= configvk.potThreshold() && isInVorkath() && !configvk.useRanged()) {
				return AVorkathState.DRINK_COMBAT;
			}
			if (configvk.superantifire() && client.getVarbitValue(6101) == 0 && isInVorkath()) {
				return AVorkathState.DRINK_ANTIFIRE;
			}
			if (!configvk.superantifire() && client.getVarbitValue(3981) == 0 && isInVorkath()) {
				return AVorkathState.DRINK_ANTIFIRE;
			}
		}
		if (client.getVar(Varbits.QUICK_PRAYER) == 0 && isInVorkath() && isVorkathAwake() && acidSpots.isEmpty() && noBomb2 && noBomb && vorkath != null)
		{
			return AVorkathState.ACTIVATE_PRAY;
		}
		if (!configvk.helperMode()) {
			if (core.findNearestNpc(8059) != null && isInVorkath() && loot.isEmpty() && core.inventoryItemContainsAmount(configvk.foodID(), 3, false, false)) {
				return AVorkathState.WAKE_VORKATH;
			}
			if (core.findNearestNpc(8059) != null && isInVorkath() && loot.isEmpty() && !core.inventoryItemContainsAmount(configvk.foodID(), 3, false, false)) {
				return AVorkathState.WALK_SECOND;
			}
			if (player.getWorldLocation().distanceTo(vorkath.getWorldArea()) <= 1 && configvk.useRanged()) {
				return AVorkathState.MOVE_AWAY;
			}
			if (!core.isItemEquipped(Collections.singleton(configvk.specWeapon())) && core.inventoryFull() && core.inventoryContains(configvk.foodID()) && configvk.normalOffhand() != 0 && calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && core.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()) {
				return AVorkathState.EAT_FOOD;
			}
			if (!core.isItemEquipped(Collections.singleton(configvk.specWeapon())) && calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && core.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()) {
				return AVorkathState.EQUIP_SPEC;
			}
			if (core.isItemEquipped(Collections.singleton(configvk.specWeapon())) && calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && core.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()) {
				return AVorkathState.SPECIAL_ATTACK;
			}
			if (noBomb && noBomb2 && core.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && client.getLocalPlayer().getInteracting() != vorkath && isInVorkath()) {
				return AVorkathState.ATTACK_VORKATH;
			}
		}
		return AVorkathState.TIMEOUT;
	}

	private boolean attacked = false;
	private int AcidTickCount = 0;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		player = client.getLocalPlayer();
		if (client != null && player != null)
		{
			if (!startTeaks) {
				return;
			}
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					timeout--;
					break;
				case SPECIAL_ATTACK:
					if (core.isItemEquipped(Collections.singleton(configvk.specWeapon()))){
						core.specialAttack();
						//clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					}
					break;
				case MOVE_AWAY:
					walking.walkTileOnScreen(new WorldPoint(player.getWorldLocation().getX(), player.getWorldLocation().getY() - 3, player.getWorldLocation().getPlane()));
					//core.walk(new WorldPoint(player.getWorldLocation().getX(), player.getWorldLocation().getY() - 3, player.getWorldLocation().getPlane()));
					//timeout = tickDelay();
					break;
				case EQUIP_SPEC:
					WidgetItem weapon = core.getInventoryWidgetItem(Collections.singletonList(configvk.specWeapon()));
					if (weapon != null) {
						core.useItem(weapon.getId(), "wield");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.CC_OP.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					break;
				case EQUIP_RUBIES:
					WidgetItem boltz = core.getInventoryWidgetItem(ItemID.RUBY_BOLTS_E, ItemID.RUBY_DRAGON_BOLTS_E);
					if (boltz != null) {
						core.useItem(boltz.getId(), "wield");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", boltz.getId(), MenuAction.CC_OP.getId(), boltz.getIndex(), WidgetInfo.INVENTORY.getId());
					}
					break;
				case DRINK_POOL:
					GameObject Pool = core.findNearestGameObject(29240, 29241);
					core.useGameObjectDirect(Pool);
					timeout = tickDelay();
					break;
				case EQUIP_DIAMONDS:
					WidgetItem bolts = core.getInventoryWidgetItem(ItemID.DIAMOND_DRAGON_BOLTS_E, ItemID.DIAMOND_BOLTS_E);
					if (bolts != null) {
						core.useItem(bolts.getId(), "wield");
					}
					//timeout = tickDelay();
					break;
				case ACID_WALK:
					calculateAcidFreePath();
					AcidTickCount++;
					if (acidFreePathLength >= 3) {
						if (FirstWalk) {
							/*if (configvk.wooxWalk() && AcidTickCount > configvk.wwTicks() ) {
								if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= (client.getRealSkillLevel(Skill.HITPOINTS) - 10)) {
									core.attackNPCDirect(vorkath);
								}
								AcidTickCount = 0;
								FirstWalk = false;
								break;
							}*/
							walking.walkTileOnScreen(acidFreePath.get(1));
							//core.walk(acidFreePath.get(1));
							FirstWalk = false;
						}

						if (!FirstWalk) {
							walking.walkTileOnScreen(acidFreePath.get(acidFreePath.size() - 1));
							//core.walk(acidFreePath.get(acidFreePath.size() - 1));
							FirstWalk = true;
						}
						break;
					}
					break;
				case ATTACK_VORKATH:
					if (isInVorkath()) {
						core.attackNPCDirect(vorkath);
					}
					break;
				case HANDLE_ICE:
					if (!core.isItemEquipped(Collections.singleton(ItemID.SLAYERS_STAFF))) {
						core.useItem(ItemID.SLAYERS_STAFF, "wield");
					}
					else {
						if (client.getLocalPlayer().getInteracting() == null) {
							core.attackNPC("Zombified Spawn");
						}
					}
					break;
				case HANDLE_BOMB:
					final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
					//final LocalPoint localLoc = LocalPoint.fromWorld(client, loc);
					dodgeRight = new WorldPoint(loc.getX() + 8, loc.getY(), client.getPlane());
					dodgeLeft = new WorldPoint(loc.getX() - 8, loc.getY(), client.getPlane());
					if (loc.distanceTo(dodgeLeft) <= 1){
						noBomb = true;
						noBomb2 = true;
					}
					if (loc.distanceTo(dodgeRight) <= 1){
						noBomb = true;
						noBomb2 = true;
					}
					if (loc.getX() < 6208) {
						walking.walkTileOnScreen(dodgeRight);
						//core.walk(dodgeRight);
						timeout = tickDelay();
						noBomb = true;
						noBomb2 = true;
						break;
					} else {
						walking.walkTileOnScreen(dodgeLeft);
						//core.walk(dodgeLeft);
						timeout = tickDelay();
						noBomb = true;
						noBomb2 = true;
						break;
					}
				case DEACTIVATE_PRAY:
					core.clickWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
					//clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1,  MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case ACTIVATE_PRAY:
					core.clickWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
					//clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1,  MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case WAKE_VORKATH:
					core.attackNPC("Vorkath");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", core.findNearestNpc("Vorkath").getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
					timeout = tickDelay();
					break;
				case MOVING:
					//core.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case DRINK_ANTIVENOM:
					WidgetItem ven = GetAntiVenomItem();
					if (ven != null) {
						core.useItem(ven.getId(), "drink");
						//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", ven.getId(), MenuAction.CC_OP.getId(), ven.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					//timeout = tickDelay();
					break;
				case DRINK_COMBAT:
					WidgetItem Cpot = GetCombatItem();
					if (Cpot != null) {
						core.useItem(Cpot.getId(), "drink");
						//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", Cpot.getId(), MenuAction.CC_OP.getId(), Cpot.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					break;
				case EAT_FOOD:
					WidgetItem food = GetFoodItem();
					if (food != null) {
						core.useItem(food.getId(), "eat");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", food.getId(), MenuAction.CC_OP.getId(), food.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					break;
				case DRINK_RANGE:
					WidgetItem Rpot = GetRangedItem();
					if (Rpot != null) {
						core.useItem(Rpot.getId(), "drink");
					}
					//timeout = tickDelay();
					break;
				case DRINK_ANTIFIRE:
					WidgetItem overload = GetAntifireItem();
					if (overload != null) {
						core.useItem(overload.getId(), "drink");
					}
					//timeout = tickDelay();
					break;
				case WALK_SECOND:
					WidgetItem tab2 = core.getInventoryWidgetItem(Collections.singletonList(8013));
					core.useItem(tab2.getId(), "break");
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.CC_OP.getId(), core.getInventoryWidgetItem(Collections.singletonList(8013)).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case LOOT_ITEMS:
					lootItem(spawnedItems);
					//lootItem(loot);
					//timeout = tickDelay();
					break;
			}
		}
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
	private int calculateHealth(NPC target, Integer maxHealth)
	{
		if (target == null || target.getName() == null)
		{
			return -1;
		}

		final int healthScale = target.getHealthScale();
		final int healthRatio = target.getHealthRatio();
		//final Integer maxHealth = 750;

		if (healthRatio < 0 || healthScale <= 0 || maxHealth == null)
		{
			return -1;
		}

		return (int)((maxHealth * healthRatio / healthScale) + 0.5f);
	}


	@Subscribe
	private void onClientTick(ClientTick event)
	{
		if (acidSpots.size() != lastAcidSpotsSize)
		{
			if (acidSpots.size() == 0)
			{
				acidFreePath.clear();
			}
			else
			{
				calculateAcidFreePath();
			}

			lastAcidSpotsSize = acidSpots.size();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event) {
		if (vorkath != null) {
			final Actor actor = event.getActor();
			if (actor.getAnimation() == 7950 && actor.getName().contains("Vorkath")) {
				Widget widget = client.getWidget(10485775);

				if (widget != null) {
					bounds = widget.getBounds();
				}


			}
			/*if (actor.getAnimation() == 7949 && actor.getName().contains("Vorkath")) {
				if (client.getVar(Varbits.QUICK_PRAYER) == 1) {
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
				}
			}*/
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event) {
		if (this.client.getGameState() == GameState.LOGGED_IN) {
			final Projectile projectile = event.getProjectile();

			final WorldPoint loc = client.getLocalPlayer().getWorldLocation();

			//final LocalPoint localLoc = LocalPoint.fromWorld(client, loc);

			if (projectile.getId() == ProjectileID.VORKATH_BOMB_AOE) {
				noBomb = false;
			}
			if (projectile.getId() == ProjectileID.VORKATH_ICE) {
				noBomb2 = false;
				if (client.getLocalPlayer().getInteracting() != null) {
					walking.walkTileOnScreen(loc);
					//core.walk(loc);
				}
			}
		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event) {
		final Projectile proj = event.getProjectile();
		final LocalPoint loc = event.getPosition();
		final WorldPoint location = WorldPoint.fromLocal(client, loc);
		final LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		final WorldPoint loc1 = client.getLocalPlayer().getWorldLocation();
		//final LocalPoint localLoc = LocalPoint.fromWorld(client, loc1);
		if (proj.getId() == ProjectileID.VORKATH_POISON_POOL_AOE) {
			addAcidSpot(WorldPoint.fromLocal(client, loc));
		}
		if (proj.getId() == ProjectileID.VORKATH_ICE) {
			noBomb2 = false;
			if (client.getLocalPlayer().getInteracting() != null) {
				walking.walkTileOnScreen(loc1);
				//core.walk(loc1);
			}
		}
		if (proj.getId() == ProjectileID.VORKATH_BOMB_AOE) {
			noBomb = false;
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		final NPC npc = event.getNpc();
		if (npc.getName() == null) {
			return;
		}

		if (npc.getName().equals("Vorkath")) {
			vorkath = event.getNpc();
		}

		if (npc.getName().equals("Zombified Spawn")) {
			noBomb2 = false;
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		final NPC npc = event.getNpc();
		if (npc.getName() == null) {
			return;
		}
		Widget widget = client.getWidget(10485775);
		if (widget != null) {
			bounds = widget.getBounds();
		}
		if (npc.getName().equals("Vorkath")) {
			vorkath = null;
			if (client.getVar(Varbits.QUICK_PRAYER) == 1) {
				core.clickWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
				//clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}
		}
		if (npc.getName().equals("Zombified Spawn")) {
			noBomb2 = true;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) throws IOException, ClassNotFoundException {
		spawnedItems.clear();
		GameState gamestate = event.getGameState();

		if (gamestate == GameState.LOADING && inFight) {
			reset();
		}
	}


	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);
	public static boolean isInPOH(Client client) {
		return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);
	}
	@Subscribe
	private void onChatMessage(ChatMessage event) {

			if (event.getType() != ChatMessageType.GAMEMESSAGE)
			{
				return;
			}

			Widget widget = client.getWidget(10485775);
			if (widget != null)
			{
				bounds = widget.getBounds();
			}

			String prayerMessage = ("Your prayers have been disabled!");
			String poisonMessage = ("You have been poisoned by venom!");
			String poisonMessageNV = ("You have been poisoned!");
			String frozenMessage = ("You have been frozen!");
			String spawnExplode = ("The spawn violently explodes, unfreezing you as it does so.");
			String unfrozenMessage = ("You become unfrozen as you kill the spawn.");
			String deathMessage = ("Oh dear, you are dead!");

			if ((event.getMessage().equals(prayerMessage) || event.getMessage().contains(prayerMessage)))
			{
				core.clickWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
				//clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}
			if ((event.getMessage().contains(deathMessage) || event.getMessage().equals(deathMessage))){
				//TODO: LOGOUT, SKIPPED
				//clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 11927560));
			}
			if (((event.getMessage().equals(frozenMessage))))
			{
				noBomb = false;
				noBomb2 = false;
			}
			if (((event.getMessage().equals(poisonMessage))))
			{
				WidgetItem pot = GetAntiVenomItem();
				if (pot != null) {
					core.useItem(pot.getId(), "drink");
				}
			}
			if (((event.getMessage().equals(poisonMessageNV))))
			{
				WidgetItem pot = GetAntiVenomItem();
				if (pot != null && configvk.antivenomplus()) {
					core.useItem(pot.getId(), "drink");
				}
			}
			if ((event.getMessage().equals(spawnExplode) || (event.getMessage().equals(unfrozenMessage))))
			{
				noBomb = true;
				noBomb2 = true;
				if (isInVorkath()) {
					core.attackNPCDirect(vorkath);
				}
			}

	}

	private void addAcidSpot(WorldPoint acidSpotLocation)
	{
		if (!acidSpots.contains(acidSpotLocation))
		{
			acidSpots.add(acidSpotLocation);
		}
	}

	private void calculateAcidFreePath()
	{
		acidFreePath.clear();

		if (vorkath == null)
		{
			return;
		}

		final int[][][] directions = {
				{
						{0, 1}, {0, -1} // Positive and negative Y
				},
				{
						{1, 0}, {-1, 0} // Positive and negative X
				}
		};

		List<WorldPoint> bestPath = new ArrayList<>();
		double bestClicksRequired = 99;

		final WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
		final WorldPoint vorkLoc = vorkath.getWorldLocation();
		final int maxX = vorkLoc.getX() + 14;
		final int minX = vorkLoc.getX() - 8;
		final int maxY = vorkLoc.getY() - 1;
		final int minY = vorkLoc.getY() - 8;

		// Attempt to search an acid free path, beginning at a location
		// adjacent to the player's location (including diagonals)
		for (int x = -1; x < 2; x++)
		{
			for (int y = -1; y < 2; y++)
			{
				final WorldPoint baseLocation = new WorldPoint(playerLoc.getX() + x,
						playerLoc.getY() + y, playerLoc.getPlane());

				if (acidSpots.contains(baseLocation) || baseLocation.getY() < minY || baseLocation.getY() > maxY)
				{
					continue;
				}

				// Search in X and Y direction
				for (int d = 0; d < directions.length; d++)
				{
					// Calculate the clicks required to start walking on the path
					double currentClicksRequired = Math.abs(x) + Math.abs(y);
					if (currentClicksRequired < 2)
					{
						currentClicksRequired += Math.abs(y * directions[d][0][0]) + Math.abs(x * directions[d][0][1]);
					}
					if (d == 0)
					{
						// Prioritize a path in the X direction (sideways)
						currentClicksRequired += 0.5;
					}

					List<WorldPoint> currentPath = new ArrayList<>();
					currentPath.add(baseLocation);

					// Positive X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][0][0],
								baseLocation.getY() + i * directions[d][0][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					// Negative X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][1][0],
								baseLocation.getY() + i * directions[d][1][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					if (currentPath.size() >= this.acidFreePathLength && currentClicksRequired < bestClicksRequired
							|| (currentClicksRequired == bestClicksRequired && currentPath.size() > bestPath.size()))
					{
						bestPath = currentPath;
						bestClicksRequired = currentClicksRequired;
					}
				}
			}
		}

		if (bestClicksRequired != 99)
		{
			acidFreePath = bestPath;
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event) {
		final GameObject obj = event.getGameObject();

		if (obj.getId() == ObjectID.ACID_POOL || obj.getId() == ObjectID.ACID_POOL_32000) {
			addAcidSpot(obj.getWorldLocation());
		}
	}
	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		final GameObject obj = event.getGameObject();
		if (obj.getId() == ObjectID.ACID_POOL || obj.getId() == ObjectID.ACID_POOL_32000) {
			acidSpots.remove(obj.getWorldLocation());
		}
	}
}
enum AVorkathState
{
	MOVING,
	TIMEOUT,
	FIND_BANK,
	EAT_FOOD,
	EQUIP_SPEC,
	DRINK_COMBAT,
	SPECIAL_ATTACK,
	WAKE_VORKATH,
	LOOT_ITEMS,
	ACTIVATE_PRAY,
	DEACTIVATE_PRAY,
	ACID_WALK,
	HANDLE_BOMB,
	ATTACK_VORKATH,
	HANDLE_ICE,
	DRINK_POOL,
	EQUIP_DIAMONDS,
	EQUIP_RUBIES,
	MOVE_AWAY,
	DRINK_RANGE,
	DRINK_ANTIVENOM,
	DRINK_ANTIFIRE,
	WALK_FIRST,
	WALK_SECOND;
}