package net.runelite.client.plugins.apker;

import net.runelite.client.config.*;

import java.awt.event.KeyEvent;

@ConfigGroup("apker")
public interface PkToolsConfig extends Config
{
	String commands = "protectitem, rigour, augury, piety, maultoags, agstomaul, singlemaulags, " +
		"incrediblereflexes, ultimatestrength, steelskin, eagleeye, mysticmight, " +
		"freeze, blood, vengeance, teleblock, entangle, spec, wait, group#, id_#, " +
		"protectfrommagic, protectfrommissiles, protectfrommelee";
	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "Instructions. Don't enter anything into this field",
			position = 0,
			section = "instructionsTitle"
	)
	default String instructions()
	{
		return "Commands are: "+commands;
	}


	@ConfigItem(
		keyName = "key1",
		name = "Hotkey 1",
		description = "Hotkey to activate script 1",
		position = 2,
			section = "section0"
	)
	default Keybind key1()
	{
		return new Keybind(KeyEvent.VK_1, 0);
	}

	@ConfigItem(
		keyName = "key2",
		name = "Hotkey 2",
		description = "Hotkey to activate script 2",
		position = 3,
			section = "section0"
	)
	default Keybind key2()
	{
		return new Keybind(KeyEvent.VK_2, 0);
	}

	@ConfigItem(
		keyName = "key3",
		name = "Hotkey 3",
		description = "Hotkey to activate script 3",
		position = 4,
			section = "section0"
	)
	default Keybind key3()
	{
		return new Keybind(KeyEvent.VK_3, 0);
	}

	@ConfigItem(
		keyName = "key4",
		name = "Hotkey 4",
		description = "Hotkey to activate script 4",
		position = 5,
			section = "section0"
	)
	default Keybind key4()
	{
		return new Keybind(KeyEvent.VK_4, 0);
	}

	@ConfigItem(
		keyName = "key5",
		name = "Hotkey 5",
		description = "Hotkey to activate script 5",
		position = 6,
			section = "section0"
	)
	default Keybind key5()
	{
		return new Keybind(KeyEvent.VK_5, 0);
	}

	@ConfigItem(
		keyName = "key6",
		name = "Hotkey 6",
		description = "Hotkey to activate script 6",
		position = 7,
			section = "section0"
	)
	default Keybind key6()
	{
		return new Keybind(KeyEvent.VK_6, 0);
	}

	@ConfigItem(
		keyName = "key7",
		name = "Hotkey 7",
		description = "Hotkey to activate script 7",
		position = 8,
			section = "section0"
	)
	default Keybind key7()
	{
		return new Keybind(KeyEvent.VK_7, 0);
	}

	@ConfigItem(
		keyName = "key8",
		name = "Hotkey 8",
		description = "Hotkey to activate script 8",
		position = 9,
			section = "section0"
	)
	default Keybind key8()
	{
		return new Keybind(KeyEvent.VK_8, 0);
	}

	@ConfigItem(
		position = 10,
		keyName = "key1_script",
		name = "Script 1",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key1_script()
	{
		return "group1\npiety\nspec\nclickenemy";
	}

	@ConfigItem(
		position = 11,
		keyName = "key2_script",
		name = "Script 2",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key2_script()
	{
		return "group2\nrigour\nclickenemy";
	}

	@ConfigItem(
		position = 12,
		keyName = "key3_script",
		name = "Script 3",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key3_script()
	{
		return "group3\naugury\nfreeze\nclickenemy";
	}

	@ConfigItem(
		position = 13,
		keyName = "key4_script",
		name = "Script 4",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key4_script()
	{
		return "teleblock\nclickenemy";
	}

	@ConfigItem(
		position = 14,
		keyName = "key5_script",
		name = "Script 5",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key5_script()
	{
		return "protectfrommagic";
	}

	@ConfigItem(
		position = 15,
		keyName = "key6_script",
		name = "Script 6",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key6_script()
	{
		return "protectfrommissiles";
	}

	@ConfigItem(
		position = 16,
		keyName = "key7_script",
		name = "Script 7",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key7_script()
	{
		return "protectfrommelee";
	}

	@ConfigItem(
		position = 17,
		keyName = "key8_script",
		name = "Script 8",
		description = PkToolsConfig.commands
			,
			section = "section0"
	)
	default String key8_script()
	{
		return "blood\nclickenemy";
	}

	@ConfigTitle(
		keyName = "label2",
		name = "Configuration",
		description = "",
		position = 18,
			section = "section0"
	)
	String label2 = "label2";

	@ConfigItem(
		position = 21,
		keyName = "autoPrayerSwitcher",
		name = "Prayer Switcher",
		description = "Automatically Switch Prayers based on opponent's gear."
			,
			section = "section0"
	)
	default boolean autoPrayerSwitcher()
	{
		return false;
	}

	@ConfigItem(
			position = 22,
			keyName = "enableMeleePrayer",
			name = "Enable Melee Prayer",
			description = "If disabled, will ignore melee prayer switches"
			,
			section = "section0"
	)
	default boolean enableMeleePrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 23,
			keyName = "enableMagicPrayer",
			name = "Enable Magic Prayer",
			description = "If disabled, will ignore magic prayer switches"
			,
			section = "section0"
	)
	default boolean enableMagicPrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 24,
			keyName = "enableRangedPrayer",
			name = "Enable Ranged Prayer",
			description = "If disabled, will ignore ranged prayer switches"
			,
			section = "section0"
	)
	default boolean enableRangedPrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 36,
			keyName = "autoGearSwap",
			name = "Automatically Swap Gear",
			description = "Swap gear based on opponent's weakness using group 1 as meelee, 2 as ranged, 3 as magic"
			,
			section = "section0"
	)
	default boolean autoGearSwap()
	{
		return true;
	}

	@ConfigItem(
			position = 37,
			keyName = "swapFromPray",
			name = "Auto Gear Swap Type",
			description = "If Enabled, will swap your gear based on opponents overhead prayer, if Disabled, uses their weapon type"
			,
			section = "section0"
	)
	default boolean swapFromPray()
	{
		return true;
	}

	@ConfigSection(
			keyName = "section0",
			name = "Objects/Widgets",
			description = "",
			position = 0
	)
	String section0 = "section0";































































	@ConfigSection(
			keyName = "section1",
			name = "Hotkey 11",
			description = "",
			position = 2
	)
	String section1 = "section1";

	@ConfigSection(
			keyName = "section2",
			name = "Hotkey 12",
			description = "",
			position = 3
	)
	String section2 = "section2";


	@ConfigSection(
			keyName = "section3",
			name = "Hotkey 13",
			description = "",
			position = 4
	)
	String section3 = "section3";


	@ConfigSection(
			keyName = "section4",
			name = "Hotkey 14",
			description = "",
			position = 5
	)
	String section4 = "section4";


	@ConfigSection(
			keyName = "section5",
			name = "Hotkey 15",
			description = "",
			position = 6
	)
	String section5 = "section5";

	@ConfigSection(
			keyName = "section6",
			name = "Hotkey 16",
			description = "",
			position = 7
	)
	String section6 = "section6";

	@ConfigSection(
			keyName = "section7",
			name = "Hotkey 17",
			description = "",
			position = 8
	)
	String section7 = "section7";

	@ConfigSection(
			keyName = "section8",
			name = "Hotkey 18",
			description = "",
			position = 9
	)
	String section8 = "section8";

	@ConfigSection(
			keyName = "section9",
			name = "Hotkey 19",
			description = "",
			position = 10
	)
	String section9 = "section9";

	@ConfigSection(
			keyName = "section10",
			name = "Hotkey 20",
			description = "",
			position = 11
	)
	String section10 = "section10";




	@ConfigTitle(
			keyName = "label1",
			name = "Hotkeys",
			description = "",
			position = 1
	)
	String label10 = "label10";

	@ConfigItem(
			keyName = "key1",
			name = "Hotkey 11",
			description = "Hotkey to activate script 11",
			position = 2,
			section = "section1"
	)
	default Keybind key11()
	{
		return new Keybind(KeyEvent.VK_1, 0);
	}

	@ConfigItem(
			keyName = "key2",
			name = "Hotkey 12",
			description = "Hotkey to activate script 12",
			position = 2,
			section = "section2"
	)
	default Keybind key12()
	{
		return new Keybind(KeyEvent.VK_2, 0);
	}

	@ConfigItem(
			keyName = "key3",
			name = "Hotkey 13",
			description = "Hotkey to activate script 13",
			position = 2,
			section = "section3"
	)
	default Keybind key13()
	{
		return new Keybind(KeyEvent.VK_3, 0);
	}

	@ConfigItem(
			keyName = "key4",
			name = "Hotkey 14",
			description = "Hotkey to activate script 14",
			position = 2,
			section = "section4"
	)
	default Keybind key14()
	{
		return new Keybind(KeyEvent.VK_4, 0);
	}

	@ConfigItem(
			keyName = "key5",
			name = "Hotkey 15",
			description = "Hotkey to activate script 15",
			position = 2,
			section = "section5"
	)
	default Keybind key15()
	{
		return new Keybind(KeyEvent.VK_5, 0);
	}

	@ConfigItem(
			keyName = "key6",
			name = "Hotkey 16",
			description = "Hotkey to activate script 16",
			position = 2,
			section = "section6"
	)
	default Keybind key16()
	{
		return new Keybind(KeyEvent.VK_6, 0);
	}

	@ConfigItem(
			keyName = "key7",
			name = "Hotkey 17",
			description = "Hotkey to activate script 17",
			position = 2,
			section = "section7"
	)
	default Keybind key17()
	{
		return new Keybind(KeyEvent.VK_7, 0);
	}

	@ConfigItem(
			keyName = "key8",
			name = "Hotkey 18",
			description = "Hotkey to activate script 18",
			position = 2,
			section = "section8"
	)
	default Keybind key18()
	{
		return new Keybind(KeyEvent.VK_8, 0);
	}

	@ConfigItem(
			keyName = "key9",
			name = "Hotkey 19",
			description = "Hotkey to activate script 19",
			position = 2,
			section = "section9"
	)
	default Keybind key19()
	{
		return new Keybind(KeyEvent.VK_9, 0);
	}

	@ConfigItem(
			keyName = "key10",
			name = "Hotkey 20",
			description = "Hotkey to activate script 20",
			position = 2,
			section = "section10"
	)
	default Keybind key20()
	{
		return new Keybind(KeyEvent.VK_0, 0);
	}





	@ConfigItem(
			position = 1,
			keyName = "type1",
			name = "Hotkey 1 Type",
			description = "",
			section = "section1"
	)
	default type1 type1() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID1",
			name = "Item ID 1",
			description = "",
			section = "section1",
			hidden = true,
			unhide = "type1",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID1() { return 0; }


	@ConfigItem(
			position = 18,
			keyName = "invItem1",
			name = "Inventory Item 1",
			description = "",
			section = "section1",
			hidden = true,
			unhide = "type1",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem1() {return item1.FIFTH; }


	@ConfigItem(
			keyName = "obj1",
			name = "Menu Action 1",
			description = "",
			position = 20,
			section = "section1",
			hidden = true,
			unhide = "type1",
			unhideValue = "OBJECT"
	)
	default obj1 obj1()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj11",
			name = "Menu Action 1",
			description = "",
			position = 20,
			section = "section1",
			hidden = true,
			unhide = "type1",
			unhideValue = "WALL"
	)
	default obj1 obj11()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type1ID",
			name = "Key 1 ID",
			description = "",
			section = "section1"
	)
	default int type1ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type1op",
			name = "Key 1 Opcode",
			description = "",
			hidden = true,
			unhide = "type1",
			unhideValue = "WIDGET",
			section = "section1"
	)
	default int type1op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc1",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type1",
			unhideValue = "WALK",
			section = "section1"
	)
	default String walkLoc1() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type1opp",
			name = "Key 1 Opcode",
			description = "",
			hidden = true,
			unhide = "type1",
			unhideValue = "NPC",
			section = "section1"
	)
	default int type1opp() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type1Widget1",
			name = "Key 1 Param 1",
			description = "",
			hidden = true,
			unhide = "type1",
			unhideValue = "WIDGET",
			section = "section1"
	)
	default int type1Widget1() { return 0; }

	@ConfigItem(
			position = 22,
			keyName = "type1Widget2",
			name = "Key 1 Param 2",
			description = "",
			hidden = true,
			unhide = "type1",
			unhideValue = "WIDGET",
			section = "section1"
	)
	default int type1Widget2() { return 0; }


	@ConfigItem(
			position = 1,
			keyName = "type2",
			name = "Hotkey 2 Type",
			description = "",
			section = "section2"
	)
	default type1 type2() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID2",
			name = "Item ID 2",
			description = "",
			section = "section2",
			hidden = true,
			unhide = "type2",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID2() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem2",
			name = "Inventory Item 2",
			description = "",
			section = "section2",
			hidden = true,
			unhide = "type2",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem2() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj2",
			name = "Menu Action 2",
			description = "",
			position = 20,
			section = "section2",
			hidden = true,
			unhide = "type2",
			unhideValue = "OBJECT"
	)
	default obj1 obj2()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj22",
			name = "Menu Action 2",
			description = "",
			position = 20,
			section = "section2",
			hidden = true,
			unhide = "type2",
			unhideValue = "WALL"
	)
	default obj1 obj22()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type2ID",
			name = "Key 2 ID",
			description = "",
			section = "section2"
	)
	default int type2ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type2op",
			name = "Key 2 Opcode",
			description = "",
			hidden = true,
			unhide = "type2",
			unhideValue = "WIDGET",
			section = "section2"
	)
	default int type2op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc2",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type2",
			unhideValue = "WALK",
			section = "section2"
	)
	default String walkLoc2() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type2opp",
			name = "Key 2 Opcode",
			description = "",
			hidden = true,
			unhide = "type2",
			unhideValue = "NPC",
			section = "section2"
	)
	default int type2opp() { return 0; }

	@ConfigItem(
			position = 26,
			keyName = "type2Widget1",
			name = "Key 2 Param 1",
			description = "",
			hidden = true,
			unhide = "type2",
			unhideValue = "WIDGET",
			section = "section2"
	)
	default int type2Widget1() { return 0; }

	@ConfigItem(
			position = 27,
			keyName = "type2Widget2",
			name = "Key 2 Param 2",
			description = "",
			hidden = true,
			unhide = "type2",
			unhideValue = "WIDGET",
			section = "section2"
	)
	default int type2Widget2() { return 0; }




	@ConfigItem(
			position = 1,
			keyName = "type3",
			name = "Hotkey 3 Type",
			description = "",
			section = "section3"
	)
	default type1 type3() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID3",
			name = "Item ID 3",
			description = "",
			section = "section3",
			hidden = true,
			unhide = "type3",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID3() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem3",
			name = "Inventory Item 3",
			description = "",
			section = "section3",
			hidden = true,
			unhide = "type3",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem3() {return item1.FIFTH; }


	@ConfigItem(
			keyName = "obj3",
			name = "Menu Action 3",
			description = "",
			position = 20,
			section = "section3",
			hidden = true,
			unhide = "type3",
			unhideValue = "OBJECT"
	)
	default obj1 obj3()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj33",
			name = "Menu Action 3",
			description = "",
			position = 20,
			section = "section3",
			hidden = true,
			unhide = "type3",
			unhideValue = "WALL"
	)
	default obj1 obj33()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type3ID",
			name = "Key 3 ID",
			description = "",
			section = "section3"
	)
	default int type3ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type3op",
			name = "Key 3 Opcode",
			description = "",
			hidden = true,
			unhide = "type3",
			unhideValue = "WIDGET",
			section = "section3"
	)
	default int type3op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc3",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type3",
			unhideValue = "WALK",
			section = "section3"
	)
	default String walkLoc3() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type3opp",
			name = "Key 3 Opcode",
			description = "",
			hidden = true,
			unhide = "type3",
			unhideValue = "NPC",
			section = "section3"
	)
	default int type3opp() { return 0; }

	@ConfigItem(
			position = 31,
			keyName = "type3Widget1",
			name = "Key 3 Param 1",
			description = "",
			hidden = true,
			unhide = "type3",
			unhideValue = "WIDGET",
			section = "section3"
	)
	default int type3Widget1() { return 0; }

	@ConfigItem(
			position = 32,
			keyName = "type3Widget2",
			name = "Key 3 Param 2",
			description = "",
			hidden = true,
			unhide = "type3",
			unhideValue = "WIDGET",
			section = "section3"
	)
	default int type3Widget2() { return 0; }







	@ConfigItem(
			position = 1,
			keyName = "type4",
			name = "Hotkey 4 Type",
			description = "",
			section = "section4"
	)
	default type1 type4() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID4",
			name = "Item ID 4",
			description = "",
			section = "section4",
			hidden = true,
			unhide = "type4",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID4() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem4",
			name = "Inventory Item 4",
			description = "",
			section = "section4",
			hidden = true,
			unhide = "type4",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem4() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj4",
			name = "Menu Action 4",
			description = "",
			position = 20,
			section = "section4",
			hidden = true,
			unhide = "type4",
			unhideValue = "OBJECT"
	)
	default obj1 obj4()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj44",
			name = "Menu Action 4",
			description = "",
			position = 20,
			section = "section4",
			hidden = true,
			unhide = "type4",
			unhideValue = "WALL"
	)
	default obj1 obj44()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type4ID",
			name = "Key 4 ID",
			description = "",
			section = "section4"
	)
	default int type4ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type4op",
			name = "Key 4 Opcode",
			description = "",
			hidden = true,
			unhide = "type4",
			unhideValue = "WIDGET",
			section = "section4"
	)
	default int type4op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc4",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type4",
			unhideValue = "WALK",
			section = "section4"
	)
	default String walkLoc4() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type4opp",
			name = "Key 4 Opcode",
			description = "",
			hidden = true,
			unhide = "type4",
			unhideValue = "NPC",
			section = "section4"
	)
	default int type4opp() { return 0; }

	@ConfigItem(
			position = 36,
			keyName = "type4Widget1",
			name = "Key 4 Param 1",
			description = "",
			hidden = true,
			unhide = "type4",
			unhideValue = "WIDGET",
			section = "section4"
	)
	default int type4Widget1() { return 0; }

	@ConfigItem(
			position = 37,
			keyName = "type1Widget2",
			name = "Key 4 Param 2",
			description = "",
			hidden = true,
			unhide = "type4",
			unhideValue = "WIDGET",
			section = "section4"
	)
	default int type4Widget2() { return 0; }




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
			section = "section5"
	)
	default type1 type5() { return type1.WIDGET; }

	@ConfigItem(
			position = 3,
			keyName = "itemID5",
			name = "Item ID 5",
			description = "",
			section = "section5",
			hidden = true,
			unhide = "type5",
			unhideValue = "INVENTORY_ITEM"
	)
	default int itemID5() { return 0; }

	@ConfigItem(
			position = 18,
			keyName = "invItem5",
			name = "Inventory Item 5",
			description = "",
			section = "section5",
			hidden = true,
			unhide = "type5",
			unhideValue = "INVENTORY_ITEM"
	)
	default item1 invItem5() {return item1.FIFTH; }

	@ConfigItem(
			keyName = "obj5",
			name = "Menu Action 5",
			description = "",
			position = 20,
			section = "section5",
			hidden = true,
			unhide = "type5",
			unhideValue = "OBJECT"
	)
	default obj1 obj5()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			keyName = "obj55",
			name = "Menu Action 5",
			description = "",
			position = 20,
			section = "section5",
			hidden = true,
			unhide = "type5",
			unhideValue = "WALL"
	)
	default obj1 obj55()
	{
		return obj1.FIRST;
	}

	@ConfigItem(
			position = 2,
			keyName = "type5ID",
			name = "Key 5 ID",
			description = "",
			section = "section5"
	)
	default int type5ID() { return 0; }

	@ConfigItem(
			position = 21,
			keyName = "type5op",
			name = "Key 5 Opcode",
			description = "",
			hidden = true,
			unhide = "type5",
			unhideValue = "WIDGET",
			section = "section5"
	)
	default int type5op() { return 0; }

	@ConfigItem(
			keyName = "walkLoc5",
			name = "Tile Location",
			description = "",
			position = 43,
			hidden = true,
			unhide = "type5",
			unhideValue = "WALK",
			section = "section5"
	)
	default String walkLoc5() {
		return "0,0,0";
	}

	@ConfigItem(
			position = 21,
			keyName = "type5opp",
			name = "Key 5 Opcode",
			description = "",
			hidden = true,
			unhide = "type5",
			unhideValue = "NPC",
			section = "section1"
	)
	default int type5opp() { return 0; }

	@ConfigItem(
			position = 41,
			keyName = "type5Widget1",
			name = "Key 5 Param 1",
			description = "",
			hidden = true,
			unhide = "type5",
			unhideValue = "WIDGET",
			section = "section5"
	)
	default int type5Widget1() { return 0; }

	@ConfigItem(
			position = 42,
			keyName = "type5Widget2",
			name = "Key 5 Param 2",
			description = "",
			hidden = true,
			unhide = "type5",
			unhideValue = "WIDGET",
			section = "section5"
	)
	default int type5Widget2() { return 0; }
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
