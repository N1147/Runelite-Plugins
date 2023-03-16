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
			description = "Disables full automation. Plugin will ONLY automate\n boss mechanics such as quick prayers, \navoid bombs, acid walk and kill ice spawns.",
			position = 0
	)
	default boolean helperMode() { return false; }

	@ConfigItem(
			keyName = "usage" ,
			name = "Instructions",
			description = "",
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
			keyName = "foodID",
			name = "ID of food 1",
			description = "ID of food to withdraw.",
			position = 80,
			section = Potions
	)
	default int foodID() { return 385; }


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

}