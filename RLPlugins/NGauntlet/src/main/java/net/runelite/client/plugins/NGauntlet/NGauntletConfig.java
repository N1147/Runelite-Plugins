/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/Kyleeld>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.NGauntlet;

//import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("NGauntletConfig")
public interface NGauntletConfig extends Config
{
	@ConfigSection(
			name = "Main",
			description = "",
			position = 0
	)
	String Main = "Main";
	@ConfigItem(
			keyName = "EnterCorrupted",
			name = "Corrupted Gauntlet",
			description = "Chose whether to enter normal gauntlet or corrupted",
			position = 1,
			section = Main
	)
	default boolean EnterCorrupted() { return false; }
	@ConfigSection(
			//keyName = "Prep",
			name = "Prep",
			description = "",
			position = 0
	)
	String Prep = "Prep";
	@ConfigItem(
			keyName = "foodAmt",
			name = "Amount of Food",
			description = "Amount of food to gather and cook.",
			position = 0,
			section = Prep
	)
	default int foodAmt() { return 16; }

	@ConfigItem(
			keyName = "AttunedArmour",
			name = "Attuned Body & Legs",
			description = "If disabled, uses basic helm, body and legs",
			position = 1,
			section = Prep
	)
	default boolean AttunedArmour() { return false; }



	@ConfigItem(
			keyName = "PerfectWeapons",
			name = "Perfect Staff",
			description = "If disabled, will only gather perfect bow and attuned staff",
			position = 1,
			section = Prep
	)
	default boolean PerfectWeapons() { return false; }

	@ConfigSection(
			//keyName = "Boss",
			name = "Boss",
			description = "",
			position = 1
	)
	String Boss = "Boss";
	@ConfigItem(
			keyName = "healthMin",
			name = "Health to Eat",
			description = "Minimum health before eating (boss fight only)",
			position = 1,
			section = Boss
	)
	default int healthMin() { return 60; }
	@ConfigItem(
			keyName = "prayMin",
			name = "Pray to Drink",
			description = "Minimum prayer before drinking pots (boss fight only)",
			position = 2,
			section = Boss
	)
	default int prayMin() { return 40; }
	@ConfigItem(
			keyName = "runMin",
			name = "Run to Drink",
			description = "Minimum run energy before drinking pots (boss fight only)",
			position = 3,
			section = Boss
	)
	default int runMin() { return 10; }




	@ConfigSection(
			//keyName = "Prayer",
			name = "Prayer",
			description = "",
			position = 2
	)
	String Prayer = "Prayer";

	@ConfigItem(
			keyName = "Rigour",
			name = "Rigour",
			description = "",
			position = 1,
			section = Prayer
	)
	default boolean Rigour() { return false; }
	@ConfigItem(
			keyName = "Augury",
			name = "Augury",
			description = "",
			position = 1,
			section = Prayer
	)
	default boolean Augury() { return false; }
	@ConfigItem(
			keyName = "Piety",
			name = "Piety",
			description = "",
			position = 1,
			section = Prayer
	)
	default boolean Piety() { return false; }

}
