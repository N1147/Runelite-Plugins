package net.runelite.client.plugins.Gatherer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;


@ConfigGroup("Gatherer")
public interface NGathererConfig extends Config
{
	@ConfigSection(
			name = "Bank",
			description = "",
			position = 2
	)
	String bank = "bank";

	@ConfigSection(
			name = "Type",
			description = "",
			position = 3
	)
	String type = "type";

	@ConfigSection(
			name = "Config",
			description = "",
			position = 4
	)
	String config = "config";


	@ConfigItem(
			keyName = "typethief",
			name = "Type",
			description = "Enable for NPC, disable for Objects.",
			position = 6,
			section = type
	)
	default boolean typethief()
	{
		return false;
	}

	@ConfigItem(
			keyName = "aerial",
			name = "Aerial Fishing",
			description = "Enable to cut fish when inventory is full.",
			position = 7,
			section = type
	)
	default boolean aerial()
	{
		return false;
	}

	@ConfigItem(
			keyName = "thieving",
			name = "Thieving",
			description = "Enable for thieving options.",
			position = 8,
			section = type
	)
	default boolean thieving()
	{
		return false;
	}

	@ConfigItem(
			keyName = "npcID",
			name = "NPC ID",
			description = "Enter the NPC ID",
			position = 9,section = type
	)
	default int npcID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "objID",
			name = "Object ID",
			description = "Enter the Object ID",
			position = 10,section = type
	)
	default int objID()
	{
		return 0;
	}


	@ConfigItem(
			keyName = "returnLoc",
			name = "Location to Return",
			description = "Tile to walk to after banking.",
			position = 15,section = bank
	)
	default String returnLoc()
	{
		return "0,0,0";
	}

	@ConfigItem(
			keyName = "bank",
			name = "Use Bank",
			description = "",
			position = 16,section = bank
	)
	default boolean bank() { return false; }

	@ConfigItem(
			keyName = "item1",
			name = "Items to Keep",
			description = "",
			position = 109,section = config
	)
	default String item1() {
		return "Rune axe,";
	}

	@ConfigItem(
			keyName = "loot",
			name = "Items to loot",
			description = "Separate with a comma (partial names work)",
			position = 111,section = config
	)
	default String loot() {
		return "nest,head";
	}

	@ConfigItem(
			keyName = "dodgynecks",
			name = "Dodgy Necklaces",
			description = "",
			position = 19,section = config
	)
	default boolean dodgynecks() { return false; }

	@ConfigItem(
			keyName = "dodgyNecks",
			name = "Dodgy Neck Amount",
			description = "Amount to withdraw from the bank.",
			position = 20,section = bank
	)
	default int dodgyNecks() { return 4; }

	/*@ConfigItem(
			keyName = "shadowVeil",
			name = "Use Shadow Veil",
			description = "",
			position = 21,section = config
	)*/
	default boolean shadowVeil() { return false; }

	@ConfigItem(
			keyName = "foodID",
			name = "Food ID",
			description = "Food ID. MUST BE 0 FOR NONE!",
			position = 23,section = config
	)
	default int foodID() { return 0; }

	@ConfigItem(
			keyName = "foodAmount",
			name = "Food Amount",
			description = "Amount of food.",
			position = 24,section = bank
	)
	default int foodAmount() { return 14; }

	@ConfigItem(
			keyName = "minHealth",
			name = "Min Health",
			description = "Minimum health allowed before eating.",
			position = 25,section = config
	)
	default int minHealth() { return 50; }

}