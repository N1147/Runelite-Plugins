/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.pestcontrol;

import lombok.Getter;
import net.runelite.api.ObjectID;

@Getter
public enum Boat
{
	NOVICE("Novice", ObjectID.GANGPLANK_14315),
	INTERMEDIATE("Intermediate", ObjectID.GANGPLANK_25631),
	VETERAN("Veteran", ObjectID.GANGPLANK_25632);

	private final String name;
	private final Integer gangplank;

	Boat(String name, Integer gangplank)
	{
		this.name = name;
		this.gangplank = gangplank;
	}
}
