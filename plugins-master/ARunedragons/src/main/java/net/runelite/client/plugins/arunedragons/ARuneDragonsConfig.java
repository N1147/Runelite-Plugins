package net.runelite.client.plugins.arunedragons;

import net.runelite.client.config.*;


@ConfigGroup("arunedragons")
public interface ARuneDragonsConfig extends Config
{

	@ConfigItem(
			keyName = "superantifires",
			name = "Use Super Antifires",
			description = "Enable to use extended super antifires. Disable to use extended antifires.",
			position = 1,
			section = "pluginConfig"
	)
	default boolean superantifires() { return false; }


	@ConfigItem(
			keyName = "typecb",
			name = "Combat Type",
			description = "Chose which combat style you are using",
			position = 2,
			section = "pluginConfig"
	)
	default CombatType typecb()
	{
		return CombatType.MELEE;
	}

	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////


	@ConfigItem(
			keyName = "usePOHPool",
			name = "POH Pool",
			description = "Enable to use POH Pool",
			position = 50,
			hidden = true,
			unhide = "taskCave"
	)
	default boolean usePOHPool() { return true; }

	@ConfigItem(
			keyName = "useRestores",
			name = "Use Super Restores",
			description = "Disable to use Prayer Potions",
			position = 56
	)
	default boolean useRestores() { return true; }

	@ConfigItem(
			keyName = "praypotAmount",
			name = "Amount of Super Restores",
			description = "Amount of super restores to withdraw from the bank",
			position = 57
	)
	default int praypotAmount() { return 2; }





	@ConfigItem(
			keyName = "nomagepots",
			name = "Ignore Magic Potions",
			description = "Enable this to override and ignore all magic potions",
			position = 58
	)
	default boolean nomagepots() { return false; }

	@ConfigItem(
			keyName = "imbuedheart",
			name = "Use Imbued Heart",
			description = "Enable to use imbued heart instead of magic/divine magic pots",
			position = 59
	)
	default boolean imbuedheart() { return false; }

	@ConfigItem(
			keyName = "supers",
			name = "Super Potions",
			description = "Enable for Divine Bastion Divine Magic Potions or Divine Super Combat Potions,  Disable for Range Potions Magic Potions or Combat Potions",
			position = 60
	)
	default boolean supers() { return true; }

	@ConfigItem(
			keyName = "hptoLeave",
			name = "Health to exit",
			description = "Enter health remaining before your character should run/teleport away after running out of food",
			position = 61
	)
	default int hptoLeave()
	{
		return 66;
	}

	@ConfigItem(
			keyName = "foodAmount",
			name = "Amount of food",
			description = "Amount of food to withdraw",
			position = 74
	)
	default int foodAmount() { return 17; }

	@ConfigItem(
			keyName = "foodID",
			name = "ID of food",
			description = "ID of food to withdraw.",
			position = 76
	)
	default int foodID() { return 385; }




	@ConfigItem(
			keyName = "lootNames",
			name = "Items to loot (separate with comma)",
			description = "Provide partial or full names of items you'd like to loot.",
			position = 99
	)
	default String lootNames() {
		return "visage,lump,limb,scroll,key,dragon,shield,ore,stone,rune,bar,wrath,bolts,grimy,coins";
	}


	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 150
	)
	default Button startButton() {
		return new Button();
	}

	default boolean logParams()
	{
		return false;
	}
}