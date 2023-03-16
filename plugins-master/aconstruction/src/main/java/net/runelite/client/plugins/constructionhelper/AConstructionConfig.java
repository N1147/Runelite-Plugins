package net.runelite.client.plugins.constructionhelper;

import net.runelite.client.config.*;

@ConfigGroup("aconstruction")
public interface AConstructionConfig extends Config {

    @ConfigItem(keyName = "mode",
            name = "Object: ",
            description = "",
            position = 0
    )
    default CHMode mode() { return CHMode.OAK_TABLE; }

   @ConfigItem(
            keyName = "minWaitTicks",
            name = "Tick Delay Min",
            description = "",
            position = 1
    )
    default int minWaitTicks() { return 1; }

   @ConfigItem(
            keyName = "maxWaitTicks",
            name = "Tick Delay Max",
            description = "",
            position = 2
    )
    default int maxWaitTicks() { return 2; }

    @ConfigItem(
		keyName = "tickDelayTarget",
		name = "Tick Delay Target",
		description = "",
		position = 3,
		section = "delayTickConfig"
	)
    default int tickDelayTarget()
    {
        return 2;
    }

    @Range(
            min = 0,
            max = 6
    )
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Tick Delay Deviation",
		description = "",
		position = 4,
		section = "delayTickConfig"
	)
    default int tickDelayDeviation()
    {
        return 1;
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
