package net.runelite.client.plugins.NInfernoHelper;

import net.runelite.client.config.*;

@ConfigGroup("NInfernoHelper")
public interface NInfernoHelperConfig extends Config
{
	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "",
			position = 0
	)
	default String instructions() { return "Swaps prayers during the inferno."; }

}