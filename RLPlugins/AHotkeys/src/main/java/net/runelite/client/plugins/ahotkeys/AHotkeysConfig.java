package net.runelite.client.plugins.ahotkeys;

import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.*;
import net.runelite.client.game.PrayerType;

import java.awt.event.KeyEvent;

@ConfigGroup("ahotkeys")
public interface AHotkeysConfig extends Config {


	@ConfigSection(
			name = "Hotkey 1",
			description = "",
			position = 0,
			closedByDefault = true
	)
	String hotkey1 = "hotkey1";

	@ConfigSection(
			name = "Hotkey 2",
			description = "",
			position = 2,
			closedByDefault = true
	)
	String hotkey2 = "hotkey2";

	@ConfigSection(
			name = "Hotkey 3",
			description = "",
			position = 3,
			closedByDefault = true
	)
	String hotkey3 = "hotkey3";

	@ConfigSection(
			name = "Hotkey 4",
			description = "",
			position = 4,
			closedByDefault = true
	)
	String hotkey4 = "hotkey4";

	@ConfigSection(
			name = "Hotkey 5",
			description = "",
			position = 5,
			closedByDefault = true
	)
	String hotkey5 = "hotkey5";

	@ConfigItem(
			keyName = "key1",
			name = "Hotkey 1",
			description = "Hotkey to activate script 1",
			position = 2,
			section = hotkey1
	)
	default Keybind key1() {
		return new Keybind(KeyEvent.VK_F1, 0);
	}

	@ConfigItem(
			keyName = "key2",
			name = "Hotkey 2",
			description = "Hotkey to activate script 2",
			position = 2,
			section = hotkey2
	)
	default Keybind key2() {
		return new Keybind(KeyEvent.VK_F2, 0);
	}

	@ConfigItem(
			keyName = "key3",
			name = "Hotkey 3",
			description = "Hotkey to activate script 3",
			position = 2,
			section = hotkey3
	)
	default Keybind key3() {
		return new Keybind(KeyEvent.VK_F3, 0);
	}

	@ConfigItem(
			keyName = "key4",
			name = "Hotkey 4",
			description = "Hotkey to activate script 4",
			position = 2,
			section = hotkey4
	)
	default Keybind key4() {
		return new Keybind(KeyEvent.VK_F4, 0);
	}

	@ConfigItem(
			keyName = "key5",
			name = "Hotkey 5",
			description = "Hotkey to activate script 5",
			position = 2,
			section = hotkey5
	)
	default Keybind key5() {
		return new Keybind(KeyEvent.VK_F5, 0);
	}

	/*@ConfigItem(
			keyName = "key6",
			name = "Hotkey 6",
			description = "Hotkey to activate script 6",
			position = 2,
			section = "section6"
	)
	default Keybind key6() {
		return new Keybind(KeyEvent.VK_F6, 0);
	}

	@ConfigItem(
			keyName = "key7",
			name = "Hotkey 7",
			description = "Hotkey to activate script 7",
			position = 2,
			section = "section7"
	)
	default Keybind key7() {
		return new Keybind(KeyEvent.VK_F7, 0);
	}

	@ConfigItem(
			keyName = "key8",
			name = "Hotkey 8",
			description = "Hotkey to activate script 8",
			position = 2
	)
	default Keybind key8() {
		return new Keybind(KeyEvent.VK_F8, 0);
	}

	@ConfigItem(
			keyName = "key9",
			name = "Hotkey 9",
			description = "Hotkey to activate script 9",
			position = 2
	)
	default Keybind key9() {
		return new Keybind(KeyEvent.VK_F9, 0);
	}

	@ConfigItem(
			keyName = "key10",
			name = "Hotkey 10",
			description = "Hotkey to activate script 10",
			position = 2
	)
	default Keybind key10() {
		return new Keybind(KeyEvent.VK_F10, 0);
	}*/


	@ConfigItem(
			position = 1,
			keyName = "type1",
			name = "Hotkey 1 Type",
			description = "",
			section = hotkey1
	)
	default String type1() {return "widget";}

