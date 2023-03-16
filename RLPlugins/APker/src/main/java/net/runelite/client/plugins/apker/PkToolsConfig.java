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



	@ConfigSection(
		//keyName = "label1",
		name = "Hotkeys",
		description = "",
		position = 1
	)
	String Hotkeys = "Hotkeys";

	@ConfigItem(
		keyName = "key1",
		name = "Hotkey 1",
		description = "Hotkey to activate script 1",
		position = 2,
			section = Hotkeys
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
		section = Hotkeys
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
			section = Hotkeys
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
			section = Hotkeys
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
			section = Hotkeys
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
			section = Hotkeys
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
			section = Hotkeys
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
			section = Hotkeys
	)
	default Keybind key8()
	{
		return new Keybind(KeyEvent.VK_8, 0);
	}

	@ConfigItem(
		position = 10,
		keyName = "key1_script",
		name = "Script 1",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key1_script()
	{
		return "group1\npiety\nspec\nclickenemy";
	}

	@ConfigItem(
		position = 11,
		keyName = "key2_script",
		name = "Script 2",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key2_script()
	{
		return "group2\nrigour\nclickenemy";
	}

	@ConfigItem(
		position = 12,
		keyName = "key3_script",
		name = "Script 3",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key3_script()
	{
		return "group3\naugury\nfreeze\nclickenemy";
	}

	@ConfigItem(
		position = 13,
		keyName = "key4_script",
		name = "Script 4",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key4_script()
	{
		return "teleblock\nclickenemy";
	}

	@ConfigItem(
		position = 14,
		keyName = "key5_script",
		name = "Script 5",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key5_script()
	{
		return "protectfrommagic";
	}

	@ConfigItem(
		position = 15,
		keyName = "key6_script",
		name = "Script 6",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key6_script()
	{
		return "protectfrommissiles";
	}

	@ConfigItem(
		position = 16,
		keyName = "key7_script",
		name = "Script 7",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key7_script()
	{
		return "protectfrommelee";
	}

	@ConfigItem(
		position = 17,
		keyName = "key8_script",
		name = "Script 8",
		description = PkToolsConfig.commands,
			section = Hotkeys
	)
	default String key8_script()
	{
		return "blood\nclickenemy";
	}

	//@ConfigTitle(
	//	keyName = "label2",
	//	name = "Configuration",
	//	description = "",
	//	position = 18
	//)
	//String label2 = "label2";


	/*@ConfigItem(
			position = 19,
			keyName = "SpecOnXPDrop",
			name = "Auto Spec on XP Drop",
			description = "Automatically spec enemy when an xp drop is above amount occurs."
	)*/
	default boolean SpecOnXPDrop()
	{
		return false;
	}


/*	@ConfigItem(
			position = 20,
			keyName = "xpDropAmount",
			name = "XP Drop Amount",
			description = "Automatically spec enemy when an xp drop above amount occurs."
	)*/
	default int xpDropAmount()
	{
		return 0;
	}

	@ConfigItem(
		position = 21,
		keyName = "autoPrayerSwitcher",
		name = "Prayer Switcher",
		description = "Automatically Switch Prayers based on opponent's gear.",
			section = Auto
	)
	default boolean autoPrayerSwitcher()
	{
		return false;
	}



	/*@ConfigItem(
		position = 22,
		keyName = "specOnHealthAmount",
		name = "Spec Below Health Amount",
		description = "Automatically spec enemy when their health is below amount"
	)
	default int specOnHealthAmount(){return 67;}


	@ConfigItem(
			position = 21,
			keyName = "specOnHealth",
			name = "Spec When Health Below",
			description = "Automatically spec enemy when their health is below amount"
	)
	default boolean specOnHealth()
	{
		return false;
	}*/



	@ConfigSection(
			//keyName = "Potions",
			name = "Full Auto",
			description = "",
			position = 2
	)
	String Auto = "Auto";

	@ConfigItem(
			position = 22,
			keyName = "enableMeleePrayer",
			name = "Enable Melee Prayer",
			description = "If disabled, will ignore melee prayer switches",
			section = Auto

	)
	default boolean enableMeleePrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 23,
			keyName = "enableMagicPrayer",
			name = "Enable Magic Prayer",
			description = "If disabled, will ignore magic prayer switches",
			section = Auto

	)
	default boolean enableMagicPrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 24,
			keyName = "enableRangedPrayer",
			name = "Enable Ranged Prayer",
			description = "If disabled, will ignore ranged prayer switches",
			section = Auto

	)
	default boolean enableRangedPrayer()
	{
		return true;
	}

	/*@ConfigItem(
		position = 28,
		keyName = "prayerHelper",
		name = "Prayer Helper",
		description = "Draws icons to suggest proper prayer switches",
		hidden = true
	)
	default boolean prayerHelper()
	{
		return false;
	}*/

	@ConfigItem(
			position = 36,
			keyName = "autoGearSwap",
			name = "Automatically Swap Gear",
			description = "Swap gear based on opponent's weakness using group 1 as meelee, 2 as ranged, 3 as magic",
			section = Auto
	)
	default boolean autoGearSwap()
	{
		return true;
	}

	@ConfigItem(
			position = 37,
			keyName = "swapFromPray",
			name = "Auto Gear Swap Type",
			description = "If Enabled, will swap your gear based on opponents overhead prayer, if Disabled, uses their weapon type",
			section = Auto
	)
	default boolean swapFromPray()
	{
		return true;
	}



	@ConfigSection(
			//keyName = "Potions",
			name = "Eating",
			description = "",
			position = 5
	)
	String Eating = "Eating";

	@ConfigItem(
			keyName = "food1",
			name = "Food 1",
			description = "ID of the first food.",
			position = 38,
			section = Eating
	)
	default int food1()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "food2",
			name = "Food 2",
			description = "ID of the second food.",
			position = 39,
			section = Eating
	)
	default int food2() {return 0;}

	@ConfigItem(
			keyName = "food3",
			name = "Food 3",
			description = "ID of the third food.",
			position = 40,
			section = Eating
	)
	default int food3() {return 0;}

	@ConfigItem(
			keyName = "singleHP",
			name = "Single HP",
			description = "Health to eat at/below",
			position = 41,
			section = Eating
	)
	default int singleHP()
	{
		return 79 ;
	}

	@ConfigItem(
			keyName = "doubleHP",
			name = "Double HP",
			description = "Health to double eat at/below",
			position = 42,
			section = Eating
	)
	default int doubleHP() { return 0;}

	@ConfigItem(
			keyName = "brews",
			name = "Sara Brews",
			description = "If enabled, will use Saradomin Brews as the second item in combo eats instead of food 2.",
			position = 43,
			section = Eating
	)
	default boolean brews() {return true;}

	@ConfigItem(
			keyName = "tripleHP",
			name = "Triple HP",
			description = "Health to triple eat at/below",
			position = 44,
			section = Eating
	)
	default int tripleHP() { return 0; }


	@ConfigItem(
			keyName = "prayer",
			name = "Min. Prayer",
			description = "Prayer to drink at/below",
			position = 45,
			section = Eating
	)
	default int prayer() { return 0; }

	@ConfigItem(
			keyName = "Antifire",
			name = "Drink antifires",
			description = "Drink any form of available antifires",
			position = 46,
			section = Eating
	)
	default boolean Antifire() { return false; }

	@ConfigItem(
			keyName = "Antivenom",
			name = "Drink antivenom",
			description = "Drink any form of available antivenom/antidote",
			position = 47,
			section = Eating
	)
	default boolean Antivenom() { return false; }


}
