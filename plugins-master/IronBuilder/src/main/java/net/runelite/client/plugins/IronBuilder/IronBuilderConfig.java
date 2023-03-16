package net.runelite.client.plugins.IronBuilder;
import net.runelite.client.config.*;

@ConfigGroup("IronBuilder")
public interface IronBuilderConfig extends Config
{
	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "Instructions. Don't enter anything into this field",
			position = 0,
			section = "instructionsTitle"
	)
	default String instructions()
	{
		return "Stats required: 35 fishing";
	}

	@ConfigItem(
			keyName = "randomorder",
			name = "Random Order",
			description = "Randomize order of skills",
			position = 1
	)
	default int randomorder() { return 0; }
}