	@ConfigItem(
			position = 3,
			keyName = "itemID1",
			name = "Item ID 1",
			description = "",
			section = hotkey1
	)
	default int itemID1() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "type1ID",
			name = "Key 1 ID",
			description = "",
			section = hotkey1
	)
	default int type1ID() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type1op",
			name = "Key 1 Opcode",
			description = "",
			section = hotkey1
	)
	default int type1op() {
		return 0;
	}

	@ConfigItem(
			keyName = "walkLoc1",
			name = "Tile Location",
			description = "",
			position = 43,
			section = hotkey1
	)
	default String walkLoc1() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type1opp",
			name = "Key 1 Opcode",
			description = "",
			section = hotkey1
	)
	default int type1opp() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type1Widget1",
			name = "Key 1 Param 1",
			description = "",
			section = hotkey1
	)
	default int type1Widget1() {
		return 0;
	}

	@ConfigItem(
			position = 22,
			keyName = "type1Widget2",
			name = "Key 1 Param 2",
			description = "",
			section = hotkey1
	)
	default int type1Widget2() {
		return 0;
	}

	@ConfigItem(
			position = 23,
			keyName = "prayer1",
			name = "Hotkey 1 Prayer",
			description = "",
			section = hotkey1
	)
	default PrayerType typep1() {return PrayerType.EAGLE_EYE;}

	@ConfigItem(
			position = 1,
			keyName = "type2",
			name = "Hotkey 2 Type",
			description = "",
			section = hotkey2
	)
	default String type2() {return "widget";}

	@ConfigItem(
			position = 3,
			keyName = "itemID2",
			name = "Item ID 2",
			description = "",
			section = hotkey2
	)
	default int itemID2() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "type2ID",
			name = "Key 2 ID",
			description = "",
			section = hotkey2
	)
	default int type2ID() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type2op",
			name = "Key 2 Opcode",
			description = "",
			section = hotkey2
	)
	default int type2op() {
		return 0;
	}

	@ConfigItem(
			keyName = "walkLoc2",
			name = "Tile Location",
			description = "",
			position = 43
			,
			section = hotkey2
	)
	default String walkLoc2() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type2opp",
			name = "Key 2 Opcode",
			description = ""
			,
			section = hotkey2
	)
	default int type2opp() {
		return 0;
	}

	@ConfigItem(
			position = 26,
			keyName = "type2Widget1",
			name = "Key 2 Param 1",
			description = ""
			,
			section = hotkey2
	)
	default int type2Widget1() {
		return 0;
	}

	@ConfigItem(
			position = 27,
			keyName = "type2Widget2",
			name = "Key 2 Param 2",
			description = ""
			,
			section = hotkey2
	)
	default int type2Widget2() {
		return 0;
	}

	@ConfigItem(
			position = 23,
			keyName = "prayer2",
			name = "Hotkey 2 Prayer",
			description = "",
			section = hotkey1
	)
	default PrayerType typep2() {return PrayerType.EAGLE_EYE;}


	@ConfigItem(
			position = 1,
			keyName = "type3",
			name = "Hotkey 3 Type",
			description = "",
			section = hotkey3
	)
	default String type3() {return "widget";}

	@ConfigItem(
			position = 3,
			keyName = "itemID3",
			name = "Item ID 3",
			description = "",
			section = hotkey3
	)
	default int itemID3() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "type3ID",
			name = "Key 3 ID",
			description = "",
			section = hotkey3
	)
	default int type3ID() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type3op",
			name = "Key 3 Opcode",
			description = "",
			section = hotkey3
	)
	default int type3op() {
		return 0;
	}

	@ConfigItem(
			keyName = "walkLoc3",
			name = "Tile Location",
			description = "",
			position = 43,
			section = hotkey3
	)
	default String walkLoc3() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type3opp",
			name = "Key 3 Opcode",
			description = "",
			section = hotkey3
	)
	default int type3opp() {
		return 0;
	}

	@ConfigItem(
			position = 31,
			keyName = "type3Widget1",
			name = "Key 3 Param 1",
			description = "",
			section = hotkey3
	)
	default int type3Widget1() {
		return 0;
	}

	@ConfigItem(
			position = 32,
			keyName = "type3Widget2",
			name = "Key 3 Param 2",
			description = "",
			section = hotkey3
	)
	default int type3Widget2() {
		return 0;
	}

	@ConfigItem(
			position = 23,
			keyName = "prayer3",
			name = "Hotkey 3 Prayer",
			description = "",
			section = hotkey1
	)
	default PrayerType typep3() {return PrayerType.EAGLE_EYE;}

	@ConfigItem(
			position = 1,
			keyName = "type4",
			name = "Hotkey 4 Type",
			description = "",
			section = hotkey4
	)
	default String type4() {return "widget";}

	@ConfigItem(
			position = 3,
			keyName = "itemID4",
			name = "Item ID 4",
			description = "",
			section = hotkey4
	)
	default int itemID4() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "type4ID",
			name = "Key 4 ID",
			description = "",
			section = hotkey4
	)
	default int type4ID() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type4op",
			name = "Key 4 Opcode",
			description = "",
			section = hotkey4
	)
	default int type4op() {
		return 0;
	}

	@ConfigItem(
			keyName = "walkLoc4",
			name = "Tile Location",
			description = "",
			position = 43,
			section = hotkey4
	)
	default String walkLoc4() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type4opp",
			name = "Key 4 Opcode",
			description = "",
			section = hotkey4
	)
	default int type4opp() {
		return 0;
	}

	@ConfigItem(
			position = 36,
			keyName = "type4Widget1",
			name = "Key 4 Param 1",
			description = "",
			section = hotkey4
	)
	default int type4Widget1() {
		return 0;
	}

	@ConfigItem(
			position = 37,
			keyName = "type1Widget2",
			name = "Key 4 Param 2",
			description = "",
			section = hotkey4
	)
	default int type4Widget2() {
		return 0;
	}

	@ConfigItem(
			position = 23,
			keyName = "prayer4",
			name = "Hotkey 4 Prayer",
			description = "",
			section = hotkey1
	)
	default PrayerType typep4() {return PrayerType.EAGLE_EYE;}


