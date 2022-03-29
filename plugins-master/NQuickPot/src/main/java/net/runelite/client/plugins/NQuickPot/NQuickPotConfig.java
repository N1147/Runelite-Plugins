package net.runelite.client.plugins.NQuickPot;

import net.runelite.client.config.*;

@ConfigGroup("NQuickPot")
public interface NQuickPotConfig extends Config
{
	@ConfigSection(
			keyName = "other",
			name = "Other",
			description = "",
			position = 0
	)
	String other = "other";

	@ConfigSection(
			keyName = "potion1",
			name = "Potion 1",
			description = "",
			position = 1
	)
	String plugin = "potion1";

	@ConfigSection(
			keyName = "potion2",
			name = "Potion 2",
			description = "",
			position = 2
	)
	String potion2 = "potion2";

	@ConfigSection(
			keyName = "potion3",
			name = "Potion 3",
			description = "",
			position = 1
	)
	String potion3 = "potion3";
//////////////////////////////////////////////////////////////////////////////
	@ConfigItem(
			keyName = "pot1",
			name = "Pot 1",
			description = "Type of potion to drink",
			position = 2,
			section = "potion1"
	)
	default NQuickPotPot pot1() { return NQuickPotPot.COMBAT; }
	@ConfigItem(
			keyName = "skill1",
			name = "Pot 1 Skill",
			description = "Skill to use",
			position = 3,
			section = "potion1"
	)
	default NQuickPotType skill1() { return NQuickPotType.STRENGTH; }

	@ConfigItem(
			keyName = "level1",
			name = "Level 1",
			description = "Level at which (or below) to drink the pot",
			position = 3,
			section = "potion1"
	)
	default int level1() { return 106; }
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
	@ConfigItem(
		keyName = "pot2",
		name = "Pot 2",
		description = "Type of potion to drink",
		position = 2,
		section = "potion2"
	)
	default NQuickPotPot pot2() { return NQuickPotPot.RANGED; }
	@ConfigItem(
			keyName = "skill2",
			name = "Pot 2 Skill",
			description = "Skill to use",
			position = 3,
			section = "potion2"
	)
	default NQuickPotType skill2() { return NQuickPotType.RANGED; }

	@ConfigItem(
			keyName = "level2",
			name = "Level 2",
			description = "Level at which (or below) to drink the pot",
			position = 3,
			section = "potion2"
	)
	default int level2() { return 106; }
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
	@ConfigItem(
		keyName = "pot3",
		name = "Pot 3",
		description = "Type of potion to drink",
		position = 2,
		section = "potion3"
	)
	default NQuickPotPot pot3() { return NQuickPotPot.MAGIC; }
	@ConfigItem(
			keyName = "skill3",
			name = "Pot 3 Skill",
			description = "Skill to use",
			position = 3,
			section = "potion3"
	)
	default NQuickPotType skill3() { return NQuickPotType.MAGIC; }

	@ConfigItem(
			keyName = "level3",
			name = "Level 3",
			description = "Level at which (or below) to drink the pot",
			position = 3,
			section = "potion3"
	)
	default int level3() { return 106; }
//////////////////////////////////////////////////////////////////////////////
	@ConfigItem(
			keyName = "antivenom",
			name = "Antivenom/poison",
			description = "Enable to drink antivenom, antipoison or superantipoison when poisoned",
			position = 3,
			section = "other"
	)
	default boolean antivenom() { return true; }

	@ConfigItem(
			keyName = "antifire",
			name = "Antifire",
			description = "Enable to drink antifire, super antifire or extended super antifires constantly",
			position = 4,
			section = "other"
	)
	default boolean antifire() { return false; }

}