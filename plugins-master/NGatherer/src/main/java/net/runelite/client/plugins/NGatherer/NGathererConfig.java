package net.runelite.client.plugins.NGatherer;

import net.runelite.client.config.*;


@ConfigGroup("NGatherer")
public interface NGathererConfig extends Config
{
	@ConfigSection(
			keyName = "delayConfig",
			name = "Delay",
			description = "Configure how the bot handles delays",
			position = 0
	)
	String delayConfig = "delayConfig";

	@ConfigSection(
			keyName = "plugin",
			name = "Plugin",
			description = "",
			position = 1
	)
	String plugin = "plugin";

	@ConfigSection(
			keyName = "bank",
			name = "Bank",
			description = "",
			position = 1
	)
	String bank = "bank";

	@ConfigSection(
			keyName = "thieving",
			name = "Overrides",
			description = "",
			position = 1
	)
	String thieving = "thieving";

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Game Tick Min",
			description = "",
			position = 8,
			section = "delayConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayMax",
			name = "Game Tick Max",
			description = "",
			position = 9,
			section = "delayConfig"
	)
	default int tickDelayMax()
	{
		return 2;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayTarget",
			name = "Game Tick Target",
			description = "",
			position = 10,
			section = "delayConfig"
	)
	default int tickDelayTarget()
	{
		return 1;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayDeviation",
			name = "Game Tick Deviation",
			description = "",
			position = 11,
			section = "delayConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "tickDelayWeightedDistribution",
			name = "Game Tick Weighted Distribution",
			description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
			position = 12,
			section = "delayConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}



	@ConfigItem(
			keyName = "aerial",
			name = "Aerial Fishing",
			description = "Enable to cut fish when inventory is full.",
			position = 11,
			section = "thieving"
	)
	default boolean aerial()
	{
		return false;
	}


	@ConfigItem(
			keyName = "typethief",
			name = "Type",
			description = "Select whether to gather from an object or NPC",
			position = 12,
			section = "plugin"
	)
	default NGathererTypeee typethief()
	{
		return NGathererTypeee.NPC;
	}

	@ConfigItem(
			keyName = "thieving",
			name = "Thieving",
			description = "Enable for thieving options.",
			position = 11,
			section = "thieving"
	)
	default boolean thieving()
	{
		return false;
	}

	@ConfigItem(
			keyName = "npcID",
			name = "NPC ID",
			description = "Enter the NPC ID",
			position = 13,
			section = "plugin",
			hidden = true,
			unhide = "typethief",
			unhideValue = "NPC"
	)
	default int npcID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "objID",
			name = "Object ID",
			description = "Enter the Object ID",
			position = 13,
			section = "plugin",
			hidden = true,
			unhide = "typethief",
			unhideValue = "OBJECT"
	)
	default int objID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "menuAction",
			name = "Menu Action",
			description = "Select which menu action is used.",
			position = 14,
			section = "plugin"
	)
	default NGathererTypee type()
	{
		return NGathererTypee.NPC_THIRD;
	}

	@ConfigItem(
			keyName = "returnLoc",
			name = "Location to Return",
			description = "Tile to walk to after banking.",
			position = 15,
			section = "bank",
			hidden = true,
			unhide = "bank"
	)
	default String returnLoc()
	{
		return "0,0,0";
	}

	@ConfigItem(
			keyName = "bank",
			name = "Use Bank",
			description = "",
			position = 16,
			section = "bank"
	)
	default boolean bank() { return false; }

	@ConfigItem(
			keyName = "items",
			name = "Items to keep",
			description = "Separate Item ID's with a comma",
			position = 110,
			section = "plugin"
	)
	default String items() {
		return "0,0";
	}

	@ConfigItem(
			keyName = "loot",
			name = "Items to loot",
			description = "Separate with a comma (partial names work)",
			position = 111,
			section = "plugin"
	)
	default String loot() {
		return "nest,head";
	}

	@ConfigItem(
			keyName = "bankID",
			name = "Bank ID",
			description = "Bank Object ID",
			position = 17,
			section = "bank",
			hidden = true,
			unhide = "bank"
	)
	default int bankID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "bankmenuAction",
			name = "Menu Action",
			description = "Select which menu action is used.",
			position = 18,
			section = "bank",
			hidden = true,
			unhide = "bank"
	)
	default NGathererBankType type2()
	{
		return NGathererBankType.SECOND;
	}

	@ConfigItem(
			keyName = "dodgynecks",
			name = "Dodgy Necklaces",
			description = "",
			position = 19,
			section = "thieving",
			hidden = true,
			unhide = "thieving"
	)
	default boolean dodgynecks() { return false; }

	@ConfigItem(
			keyName = "dodgyNecks",
			name = "Dodgy Neck Amount",
			description = "Amount to withdraw from the bank.",
			position = 20,
			section = "thieving",
			hidden = true,
			unhide = "bank"
	)
	default int dodgyNecks() { return 4; }

	@ConfigItem(
			keyName = "shadowVeil",
			name = "Use Shadow Veil",
			description = "",
			position = 21,
			section = "thieving",
			hidden = true,
			unhide = "thieving"
	)
	default boolean shadowVeil() { return false; }

	@ConfigItem(
			keyName = "maxPouches",
			name = "Max Coin Pouches",
			description = "",
			position = 22,
			section = "thieving",
			hidden = true,
			unhide = "thieving"
	)
	default int maxPouches() { return 15; }

	@ConfigItem(
			keyName = "foodID",
			name = "Food ID",
			description = "Food ID",
			position = 23,
			section = "thieving",
			hidden = true,
			unhide = "thieving"
	)
	default int foodID() { return 1993; }

	@ConfigItem(
			keyName = "foodAmount",
			name = "Food Amount",
			description = "Amount of food.",
			position = 24,
			section = "bank",
			hidden = true,
			unhide = "bank"
	)
	default int foodAmount() { return 14; }

	@ConfigItem(
			keyName = "minHealth",
			name = "Min Health",
			description = "Minimum health allowed before eating.",
			position = 25,
			section = "thieving",
			hidden = true,
			unhide = "thieving"
	)
	default int minHealth() { return 50; }

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