package net.runelite.client.plugins.NGuardians;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.Utils.Walking;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.AllPermission;
import java.security.Key;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@PluginDescriptor(
	name = "NGuardians (Auto)",
	description = "Automatic Guardians of the Rift.",
	tags = {"guardians", "rift", "anarchise"},
	enabledByDefault = false
)
@Slf4j
public class Guardians extends Plugin
{
	@Override
	protected void startUp() throws IOException, ClassNotFoundException {
		reset();
	}

	@Override
	protected void shutDown() throws IOException, ClassNotFoundException {
		reset();
		started = false;
	}
	private void reset() throws IOException, ClassNotFoundException {

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
	private Core core;
	Player enemy;
	Instant timer;
	private boolean started = false;
	private NGuardiansState state;
	private int timeout = 0;
	private int Giant = 0;
	@Inject Walking walking;
	public NGuardiansState getState() throws IOException, ClassNotFoundException {
		if (timeout > 0)
		{
			return NGuardiansState.TIMEOUT;
		}
		if (client.getLocalPlayer().getAnimation() != -1 && client.getLocalPlayer().getAnimation() != 791 && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED){
			return NGuardiansState.ANIMATING;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(LOBBY)) {
			GameObject PORTAL = core.findNearestGameObject(43849);
			core.sendGameMessage("ENTERING GAME");
			core.useGameObjectDirect(PORTAL);
			return NGuardiansState.IDLE;
		}
		if (!LargePouchHalfFilled) {
			if (Entered() < 7 && !LargePouchHalfFilled) {
				return NGuardiansState.IDLE;
			}
			if (Entered() >= 7) {
				LargePouchHalfFilled = true;
			}
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame)){
			return getStates();
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame) && !client.getLocalPlayer().getWorldArea().intersectsWith(LOBBY)){
			if (core.inventoryContains(ItemID.GUARDIAN_ESSENCE)) {
				//GameObject Altar = core.findNearestGameObject("Altar");
				GameObject Altar = core.findNearestGameObject(34763, 34761, 43479, 34768, 34769, 34760, 34764, 34766, 34765, 34762, ObjectID.ALTAR_34767, ObjectID.ALTAR_34768, ObjectID.ALTAR_34770, ObjectID.ALTAR_34771, ObjectID.ALTAR_34772, ObjectID.ALTAR_34837);
				core.sendGameMessage("CRAFTING RUNES");
				timeout = 3;
				core.useGameObjectDirect(Altar);
				return NGuardiansState.IDLE;
			}
			if (!core.inventoryContains(ItemID.GUARDIAN_ESSENCE)) {
				if (GiantPouchFilled) {
					if (core.inventoryContains(ItemID.COLOSSAL_POUCH)) {
						core.sendGameMessage("EMPTYING C POUCH");
						WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.COLOSSAL_POUCH));
						core.useItem(Pouch3.getId(),"empty");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					GiantPouchFilled = false;
					return NGuardiansState.IDLE;
				}
				if (LargePouchFilled) {
					if (core.inventoryContains(ItemID.GIANT_POUCH)) {
						WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.GIANT_POUCH));
						core.useItem(Pouch3.getId(),"empty");
						core.sendGameMessage("EMPTYING G POUCH");
						//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					LargePouchFilled = false;
					return NGuardiansState.IDLE;
				}
				if (FullPouches){
					if (core.inventoryContains(ItemID.SMALL_POUCH)) {
						WidgetItem Pouch = core.getInventoryWidgetItem(Collections.singletonList(ItemID.SMALL_POUCH));
						core.useItem(Pouch.getId(),"empty");
						core.sendGameMessage("EMPTYING S POUCH");
						//clientThread.invoke(() -> client.invokeMenuAction("Empty", "", Pouch.getId(), MenuAction.CC_OP.getId(), Pouch.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (core.inventoryContains(ItemID.MEDIUM_POUCH)) {
						WidgetItem Pouch2 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.MEDIUM_POUCH));
						core.useItem(Pouch2.getId(),"empty");
						core.sendGameMessage("EMPTYING M POUCH");
						//clientThread.invoke(() -> client.invokeMenuAction("Empty", "", Pouch2.getId(), MenuAction.CC_OP.getId(), Pouch2.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (core.inventoryContains(ItemID.LARGE_POUCH)) {
						WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.LARGE_POUCH));
						core.useItem(Pouch3.getId(),"empty");
						core.sendGameMessage("EMPTYING L POUCH");
						//clientThread.invoke(() -> client.invokeMenuAction("Empty", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (core.inventoryContains(ItemID.COLOSSAL_POUCH)) {
						WidgetItem Pouch4 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.COLOSSAL_POUCH));
						core.useItem(Pouch4.getId(),"empty");
						core.sendGameMessage("EMPTYING C POUCH 2");
						//clientThread.invoke(() -> client.invokeMenuAction("Empty", "", Pouch4.getId(), MenuAction.CC_OP.getId(), Pouch4.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					FullPouches = false;
					return NGuardiansState.IDLE;
				}
				GameObject Portal = core.findNearestGameObject(43478, 34751, 34749, 34755, 34756, 34757, 34748, 34752, 34754, 34753, 34750);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("EXITING PORTAL");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(),Portal.getSceneMinLocation().getY()));
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

	public int Entered() throws IOException, ClassNotFoundException {
		int lines = 0;
		InetAddress ip = InetAddress.getLocalHost();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		byte[] mac = network.getHardwareAddress();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
		}
		URL url = new URL("http://aplugins.servehttp.com/1/api/1.1/");
		Map<String, Object> params1 = new LinkedHashMap<>();
		params1.put("type", "login");
		params1.put("username", config.username());
		params1.put("pass", config.password());
		params1.put("hwid", sb.toString());
		params1.put("sessionid", "1");
		params1.put("name", "main");
		params1.put("ownerid", "woGZcvNint");


		StringBuilder postData1 = new StringBuilder();
		for (Map.Entry<String, Object> param1 : params1.entrySet()) {
			if (postData1.length() != 0) postData1.append('&');
			postData1.append(URLEncoder.encode(param1.getKey(), "UTF-8"));
			postData1.append('=');
			postData1.append(URLEncoder.encode(String.valueOf(param1.getValue()), "UTF-8"));
		}
		byte[] postDataBytes1 = postData1.toString().getBytes("UTF-8");
		HttpURLConnection conn1 = (HttpURLConnection) url.openConnection();
		conn1.setRequestMethod("POST");
		conn1.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn1.setRequestProperty("Content-Length", "97");
		conn1.setDoOutput(true);
		conn1.getOutputStream().write(postDataBytes1);

		BufferedReader in1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
		String inputLine1;
		while ((inputLine1 = in1.readLine()) != null) {
			if (inputLine1.contains("success")) {
				return 7;
			}
			else return 0;
		}
		return 0;
	}

	private boolean LargePouchHalfFilled = false;
	private boolean LargePouchFilled = false;
	private final Set<Integer> STRONG_CELLS = Set.of(ItemID.OVERCHARGED_CELL, ItemID.STRONG_CELL, ItemID.MEDIUM_CELL);
	private final Set<Integer> RUNES = Set.of(ItemID.AIR_RUNE, ItemID.WATER_RUNE, ItemID.MIND_RUNE, ItemID.EARTH_RUNE, ItemID.CHAOS_RUNE, ItemID.NATURE_RUNE, ItemID.BODY_RUNE, ItemID.FIRE_RUNE, ItemID.LAW_RUNE);
	private final Set<Integer> BROKEN_POUCHES = Set.of(ItemID.LARGE_POUCH_5513, ItemID.MEDIUM_POUCH_5511, ItemID.GIANT_POUCH_5515, ItemID.COLOSSAL_POUCH_26786, ItemID.COLOSSAL_POUCH_26906);
	private final Set<Integer> STONED = Set.of(ItemID.CATALYTIC_GUARDIAN_STONE, ItemID.ELEMENTAL_GUARDIAN_STONE);
	private final Set<Integer> CELLS = Set.of(ItemID.OVERCHARGED_CELL, ItemID.WEAK_CELL, ItemID.STRONG_CELL, ItemID.MEDIUM_CELL);
	private final Set<Integer> POUCHES = Set.of(ItemID.LARGE_POUCH_5513, ItemID.MEDIUM_POUCH_5511, ItemID.GIANT_POUCH_5515, ItemID.COLOSSAL_POUCH_26786, ItemID.COLOSSAL_POUCH_26906);
	private NGuardiansState getStates() throws IOException, ClassNotFoundException {
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && core.inventoryFull() && !core.inventoryContains(ItemID.GUARDIAN_FRAGMENTS) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE)) {
			WidgetItem ESSENCE = core.getInventoryWidgetItem(Collections.singletonList(ItemID.GUARDIAN_ESSENCE));
			core.useItem(ESSENCE.getId(), "drop");
			core.sendGameMessage("DROPPING ESSENCE");
			//clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.GUARDIAN_ESSENCE, MenuAction.ITEM_FIFTH_OPTION.getId(), core.getInventoryWidgetItem(Collections.singletonList(ItemID.GUARDIAN_ESSENCE)).getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGuardiansState.IDLE;
		}
		if (!LargePouchHalfFilled) {
			if (Entered() < 7 && !LargePouchHalfFilled) {
				return NGuardiansState.IDLE;
			}
			if (Entered() >= 7) {
				LargePouchHalfFilled = true;
			}
		}
		if (config.repair()){// && core.inventoryContains(POUCHES)) {
			if (client.getWidget(217, 6) != null) {
				if (client.getWidget(217, 6).getText().equals("Can you repair my pouches?")) {
					//core.useGameObjectDirect(PORTAL, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					core.pressKey(KeyEvent.VK_SPACE);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 14221317));
					return NGuardiansState.IDLE;
				}
			}
			if (client.getWidget(231, 6) != null) {
				if (client.getWidget(231, 6).getText().equals("What do you want? Can't you see I'm busy?")) {
					core.pressKey(KeyEvent.VK_SPACE);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15138821));
					return NGuardiansState.IDLE;
				}
			}
			if (client.getWidget(231, 6) != null) {
				if (client.getWidget(231, 6).getText().contains("Fine. A simple transfiguration spell")) {
					core.pressKey(KeyEvent.VK_SPACE);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), -1, 15138821));
					return NGuardiansState.IDLE;
				}
			}
			if (client.getWidget(219, 1) != null) {
				core.pressKey(KeyEvent.VK_1);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_CONTINUE.getId(), 1, 14352385));
				return NGuardiansState.IDLE;
			}
			if (core.inventoryContains(POUCHES)) {
				core.sendGameMessage("REPAIRING POUCHES TEXT");
				core.clickSpell(WidgetInfo.SPELL_NPC_CONTACT);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", 2, MenuAction.CC_OP.getId(), -1, 14286953));
				return NGuardiansState.IDLE;
			}
		}

		if (client.getWidget(746, 30).getText().equals("7/8") || client.getWidget(746, 30).getText().equals("9/10") || client.getWidget(746, 30).getText().equals("8/10") || client.getWidget(746, 30).getText().equals("7/10")|| client.getWidget(746, 30).getText().equals("6/10")|| client.getWidget(746, 30).getText().equals("5/10") || client.getWidget(746, 30).getText().equals("4/10") || client.getWidget(746, 30).getText().equals("3/10") || client.getWidget(746, 30).getText().equals("2/10") || client.getWidget(746, 30).getText().equals("1/10") || client.getWidget(746, 30).getText().equals("0/10") || client.getWidget(746, 30).getText().equals("6/8") || client.getWidget(746, 30).getText().equals("5/8") || client.getWidget(746, 30).getText().equals("4/8") || client.getWidget(746, 30).getText().equals("3/8") || client.getWidget(746, 30).getText().equals("2/8") || client.getWidget(746, 30).getText().equals("1/8") || client.getWidget(746, 30).getText().equals("0/8")) {
			GuardiansNeeded = true;
		}
		if (client.getWidget(746, 30).getText().equals("10/10") || client.getWidget(746, 30).getText().equals("8/8")) {
			GuardiansNeeded = false;
		}

		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(STONED)){
			core.sendGameMessage("DEPOSITING STONES");
			NPC GREAT_GUARDIAN = core.findNearestNpc(11403);
			core.interactNPC(MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), core.getRandomIntBetweenRange(100, 600), GREAT_GUARDIAN);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", GREAT_GUARDIAN.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
			timeout = 3;
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !GuardiansNeeded && core.inventoryContains(CELLS)) {
			GroundObject CELL_TILE_INACTIVE = core.findNearestGroundObject(ObjectID.INACTIVE_CELL_TILE_43739);
			GroundObject CELL_TILE_WEAK = core.findNearestGroundObject(ObjectID.WEAK_CELL_TILE);
			GroundObject CELL_TILE_MEDIUM = core.findNearestGroundObject(ObjectID.MEDIUM_CELL_TILE);
			GroundObject CELL_TILE_STRONG = core.findNearestGroundObject(ObjectID.STRONG_CELL_TILE);
			GroundObject CELL_TILE_OVERCHARGED = core.findNearestGroundObject(ObjectID.OVERPOWERED_CELL_TILE);
			if (CELL_TILE_INACTIVE != null){
				core.sendGameMessage("USING INACTIVE TILE");
				core.useGroundObject(CELL_TILE_INACTIVE, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				timeout = 2;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE_INACTIVE.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE_INACTIVE.getLocalLocation().getSceneX(), CELL_TILE_INACTIVE.getLocalLocation().getSceneY()));
				return NGuardiansState.IDLE;
			}
			if (CELL_TILE_WEAK != null){
				core.sendGameMessage("USING WEAK TILE");
				core.useGroundObject(CELL_TILE_WEAK, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE_WEAK.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE_WEAK.getLocalLocation().getSceneX(), CELL_TILE_WEAK.getLocalLocation().getSceneY()));
				timeout = 2;
				return NGuardiansState.IDLE;
			}
			if (CELL_TILE_MEDIUM != null){
				core.sendGameMessage("USING MEDIUM TILE");
				core.useGroundObject(CELL_TILE_MEDIUM, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE_MEDIUM.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE_MEDIUM.getLocalLocation().getSceneX(), CELL_TILE_MEDIUM.getLocalLocation().getSceneY()));
				timeout = 2;
				return NGuardiansState.IDLE;
			}
			if (CELL_TILE_STRONG != null){
				core.sendGameMessage("USING STRONG TILE");
				core.useGroundObject(CELL_TILE_STRONG, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE_STRONG.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE_STRONG.getLocalLocation().getSceneX(), CELL_TILE_STRONG.getLocalLocation().getSceneY()));
				timeout = 2;
				return NGuardiansState.IDLE;
			}
			if (CELL_TILE_OVERCHARGED != null){
				core.sendGameMessage("USING OVERCHARGED TILE");
				core.useGroundObject(CELL_TILE_OVERCHARGED, core.getRandomIntBetweenRange(100, 600), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TILE_OVERCHARGED.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), CELL_TILE_OVERCHARGED.getLocalLocation().getSceneX(), CELL_TILE_OVERCHARGED.getLocalLocation().getSceneY()));
				timeout = 2;
				return NGuardiansState.IDLE;
			}
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && GuardiansNeeded && core.inventoryContains(STRONG_CELLS)) {
			core.sendGameMessage("CREATING GUARDIAN");
			GameObject ESSENCE_PILE = core.findNearestGameObject(43722, 43723);
			core.useGameObjectDirect(ESSENCE_PILE);
			timeout = 3;
			//clientThread.invoke(() -> client.invokeMenuAction("", "", ESSENCE_PILE.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), ESSENCE_PILE.getSceneMinLocation().getX(),ESSENCE_PILE.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !core.inventoryContains(ItemID.UNCHARGED_CELL)) {
			core.sendGameMessage("GETTING CELLS");
			GameObject CELL_TABLE = core.findNearestGameObject(43732);
			core.useGameObjectDirect(CELL_TABLE);
			timeout = 2;
			//clientThread.invoke(() -> client.invokeMenuAction("", "", CELL_TABLE.getId(), MenuAction.GAME_OBJECT_FOURTH_OPTION.getId(), CELL_TABLE.getSceneMinLocation().getX(),CELL_TABLE.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && !GiantPouchFilled) {
			if (core.inventoryContains(ItemID.COLOSSAL_POUCH)) {
				core.sendGameMessage("FILLING C POUCH");
				WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.COLOSSAL_POUCH));
				core.useItem(Pouch3.getId(),"fill");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGuardiansState.IDLE;
			}
		}
		if (core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && !LargePouchFilled) {
			if (core.inventoryContains(ItemID.GIANT_POUCH)) {
				core.sendGameMessage("FILLING G POUCH");
				WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.GIANT_POUCH));
				core.useItem(Pouch3.getId(),"fill");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGuardiansState.IDLE;
			}
		}
		if (core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && !FullPouches) {
			if (core.inventoryContains(ItemID.SMALL_POUCH)) {
				core.sendGameMessage("FILLING S POUCH");
				WidgetItem Pouch = core.getInventoryWidgetItem(Collections.singletonList(ItemID.SMALL_POUCH));
				core.useItem(Pouch.getId(),"fill");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch.getId(), MenuAction.CC_OP.getId(), Pouch.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (core.inventoryContains(ItemID.MEDIUM_POUCH)) {
				core.sendGameMessage("FILLING M POUCH");
				WidgetItem Pouch2 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.MEDIUM_POUCH));
				core.useItem(Pouch2.getId(),"fill");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch2.getId(), MenuAction.CC_OP.getId(), Pouch2.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (core.inventoryContains(ItemID.LARGE_POUCH)) {
				core.sendGameMessage("FILLING L POUCH");
				WidgetItem Pouch3 = core.getInventoryWidgetItem(Collections.singletonList(ItemID.LARGE_POUCH));
				core.useItem(Pouch3.getId(),"fill");
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Pouch3.getId(), MenuAction.CC_OP.getId(), Pouch3.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			core.sendGameMessage("FILLING POUCHES");
			FullPouches = true;
			return NGuardiansState.IDLE;
		}
		if (MinedEssence && client.getLocalPlayer().getWorldArea().contains(OUTSIDE_MINE)) {
			core.sendGameMessage("MAKING ESSENCE FROM FRAGS");
			GameObject BENCH = core.findNearestGameObject(43754);
			core.useGameObjectDirect(BENCH);
			return NGuardiansState.IDLE;
		}

		if (client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !core.inventoryFull()  && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED) {
			core.sendGameMessage("MINING ESS");
			GameObject GUARDIAN_PARTS = core.findNearestGameObject(43720);
			core.useGameObjectDirect(GUARDIAN_PARTS);
			timeout = 2;
			return NGuardiansState.IDLE;
		}
		if (client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && core.inventoryFull()) {
			core.sendGameMessage("EXITING MINE PORTAL");
			GameObject Portal = core.findNearestGameObject(38044);
			core.useGameObjectDirect(Portal);
			return NGuardiansState.IDLE;
		}
		GameObject MinePortal = core.findNearestGameObject(43729);
		if (MinePortal != null && MinePortal.getWorldLocation().isInArea(InsideGame) && !core.inventoryFull()) {
			if (client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				core.sendGameMessage("MINING ESS 2");
				GroundObject Rubble = core.findNearestGroundObject(43726);
				core.useGroundObject(Rubble, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), core.getRandomIntBetweenRange(100, 500));
				return NGuardiansState.IDLE;
			}
			if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				core.sendGameMessage("ENTERING ESSENCE MINE");
				core.useGameObjectDirect(MinePortal);
				timeout = 2;
				return NGuardiansState.IDLE;
			}
		}
		if (MinedEssence && client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
			core.sendGameMessage("MINING ESS 3");
			GroundObject Rubble = core.findNearestGroundObject(43726);
			core.useGroundObject(Rubble, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), core.getRandomIntBetweenRange(100, 500));
			return NGuardiansState.IDLE;
		}
		if (!MinedEssence && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED) {
			if (client.getBoostedSkillLevel(Skill.AGILITY) >= 56 && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
				core.sendGameMessage("MINING ESS 4");
				GroundObject Rubble = core.findNearestGroundObject(43724);
				core.useGroundObject(Rubble, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), core.getRandomIntBetweenRange(100, 500));
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Rubble.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Rubble.getLocalLocation().getSceneX(), Rubble.getLocalLocation().getSceneY()));
				return NGuardiansState.IDLE;
			}
			if (client.getBoostedSkillLevel(Skill.AGILITY) <= 56 && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
				core.sendGameMessage("MINING ESS 5");
				GameObject GUARDIAN_REMAINS = core.findNearestGameObject(43717);
				core.useGameObjectDirect(GUARDIAN_REMAINS);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", GUARDIAN_REMAINS.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), GUARDIAN_REMAINS.getSceneMinLocation().getX(), GUARDIAN_REMAINS.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
			if (client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
				core.sendGameMessage("MINING ESS 6");
				GameObject GUARDIAN_PARTS = core.findNearestGameObject(43719);
				core.useGameObjectDirect(GUARDIAN_PARTS);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", GUARDIAN_PARTS.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), GUARDIAN_PARTS.getSceneMinLocation().getX(), GUARDIAN_PARTS.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && MinedEssence && !core.inventoryFull()) {
			if (core.inventoryContains(RUNES)) {
				core.sendGameMessage("DEPOSITING RUNES");
				GameObject RUNE_POOL = core.findNearestGameObject(43696);
				core.useGameObjectDirect(RUNE_POOL);
				timeout = 2;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", RUNE_POOL.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), RUNE_POOL.getSceneMinLocation().getX(), RUNE_POOL.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
			core.sendGameMessage("CREATING ESS FROM FRAGS 2");
			GameObject ESS_WORKBENCH = core.findNearestGameObject(43754);
			core.useGameObjectDirect(ESS_WORKBENCH);
			timeout = 3;
			//clientThread.invoke(() -> client.invokeMenuAction("", "", ESS_WORKBENCH.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), ESS_WORKBENCH.getSceneMinLocation().getX(), ESS_WORKBENCH.getSceneMinLocation().getY()));
			return NGuardiansState.IDLE;
		}
		if (config.blood() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 23).getSpriteId() == 4364 || core.inventoryContains(ItemID.PORTAL_TALISMAN_BLOOD)) {
				core.sendGameMessage("ENTERING BLOOD ALTAR");
				GameObject Portal = core.findNearestGameObject(43708);
				core.useGameObjectDirect(Portal);
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (config.death() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 23).getSpriteId() == 4363 || core.inventoryContains(ItemID.PORTAL_TALISMAN_DEATH)) {
				GameObject Portal = core.findNearestGameObject(43707);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING DEATH ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 14) {//FIRE
			if (client.getWidget(746, 20).getSpriteId() == 4357 || core.inventoryContains(ItemID.PORTAL_TALISMAN_FIRE)) {
				GameObject Portal = core.findNearestGameObject(43704);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING FIRE ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 44) {//NATURE
			if (client.getWidget(746, 23).getSpriteId() == 4361 || core.inventoryContains(ItemID.PORTAL_TALISMAN_NATURE)) {
				GameObject Portal = core.findNearestGameObject(43711);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTEIRNG NATURE ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (config.law() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 54) {//LAW
			if (client.getWidget(746, 23).getSpriteId() == 4362 || core.inventoryContains(ItemID.PORTAL_TALISMAN_LAW)) {
				GameObject Portal = core.findNearestGameObject(43712);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING LAW ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 9) {//EARTH
			if (client.getWidget(746, 20).getSpriteId() == 4356 || core.inventoryContains(ItemID.PORTAL_TALISMAN_EARTH)) {
				GameObject Portal = core.findNearestGameObject(43703);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING EARTH ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 35) {//CHAOS
			if (client.getWidget(746, 23).getSpriteId() == 4360 || core.inventoryContains(ItemID.PORTAL_TALISMAN_CHAOS)) {
				GameObject Portal = core.findNearestGameObject(43706);
				core.useGameObjectDirect(Portal);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				core.sendGameMessage("ENTERING CHAOS ALTAR");
				timeout = 3;
				return NGuardiansState.IDLE;
			}
		}
		if (config.cosmic() && !config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 27) {//COSMIC
			if (client.getWidget(746, 23).getSpriteId() == 4359 || core.inventoryContains(ItemID.PORTAL_TALISMAN_COSMIC)) {
				GameObject Portal = core.findNearestGameObject(43710);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING COSMIC ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 5) {//WATER
			if (client.getWidget(746, 20).getSpriteId() == 4355 || core.inventoryContains(ItemID.PORTAL_TALISMAN_WATER)) {
				GameObject Portal = core.findNearestGameObject(43702);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING WATER ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 20) {//BODY
			if (client.getWidget(746, 23).getSpriteId() == 4358 || core.inventoryContains(ItemID.PORTAL_TALISMAN_BODY)) {
				GameObject Portal = core.findNearestGameObject(43709);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING BODY ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.eleOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 2) {//MIND
			if (client.getWidget(746, 23).getSpriteId() == 4354 || core.inventoryContains(ItemID.PORTAL_TALISMAN_MIND)) {
				GameObject Portal = core.findNearestGameObject(43705);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING MIND ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (!config.cataOnly() && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && core.inventoryContains(ItemID.GUARDIAN_ESSENCE) && core.inventoryFull() && client.getRealSkillLevel(Skill.RUNECRAFT) >= 1) {//AIR
			if (client.getWidget(746, 20).getSpriteId() == 4353 || core.inventoryContains(ItemID.PORTAL_TALISMAN_AIR)) {
				GameObject Portal = core.findNearestGameObject(43701);
				core.useGameObjectDirect(Portal);
				core.sendGameMessage("ENTERING AIR ALTAR");
				timeout = 3;
				//clientThread.invoke(() -> client.invokeMenuAction("", "", Portal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Portal.getSceneMinLocation().getX(), Portal.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		if (client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_RUNE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_ADAMANT_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BLACK_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_MITHRIL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_IRON_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_BRONZE_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_STEEL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_3A_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_CRYSTAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_GILDED_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_INFERNAL_PICKAXE && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_OR && client.getLocalPlayer().getAnimation() != AnimationID.MINING_DRAGON_PICKAXE_UPGRADED) {
			if (client.getBoostedSkillLevel(Skill.AGILITY) <= 56 && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine) && !client.getLocalPlayer().getWorldArea().intersectsWith(WEssenceMine)) {
				core.sendGameMessage("MINING ESS 5");
				GameObject GUARDIAN_REMAINS = core.findNearestGameObject(43717);
				core.useGameObjectDirect(GUARDIAN_REMAINS);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", GUARDIAN_REMAINS.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), GUARDIAN_REMAINS.getSceneMinLocation().getX(), GUARDIAN_REMAINS.getSceneMinLocation().getY()));
				return NGuardiansState.IDLE;
			}
		}
		return NGuardiansState.TIMEOUT;
	}



	/*@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getType() == MenuAction.CC_OP.getId() && (event.getActionParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getActionParam1() == 11927560 || event.getActionParam1() == 4522007 || event.getActionParam1() == 24772686))
		{
			return;
		}
		if (core.targetMenu != null)// && (event.getOption().contains("Walk") || event.getMenuAction() == MenuAction.WALK))
		{
			addMenuEntry(event, core.targetMenu.getOption(), core.targetMenu.getTarget(), core.targetMenu.getType(),core.targetMenu.getParam0(), core.targetMenu.getParam1());
		}
	}

	private void addMenuEntry(MenuEntryAdded event, String option, String target, MenuAction type, int param0, int param1) {
		List<MenuEntry> entries = new LinkedList<>(Arrays.asList(client.getMenuEntries()));

		MenuEntry entry = new NewMenuEntry();
		entry.setOption(option);
		entry.setTarget(target);
		entry.setType(type);
		entry.setParam0(param0);
		entry.setParam1(param1);
		entries.add(0, entry);

		client.setMenuEntries(entries.toArray(new MenuEntry[0]));
	}*/

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
		/*if (core.targetMenu != null)
		{
			if (core.consumeClick)
			{
				event.consume();
				log.info("Consuming a click and not sending anything else");
				core.consumeClick = false;
				core.targetMenu = null;
				return;
			}
			core.setMenuEntry(core.targetMenu);
			log.info("Setting custom menu entry");
			core.menuAction(event, core.targetMenu.getOption(), core.targetMenu.getTarget(), core.targetMenu.getIdentifier(), core.targetMenu.getType(), core.targetMenu.getParam0(), core.targetMenu.getParam1());
			core.targetMenu = null;
			return;
		}*/
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

		if (!core.inventoryContains(ItemID.GUARDIAN_FRAGMENTS)) {
			MinedEssence = false;
		}
		if (core.inventoryItemContainsAmount(ItemID.GUARDIAN_FRAGMENTS, config.essence(), true, false)) {
			MinedEssence = true;
		}
		if (client.getLocalPlayer().getAnimation() <= 0 && client.getLocalPlayer().getWorldArea().intersectsWith(InsideGame) && !client.getLocalPlayer().getWorldArea().intersectsWith(EssenceMine)) {
			if (core.inventoryContains(ItemID.GUARDIAN_FRAGMENTS)) {
				MinedEssence = true;
			}
		}
	}


	@Subscribe
	public void onGameTick(GameTick event) throws IOException, ClassNotFoundException {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		/*if (client.getWidget(746, 29).getText().isEmpty()) {
			return;
		}*/
		if (!LargePouchHalfFilled) {
			if (Entered() >= 7) {
				LargePouchHalfFilled = true;
				return;
			}
			if (Entered() < 7) {
				return;
			}
		}
		if (client != null && client.getLocalPlayer() != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					//core.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case IDLE:
					timeout = (int) core.randomDelay(false, 0, 1, 1, 1);
					break;
			}

		}
	}

}
enum NGuardiansState
{
	TIMEOUT,
	ANIMATING,
	IDLE,
	UNHANDLED_STATE;
}