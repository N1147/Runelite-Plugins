
package net.runelite.client.plugins.abankstander;

import net.runelite.client.config.*;

@ConfigGroup("AFletcher")
public interface ABankStanderConfig extends Config
{

	@ConfigItem(
			keyName = "Type",
			name = "Type",
			description = "Type of skill to train.",
			position = 0,
			title = "agilityTitle"
	)
	default ABankStanderType Type() {
		return ABankStanderType.Fletcher;
	}

	@ConfigItem(
			keyName = "fletchType",
			name = "Items to fletch",
			description = "Type of item to fletch.",
			position = 2,
			title = "fletchTitle",
			hidden = true,
			unhide = "Type",
			unhideValue = "Fletcher"

	)
	default ABankStanderTypeFletching fletchType() {
		return ABankStanderTypeFletching.SHORTBOWS;
	}

	@ConfigItem(
			keyName = "runePouch",
			name = "Use Rune Pouch",
			description = "Uses rune pouch instead of runes.",
			position = 1,
			title = "pouchTitle"
	)
	default boolean runePouch() {
		return false;
	}

	@ConfigItem(
			keyName = "gemId",
			name = "Gem ID",
			description = "Enter the Id of the gem you want to cut.",
			position = 4,
			hidden = true,
			unhide = "Type",
			unhideValue = "Crafter"
	)
	default int gemId() { return 0; }

	@ConfigItem(
			keyName = "herbId",
			name = "Herb ID",
			description = "Enter the Id of the herb you want to clean.",
			position = 5,
			hidden = true,
			unhide = "Type",
			unhideValue = "HERB_CLEANER"
	)
	default int herbId() { return 0; }


	@ConfigItem(
			keyName = "logId",
			name = "Log ID (Fletcher)",
			description = "Enter the Id of the log you want to use.",
			position = 6,
			hidden = true,
			unhide = "Type",
			unhideValue = "Fletcher"
	)
	default int logId() { return 0; }



	@ConfigItem(
			keyName = "logId1",
			name = "Unstrung Bow ID",
			description = "Enter the Id of the bow you want to use.",
			position = 6,
			hidden = true,
			unhide = "Type",
			unhideValue = "STRING_BOWS"
	)
	default int logId1() { return 0; }


	@ConfigItem(
			keyName = "planklogId1",
			name = "Log ID (Sawmill)",
			description = "Enter the Id of the log you want to use.",
			position = 7,
			hidden = true,
			unhide = "Type",
			unhideValue = "SAWMILL_PLANKS"
	)
	default int planklogId1() { return 0; }

	@ConfigItem(
			keyName = "planklogId",
			name = "Log ID (Plankmake)",
			description = "Enter the Id of the log you want to use.",
			position = 7,
			hidden = true,
			unhide = "Type",
			unhideValue = "PLANK_MAKE_SPELL"
	)
	default int planklogId() { return 0; }


	@ConfigItem(
			keyName = "foodId",
			name = "Food ID (Cooker)",
			description = "Enter the Id of the food you want to use.",
			position = 8,
			hidden = true,
			unhide = "Type",
			unhideValue = "COOKER"
	)
	default int foodId() { return 0; }
	@ConfigItem(
			keyName = "rangeId",
			name = "Range ID",
			description = "Enter the Id of the range you want to use.",
			position = 9,
			hidden = true,
			unhide = "Type",
			unhideValue = "COOKER"
	)
	default int rangeId() { return 21302; }

	@ConfigItem(
			keyName = "rangeIdK",
			name = "Range ID",
			description = "Enter the Id of the range you want to use.",
			position = 9,
			hidden = true,
			unhide = "Type",
			unhideValue = "COOK_KARAMBWANS"
	)
	default int rangeIdK() { return 21302; }







	@ConfigItem(
			keyName = "toSmith",
			name = "Item to Smith",
			description = "Type of item to make.",
			position = 11,
			title = "agilityTitle",
			hidden = true,
			unhide = "Type",
			unhideValue = "SMITHER"
	)
	default ABankStanderTypeToSmith toSmith() {
		return ABankStanderTypeToSmith.DART_TIPS;
	}

