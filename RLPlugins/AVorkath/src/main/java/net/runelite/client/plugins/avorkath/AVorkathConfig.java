package net.runelite.client.plugins.avorkath;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;


@ConfigGroup("avork")
public interface AVorkathConfig extends Config
{
	@ConfigItem(
			keyName = "helperMode",
			name = "Helper Mode",
			description = "Disables full automation. Plugin will only enable quick prayers, avoid bombs, acid walk and kill ice spawns. Will NOT attack, loot or bank.",
			position = 0
	)
	default boolean helperMode() { return false; }

	@ConfigItem(
			keyName = "usage" ,
			name = "Instructions",
			description = "Requires a POH mounted glory, and house to be in Relekka.",
			position = 1
	)
	default String usage() { return ""; }


	@ConfigSection(
			//keyName = "Potions",
			name = "Pots/Food",
			description = "",
			position = 14
	)
	String Potions = "Potions";

	@ConfigSection(
			//keyName = "Spec",
			name = "Spec",
			description = "",
			position = 15
	)
	String Spec = "Spec";

	@ConfigSection(
			//keyName = "Other",
			name = "Other",
			description = "",
			position = 16
	)
	String Other = "Other";

	@ConfigItem(
			keyName = "potThreshold",
			name = "Level to Drink",
			description = "Enter level to drink combat related potions, e.g set at 99, it will drink at or below 99",
			position = 56,
			section = Potions
	)
	default int potThreshold() { return 99; }

	@ConfigItem(
			keyName = "useRanged",
			name = "Ranged Mode",
			description = "If disabled, uses melee",
			position = 56,
			section = Other
	)
	default boolean useRanged() { return true; }

	@ConfigItem(
			keyName = "autoBank",
			name = "Auto Restock",
			description = "If disabled, will only automate the kills and not bank for you.",
			position = 57,
			section = Other
	)
	default boolean autoBank() { return true; }

	@ConfigItem(
			keyName = "useBlowpipe",
			name = "Blowpipe",
			description = "If disabled, will attempt to swap bolts",
			position = 58,
			section = Other
	)
	default boolean useBlowpipe() { return false; }

	@ConfigItem(
			keyName = "lootNames",
			name = "Items to loot (separate with comma)",
			description = "Provide partial or full names of items you'd like to loot.",
			position = 65,
			section = Other
	)
	default String lootNames() {
		return "visage,lump,limb,scroll,key,med,legs,shield,shield,ore,stone,rune,bar,wrath,bolts,grimy,coins";
	}

	@ConfigItem(
			keyName = "superantifire",
			name = "Ext. Super Antifire",
			description =  "Enable to use Extended Super Antifire. Disable to use regular antifire.",
			position = 66,
			section = Potions
	)
	default boolean superantifire()
	{
		return true;
	}

	@ConfigItem(
			keyName = "antivenomplus",
			name = "Anti Venom+",
			description =  "Enable to use Anti-venom+. Disable to use Antidote++",
			position = 67,
			section = Potions
	)
	default boolean antivenomplus()
	{
		return true;
	}

	@ConfigItem(
			keyName = "antipoisonamount",
			name = "Antivenom Amount",
			description =  "Amount of (4) dose Antivenom+, or Antidote++ to take",
			position = 67,
			section = Potions
	)
	default int antipoisonamount() { return 1; }

	@ConfigItem(
			keyName = "usePOHpool",
			name = "Drink POH Pool",
			description =  "Enable to drink from POH pool to restore HP / Prayer.",
			position = 68,
			section = Other
	)
	default boolean usePOHpool()
	{
		return true;
	}


	@ConfigItem(
			keyName = "praypotAmount",
			name = "Amount of Super Restores",
			description = "Amount of super restores to withdraw from the bank",
			position = 69,
			section = Potions
	)
	default int praypotAmount() { return 2; }

	@ConfigItem(
			keyName = "useRestores",
			name = "Use Super Restores",
			description = "Disable to use Prayer Potions",
			position = 70,
			section = Potions
	)
	default boolean useRestores() { return true; }

	@ConfigItem(
			keyName = "onlytelenofood",
			name = "Only Tele With No Food",
			description =  "Enable to only teleport out when you have 0 food and / or 0 restore pots. Disable to teleport out after every kill.",
			position = 71,
			section = Other
	)
	default boolean onlytelenofood()
	{
		return false;
	}

	@ConfigItem(
			keyName = "foodAmount",
			name = "Amount of food 1",
			description = "Amount of food to withdraw",
			position = 79,
			section = Potions
	)
	default int foodAmount() { return 17; }

	@ConfigItem(
			keyName = "foodID",
			name = "ID of food 1",
			description = "ID of food to withdraw.",
			position = 80,
			section = Potions
	)
	default int foodID() { return 385; }

	@ConfigItem(
			keyName = "foodAmount2",
			name = "Amount of food 2",
			description = "Amount of food to withdraw",
			position = 81,
			section = Potions
	)
	default int foodAmount2() { return 4; }

	@ConfigItem(
			keyName = "foodID2",
			name = "ID of food 2",
			description = "ID of food to withdraw. (Only use if combo eating is required)",
			position = 82,
			section = Potions
	)
	default int foodID2() { return 0; }

	@ConfigItem(
			keyName = "healthTP",
			name = "Min Health",
			description = "Minimum health to allow before teleporting (after running out of food)",
			position = 82,
			section = Other
	)
	default int healthTP() { return 40; }

	@ConfigItem(
			keyName = "prayTP",
			name = "Min Pray",
			description = "Minimum prayer to allow before teleporting (after running out of potions)",
			position = 82,
			section = Other
	)
	default int prayTP() { return 1; }

	@ConfigItem(
			keyName = "useSpec",
			name = "Use Spec Weapon",
			description = "Enable to use a special attack.",
			position = 83,
			section = Spec
	)
	default boolean useSpec() { return false; }

	@ConfigItem(
			keyName = "specWeapon",
			name = "Spec Weapon ID",
			description = "ID of special attack weapon",
			position = 84,

			section = Spec
	)
	default int specWeapon() { return 0; }

	@ConfigItem(
			keyName = "normalWeapon",
			name = "Regular Weapon ID",
			description = "ID of regular weapon",
			position = 85,

			section = Spec
	)
	default int normalWeapon() { return 0; }

	@ConfigItem(
			keyName = "normalOffhand",
			name = "Regular Offhand ID",
			description = "ID of regular offhand (0 for none)",
			position = 85,

			section = Spec
	)
	default int normalOffhand() { return 0; }

	@ConfigItem(
			keyName = "specHP",
			name = "Spec HP",
			description = "Minimum health Vorkath must have before spec",
			position = 86,

			section = Spec
	)
	default int specHP() { return 200; }

	@ConfigItem(
			keyName = "specThreshold",
			name = "Spec Energy",
			description = "Amount of special attack energy required to spec",

			section = Spec
	)
	default int specThreshold() { return 50; }


	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 150
	)
	default Button startButton() {
		return new Button();
	}

}