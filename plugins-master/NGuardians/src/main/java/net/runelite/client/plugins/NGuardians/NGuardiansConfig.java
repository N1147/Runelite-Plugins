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
			position = 2
	)
	default int essence() { return 230; }
	@ConfigItem(
			keyName = "eleOnly",
			name = "Elementals Only",
			description = "Only gather runes from Elemental altars.",
			position = 3
	)
	default boolean eleOnly() { return false; }
	@ConfigItem(
			keyName = "cataOnly",
			name = "Catalytics Only",
			description = "Only gather runes from Catalytic altars.",
			position = 4
	)
	default boolean cataOnly() { return false; }

	@ConfigItem(
			keyName = "cosmic",
			name = "Cosmic Runes",
			description = "",
			position = 5
	)
	default boolean cosmic() { return true; }

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
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 150
	)
	default Button startButton() {
		return new Button();
	}
}
