package net.runelite.client.plugins.constructionhelper;

import net.runelite.api.Client;

import java.util.Arrays;
import java.util.List;

public class MiscUtils {

    private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);

    public static boolean isInPOH(Client client) {
        //if inside house
        return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);
    }
}
