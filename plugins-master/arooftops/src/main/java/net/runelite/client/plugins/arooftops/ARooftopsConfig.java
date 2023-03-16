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
package net.runelite.client.plugins.arooftops;

import net.runelite.client.config.*;

@ConfigGroup("ARooftops")
public interface ARooftopsConfig extends Config {
    @ConfigTitle(
            keyName = "delayConfig",
            name = "Delay Configuration",
            description = "Configure how the bot handles sleep delays in milliseconds",
            position = 1
    )
    String delayConfig = "delayConfig";

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMin",
            name = "Sleep Min",
            description = "",
            position = 2,
            section = "delayConfig"
    )
    default int sleepMin() {
        return 60;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 3,
            section = "delayConfig"
    )
    default int sleepMax() {
        return 350;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 4,
            section = "delayConfig"
    )
    default int sleepTarget() {
        return 100;
    }

    @Range(
            min = 0,
            max = 550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 5,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 6,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

    /*@ConfigTitle(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 7,
            hidden = true
    )*/
    String delayTickConfig = "delayTickConfig";

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMin",
            name = "Game Tick Min",
            description = "",
            position = 8,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayMin() {
        return 1;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 9,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayMax() {
        return 3;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayTarget",
            name = "Game Tick Target",
            description = "",
            position = 10,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayTarget() {
        return 2;
    }

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayDeviation",
            name = "Game Tick Deviation",
            description = "",
            position = 11,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 12,
            section = "delayTickConfig",
            hidden = true
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
            keyName = "agilityTitle",
            name = "Rooftop Configuration",
            description = "",
            position = 13,
            hidden = true
    )
    String agilityTitle = "agilityTitle";

    @ConfigItem(
            keyName = "highAlch",
            name = "High Alch",
            description = "Enable to High Alch while running",
            position = 14,
            title = "agilityTitle",
            hidden = true
    )
    default boolean highAlch() {
        return false;
    }

    @ConfigItem(
            keyName = "Course",
            name = "Course",
            description = "Supported agility courses",
            position = 15,
            title = "agilityTitle",
            hidden = false,
            hide = "highAlch",
            hideValue = "true"
    )
    default Course course() {
        return Course.GNOME;
    }


    @ConfigItem(
            keyName = "alchCourse",
            name = "Banking Courses",
            description = "Agility courses that support bank restocking",
            position = 16,
            title = "agilityTitle",
            hidden = true,
            unhide = "highAlch",
            unhideValue = "true"
    )
    default AlchCourse alchcourse() {
        return AlchCourse.DRAYNOR;
    }

    @ConfigItem(
            keyName = "alchItemID",
            name = "Alch Item ID",
            description = "Item ID (un-noted) of item you wish to high alch.",
            position = 17,
            title = "agilityTitle",
            hidden = true,
            unhide = "highAlch"
    )
    default int alchItemID() {
        return 0;
    }

    @ConfigItem(
            keyName = "bankRestock",
            name = "Bank to restock items",
            description = "Go to bank to restock items for high alch. Auto-disables at unsupported locations or bank doesn't contain item.",
            position = 18,
            title = "agilityTitle",
            hidden = true,
            unhide = "highAlch"
    )
    default boolean bankRestock() {
        return false;
    }

    @ConfigItem(
            keyName = "mogPickup",
            name = "Collect Marks",
            description = "Collects marks of grace.",
            position = 19,
            title = "agilityTitle"
    )
    default boolean mogPickup() {
        return true;
    }

    @ConfigItem(
            keyName = "mogStack",
            name = "Ardougne marks stack",
            description = "The number of marks to stack before picking up at Ardougne.",
            position = 20,
            title = "agilityTitle",
            hidden = true
            /*,
            hide = "highAlch",
            hideValue = "true"*/
    )
    default int mogStack() {
        return 0;
    }

    @ConfigItem(
            keyName = "alchMogStack",
            name = "Ardougne marks stack",
            description = "The number of marks of grace to be stacked before it is picked up at Ardougne.",
            position = 21,
            title = "agilityTitle",
            hidden = true
            /*,
            unhide = "highAlch",
            unhideValue = "true"*/
    )
    default int alchMogStack() {
        return 0;
    }

    @ConfigItem(
            keyName = "lowHP",
            name = "Minimum HP",
            description = "Stop if your HP goes below this amount.",
            position = 22,
            title = "agilityTitle"
    )
    default int lowHP() {
        return 9;
    }

    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 23,
            title = "agilityTitle"
    )
    default boolean enableUI() {
        return true;
    }

    @ConfigItem(
            keyName = "camelotTeleport",
            name = "Camelot Teleport",
            description = "Will teleport when running Camelot course.",
            position = 24,
            title = "agilityTitle"
    )
    default boolean camelotTeleport() {
        return false;
    }

    @ConfigItem(
            keyName = "boostWithPie",
            name = "Summer Pies",
            description = "Use summer pies.",
            position = 25,
            title = "agilityTitle"
    )
    default boolean boostWithPie() {
        return false;
    }

    @ConfigItem(
            keyName = "pieLevel",
            name = "Min boost level",
            description = "A summer pie will be eaten when your Agility is below this level.",
            position = 26,
            title = "agilityTitle",
            hidden = true,
            unhide = "boostWithPie",
            unhideValue = "true"
    )
    default int pieLevel() {
        return 80;
    }


}
