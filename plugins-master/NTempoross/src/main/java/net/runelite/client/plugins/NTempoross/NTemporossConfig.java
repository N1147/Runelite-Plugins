/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
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
 *
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
package net.runelite.client.plugins.NTempoross;

import net.runelite.client.config.*;

@ConfigGroup("NTempoross")
public interface NTemporossConfig extends Config
{
	@ConfigSection(
		keyName = "title",
		name = "Plugin",
		description = "",
		position = 60
	)
	String title = "title";
	@ConfigItem(
			keyName = "amount",
			name = "Amount of fish",
			description = "Amount of fish to obtain before cooking and/or depositing",
			position = 138,
			section = "title"
	)
	default int amount()
	{
		return 12;
	}
	/*@ConfigItem(
			keyName = "firstInventory",
			name = "First Inventory",
			description = "Amount of fish to obtain before cooking the first inventory.",
			position = 138,
			section = "title"
	)
	default int firstInventory()
	{
		return 7;
	}*/
	@ConfigItem(
			keyName = "OnlyDepositCooked",
			name = "Cook fish",
			description = "If disabled, will deposit raw fish instead.",
			position = 137,
			section = "title"
	)
	default boolean OnlyDepositCooked()
	{
		return true;
	}
	@ConfigItem(
			keyName = "useBuckets",
			name = "Put fires out",
			description = "Must have humidify runes and be on lunar spellbook.",
			position = 139,
			section = "title"
	)
	default boolean useBuckets()
	{
		return true;
	}
	@ConfigItem(
			keyName = "getRopes",
			name = "Skip ropes",
			description = "Enable to skip grabbing a rope if you have the spirit angler outfit.",
			position = 140,
			section = "title"
	)
	default boolean getRopes()
	{
		return true;
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
