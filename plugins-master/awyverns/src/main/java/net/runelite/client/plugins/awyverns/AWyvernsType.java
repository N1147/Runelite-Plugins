/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.awyverns;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum AWyvernsType
{
    TALONED(7793, new WorldPoint(3612, 10214, 0)),
    SPITTING(7794, new WorldPoint(3624, 10187, 0)),
    LONG_TAILED(7792, new WorldPoint(3603,10192,0)),
    ANCIENT(7795, new WorldPoint(3636, 10209, 0)),

    TALONED_TASK(7793, new WorldPoint(3626, 10289, 0)),
    SPITTING_TASK(7794, new WorldPoint(3613, 10279, 0)),
    LONG_TAILED_TASK(7792, new WorldPoint(3595,10258,0)),
    ANCIENT_TASK(7795, new WorldPoint(3633, 10264, 0));

    int npcID;
    WorldPoint worldPoint;

    AWyvernsType(int npcID, WorldPoint worldPoint)
    {
        this.npcID = npcID;
        this.worldPoint = worldPoint;
    }
}
