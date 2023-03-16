package net.runelite.client.plugins.constructionhelper;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;

public enum CHMode {
   // CRUDE_WOODEN_CHAIR("Crude Wooden Chair", ItemID.PLANK, ObjectID.CHAIR_6752, ObjectID.CHAIR_SPACE_4516, 2, CHWidget.CRUDE_WOODEN_CHAIR, ItemID.STEEL_NAILS),
   // WOODEN_CHAIR("Wooden Chair", ItemID.PLANK, ObjectID.CHAIR_6753, ObjectID.CHAIR_SPACE, 3, CHWidget.WOODEN_CHAIR, ItemID.STEEL_NAILS),
    OAK_TABLE("Oak Tables", ItemID.OAK_PLANK, ObjectID.OAK_TABLE, ObjectID.TABLE_SPACE, 4, CHWidget.OAK_TABLE),
    //CARVED_OAK_TABLE("Carved Oak Tables", ItemID.OAK_PLANK, ObjectID.OAK_TABLE_13295, ObjectID.TABLE_SPACE, 6, CHWidget.CARVED_OAK_TABLE),
    MAHOGANY_TABLE("Mahogany Tables", ItemID.MAHOGANY_PLANK, ObjectID.MAHOGANY_TABLE, ObjectID.TABLE_SPACE, 6, CHWidget.MAHOGANY_TABLE),
    OAK_LARDERS("Oak Larders", ItemID.OAK_PLANK, ObjectID.LARDER_13566, ObjectID.LARDER_SPACE, 8, CHWidget.OAK_LARDER),
    MYTHICAL_CAPE("Mythical Capes", ItemID.TEAK_PLANK, ObjectID.MYTHICAL_CAPE, ObjectID.GUILD_TROPHY_SPACE, 3, CHWidget.MYTHICAL_CAPE, ItemID.MYTHICAL_CAPE_22114),
    CRAFT_VARROCK_TABS("Craft Varrock Tablets", ItemID.SOFT_CLAY, 37349, 0, 1, CHWidget.CRAFT_VARROCK_TABS);
    private final String name;
    private final int plankId;
    private final int objectId;
    private final int objectSpaceId;
    private final int plankCost;
    private final CHWidget widget;
    private final int[] otherReqs;

    CHMode(String name, int plankId, int objectId, int objectSpaceId, int plankCost, CHWidget widget, int... otherReqs) {
        this.name = name;
        this.plankId = plankId;
        this.objectId = objectId;
        this.objectSpaceId = objectSpaceId;
        this.plankCost = plankCost;
        this.widget = widget;
        this.otherReqs = otherReqs;
    }

    public String getName() {
        return name;
    }

    public int getPlankId() {
        return plankId;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getObjectSpaceId() {
        return objectSpaceId;
    }

    public int getPlankCost() {
        return plankCost;
    }

    public CHWidget getWidget() {
        return widget;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int[] getOtherReqs() {
        return otherReqs;
    }
}
