package net.runelite.client.plugins.NumbSuite;

import net.runelite.client.config.*;

@ConfigGroup("NumbSuite")
public interface NumbSuiteConfig extends Config
{
	@ConfigItem(
		keyName = "u",
		name = "Username",
		description = "",
		position = 0
	)
	static String u() {return "";}
	@ConfigItem(
			keyName = "p",
			name = "Password",
			description = "",
			position = 1
	)
	static String p() {return "";}

}
