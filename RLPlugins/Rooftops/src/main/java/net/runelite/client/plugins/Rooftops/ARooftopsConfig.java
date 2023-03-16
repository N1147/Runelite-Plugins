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
package net.runelite.client.plugins.Rooftops;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ARooftops")
public interface ARooftopsConfig extends Config {

    @ConfigItem(
            keyName = "mogPickup",
            name = "Collect Marks",
            description = "Collects marks of grace.",
            position = 19
    )
    default boolean mogPickup() {
        return true;
    }

    @ConfigItem(
            keyName = "mogStack",
            name = "Ardougne marks stack",
            description = "The number of marks to stack before picking up at Ardougne.",
            position = 20
    )
    default int mogStack() {
        return 0;
    }

    @ConfigItem(
            keyName = "alchMogStack",
            name = "Ardougne marks stack",
            description = "The number of marks of grace to be stacked before it is picked up at Ardougne.",
            position = 21
    )
    default int alchMogStack() {
        return 0;
    }

    @ConfigItem(
            keyName = "lowHP",
            name = "Minimum HP",
            description = "Stop if your HP goes below this amount.",
            position = 22
    )
    default int lowHP() {
        return 9;
    }

}
