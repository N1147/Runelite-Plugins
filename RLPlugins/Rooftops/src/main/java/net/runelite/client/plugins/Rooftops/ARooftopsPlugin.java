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

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.Utils.NewMenuEntry;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.api.ObjectID.*;

@PluginDescriptor(
        name = "ARooftops (Auto)",
        enabledByDefault = false,
        description = "Automated rooftop Agility. Supports Gnome course.",
        tags = {"agility","anarchise","rooftops"}
)
@Slf4j
public class ARooftopsPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private Core core;

    @Inject
    private ARooftopsConfig config;

    @Inject
    PluginManager pluginManager;

    @Inject
    ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    ItemManager itemManager;

    ARooftopsState state;

    Player player;

    Instant botTimer;
    TileItem markOfGrace;
    Tile markOfGraceTile;
    MenuEntry targetMenu;
    LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
    WidgetItem alchItem;

    Set<Integer> inventoryItems = new HashSet<>();
    GameObject spawnedPortal;

    private final Set<Integer> REGION_IDS = Set.of(9781, 12853, 12597, 12084, 12339, 12338, 10806, 10297, 10553, 13358, 13878, 10547, 13105, 9012, 9013, 12895, 13151, 13152, 11050, 10794);
    WorldPoint CAMELOT_TELE_LOC = new WorldPoint(2705, 3463, 0);
    Set<Integer> AIR_STAFFS = Set.of(ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.DUST_BATTLESTAFF, ItemID.MIST_BATTLESTAFF,
            ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF, ItemID.MYSTIC_DUST_STAFF, ItemID.MYSTIC_SMOKE_STAFF, ItemID.MYSTIC_MIST_STAFF);

    //private List<Integer> SUMMER_PIE_IDS = new ArrayList<>();//Set.of(

    int timeout;
    int alchTimeout;
    int mogSpawnCount;
    int mogCollectCount;
    int mogInventoryCount = -1;
    int marksPerHour;
    long sleepLength;
    boolean startAgility;
    boolean restockBank;
    boolean setHighAlch;
    boolean alchClick;

    @Override
    protected void startUp() {
        if (!startAgility) {
            startAgility = true;
            state = null;
            targetMenu = null;
            botTimer = Instant.now();
            inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE));
        } else {
            resetVals();
        }
    }

    @Override
    protected void shutDown() {
        resetVals();
    }

    @Provides
    ARooftopsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ARooftopsConfig.class);
    }

    private void resetVals() {
        markOfGraceTile = null;
        markOfGrace = null;
        startAgility = false;
        botTimer = null;
        mogSpawnCount = 0;
        mogCollectCount = 0;
        mogInventoryCount = -1;
        marksPerHour = 0;
        alchTimeout = 0;
        inventoryItems.clear();
    }

    private long sleepDelay() {
        sleepLength = core.randomDelay(false, 150, 700, 300, 250);
        return sleepLength;
    }

    private int tickDelay() {
        int tickLength = (int) core.randomDelay(false, 1, 3, 1, 1);
        return tickLength;
    }

    public long getMarksPH() {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) mogCollectCount * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

    private ARooftopsObstacles getCurrentObstacle() {
        return ARooftopsObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
    }

    private void findObstacle() {
        ARooftopsObstacles obstacle = getCurrentObstacle();
        if (obstacle != null) {

            if (obstacle.getObstacleType() == net.runelite.client.plugins.Rooftops.ARooftopsObstacleType.DECORATION) {
                DecorativeObject decObstacle = core.findNearestDecorObject(obstacle.getObstacleId());
                if (decObstacle != null) {
                    core.targetMenu = new NewMenuEntry("", "",decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
                    core.doInvoke(core.targetMenu, decObstacle.getConvexHull().getBounds());
                    //clientThread.invoke(() -> client.invokeMenuAction("", "",decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY()));

                    //targetMenu = new MenuEntry("", "", decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
                    //core.setMenuEntry(targetMenu);
                    //Rectangle clickPoint = (decObstacle.getConvexHull() != null) ? decObstacle.getConvexHull().getBounds() :
                          //  new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                   // core.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            if (obstacle.getObstacleType() == net.runelite.client.plugins.Rooftops.ARooftopsObstacleType.GROUND_OBJECT) {
                GroundObject groundObstacle = core.findNearestGroundObject(obstacle.getObstacleId());
                if (groundObstacle != null) {
                    core.targetMenu = new NewMenuEntry("", "",groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
                    core.doInvoke(core.targetMenu, groundObstacle.getConvexHull().getBounds());

                    //clientThread.invoke(() -> client.invokeMenuAction("", "",groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY()));

                    // targetMenu = new MenuEntry("", "", groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
                    //core.setMenuEntry(targetMenu);
                   // Rectangle clickPoint = (groundObstacle.getConvexHull() != null) ? groundObstacle.getConvexHull().getBounds() :
                    //        new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                   // core.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            GameObject objObstacle = core.findNearestGameObject(obstacle.getObstacleId());
            if (objObstacle != null) {
                core.targetMenu = new NewMenuEntry("", "",objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
                core.doInvoke(core.targetMenu, objObstacle.getConvexHull().getBounds());

                //clientThread.invoke(() -> client.invokeMenuAction("", "",objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY()));

                // targetMenu = new MenuEntry("", "", objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
               // core.setMenuEntry(targetMenu);
               // Rectangle clickPoint = (objObstacle.getConvexHull() != null) ? objObstacle.getConvexHull().getBounds() :
               //         new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
               // core.delayMouseClick(clickPoint, sleepDelay());
                return;
            }
        } else {

        }
    }

    public ARooftopsState getState() {
        if (timeout > 0) {
            if (alchClick) {
                ARooftopsObstacles currentObstacle = getCurrentObstacle();
                if (currentObstacle != null) {
                    if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!core.inventoryFull() || core.inventoryContains(ItemID.MARK_OF_GRACE))) {
                        if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                            if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                            {
                                if (config.alchMogStack() > 1) {
                                    if (markOfGrace.getQuantity() >= config.alchMogStack()) {
                                        return ARooftopsState.MARK_OF_GRACE;
                                    }
                                } else {
                                    return ARooftopsState.MARK_OF_GRACE;
                                }
                            } else {
                                markOfGrace = null;
                            }
                        }
                    }

                }
            }
            return ARooftopsState.TIMEOUT;
        }
        if (client.getLocalPlayer().getAnimation() != -1) {
            timeout = tickDelay();
            return ARooftopsState.MOVING;
        }
        if (core.isMoving(beforeLoc)) {
            timeout = tickDelay();
            return ARooftopsState.MOVING;
        }
        ARooftopsObstacles currentObstacle = ARooftopsObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
        if (currentObstacle == null) {
            timeout = tickDelay();
            return ARooftopsState.MOVING;
        }

        if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!core.inventoryFull() || core.inventoryContains(ItemID.MARK_OF_GRACE))) {
            if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                {
                    if (config.mogStack() > 1) {
                        if (markOfGrace.getQuantity() >= config.mogStack()) {
                            return ARooftopsState.MARK_OF_GRACE;
                        }
                    } else {
                        return ARooftopsState.MARK_OF_GRACE;
                    }
                } else {
                    markOfGrace = null;
                }
            }
        }
        if (!core.isMoving(beforeLoc)) {
            return ARooftopsState.FIND_OBSTACLE;
        }
        return ARooftopsState.ANIMATING;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!startAgility){// || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        player = client.getLocalPlayer();
        if (alchTimeout > 0) {
            alchTimeout--;
        }
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN && client.getBoostedSkillLevel(Skill.HITPOINTS) > config.lowHP()) {
            if (!REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
                return;
            }
            marksPerHour = (int) getMarksPH();
           // core.handleRun(40, 20);
            state = getState();
            beforeLoc = client.getLocalPlayer().getLocalLocation();
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case MARK_OF_GRACE:
                    core.targetMenu = new NewMenuEntry("", "",ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
                    core.doInvoke(core.targetMenu, markOfGraceTile.getSceneLocation());
                    //clientThread.invoke(() -> client.invokeMenuAction("", "",ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY()));
                    //targetMenu = new MenuEntry("", "", ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
                    //core.setMenuEntry(targetMenu);
                    //core.delayClickRandomPointCenter(-200, 200, sleepDelay());
                    break;
                case FIND_OBSTACLE:
                    findObstacle();
                    break;
                case MOVING:
                    break;
                case PRIFF_PORTAL:
                    core.targetMenu = new NewMenuEntry("", "",spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY(), false);
                    core.doInvoke(core.targetMenu, spawnedPortal.getConvexHull().getBounds());
                    // clientThread.invoke(() -> client.invokeMenuAction("", "",spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY()));
                    //targetMenu = new MenuEntry("", "", spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY(), false);
                    //core.setMenuEntry(targetMenu);
                   //core.delayMouseClick(spawnedPortal.getConvexHull().getBounds(), sleepDelay());
                    break;
            }
        } else {
            return;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && startAgility) {
            markOfGraceTile = null;
            markOfGrace = null;
            state = ARooftopsState.TIMEOUT;
            timeout = 2;
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!startAgility) {
            return;
        }
        if (targetMenu != null) {
            //alchClick = (targetMenu.getOption().equals("Cast"));
            timeout = tickDelay();
        }
    }

    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) ||
                !config.mogPickup()) {
            return;
        }

        TileItem item = event.getItem();
        Tile tile = event.getTile();

        if (item.getId() == ItemID.MARK_OF_GRACE) {
            markOfGrace = item;
            markOfGraceTile = tile;
            WidgetItem mogInventory = core.getInventoryWidgetItem(Collections.singletonList(ItemID.MARK_OF_GRACE));
            mogInventoryCount = (mogInventory != null) ? mogInventory.getQuantity() : 0;
            mogSpawnCount++;
        }
    }

    @Subscribe
    private void onItemDespawned(ItemDespawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) || !config.mogPickup()) {
            return;
        }

        TileItem item = event.getItem();

        if (item.getId() == ItemID.MARK_OF_GRACE) {
            markOfGrace = null;
            markOfGraceTile = null;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != 93 || mogInventoryCount == -1) {
            return;
        }
        if (event.getItemContainer().count(ItemID.MARK_OF_GRACE) > mogInventoryCount) {
            mogCollectCount++;
            mogInventoryCount = -1;
        }
    }
}