///////////////////////////////////////////////////////////////////////////////////////////////////


	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type5",
			name = "Hotkey 5 Type",
			description = "",
			section = hotkey5
	)
	default String type5() {return "widget";}

	@ConfigItem(
			position = 3,
			keyName = "itemID5",
			name = "Item ID 5",
			description = "",
			section = hotkey5
	)
	default int itemID5() {
		return 0;
	}

	@ConfigItem(
			position = 2,
			keyName = "type5ID",
			name = "Key 5 ID",
			description = "",
			section = hotkey5
	)
	default int type5ID() {
		return 0;
	}

	@ConfigItem(
			position = 21,
			keyName = "type5op",
			name = "Key 5 Opcode",
			description = "",
			section = hotkey5
	)
	default int type5op() {
		return 0;
	}

	@ConfigItem(
			keyName = "walkLoc5",
			name = "Tile Location",
			description = "",
			position = 43,
			section = hotkey5
	)
	default String walkLoc5() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type5opp",
			name = "Key 5 Opcode",
			description = "",
			section = hotkey5
	)
	default int type5opp() {
		return 0;
	}

	@ConfigItem(
			position = 41,
			keyName = "type5Widget1",
			name = "Key 5 Param 1",
			description = "",
			section = hotkey5
	)
	default int type5Widget1() {
		return 0;
	}

	@ConfigItem(
			position = 42,
			keyName = "type5Widget2",
			name = "Key 5 Param 2",
			description = "",
			section = hotkey5
	)
	default int type5Widget2() {
		return 0;
	}

	@ConfigItem(
			position = 23,
			keyName = "prayer5",
			name = "Hotkey 5 Prayer",
			description = "",
			section = hotkey1
	)
	default PrayerType typep5() {return PrayerType.EAGLE_EYE;}
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////



