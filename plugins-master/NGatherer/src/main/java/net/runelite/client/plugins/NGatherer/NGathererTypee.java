/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.NGatherer;

import lombok.Getter;
import net.runelite.api.MenuAction;

import static net.runelite.api.MenuAction.*;

@Getter
public enum NGathererTypee
{
    NPC_FIRST(NPC_FIRST_OPTION),
    NPC_SECOND(NPC_SECOND_OPTION),
    NPC_THIRD(NPC_THIRD_OPTION),
    NPC_FOURTH(NPC_FOURTH_OPTION),
    NPC_FIFTH(NPC_FIFTH_OPTION),

    OBJECT_FIRST(GAME_OBJECT_FIRST_OPTION),
    OBJECT_SECOND(GAME_OBJECT_SECOND_OPTION),
    OBJECT_THIRD(GAME_OBJECT_THIRD_OPTION),
    OBJECT_FOURTH(GAME_OBJECT_FOURTH_OPTION),
    OBJECT_FIFTH(GAME_OBJECT_FIFTH_OPTION);
    MenuAction action;

    NGathererTypee(MenuAction action)
    {
        this.action = action;
    }
}
