package net.runelite.client.plugins.NGatherer;

import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.util.Arrays;

import static net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_SECOND_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_THIRD_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_FOURTH_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_FIFTH_OPTION;

import static net.runelite.api.MenuAction.NPC_FIRST_OPTION;
import static net.runelite.api.MenuAction.NPC_SECOND_OPTION;
import static net.runelite.api.MenuAction.NPC_THIRD_OPTION;
import static net.runelite.api.MenuAction.NPC_FOURTH_OPTION;
import static net.runelite.api.MenuAction.NPC_FIFTH_OPTION;


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


//* enums


enum NGathererType
{
	PRAYER_POTION(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4),
	SUPER_RESTORE(ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4,
			ItemID.BLIGHTED_SUPER_RESTORE1, ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3,
			ItemID.BLIGHTED_SUPER_RESTORE4),
	RANGED(ItemID.DIVINE_BASTION_POTION1, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION4, ItemID.BASTION_POTION1, ItemID.BASTION_POTION2, ItemID.BASTION_POTION3, ItemID.BASTION_POTION4,ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4),
	ANTIFIRE(ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4,ItemID.EXTENDED_SUPER_ANTIFIRE1,ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4, ItemID.EXTENDED_ANTIFIRE1, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE4),
	ANTIVENOM(ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952, ItemID.ANTIVENOM1_12919 ,ItemID.ANTIVENOM2_12917,ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM4_12913),
	SANFEW_SERUM(ItemID.SANFEW_SERUM1, ItemID.SANFEW_SERUM2, ItemID.SANFEW_SERUM3, ItemID.SANFEW_SERUM4),
	MAGIC(ItemID.IMBUED_HEART, ItemID.DIVINE_MAGIC_POTION1, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION4, ItemID.MAGIC_POTION1, ItemID.MAGIC_POTION2, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4),
	COMBAT(ItemID.DIVINE_SUPER_COMBAT_POTION1, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4, ItemID.COMBAT_POTION1, ItemID.COMBAT_POTION2, ItemID.COMBAT_POTION3, ItemID.COMBAT_POTION4);

	public int[] ItemIDs;

	NGathererType(int... ids)
	{
		this.ItemIDs = ids;
	}

	public boolean containsId(int id)
	{
		return Arrays.stream(this.ItemIDs).anyMatch(x -> x == id);
	}

	public WidgetItem getItemFromInventory(Client client)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
		{
			return null;
		}

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (Arrays.stream(ItemIDs).anyMatch(i -> i == item.getId()))
			{
				return item;
			}
		}

		return null;
	}
}

enum NGathererTypee
{
	NPC_FIRST(NPC_FIRST_OPTION),
	NPC_SECOND(NPC_SECOND_OPTION),
	NPC_THIRD(NPC_THIRD_OPTION),
	NPC_FOURTH(NPC_FOURTH_OPTION),
	NPC_FIFTH(NPC_FIFTH_OPTION),

	OBJECT_FIRST(GAME_OBJECT_FIRST_OPTION),
	OBJECT_SECOND(GAME_OBJECT_SECOND_OPTION),
	OBJECT_THIRD(GAME_OBJECT_THIRD_OPTION),
	OBJECT_FOURTH(GAME_OBJECT_FOURTH_OPTION),
	OBJECT_FIFTH(GAME_OBJECT_FIFTH_OPTION);
	MenuAction action;

	NGathererTypee(MenuAction action)
	{
		this.action = action;
	}
}


enum NGathererTypeee
{
	NPC,
	OBJECT
}

enum NGathererBankType
{
	FIRST(GAME_OBJECT_FIRST_OPTION),
	SECOND(GAME_OBJECT_SECOND_OPTION),
	THIRD(GAME_OBJECT_THIRD_OPTION),
	FOURTH(GAME_OBJECT_FOURTH_OPTION),
	FIFTH(GAME_OBJECT_FIFTH_OPTION);

	MenuAction action;

	NGathererBankType(MenuAction action)
	{
		this.action = action;
	}
}