/*
	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type6",
			name = "Hotkey 6 Type",
			description = "",
			section = "section6"
	)
	default type1 type6() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID6",
			name = "Item ID 6",
			description = "",
			section = "section6",
			hidden = true,
			unhide = "type6",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID6() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem6",
			name = "Inventory Item 6",
			description = "",
			section = "section6",
			hidden = true,
			unhide = "type6",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem6() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj6",
			name = "Menu Action 6",
			description = "",
			position = 20,
			section = "section6",
			hidden = true,
			unhide = "type6",
			unhideValue = "OBJECT"
	)
	default obj1 obj6()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj66",
			name = "Menu Action 6",
			description = "",
			position = 20,
			section = "section6",
			hidden = true,
			unhide = "type6",
			unhideValue = "WALL"
	)
	default obj1 obj66()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type6ID",
			name = "Key 6 ID",
			description = "",
			section = "section6"
	)
	default int type6ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type6op",
			name = "Key 6 Opcode",
			description = "",
			hidden = true,
			unhide = "type6",
			unhideValue = "WIDGET",
			section = "section6"
	)
	default int type6op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc6",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type6",
			unhideValue = "WALK",
			section = "section6"
	)
	default String walkLoc6() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type6opp",
			name = "Key 6 Opcode",
			description = "",
			hidden = true,
			unhide = "type6",
			unhideValue = "NPC",
			section = "section6"
	)
	default int type6opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type6Widget1",
			name = "Key 6 Param 1",
			description = "",
			hidden = true,
			unhide = "type6",
			unhideValue = "WIDGET",
			section = "section6"
	)
	default int type6Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type6Widget2",
			name = "Key 6 Param 2",
			description = "",
			hidden = true,
			unhide = "type6",
			unhideValue = "WIDGET",
			section = "section6"
	)
	default int type6Widget2() { return 0; }
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type7",
			name = "Hotkey 7 Type",
			description = "",
			section = "section7"
	)
	default type1 type7() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID7",
			name = "Item ID 7",
			description = "",
			section = "section7",
			hidden = true,
			unhide = "type7",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID7() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem7",
			name = "Inventory Item 7",
			description = "",
			section = "section7",
			hidden = true,
			unhide = "type7",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem7() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj7",
			name = "Menu Action 7",
			description = "",
			position = 20,
			section = "section7",
			hidden = true,
			unhide = "type7",
			unhideValue = "OBJECT"
	)
	default obj1 obj7()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj77",
			name = "Menu Action 7",
			description = "",
			position = 20,
			section = "section7",
			hidden = true,
			unhide = "type7",
			unhideValue = "WALL"
	)
	default obj1 obj77()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type7ID",
			name = "Key 7 ID",
			description = "",
			section = "section7"
	)
	default int type7ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type7op",
			name = "Key 7 Opcode",
			description = "",
			hidden = true,
			unhide = "type7",
			unhideValue = "WIDGET",
			section = "section7"
	)
	default int type7op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc7",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type7",
			unhideValue = "WALK",
			section = "section7"
	)
	default String walkLoc7() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type7opp",
			name = "Key 7 Opcode",
			description = "",
			hidden = true,
			unhide = "type7",
			unhideValue = "NPC",
			section = "section7"
	)
	default int type7opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type7Widget1",
			name = "Key 7 Param 1",
			description = "",
			hidden = true,
			unhide = "type7",
			unhideValue = "WIDGET",
			section = "section7"
	)
	default int type7Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type7Widget2",
			name = "Key 7 Param 2",
			description = "",
			hidden = true,
			unhide = "type7",
			unhideValue = "WIDGET",
			section = "section7"
	)
	default int type7Widget2() { return 0; }
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type8",
			name = "Hotkey 8 Type",
			description = "",
			section = "section8"
	)
	default type1 type8() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID8",
			name = "Item ID 8",
			description = "",
			section = "section8",
			hidden = true,
			unhide = "type8",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID8() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem8",
			name = "Inventory Item 8",
			description = "",
			section = "section8",
			hidden = true,
			unhide = "type8",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem8() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj8",
			name = "Menu Action 8",
			description = "",
			position = 20,
			section = "section8",
			hidden = true,
			unhide = "type8",
			unhideValue = "OBJECT"
	)
	default obj1 obj8()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj88",
			name = "Menu Action 8",
			description = "",
			position = 20,
			section = "section8",
			hidden = true,
			unhide = "type8",
			unhideValue = "WALL"
	)
	default obj1 obj88()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type8ID",
			name = "Key 8 ID",
			description = "",
			section = "section8"
	)
	default int type8ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type8op",
			name = "Key 8 Opcode",
			description = "",
			hidden = true,
			unhide = "type8",
			unhideValue = "WIDGET",
			section = "section8"
	)
	default int type8op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc8",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type8",
			unhideValue = "WALK",
			section = "section8"
	)
	default String walkLoc8() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type8opp",
			name = "Key 8 Opcode",
			description = "",
			hidden = true,
			unhide = "type8",
			unhideValue = "NPC",
			section = "section8"
	)
	default int type8opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type8Widget1",
			name = "Key 8 Param 1",
			description = "",
			hidden = true,
			unhide = "type8",
			unhideValue = "WIDGET",
			section = "section8"
	)
	default int type8Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type8Widget2",
			name = "Key 8 Param 2",
			description = "",
			hidden = true,
			unhide = "type8",
			unhideValue = "WIDGET",
			section = "section8"
	)
	default int type8Widget2() { return 0; }
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////



	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type9",
			name = "Hotkey 9 Type",
			description = "",
			section = "section9"
	)
	default type1 type9() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID9",
			name = "Item ID 9",
			description = "",
			section = "section9",
			hidden = true,
			unhide = "type9",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID9() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem9",
			name = "Inventory Item 9",
			description = "",
			section = "section9",
			hidden = true,
			unhide = "type9",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem9() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj9",
			name = "Menu Action 9",
			description = "",
			position = 20,
			section = "section9",
			hidden = true,
			unhide = "type9",
			unhideValue = "OBJECT"
	)
	default obj1 obj9()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj99",
			name = "Menu Action 9",
			description = "",
			position = 20,
			section = "section9",
			hidden = true,
			unhide = "type9",
			unhideValue = "WALL"
	)
	default obj1 obj99()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type9ID",
			name = "Key 9 ID",
			description = "",
			section = "section9"
	)
	default int type9ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type9op",
			name = "Key 9 Opcode",
			description = "",
			hidden = true,
			unhide = "type9",
			unhideValue = "WIDGET",
			section = "section9"
	)
	default int type9op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc9",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type9",
			unhideValue = "WALK",
			section = "section9"
	)
	default String walkLoc9() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type9opp",
			name = "Key 9 Opcode",
			description = "",
			hidden = true,
			unhide = "type9",
			unhideValue = "NPC",
			section = "section9"
	)
	default int type9opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type9Widget1",
			name = "Key 9 Param 1",
			description = "",
			hidden = true,
			unhide = "type9",
			unhideValue = "WIDGET",
			section = "section9"
	)
	default int type9Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type9Widget2",
			name = "Key 9 Param 2",
			description = "",
			hidden = true,
			unhide = "type9",
			unhideValue = "WIDGET",
			section = "section9"
	)
	default int type9Widget2() { return 0; }
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////


	///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

	@ConfigItem(
			position = 1,
			keyName = "type10",
			name = "Hotkey 10 Type",
			description = "",
			section = "section10"
	)
	default type1 type10() { return type1.WIDGET; }

	@ConfigItem(
			position = 18,
			keyName = "invItem10",
			name = "Inventory Item 10",
			description = "",
			section = "section10",
			hidden = true,
			unhide = "type10",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem10() {return item1.FIFTH; }

	@ConfigItem(
			position = 3,
			keyName = "itemID10",
			name = "Item ID 10",
			description = "",
			section = "section10",
			hidden = true,
			unhide = "type10",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID10() { return 0; }

	@ConfigItem(
			keyName = "obj10",
			name = "Menu Action 10",
			description = "",
			position = 20,
			section = "section10",
			hidden = true,
			unhide = "type10",
			unhideValue = "OBJECT"
	)
	default obj1 obj10()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj111",
			name = "Menu Action 10",
			description = "",
			position = 20,
			section = "section10",
			hidden = true,
			unhide = "type10",
			unhideValue = "WALL"
	)
	default obj1 obj111()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type10ID",
			name = "Key 10 ID",
			description = "",
			section = "section10"
	)
	default int type10ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type10op",
			name = "Key 10 Opcode",
			description = "",
			hidden = true,
			unhide = "type10",
			unhideValue = "WIDGET",
			section = "section10"
	)
	default int type10op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc10",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type10",
			unhideValue = "WALK",
			section = "section10"
	)
	default String walkLoc10() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type10opp",
			name = "Key 10 Opcode",
			description = "",
			hidden = true,
			unhide = "type10",
			unhideValue = "NPC",
			section = "section10"
	)
	default int type10opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type10Widget1",
			name = "Key 10 Param 1",
			description = "",
			hidden = true,
			unhide = "type10",
			unhideValue = "WIDGET",
			section = "section10"
	)
	default int type10Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type10Widget2",
			name = "Key 10 Param 2",
			description = "",
			hidden = true,
			unhide = "type10",
			unhideValue = "WIDGET",
			section = "section10"
	)
	default int type10Widget2() { return 0; }
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
}
*

 */
}