	@ConfigItem(
			keyName = "barType",
			name = "Type of Bar",
			description = "Type of bar to make.",
			position = 12,
			title = "agilityTitle",
			hidden = true,
			unhide = "Type",
			unhideValue = "SMITHER"

	)
	default ABankStanderBarType barType() {
		return ABankStanderBarType.Bronze;
	}

	@ConfigItem(
			keyName = "barType1",
			name = "Type of Bar",
			description = "Type of bar to use.",
			position = 12,
			title = "agilityTitle",
			hidden = true,
			unhide = "Type",
			unhideValue = "SMELTER"

	)
	default ABankStanderBarType barType1() {
		return ABankStanderBarType.Bronze;
	}

	@ConfigItem(
			keyName = "furnaceId",
			name = "Furnace ID",
			description = "Enter the Id of the furnace you want to use.",
			position = 13,
			hidden = true,
			unhide = "Type",
			unhideValue = "SMELTER"
	)
	default int furnaceId() { return 16469; }


	@ConfigItem(
			keyName = "useStun",
			name = "Stun?",
			description = "Enable to stun alch",
			position = 11,
			hidden = true,
			unhide = "Type",
			unhideValue = "HIGH_ALCHER"
	)
	default boolean useStun() { return false; }

	@ConfigItem(
			keyName = "alchID",
			name = "Item ID (High Alcher)",
			description = "Enter the Id of the item you want to Alch.",
			position = 10,
			hidden = true,
			unhide = "Type",
			unhideValue = "HIGH_ALCHER"
	)
	default int alchID() { return 0; }

	@ConfigItem(
			keyName = "stunID",
			name = "NPC ID",
			description = "Enter the Id of the NPC you want to stun.",
			position = 14,
			hidden = true,
			unhide = "Type",
			unhideValue = "HIGH_ALCHER"
	)
	default int stunID() { return 0; }

	@ConfigSection(
			keyName = "delayConfig",
			name = "Delay Configuration",
			description = "Configure how the bot handles sleep delays in milliseconds",
			position = 19
	)
	String delayConfig = "delayConfig";


	@ConfigItem(
			keyName = "sleepDelayMin",
			name = "Sleep Delay Minimum",
			description = "Sleep delay minimum.",
			position = 20,
			section = "delayConfig"

	)
	default int sleepDelayMin() { return 10; }

	@ConfigItem(
			keyName = "sleepDelayMax",
			name = "Sleep Delay Maximum",
			description = "Sleep delay maximum.",
			position = 21,
			section = "delayConfig"
	)
	default int sleepDelayMax() { return 550; }

	@ConfigItem(
			keyName = "sleepDelayDev",
			name = "Sleep Delay Deviation",
			description = "Sleep delay deviation.",
			position = 22,
			section = "delayConfig"
	)
	default int sleepDelayDev() { return 70; }

	@ConfigItem(
			keyName = "sleepDelayTarg",
			name = "Sleep Delay Target",
			description = "Sleep Tick delay target.",
			position =23,
			section = "delayConfig"
	)
	default int sleepDelayTarg() { return 100; }




	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Tick Delay Minimum",
			description = "Tick delay minimum.",
			position = 24,
			section = "delayConfig"

	)
	default int tickDelayMin() { return 1; }

	@ConfigItem(
			keyName = "tickDelayMax",
			name = "Tick Delay Maximum",
			description = "Tick delay maximum.",
			position = 25,
			section = "delayConfig"
	)
	default int tickDelayMax() { return 3; }

	@ConfigItem(
			keyName = "tickDelayDev",
			name = "Tick Delay Deviation",
			description = "Tick delay deviation.",
			position = 26,
			section = "delayConfig"
	)
	default int tickDelayDev() { return 1; }

	@ConfigItem(
			keyName = "tickDelayTarg",
			name = "Tick Delay Target",
			description = "Tick delay target.",
			position =27,
			section = "delayConfig"
	)
	default int tickDelayTarg() { return 1; }

	@ConfigItem(
			keyName = "enableUI",
			name = "Enable UI",
			description = "Enable to turn on in game UI",
			position = 140
	)
	default boolean enableUI()
	{
		return true;
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