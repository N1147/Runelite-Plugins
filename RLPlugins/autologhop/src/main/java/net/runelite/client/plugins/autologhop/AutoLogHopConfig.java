package net.runelite.client.plugins.autologhop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("autologhop")
public interface AutoLogHopConfig extends Config
{


    @ConfigItem(
            keyName = "method",
            name = "Method",
            description = "Logout = log out (0 tick), hop = hop worlds (1 tick), log then hop = logout and hop worlds from client screen (0 tick)",
            position = 10
    )
    default Method method()
    {
        return Method.LOGOUT_HOP;
    }


    @ConfigItem(
            keyName = "username",
            name = "username",
            description = "Username for login",
            position = 12
    )
    default String user() {
        return "";
    }

    @ConfigItem(
            keyName = "password",
            name = "password",
            description = "Password for login",
            position = 13
    )
    default String password() {
        return "";
    }

    @ConfigItem(
            keyName = "disableWildyChecks",
            name = "Disable Wilderness Checks",
            description = "Disable wilderness checks. Makes plugin work everywhere.",
            position = 14
    )
    default boolean disableWildyChecks()
    {
        return false;
    }

    @ConfigItem(
            keyName = "whitelist",
            name = "Whitelist",
            description = "Players to ignore - separate with , and don't leave leading/trailing spaces",
            position = 15
    )
    default String whitelist()
    {
        return "";
    }

    @ConfigItem(
            keyName = "membersWorlds",
            name = "Members Worlds",
            description = "Hop to members worlds.",
            position = 16
    )
    default boolean membersWorlds()
    {
        return true;
    }

    @ConfigItem(
            keyName = "combatRange",
            name = "Within combat range",
            description = "Will only consider players within combat level bracket of wilderness level",
            position = 18
    )
    default boolean combatRange()
    {
        return true;
    }

    @ConfigItem(
            keyName = "skulledOnly",
            name = "Skulled Players Only",
            description = "Only triggers on skulled players.",
            position = 20
    )
    default boolean skulledOnly()
    {
        return false;
    }
/*
    @ConfigItem(
            keyName = "deadmanSkulls",
            name = "Include Deadman Skulls",
            description = "Include deadman skulls.",
            position = 22,
            section = title,
            hidden = true,
            unhide = "skulledOnly"
    )
    default boolean deadmanSkulls()
    {
        return false;
    }
*/
}
