/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.apker;

import lombok.Getter;
import net.runelite.api.MenuAction;

import static net.runelite.api.MenuAction.*;

@Getter
public enum item1
{
    USE(ITEM_USE),
//    ON_OBJECT(ITEM_USE_ON_GAME_OBJECT),
//    ON_GROUND(ITEM_USE_ON_GROUND_ITEM),
//    ON_PLAYER(ITEM_USE_ON_PLAYER),
//    ON_NPC(ITEM_USE_ON_NPC),
    FIRST(ITEM_FIRST_OPTION),
    SECOND(ITEM_SECOND_OPTION),
    THIRD(ITEM_THIRD_OPTION),
    FOURTH(ITEM_FOURTH_OPTION),
    FIFTH(ITEM_FIFTH_OPTION),
    ;

    MenuAction action;

    item1(MenuAction action)
    {
        this.action = action;
    }
}
