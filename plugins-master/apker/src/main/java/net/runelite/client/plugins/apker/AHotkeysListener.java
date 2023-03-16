package net.runelite.client.plugins.apker;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.plugins.NUtils.PUtils;

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
	private PkToolsPlugin plugin;

	@Inject
	private PkToolsConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	PUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	private AHotkeysListener(Client client, PkToolsConfig config, PkToolsPlugin plugin)
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

			if (key_code == config.key11().getKeyCode())
			{
				if (config.type1() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID1(),
							config.invItem1().action.getId(),
							utils.getInventoryWidgetItem(config.itemID1()).getIndex(),
							9764864));
				}
				if (config.type1() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type1ID());
					clientThread.invoke(() -> client.invokeMenuAction("", "", obj.getId(), config.obj1().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type1() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type1ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj11().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type1() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc1());
					WorldPoint WalkLoc1 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc1);
				}
				if (config.type1() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type1ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type1opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type1() == type1.WIDGET && config.type1Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type1ID(),
									config.type1op(),
									-1,
									config.type1Widget2()));
				}
				if (config.type1() == type1.WIDGET && config.type1Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type1ID(),
									config.type1op(),
									config.type1Widget1(),
									config.type1Widget2()));
				}
			}


			if (key_code == config.key12().getKeyCode())
			{
				if (config.type2() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID2(),
							config.invItem2().action.getId(),
							utils.getInventoryWidgetItem(config.itemID2()).getIndex(),
							9764864));
				}
				if (config.type2() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type2ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj2().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type2() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type2ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj22().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type2() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc2());
					WorldPoint WalkLoc2 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc2);
				}
				if (config.type2() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type2ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type2opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type2() == type1.WIDGET && config.type2Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type2ID(),
									config.type2op(),
									-1,
									config.type2Widget2()));
				}
				if (config.type2() == type1.WIDGET && config.type2Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type2ID(),
									config.type2op(),
									config.type2Widget1(),
									config.type2Widget2()));
				}
			}



			if (key_code == config.key13().getKeyCode())
			{
				if (config.type3() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID3(),
							config.invItem3().action.getId(),
							utils.getInventoryWidgetItem(config.itemID3()).getIndex(),
							9764864));
				}
				if (config.type3() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type3ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj3().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type3() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type3ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj33().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type3() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc3());
					WorldPoint WalkLoc3 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc3);
				}
				if (config.type3() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type3ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type3opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type3() == type1.WIDGET && config.type3Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type3ID(),
									config.type3op(),
									-1,
									config.type3Widget2()));
				}
				if (config.type3() == type1.WIDGET && config.type3Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type3ID(),
									config.type3op(),
									config.type3Widget1(),
									config.type3Widget2()));
				}
			}


			if (key_code == config.key14().getKeyCode())
			{
				if (config.type4() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID4(),
							config.invItem4().action.getId(),
							utils.getInventoryWidgetItem(config.itemID4()).getIndex(),
							9764864));
				}
				if (config.type4() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type4ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj4().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type4() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type4ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj44().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type4() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc4());
					WorldPoint WalkLoc4 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc4);
				}
				if (config.type4() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type4ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type4opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type4() == type1.WIDGET && config.type4Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type4ID(),
									config.type4op(),
									-1,
									config.type4Widget2()));
				}
				if (config.type4() == type1.WIDGET && config.type4Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type4ID(),
									config.type4op(),
									config.type4Widget1(),
									config.type4Widget2()));
				}
			}



			if (key_code == config.key15().getKeyCode())
			{
				if (config.type5() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID5(),
							config.invItem5().action.getId(),
							utils.getInventoryWidgetItem(config.itemID5()).getIndex(),
							9764864));
				}
				if (config.type5() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type5ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj5().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type5() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type5ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj55().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type5() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc5());
					WorldPoint WalkLoc5 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc5);
				}
				if (config.type5() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type5ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type5opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type5() == type1.WIDGET && config.type5Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type5ID(),
									config.type5op(),
									-1,
									config.type5Widget2()));
				}
				if (config.type5() == type1.WIDGET && config.type5Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type5ID(),
									config.type5op(),
									config.type5Widget1(),
									config.type5Widget2()));
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////


			if (key_code == config.key16().getKeyCode())
			{
				if (config.type6() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID6(),
							config.invItem6().action.getId(),
							utils.getInventoryWidgetItem(config.itemID6()).getIndex(),
							9764864));
				}
				if (config.type6() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type6ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj6().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type6() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type6ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj66().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type6() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc6());
					WorldPoint WalkLoc6 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc6);
				}
				if (config.type6() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type6ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type6opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type6() == type1.WIDGET && config.type6Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type6ID(),
									config.type6op(),
									-1,
									config.type6Widget2()));
				}
				if (config.type6() == type1.WIDGET && config.type6Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type6ID(),
									config.type6op(),
									config.type6Widget1(),
									config.type6Widget2()));
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////


			if (key_code == config.key17().getKeyCode())
			{
				if (config.type7() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID7(),
							config.invItem7().action.getId(),
							utils.getInventoryWidgetItem(config.itemID7()).getIndex(),
							9764864));
				}
				if (config.type7() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type7ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj7().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type7() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type7ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj77().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type7() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc7());
					WorldPoint WalkLoc7 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc7);
				}
				if (config.type7() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type7ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type7opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type7() == type1.WIDGET && config.type7Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type7ID(),
									config.type7op(),
									-1,
									config.type7Widget2()));
				}
				if (config.type7() == type1.WIDGET && config.type7Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type7ID(),
									config.type7op(),
									config.type7Widget1(),
									config.type7Widget2()));
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////


			if (key_code == config.key18().getKeyCode())
			{
				if (config.type8() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID8(),
							config.invItem8().action.getId(),
							utils.getInventoryWidgetItem(config.itemID8()).getIndex(),
							9764864));
				}
				if (config.type8() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type8ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj8().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type8() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type8ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj88().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type8() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc8());
					WorldPoint WalkLoc8 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc8);
				}
				if (config.type8() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type8ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type8opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type8() == type1.WIDGET && config.type8Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type8ID(),
									config.type8op(),
									-1,
									config.type8Widget2()));
				}
				if (config.type8() == type1.WIDGET && config.type8Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type8ID(),
									config.type8op(),
									config.type8Widget1(),
									config.type8Widget2()));
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////


			if (key_code == config.key19().getKeyCode())
			{
				if (config.type9() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID9(),
							config.invItem9().action.getId(),
							utils.getInventoryWidgetItem(config.itemID9()).getIndex(),
							9764864));
				}
				if (config.type9() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type9ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj9().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type9() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type9ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj99().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type9() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc9());
					WorldPoint WalkLoc9 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc9);
				}
				if (config.type9() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type9ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type9opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type9() == type1.WIDGET && config.type9Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type9ID(),
									config.type9op(),
									-1,
									config.type9Widget2()));
				}
				if (config.type9() == type1.WIDGET && config.type9Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type9ID(),
									config.type9op(),
									config.type9Widget1(),
									config.type9Widget2()));
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////

			if (key_code == config.key20().getKeyCode())
			{
				if (config.type10() == type1.INVENTORY_ITEM) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",
							config.itemID10(),
							config.invItem10().action.getId(),
							utils.getInventoryWidgetItem(config.itemID10()).getIndex(),
							9764864));
				}
				if (config.type10() == type1.OBJECT) {
					GameObject obj = utils.findNearestGameObject(config.type10ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj10().action.getId(), obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
				}
				if (config.type10() == type1.WALL) {
					WallObject obj = utils.findNearestWallObject(config.type10ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "", obj.getId(),
									config.obj111().action.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY()));
				}
				if (config.type10() == type1.WALK) {
					int[] Location = utils.stringToIntArray(config.walkLoc10());
					WorldPoint WalkLoc10 = new WorldPoint(Location[0], Location[1], Location[2]);
					utils.walk(WalkLoc10);
				}
				if (config.type10() == type1.NPC) {
					NPC npc = utils.findNearestNpc(config.type10ID());
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									npc.getIndex(),
									config.type10opp(), 	//Extra for npc's
									0, 0));
				}
				if (config.type10() == type1.WIDGET && config.type10Widget1() == 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type10ID(),
									config.type10op(),
									-1,
									config.type10Widget2()));
				}
				if (config.type10() == type1.WIDGET && config.type10Widget1() != 999) {
					clientThread.invoke(() ->
							client.invokeMenuAction("", "",
									config.type10ID(),
									config.type10op(),
									config.type10Widget1(),
									config.type10Widget2()));
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