enum ARooftopsObstacleType {
    NORMAL,
    DECORATION,
    GROUND_OBJECT
}
enum ARooftopsObstacles {
    //TREE GNOME
    GNOME_LOG(new WorldPoint(2470, 3435, 0), new WorldPoint(2489, 3447, 0), LOG_BALANCE_23145, ARooftopsObstacleType.GROUND_OBJECT),
    GNOME_NET(new WorldPoint(2470, 3423, 0), new WorldPoint(2477, 3430, 0), OBSTACLE_NET_23134),
    GNOME_TREE(new WorldPoint(2470, 3421, 1), new WorldPoint(2476, 3425, 1), TREE_BRANCH_23559),
    GNOME_ROPE(new WorldPoint(2469, 3416, 2), new WorldPoint(2479, 3423, 2), BALANCING_ROPE_23557, ARooftopsObstacleType.GROUND_OBJECT),
    GNOME_TREE_TWO(new WorldPoint(2482, 3416, 2), new WorldPoint(2489, 3423, 2), TREE_BRANCH_23560),
    GNOME_NET_TWO(new WorldPoint(2482, 3418, 0), new WorldPoint(2489, 3427, 0), OBSTACLE_NET_23135),
    GNOME_PIPE(new WorldPoint(2482, 3427, 0), new WorldPoint(2489, 3433, 0), OBSTACLE_PIPE_23139),
    //DRAYNOR
    DRAY_WALL(new WorldPoint(3082, 3238, 0), new WorldPoint(3105, 3293, 0), ROUGH_WALL, ARooftopsObstacleType.DECORATION, BANK_BOOTH_10355),
    DRAY_TIGHTROPE(new WorldPoint(3096, 3275, 3), new WorldPoint(3103, 3282, 3), TIGHTROPE, ARooftopsObstacleType.GROUND_OBJECT),
    DRAY_TIGHTROPE_TWO(new WorldPoint(3086, 3271, 3), new WorldPoint(3093, 3279, 3), TIGHTROPE_11406, ARooftopsObstacleType.GROUND_OBJECT),
    DRAY_NARROW_WALL(new WorldPoint(3087, 3263, 3), new WorldPoint(3095, 3269, 3), NARROW_WALL),
    DRAY_WALL_TWO(new WorldPoint(3082, 3256, 3), new WorldPoint(3089, 3262, 3), WALL_11630), //COULD CONFLICT WITH NEXT LINE
    DRAY_GAP(new WorldPoint(3087, 3254, 3), new WorldPoint(3095, 3256, 3), GAP_11631),
    DRAY_CRATE(new WorldPoint(3095, 3255, 3), new WorldPoint(3102, 3262, 3), CRATE_11632),
    //ALKHARID
    ALK_ROUGHWALL(new WorldPoint(3268, 3159, 0), new WorldPoint(3322, 3200, 0), ROUGH_WALL_11633, ARooftopsObstacleType.DECORATION),
    ALK_TIGHTROPE(new WorldPoint(3270, 3179, 3), new WorldPoint(3278, 3193, 3), TIGHTROPE_14398, ARooftopsObstacleType.GROUND_OBJECT),
    ALK_CABLE(new WorldPoint(3263, 3160, 3), new WorldPoint(3274, 3174, 3), CABLE),
    ALK_ZIPLINE(new WorldPoint(3282, 3159, 3), new WorldPoint(3303, 3176, 3), ZIP_LINE_14403),
    ALK_TROPICAL_TREE(new WorldPoint(3312, 3159, 1), new WorldPoint(3319, 3166, 1), TROPICAL_TREE_14404),
    ALK_ROOF_TOP_BEAMS(new WorldPoint(3311, 3172, 2), new WorldPoint(3319, 3180, 2), ROOF_TOP_BEAMS, ARooftopsObstacleType.DECORATION),
    ALK_TIGHTROPE_TWO(new WorldPoint(3311, 3180, 3), new WorldPoint(3319, 3187, 3), TIGHTROPE_14409, ARooftopsObstacleType.GROUND_OBJECT),
    ALK_GAP(new WorldPoint(3296, 3184, 3), new WorldPoint(3306, 3194, 3), GAP_14399),
    //VARROCK
    COURSE_GROUND(new WorldPoint(3184, 3386, 0), new WorldPoint(3258, 3428, 0), ROUGH_WALL_14412, ARooftopsObstacleType.DECORATION, ObjectID.BANK_BOOTH_10583),
    ROOFTOP_ONE(new WorldPoint(3213, 3409, 3), new WorldPoint(3220, 3420, 3), CLOTHES_LINE),
    ROOFTOP_TWO(new WorldPoint(3200, 3412, 3), new WorldPoint(3209, 3420, 3), GAP_14414),
    CROSSWALK(new WorldPoint(3192, 3415, 1), new WorldPoint(3198, 3417, 1), WALL_14832),
    ROOFTOP_THREE(new WorldPoint(3191, 3401, 3), new WorldPoint(3198, 3407, 3), GAP_14833),
    ROOFTOP_FOUR(new WorldPoint(3181, 3393, 3), new WorldPoint(3209, 3401, 3), GAP_14834),
    ROOFTOP_FIVE(new WorldPoint(3217, 3392, 3), new WorldPoint(3233, 3404, 3), GAP_14835),
    ROOFTOP_SIX(new WorldPoint(3235, 3402, 3), new WorldPoint(3240, 3409, 3), LEDGE_14836),
    ROOFTOP_SEVEN(new WorldPoint(3235, 3410, 3), new WorldPoint(3240, 3416, 3), EDGE),
    //Canifis
    CAN_GROUND(new WorldPoint(3459, 3464, 0), new WorldPoint(3519, 3514, 0), TALL_TREE_14843, BANK_BOOTH_24347),
    CAN_ROOFTOP_ONE(new WorldPoint(3504, 3491, 2), new WorldPoint(3512, 3499, 2), GAP_14844),
    CAN_ROOFTOP_TWO(new WorldPoint(3495, 3503, 2), new WorldPoint(3505, 3508, 2), GAP_14845),
    CAN_ROOFTOP_THREE(new WorldPoint(3484, 3498, 2), new WorldPoint(3494, 3506, 2), GAP_14848),
    CAN_ROOFTOP_FOUR(new WorldPoint(3474, 3491, 3), new WorldPoint(3481, 3501, 3), GAP_14846),
    CAN_ROOFTOP_FIVE(new WorldPoint(3477, 3481, 2), new WorldPoint(3485, 3488, 2), POLEVAULT),
    CAN_ROOFTOP_SIX(new WorldPoint(3488, 3468, 3), new WorldPoint(3505, 3480, 3), GAP_14847),
    CAN_ROOFTOP_SEVEN(new WorldPoint(3508, 3474, 2), new WorldPoint(3517, 3484, 2), GAP_14897),
    //APE ATOLL
    /*APE_STEPSTONE(new WorldPoint(2754, 2741, 0), new WorldPoint(2784, 2751, 0), STEPPING_STONE_15412, ARooftopsObstacleType.GROUND_OBJECT),
    APE_TROPTREE1(new WorldPoint(2753, 2742, 0), new WorldPoint(2751, 2739, 0), TROPICAL_TREE_15414),
    APE_MONKEYBARS(new WorldPoint(2753, 2742, 2), new WorldPoint(2752, 2741, 2), MONKEYBARS_15417),
    APE_SKULLSLOPE(new WorldPoint(2747, 2741, 0), new WorldPoint(2746, 2741, 0), SKULL_SLOPE_15483, ARooftopsObstacleType.GROUND_OBJECT),
    APE_ROPE(new WorldPoint(2735, 2726, 0), new WorldPoint(2754, 2742, 0), ROPE_15487),
    APE_TROPTREE2(new WorldPoint(2755, 2726, 0), new WorldPoint(2760, 2737, 0), TROPICAL_TREE_16062),*/
    //FALADOR
    FAL_GROUND(new WorldPoint(3008, 3328, 0), new WorldPoint(3071, 3391, 0), ROUGH_WALL_14898, ARooftopsObstacleType.DECORATION, ObjectID.BANK_BOOTH_24101),
    FAL_ROOFTOP_ONE(new WorldPoint(3034, 3342, 3), new WorldPoint(3040, 3347, 3), TIGHTROPE_14899, ARooftopsObstacleType.GROUND_OBJECT),
    FAL_ROOFTOP_TWO(new WorldPoint(3043, 3341, 3), new WorldPoint(3051, 3350, 3), HAND_HOLDS_14901),
    FAL_ROOFTOP_THREE(new WorldPoint(3047, 3356, 3), new WorldPoint(3051, 3359, 3), GAP_14903),
    FAL_ROOFTOP_FOUR(new WorldPoint(3044, 3360, 3), new WorldPoint(3049, 3367, 3), GAP_14904),
    FAL_ROOFTOP_FIVE(new WorldPoint(3033, 3360, 3), new WorldPoint(3042, 3364, 3), TIGHTROPE_14905),
    FAL_ROOFTOP_SIX(new WorldPoint(3025, 3352, 3), new WorldPoint(3029, 3355, 3), TIGHTROPE_14911, ARooftopsObstacleType.GROUND_OBJECT),
    FAL_ROOFTOP_SEVEN(new WorldPoint(3008, 3352, 3), new WorldPoint(3021, 3358, 3), GAP_14919),
    FAL_ROOFTOP_EIGHT(new WorldPoint(3015, 3343, 3), new WorldPoint(3022, 3350, 3), LEDGE_14920),
    FAL_ROOFTOP_NINE(new WorldPoint(3010, 3343, 3), new WorldPoint(3015, 3347, 3), LEDGE_14921),
    FAL_ROOFTOP_TEN(new WorldPoint(3008, 3335, 3), new WorldPoint(3014, 3343, 3), LEDGE_14922),
    FAL_ROOFTOP_ELEVEN(new WorldPoint(3013, 3331, 3), new WorldPoint(3018, 3335, 3), LEDGE_14924),
    FAL_ROOFTOP_TWELVE(new WorldPoint(3019, 3331, 3), new WorldPoint(3027, 3335, 3), EDGE_14925),
    //SEERS
    SEERS_GROUND(new WorldPoint(2689, 3457, 0), new WorldPoint(2750, 3517, 0), WALL_14927, ARooftopsObstacleType.DECORATION, BANK_BOOTH_25808),
    SEERS_ROOF_ONE(new WorldPoint(2720, 3489, 3), new WorldPoint(2731, 3498, 3), GAP_14928),
    SEERS_ROOF_TWO(new WorldPoint(2702, 3486, 2), new WorldPoint(2714, 3499, 2), TIGHTROPE_14932, ARooftopsObstacleType.GROUND_OBJECT),
    SEERS_ROOF_THREE(new WorldPoint(2707, 3475, 2), new WorldPoint(2717, 3483, 2), GAP_14929),
    SEERS_ROOF_FOUR(new WorldPoint(2697, 3468, 3), new WorldPoint(2718, 3478, 3), GAP_14930),
    SEERS_ROOF_FIVE(new WorldPoint(2689, 3458, 2), new WorldPoint(2704, 3467, 2), EDGE_14931),
    //Pollniveach
    POLL_GROUND(new WorldPoint(3328, 2944, 0), new WorldPoint(3392, 3008, 0), BASKET_14935),
    POLL_ROOF_ONE(new WorldPoint(3346, 2963, 1), new WorldPoint(3352, 2969, 1), MARKET_STALL_14936),
    POLL_ROOF_TWO(new WorldPoint(3352, 2973, 1), new WorldPoint(3356, 2977, 1), BANNER_14937),
    POLL_ROOF_THREE(new WorldPoint(3360, 2977, 1), new WorldPoint(3363, 2980, 1), GAP_14938),
    POLL_ROOF_FOUR(new WorldPoint(3366, 2976, 1), new WorldPoint(3372, 2975, 1), TREE_14939),
    POLL_ROOF_FIVE(new WorldPoint(3365, 2982, 1), new WorldPoint(3370, 2987, 1), ROUGH_WALL_14940, ARooftopsObstacleType.DECORATION),
    POLL_ROOF_SIX(new WorldPoint(3355, 2980, 2), new WorldPoint(3366, 2986, 2), MONKEYBARS),
    POLL_ROOF_SEVEN(new WorldPoint(3357, 2991, 2), new WorldPoint(3367, 2996, 2), TREE_14944),
    POLL_ROOF_EIGHT(new WorldPoint(3356, 3000, 2), new WorldPoint(3363, 3005, 2), DRYING_LINE),
    //Prifddinas
    PRIF_LADDER(new WorldPoint(3237, 6099, 0), new WorldPoint(3275, 6114, 0), LADDER_36221, ObjectID.BANK_BOOTH_10355),
    PRIF_TIGHTROPE(new WorldPoint(3254, 6102, 2), new WorldPoint(3259, 6112, 2), TIGHTROPE_36225),//TIGHTROPE_36255
    PRIF_CHIMNEY(new WorldPoint(3271, 6104, 2), new WorldPoint(3276, 6107, 2), CHIMNEY_36227),
    PRIF_ROOFEDGE(new WorldPoint(3268, 6111, 2), new WorldPoint(3270, 6116, 2), ROOF_EDGE),
    PRIF_DARK_HOLE(new WorldPoint(3267, 6115, 0), new WorldPoint(3271, 6119, 0), DARK_HOLE_36229),
    PRIF_LADDER_TWO(new WorldPoint(2239, 3386, 0), new WorldPoint(2272, 3410, 0), LADDER_36231),
    PRIF_LADDER_FAIL(new WorldPoint(3265, 6138, 0), new WorldPoint(3276, 6150, 0), LADDER_36232),
    PRIF_ROPE_BRIDGE(new WorldPoint(2264, 3388, 2), new WorldPoint(2270, 3394, 2), ROPE_BRIDGE_36233),
    PRIF_TIGHTROPE_TWO(new WorldPoint(2252, 3386, 2), new WorldPoint(2259, 3391, 2), TIGHTROPE_36234),
    PRIF_ROPE_BRIDGE_TWO(new WorldPoint(2242, 3393, 2), new WorldPoint(2248, 3399, 2), ROPE_BRIDGE_36235),
    PRIF_TIGHTROPE_THREE(new WorldPoint(2243, 3404, 2), new WorldPoint(2249, 3411, 2), TIGHTROPE_36236),
    PRIF_TIGHTROPE_FOUR(new WorldPoint(2248, 3414, 2), new WorldPoint(2254, 3420, 2), TIGHTROPE_36237),
    PRIF_DARKHOLE_TWO(new WorldPoint(2255, 3424, 0), new WorldPoint(2263, 3436, 0), DARK_HOLE_36238),
    //Rellekka
    RELL_GROUND(new WorldPoint(2612, 3654, 0), new WorldPoint(2672, 3687, 0), ROUGH_WALL_14946, ARooftopsObstacleType.DECORATION),
    RELL_ROOF_ONE(new WorldPoint(2621, 3671, 3), new WorldPoint(2627, 3677, 3), GAP_14947),
    RELL_ROOF_TWO(new WorldPoint(2614, 3657, 3), new WorldPoint(2623, 3669, 3), TIGHTROPE_14987),
    RELL_ROOF_THREE(new WorldPoint(2625, 3649, 3), new WorldPoint(2631, 3656, 3), GAP_14990),
    RELL_ROOF_FOUR(new WorldPoint(2638, 3648, 3), new WorldPoint(2645, 3654, 3), GAP_14991),
    RELL_ROOF_FIVE(new WorldPoint(2642, 3656, 3), new WorldPoint(2651, 3663, 3), TIGHTROPE_14992),
    RELL_ROOF_SIX(new WorldPoint(2654, 3663, 3), new WorldPoint(2667, 3686, 3), PILE_OF_FISH),
    //Ardougne
    ARDY_GROUND(new WorldPoint(2640, 3274, 0), new WorldPoint(2678, 3321, 0), WOODEN_BEAMS, ARooftopsObstacleType.DECORATION, ObjectID.BANK_BOOTH_10355),
    ARDY_GAP(new WorldPoint(2670, 3298, 3), new WorldPoint(2675, 3312, 3), GAP_15609),
    ARDY_BEAM(new WorldPoint(2660, 3317, 3), new WorldPoint(2666, 3323, 3), PLANK_26635, ARooftopsObstacleType.GROUND_OBJECT),
    ARDY_GAP_TWO(new WorldPoint(2652, 3317, 3), new WorldPoint(2658, 3322, 3), GAP_15610),
    ARDY_GAP_THREE(new WorldPoint(2647, 3310, 3), new WorldPoint(2654, 3315, 3), GAP_15611),
    ARDY_STEEP_ROOF(new WorldPoint(2650, 3299, 3), new WorldPoint(2656, 3310, 3), STEEP_ROOF),
    ARDY_GAP_FOUR(new WorldPoint(2653, 3290, 3), new WorldPoint(2658, 3298, 3), GAP_15612);

