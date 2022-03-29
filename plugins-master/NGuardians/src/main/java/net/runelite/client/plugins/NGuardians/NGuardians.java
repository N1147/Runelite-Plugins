package net.runelite.client.plugins.NGuardians;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
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
import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NGuardians",
	description = "Automatic Guardians of the Rift.",
	tags = {"ztd","numb","guardians", "rift"},
	enabledByDefault = false
)
public class NGuardians extends Plugin
{
	@Override
	protected void startUp() throws IOException {
		reset();
	}

	@Override
	protected void shutDown() throws IOException {
		reset();
		started = false;
	}
	private void reset() throws IOException {
		loaded = false;
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
	}
	@Provides
	NGuardiansConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(NGuardiansConfig.class);
	}
	@Inject
	private NGuardiansConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;
	@Inject
	private PUtils utils;
	Player enemy;
	Instant timer;
	private boolean started = false;
	private NGuardiansState state;
	private int timeout = 0;
	private int Giant = 0;
	private boolean loaded = false;

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NGuardians")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!loaded) {
				loaded = true;
				state = null;
			} else {
				reset();
			}
		}
	}

	public NGuardiansState getState()
	{
		if (timeout > 0)
		{
			return NGuardiansState.TIMEOUT;
		}
		if (client.getLocalPlayer().getAnimation() != -1 && client.getLocalPlayer().getAnimation() != 791 && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED){
			return NGuardiansState.ANIMATING;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(LOBBY)) {
			GameObject PORTAL = utils.findNearestGameObject("Barrier");
			clientThread.invoke(() -> client.invokeMenuAction("", "", PORTAL.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), PORTAL.getSceneMinLocation().getX(),PORTAL.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame)){
			return getStates();
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame) && !client.getLocalPlayer().getWorldArea().intersectsWith(LOBBY)){
			if (utils.inventoryContains(ItemID.GUARDIAN_ESSENCE)) {
				GameObject Altar = utils.findNearestGameObject("Altar");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Altar.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Altar.getSceneMinLocation().getX(),Altar.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
			if (!utils.inventoryContains(ItemID.GUARDIAN_ESSENCE)) {
				if (GiantPouchFilled) {
					if (utils.inventoryContains(ItemID.COLOSSAL_POUCH)) {
						WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.COLOSSAL_POUCH);
						clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					return NGuardiansState.IDLE;
				}
				if (LargePouchFilled) {
					if (utils.inventoryContains(ItemID.GIANT_POUCH)) {
						WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.GIANT_POUCH);
						clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					return NGuardiansState.IDLE;
				}
				if (FullPouches){
					if (utils.inventoryContains(ItemID.SMALL_POUCH)) {
						WidgetItem Pouch = utils.getInventoryWidgetItem(ItemID.SMALL_POUCH);
						clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Pouch.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(ItemID.MEDIUM_POUCH)) {
						WidgetItem Pouch2 = utils.getInventoryWidgetItem(ItemID.MEDIUM_POUCH);
						clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch2.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Pouch2.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(ItemID.LARGE_POUCH)) {
						WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.LARGE_POUCH);
						clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					FullPouches = false;
					return NGuardiansState.IDLE;
				}
				GameObject Portal = utils.findNearestGameObject("Portal");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(),Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		return NGuardiansState.UNHANDLED_STATE;
	}
	private WorldArea EssenceMine = new WorldArea(new WorldPoint(3636, 9497, 0), new WorldPoint(3646, 9513, 0));
	private WorldArea WEssenceMine = new WorldArea(new WorldPoint(3583, 9395, 0), new WorldPoint(3596, 9514, 0));

	private WorldArea LOBBY = new WorldArea(new WorldPoint(3600, 9460, 0), new WorldPoint(3622, 9483, 0));

	private boolean GuardiansNeeded = false;
	private boolean FullPouches = false;
	private boolean GiantPouchHalfFilled = false;
	private boolean GiantPouchFilled = false;

	private boolean LargePouchHalfFilled = false;
	private boolean LargePouchFilled = false;

	private NGuardiansState getStates() {
		/*if (client.getWidget(219, 1) != null){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), 1, 14352385));
		}
		if (client.getWidget(217, 6).getText().contains("Thanks")){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), -1, 14221317));
		}
		if (client.getWidget(217, 6).getText().contains("Can you repair my")){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), -1, 14221317));
		}
		if (client.getWidget(231, 6).getText().contains("can you leave me alone")){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), -1, 15138821));
		}
		if (client.getWidget(231, 6).getText().contains("A simple transfiguration spell")){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), -1, 15138821));
		}
		if (client.getWidget(231, 6).getText().contains("What do you want")){
			clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), -1, 15138821));
		}
		if (utils.inventoryContains(ItemID.LARGE_POUCH_5513, ItemID.MEDIUM_POUCH_5511)) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", 2, MenuAction.CC_OP.getId(), -1, 14286953));
			return NGuardiansState.IDLE;
		}*/
		if (client.getWidget(746, 29).getText().equals("7/8") || client.getWidget(746, 29).getText().equals("9/10") || client.getWidget(746, 29).getText().equals("8/10") || client.getWidget(746, 29).getText().equals("7/10")|| client.getWidget(746, 29).getText().equals("6/10")|| client.getWidget(746, 29).getText().equals("5/10") || client.getWidget(746, 29).getText().equals("4/10") || client.getWidget(746, 29).getText().equals("3/10") || client.getWidget(746, 29).getText().equals("2/10") || client.getWidget(746, 29).getText().equals("1/10") || client.getWidget(746, 29).getText().equals("0/10") || client.getWidget(746, 29).getText().equals("6/8") || client.getWidget(746, 29).getText().equals("5/8") || client.getWidget(746, 29).getText().equals("4/8") || client.getWidget(746, 29).getText().equals("3/8") || client.getWidget(746, 29).getText().equals("2/8") || client.getWidget(746, 29).getText().equals("1/8") || client.getWidget(746, 29).getText().equals("0/8")) {
			GuardiansNeeded = true;
		}
		if (client.getWidget(746, 29).getText().equals("10/10") || client.getWidget(746, 29).getText().equals("8/8")) {
			GuardiansNeeded = false;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.CATALYTIC_GUARDIAN_STONE, ItemID.ELEMENTAL_GUARDIAN_STONE)){
			NPC GREAT_GUARDIAN = utils.findNearestNpc(11403);
			clientThread.invoke(() -> client.invokeMenuAction("", "", GREAT_GUARDIAN.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !GuardiansNeeded && utils.inventoryContains(ItemID.OVERCHARGED_CELL, ItemID.WEAK_CELL, ItemID.STRONG_CELL, ItemID.MEDIUM_CELL)) {
			GroundObject CELL_TILE = utils.findNearestGroundObject(ObjectID.INACTIVE_CELL_TILE_43739, ObjectID.WEAK_CELL_TILE, ObjectID.MEDIUM_CELL_TILE, ObjectID.STRONG_CELL_TILE, ObjectID.OVERPOWERED_CELL_TILE);
			clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE.getLocalLocation().getSceneX(), CELL_TILE.getLocalLocation().getSceneY()));
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && GuardiansNeeded && utils.inventoryContains(ItemID.OVERCHARGED_CELL, ItemID.WEAK_CELL, ItemID.STRONG_CELL, ItemID.MEDIUM_CELL)) {
			GameObject ESSENCE_PILE = utils.findNearestGameObject(43722, 43723);
			clientThread.invoke(() -> client.invokeMenuAction("", "", ESSENCE_PILE.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), ESSENCE_PILE.getSceneMinLocation().getX(),ESSENCE_PILE.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !utils.inventoryContains(ItemID.UNCHARGED_CELL)) {
			GameObject CELL_TABLE = utils.findNearestGameObject(43732);
			clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TABLE.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION.getId(), CELL_TABLE.getSceneMinLocation().getX(),CELL_TABLE.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && !GiantPouchFilled) {
			if (utils.inventoryContains(ItemID.COLOSSAL_POUCH)) {
				WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.COLOSSAL_POUCH);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGuardiansState.IDLE;
			}
		}
		if (utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && !LargePouchFilled) {
			if (utils.inventoryContains(ItemID.GIANT_POUCH)) {
				WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.GIANT_POUCH);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGuardiansState.IDLE;
			}
		}
		if (utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && !FullPouches) {
			if (utils.inventoryContains(ItemID.SMALL_POUCH)) {
				WidgetItem Pouch = utils.getInventoryWidgetItem(ItemID.SMALL_POUCH);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), Pouch.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (utils.inventoryContains(ItemID.MEDIUM_POUCH)) {
				WidgetItem Pouch2 = utils.getInventoryWidgetItem(ItemID.MEDIUM_POUCH);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch2.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), Pouch2.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (utils.inventoryContains(ItemID.LARGE_POUCH)) {
				WidgetItem Pouch3 = utils.getInventoryWidgetItem(ItemID.LARGE_POUCH);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			FullPouches = true;
			return NGuardiansState.IDLE;
		}
		if (MinedEssence && client.getLocalPlayer().getWorldArea().intersectsWith(OUTSIDE_MINE.toWorldArea())) {
			GameObject BENCH = utils.findNearestGameObject(43754);
			utils.walk(BENCH.getWorldLocation());
			return NGuardiansState.IDLE;
		}

		if (client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !utils.inventoryFull()  && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED) {
			GameObject GUARDIAN_PARTS = utils.findNearestGameObject(43720);
			clientThread.invoke(() -> client.invokeMenuAction("", "", GUARDIAN_PARTS.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), GUARDIAN_PARTS.getSceneMinLocation().getX(), GUARDIAN_PARTS.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && utils.inventoryFull()) {
			GameObject Portal = utils.findNearestGameObject(38044);
			clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(),Portal.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		GameObject MinePortal = utils.findNearestGameObject("Portal");
		if (MinePortal != null && MinePortal.getWorldLocation().toWorldArea().intersectsWith(InsideGame) && !utils.inventoryFull()) {
			if (client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				GroundObject Rubble = utils.findNearestGroundObject(43726);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Rubble.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Rubble.getLocalLocation().getSceneX(), Rubble.getLocalLocation().getSceneY()));
				return NGuardiansState.IDLE;
			}
			if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", MinePortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), MinePortal.getSceneMinLocation().getX(), MinePortal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (MinedEssence && client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
			GroundObject Rubble = utils.findNearestGroundObject(43726);
			clientThread.invoke(() -> client.invokeMenuAction("", "", Rubble.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Rubble.getLocalLocation().getSceneX(), Rubble.getLocalLocation().getSceneY()));
			return NGuardiansState.IDLE;
		}
		if (!MinedEssence && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED) {
			if (!client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
				GroundObject Rubble = utils.findNearestGroundObject(43724);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Rubble.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Rubble.getLocalLocation().getSceneX(), Rubble.getLocalLocation().getSceneY()));
				return NGuardiansState.IDLE;
			}
			if (client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				GameObject GUARDIAN_PARTS = utils.findNearestGameObject(43719);
				clientThread.invoke(() -> client.invokeMenuAction("", "", GUARDIAN_PARTS.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), GUARDIAN_PARTS.getSceneMinLocation().getX(), GUARDIAN_PARTS.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && MinedEssence && !utils.inventoryFull()) {
			if (utils.inventoryContains(ItemID.AIR_RUNE, ItemID.WATER_RUNE, ItemID.MIND_RUNE, ItemID.EARTH_RUNE, ItemID.CHAOS_RUNE, ItemID.NATURE_RUNE, ItemID.BODY_RUNE, ItemID.FIRE_RUNE, ItemID.LAW_RUNE)) {
				GameObject RUNE_POOL = utils.findNearestGameObject(43696);
				clientThread.invoke(() -> client.invokeMenuAction("", "", RUNE_POOL.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), RUNE_POOL.getSceneMinLocation().getX(), RUNE_POOL.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
			GameObject ESS_WORKBENCH = utils.findNearestGameObject(43754);
			clientThread.invoke(() -> client.invokeMenuAction("", "", ESS_WORKBENCH.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), ESS_WORKBENCH.getSceneMinLocation().getX(), ESS_WORKBENCH.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 19).getSpriteId() == 4357 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_FIRE)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Fire");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (config.blood() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 19).getSpriteId() == 4364 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_BLOOD)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Blood");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (config.death() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 19).getSpriteId() == 4363 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_DEATH)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Death");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 44) {//NATURE
			if (client.getWidget(746, 22).getSpriteId() == 4361 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_NATURE)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Nature");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 54) {//LAW
			if (client.getWidget(746, 22).getSpriteId() == 4362 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_LAW)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Law");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 9) {//EARTH
			if (client.getWidget(746, 19).getSpriteId() == 4356 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_EARTH)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Earth");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 35) {//CHAOS
			if (client.getWidget(746, 22).getSpriteId() == 4360 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_CHAOS)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Chaos");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (config.cosmic() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 27) {//COSMIC
			if (client.getWidget(746, 22).getSpriteId() == 4359 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_COSMIC)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Cosmic");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 5) {//WATER
			if (client.getWidget(746, 19).getSpriteId() == 4355 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_WATER)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Water");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 20) {//BODY
			if (client.getWidget(746, 22).getSpriteId() == 4358 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_BODY)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Body");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 2) {//MIND
			if (client.getWidget(746, 22).getSpriteId() == 4354 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_MIND)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Mind");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && utils.inventoryContains(ItemID.GUARDIAN_ESSENCE) && utils.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 1) {//AIR
			if (client.getWidget(746, 19).getSpriteId() == 4353 || utils.inventoryContains(ItemID.PORTAL_TALISMAN_AIR)) {
				GameObject Portal = utils.findNearestGameObject("Guardian of Air");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		return NGuardiansState.TIMEOUT;
	}
	private WorldPoint OUTSIDE_MINE = new WorldPoint(3633, 9503, 0);
	private WorldArea InsideGame = new WorldArea(new WorldPoint(3580, 9483, 0), new WorldPoint(3654, 9528, 0));
	private boolean MinedEssence = false;
	GameObject ActiveCataRift = null;
	GameObject ActiveEleRift = null;

	private String POUCH_FULL = "cannot add any more essence";
	private String POUCH_EMPTY = "no essences in this pouch";
	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains(POUCH_FULL) && !GiantPouchFilled) {
			GiantPouchFilled = true;
		}
		if (message.contains(POUCH_FULL) && !LargePouchFilled) {
			LargePouchFilled = true;
		}
		if (message.contains(POUCH_EMPTY) && GiantPouchFilled) {
			GiantPouchFilled = false;
		}
		if (message.contains(POUCH_EMPTY) && LargePouchFilled) {
			LargePouchFilled = false;
		}

	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (!loaded) {
			return;
		}
		if (!utils.inventoryContains(ItemID.GUARDIAN_FRAGMENTS)) {
			MinedEssence = false;
		}
		if (utils.inventoryItemContainsAmount(ItemID.GUARDIAN_FRAGMENTS, config.essence(), true, false)) {
			MinedEssence = true;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
			if (utils.inventoryContains(ItemID.GUARDIAN_FRAGMENTS)) {
				MinedEssence = true;
			}
		}
	}


	@Subscribe
	public void onGameTick(GameTick event) throws IOException {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			return;
		}
		if (!loaded) {
			return;
		}
		/*if (client.getWidget(746, 29).getText().isEmpty()) {
			return;
		}*/

		if (client != null && client.getLocalPlayer() != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case IDLE:
					timeout = (int)utils.randomDelay(false, 0, 1, 1, 1);
					break;
			}

		}
	}

}