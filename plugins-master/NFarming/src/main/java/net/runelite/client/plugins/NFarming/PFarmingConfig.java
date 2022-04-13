package net.runelite.client.plugins.NFarming;

import net.runelite.client.config.*;


@ConfigGroup("PFarming")
public interface PFarmingConfig extends Config
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
			keyName = "treeSappling",
			name = "Tree Sappling ID",
			description = "",
			position = 11,
			section = "plugin"
	)
	default int treeSappling()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "fruitSappling",
			name = "Fruit Sappling ID",
			description = "",
			position = 12,
			section = "plugin"
	)
	default int fruitSappling()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "treePayment",
			name = "Tree Payment ID",
			description = "",
			position = 13,
			section = "plugin"
	)
	default int treePayment()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "fruitPayment",
			name = "Fruit Payment ID",
			description = "",
			position = 14,
			section = "plugin"
	)
	default int fruitPayment()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "items",
			name = "Items to Drop",
			description = "Only starts dropping when inventory is full",
			position = 11,
			section = "plugin"
	)
	default String items()
	{
		return "6055,5350";
	}

	@ConfigItem(
			keyName = "itemsToNote",
			name = "Items to Note",
			description = "Only notes when inventory is full",
			position = 11,
			section = "plugin"
	)
	default String itemsToNote()
	{
		return "6055,5350";
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
}