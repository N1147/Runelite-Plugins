package net.runelite.client.plugins.ahotkeys;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.plugins.Utils.Core;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.time.Duration;
import java.time.Instant;

public class AHotkeysListener extends MouseAdapter implements KeyListener
{
	private Client client;

	private Instant lastPress;

	@Inject
	private AHotkeys plugin;

	@Inject
	private AHotkeysConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	Core core;

	@Inject
	private ConfigManager configManager;

	@Inject
	private AHotkeysListener(Client client, AHotkeysConfig config, AHotkeys plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
	}



	public int param;
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		try
		{
			if (lastPress != null && Duration.between(lastPress, Instant.now()).getNano() > 1000)
			{
				lastPress = null;
			}

			if (lastPress != null)
			{
				return;
			}
			int key_code = e.getKeyCode();

			if (key_code == config.key1().getKeyCode())
			{
				if (config.type1().toLowerCase().contains("prayer")){
					core.activatePrayer(config.typep1().widgetInfo);
				}
				if (config.type1().toLowerCase().contains("tb")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_TELEBLOCK);
				}
				if (config.type1().toLowerCase().contains("entangle")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_ENTANGLE);
				}
				if (config.type1().toLowerCase().contains("veng")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_VENGEANCE);
				}
				if (config.type1().toLowerCase().contains("magicimbue")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_MAGIC_IMBUE);
				}
				if (config.type1().toLowerCase().contains("plankmake")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_PLANK_MAKE);
				}
				if (config.type1().toLowerCase().contains("npccontact")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_NPC_CONTACT);
				}
				if (config.type1().toLowerCase().contains("alch")){ // == type1.INVENTORY_ITEM) {
					core.clickSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);
					//core.sleep(150);
					//core.useItem(config.itemID1(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type1().toLowerCase().contains("item")){ // == type1.INVENTORY_ITEM) {
					core.useItem(config.itemID1(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type1().toLowerCase().contains("object")){ // == type1.OBJECT) {
					GameObject obj = core.findNearestGameObject(config.type1ID());
					core.useGameObjectDirect(obj);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", obj.getId(), config.obj1().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type1().toLowerCase().contains("wall")){ // == type1.WALL) {
					WallObject obj = core.findNearestWallObject(config.type1ID());
					//clientThread.invoke(() -> client.invokeMenuAction("", "", obj.getId(), config.obj11().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
					core.useWallObjectDirect(obj, core.getRandomIntBetweenRange(100, 400),MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				}
				if (config.type1().toLowerCase().contains("walk")){ // == type1.WALK) {
					int[] Location = core.stringToIntArray(config.walkLoc1());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					core.walk(WalkLoc1);
				}
				if (config.type1().toLowerCase().contains("npc")){ // == type1.NPC) {
					NPC npc = core.findNearestNpc(config.type1ID());
					//clientThread.invoke(() -> client.invokeMenuAction("", "", npc.getIndex(), config.type1opp(), 	//Extra for npc's0, 0));
					core.interactNPC(config.type1opp(), core.getRandomIntBetweenRange(100,400), npc);
				}
				if (config.type1().toLowerCase().contains("widget")) {
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.type1ID(), config.type1op(), -1, config.type1Widget2()));
					Widget widget = client.getWidget(config.type1Widget1(), config.type1Widget2());
					core.moveClick(widget.getBounds());
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////










			if (key_code == config.key2().getKeyCode())
			{
				if (config.type2().toLowerCase().contains("prayer")){
					core.activatePrayer(config.typep2().widgetInfo);
				}
				if (config.type2().toLowerCase().contains("item")){ // == type1.INVENTORY_ITEM) {
					core.useItem(config.itemID2(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type2().toLowerCase().contains("object")){ // == type1.OBJECT) {
					GameObject obj = core.findNearestGameObject(config.type2ID());
					core.useGameObjectDirect(obj);
				}
				if (config.type2().toLowerCase().contains("wall")){ // == type1.WALL) {
					WallObject obj = core.findNearestWallObject(config.type2ID());
					core.useWallObjectDirect(obj, core.getRandomIntBetweenRange(100, 400), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				}
				if (config.type2().toLowerCase().contains("walk")){ // == type1.WALK) {
					int[] Location = core.stringToIntArray(config.walkLoc2());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					core.walk(WalkLoc1);
				}
				if (config.type2().toLowerCase().contains("npc")){ // == type1.NPC) {
					NPC npc = core.findNearestNpc(config.type2ID());
					core.interactNPC(config.type2opp(), core.getRandomIntBetweenRange(100,400), npc);
				}
				if (config.type2().toLowerCase().contains("widget")){ // == type1.WIDGET && config.type2Widget1() == 999) {
					Widget widget = client.getWidget(config.type2Widget1(), config.type2Widget2());
					core.moveClick(widget.getBounds());
				}
			}
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////



			if (key_code == config.key3().getKeyCode())
			{
				if (config.type3().toLowerCase().contains("prayer")){
					core.activatePrayer(config.typep3().widgetInfo);
				}
				if (config.type3().toLowerCase().contains("item")){ //== type1.INVENTORY_ITEM) {
					core.useItem(config.itemID3(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type3().toLowerCase().contains("object")){ // == type1.OBJECT) {
					GameObject obj = core.findNearestGameObject(config.type3ID());
					core.useGameObjectDirect(obj);
				}
				if (config.type3().toLowerCase().contains("wall")){ // == type1.WALL) {
					WallObject obj = core.findNearestWallObject(config.type3ID());
					core.useWallObjectDirect(obj, core.getRandomIntBetweenRange(100, 400), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				}
				if (config.type3().toLowerCase().contains("walk")){ // == type1.WALK) {
					int[] Location = core.stringToIntArray(config.walkLoc3());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					core.walk(WalkLoc1);
				}
				if (config.type3().toLowerCase().contains("npc")){ // == type1.NPC) {
					NPC npc = core.findNearestNpc(config.type3ID());
					core.interactNPC(config.type3opp(), core.getRandomIntBetweenRange(100,400), npc);
				}
				if (config.type3().toLowerCase().contains("widget")){ // == type1.WIDGET && config.type3Widget1() == 999) {
					Widget widget = client.getWidget(config.type3Widget1(), config.type3Widget2());
					core.moveClick(widget.getBounds());
				}
			}
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////





			if (key_code == config.key4().getKeyCode())
			{
				if (config.type4().toLowerCase().contains("prayer")){
					core.activatePrayer(config.typep4().widgetInfo);
				}
				if (config.type4().toLowerCase().contains("item")){ // == type1.INVENTORY_ITEM) {
					core.useItem(config.itemID4(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type4().toLowerCase().contains("object")){ // == type1.OBJECT) {
					GameObject obj = core.findNearestGameObject(config.type4ID());
					core.useGameObjectDirect(obj);
				}
				if (config.type4() .toLowerCase().contains("wall")){ //== type1.WALL) {
					WallObject obj = core.findNearestWallObject(config.type4ID());
					core.useWallObjectDirect(obj, core.getRandomIntBetweenRange(100, 400), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				}
				if (config.type4().toLowerCase().contains("walk")){ // == type1.WALK) {
					int[] Location = core.stringToIntArray(config.walkLoc4());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					core.walk(WalkLoc1);
				}
				if (config.type4().toLowerCase().contains("npc")){ // == type1.NPC) {
					NPC npc = core.findNearestNpc(config.type4ID());
					core.interactNPC(config.type4opp(), core.getRandomIntBetweenRange(100,400), npc);
				}
				if (config.type4().toLowerCase().contains("widget")){ // == type1.WIDGET && config.type4Widget1() == 999) {
					Widget widget = client.getWidget(config.type4Widget1(), config.type4Widget2());
					core.moveClick(widget.getBounds());
				}
			}
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////



			if (key_code == config.key5().getKeyCode())
			{
				if (config.type5().toLowerCase().contains("prayer")){
					core.activatePrayer(config.typep5().widgetInfo);
				}
				if (config.type5().toLowerCase().contains("item")){ // == type1.INVENTORY_ITEM) {
					core.useItem(config.itemID5(), MenuAction.ITEM_FIRST_OPTION);
					//clientThread.invoke(() -> client.invokeMenuAction("", "", config.itemID1(), config.invItem1().action.getId(), core.getInventoryWidgetItem(config.itemID1()).getIndex(), 9764864));
				}
				if (config.type5().toLowerCase().contains("object")){ // == type1.OBJECT) {
					GameObject obj = core.findNearestGameObject(config.type5ID());
					core.useGameObjectDirect(obj);
				}
				if (config.type5().toLowerCase().contains("wall")){ // == type1.WALL) {
					WallObject obj = core.findNearestWallObject(config.type5ID());
					core.useWallObjectDirect(obj, core.getRandomIntBetweenRange(100, 400), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
				}
				if (config.type5().toLowerCase().contains("walk")){ // == type1.WALK) {
					int[] Location = core.stringToIntArray(config.walkLoc5());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					core.walk(WalkLoc1);
				}
				if (config.type5().toLowerCase().contains("npc")){ // == type1.NPC) {
					NPC npc = core.findNearestNpc(config.type5ID());
					core.interactNPC(config.type5opp(), core.getRandomIntBetweenRange(100,400), npc);
				}
				if (config.type5().toLowerCase().contains("widget")){ // == type1.WIDGET && config.type5Widget1() == 999) {
					Widget widget = client.getWidget(config.type5Widget1(), config.type5Widget2());
					core.moveClick(widget.getBounds());
				}

			}
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////

		}
		catch (Throwable ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

}
