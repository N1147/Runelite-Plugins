package net.runelite.client.plugins.NGauntlet;
import net.runelite.client.config.*;

@ConfigGroup("PQuickEat")
public interface PQuickEatConfig extends Config
{

	@ConfigItem(
			keyName = "food1",
			name = "Food 1",
			description = "ID of the first food.",
			position = 29
	)
	default int food1()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "food2",
			name = "Food 2",
			description = "ID of the second food.",
			position = 30
	)
	default int food2() {return 0;}

	@ConfigItem(
			keyName = "food3",
			name = "Food 3",
			description = "ID of the third food.",
			position = 31
	)
	default int food3() {return 0;}

	@ConfigItem(
			keyName = "singleHP",
			name = "Single HP",
			description = "Health to eat at/below",
			position = 32
	)
	default int singleHP()
	{
		return 79 ;
	}

	@ConfigItem(
			keyName = "doubleHP",
			name = "Double HP",
			description = "Health to double eat at/below",
			position = 33
	)
	default int doubleHP() { return 0;}

	@ConfigItem(
			keyName = "brews",
			name = "Sara Brews",
			description = "If enabled, will use Saradomin Brews as the second item in combo eats instead of food 2.",
			position = 35
	)
	default boolean brews() {return true;}

	@ConfigItem(
			keyName = "tripleHP",
			name = "Triple HP",
			description = "Health to triple eat at/below",
			position = 34
	)
	default int tripleHP() { return 0; }
}