    @Getter(AccessLevel.PACKAGE)
    private final WorldArea location;

    @Getter(AccessLevel.PACKAGE)
    private final int obstacleId;

    @Getter(AccessLevel.PACKAGE)
    private ARooftopsObstacleType type = ARooftopsObstacleType.NORMAL;

    @Getter(AccessLevel.PACKAGE)
    private int bankID = 0;

    ARooftopsObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId) {
        this.location = new WorldArea(min, max);
        this.obstacleId = obstacleId;
    }

    ARooftopsObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final int bankID) {
        this.location = new WorldArea(min, max);
        this.obstacleId = obstacleId;
        this.bankID = bankID;
    }

    ARooftopsObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final ARooftopsObstacleType type) {
        this.location = new WorldArea(min, max);
        this.obstacleId = obstacleId;
        this.type = type;
    }

    ARooftopsObstacles(final WorldPoint min, final WorldPoint max, final int obstacleId, final ARooftopsObstacleType type, final int bankID) {
        this.location = new WorldArea(min, max);
        this.obstacleId = obstacleId;
        this.type = type;
        this.bankID = bankID;
    }

    public ARooftopsObstacleType getObstacleType() {
        return type;
    }

    public static ARooftopsObstacles getObstacle(WorldPoint worldPoint) {
        for (ARooftopsObstacles obstacle : values()) {
            if (obstacle.getLocation().distanceTo(worldPoint) == 0) {
                return obstacle;
            }
        }
        return null;
    }

}
enum ARooftopsState {
    ANIMATING,
    FIND_OBSTACLE,
    MARK_OF_GRACE,
    MOVING,
    PRIFF_PORTAL,
    TIMEOUT;
}