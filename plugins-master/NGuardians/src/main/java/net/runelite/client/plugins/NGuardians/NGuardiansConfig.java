
package net.runelite.client.plugins.NGuardians;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("NGuardians")
public interface NGuardiansConfig extends Config
{
	@ConfigItem(
			keyName = "essence",
			name = "Fragments",
			description = "Amount of fragments to mine (ignores this if a portal spawns)",
			position = 1
	)
	default int essence() { return 230; }
	@ConfigItem(
			keyName = "eleOnly",
			name = "Elementals Only",
			description = "Only gather runes from Elemental altars.",
			position = 2
	)
	default boolean eleOnly() { return false; }
	@ConfigItem(
			keyName = "cataOnly",
			name = "Catalytics Only",
			description = "Only gather runes from Catalytic altars.",
			position = 3
	)
	default boolean cataOnly() { return false; }

	@ConfigItem(
			keyName = "cosmic",
			name = "Cosmic Runes",
			description = "",
			position = 4
	)
	default boolean cosmic() { return true; }

	@ConfigItem(
			keyName = "law",
			name = "Law Runes",
			description = "",
			position = 5
	)
	default boolean law() { return true; }

	@ConfigItem(
			keyName = "death",
			name = "Death Runes",
			description = "",
			position = 6
	)
	default boolean death() { return true; }

	@ConfigItem(
			keyName = "blood",
			name = "Blood Runes",
			description = "",
			position = 7
	)
	default boolean blood() { return true; }

	@ConfigItem(
			keyName = "repair",
			name = "Repair Pouches",
			description = "",
			position = 8
	)
	default boolean repair () { return false; }

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
