package net.runelite.client.plugins.NQuickPray;

import net.runelite.client.config.*;

@ConfigGroup("NQuickPray")
public interface NQuickPrayConfig extends Config
{
	@ConfigItem(
			keyName = "prayMin",
			name = "Minimum Prayer",
			description = "",
			position = 1
	)
	default int prayMin() { return 1; }

	@ConfigItem(
			keyName = "prayMax",
			name = "Maximum Prayer",
			description = "",
			position = 2
	)
	default int prayMax() { return 30; }

}