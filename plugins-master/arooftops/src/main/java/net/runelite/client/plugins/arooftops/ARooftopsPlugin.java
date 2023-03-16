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

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static net.runelite.client.plugins.arooftops.ARooftopsState.*;


@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
        name = "ARooftops",
        enabledByDefault = false,
        description = "Does rooftop Agility",
        tags = {"agility"}
)
@Slf4j
public class ARooftopsPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private PUtils utils;

    @Inject
    private ARooftopsConfig config;

    @Inject
    PluginManager pluginManager;

    @Inject
    ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;
    @Inject
    public ReflectBreakHandler chinBreakHandler;
    @Inject
    ARooftopsOverlay overlay;

    @Inject
    ItemManager itemManager;



    Player player;
    ARooftopsState state;
    Instant botTimer;
    TileItem markOfGrace;
    Tile markOfGraceTile;
    MenuEntry targetMenu;
    LocalPoint beforeLoc = new LocalPoint(0, 0); //initiate to mitigate npe
    WidgetItem alchItem;
    Portals priffPortal;
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
        chinBreakHandler.registerPlugin(this);
        if (!startAgility) {
            startAgility = true;
            chinBreakHandler.startPlugin(this);
            state = null;
            targetMenu = null;
            botTimer = Instant.now();
            restockBank = config.bankRestock();
            inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE));
            //SUMMER_PIE_IDS.add(ItemID.SUMMER_PIE, ItemID.HALF_A_SUMMER_PIE);
            if (config.alchItemID() != 0) {
                inventoryItems.addAll(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
            }
            overlayManager.add(overlay);
        } else {
            resetVals();
            chinBreakHandler.stopPlugin(this);
        }
    }

    @Override
    protected void shutDown() {
        resetVals();
        chinBreakHandler.unregisterPlugin(this);
    }

    @Provides
    ARooftopsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ARooftopsConfig.class);
    }

    private void resetVals() {
        overlayManager.remove(overlay);
        //chinBreakHandler.stopPlugin(this);
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

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("ARooftops")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        switch (configButtonClicked.getKey()) {
            case "startButton":
                if (!startAgility) {
                    startAgility = true;
                    chinBreakHandler.startPlugin(this);
                    state = null;
                    targetMenu = null;
                    botTimer = Instant.now();
                    restockBank = config.bankRestock();
                    inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE));
                    if (config.alchItemID() != 0) {
                        inventoryItems.addAll(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
                    }
                    overlayManager.add(overlay);
                } else {
                    resetVals();
                    chinBreakHandler.stopPlugin(this);
                }
                break;
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("ARooftops")) {
            switch (event.getKey()) {
                case "bankRestock":
                    restockBank = config.bankRestock();
                    break;
                case "alchItemID":
                    inventoryItems.clear();
                    inventoryItems.addAll(Set.of(ItemID.NATURE_RUNE, ItemID.MARK_OF_GRACE, config.alchItemID(), (config.alchItemID() + 1)));
                    break;
            }
        }
    }

    private long sleepDelay() {
        sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    public long getMarksPH() {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) mogCollectCount * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

 /*   private boolean shouldEatSummerPie() {
        return config.boostWithPie() && 
                (client.getBoostedSkillLevel(Skill.AGILITY) < config.pieLevel()) &&
                utils.inventoryContains(SUMMER_PIE_IDS);
    }*/
    
    private boolean shouldCastTeleport() {
        return config.camelotTeleport() && client.getBoostedSkillLevel(Skill.MAGIC) >= 45 &&
                CAMELOT_TELE_LOC.distanceTo(client.getLocalPlayer().getWorldLocation()) <= 3 &&
                (utils.inventoryContains(ItemID.LAW_RUNE) && utils.inventoryContainsStack(ItemID.AIR_RUNE, 5) ||
                        utils.inventoryContains(ItemID.LAW_RUNE) && utils.isItemEquipped(AIR_STAFFS));
    }

    private boolean shouldAlch() {
        return config.highAlch() &&
                config.alchItemID() != 0 &&
                client.getBoostedSkillLevel(Skill.MAGIC) >= 55;
    }
/*
    private void eatSummerPie() {
        WidgetItem summerPieItem = utils.getInventoryWidgetItem(SUMMER_PIE_IDS);
        utils.useItem(summerPieItem.getId(),"eat");
        //clientThread.invoke(() -> client.invokeMenuAction("", "",summerPieItem.getId(), MenuAction.CC_OP.getId(), summerPieItem.getIndex(), WidgetInfo.INVENTORY.getId()));

        //targetMenu = new MenuEntry("", "", summerPieItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), summerPieItem.getIndex(), WidgetInfo.INVENTORY.getId(), false);
        //utils.setMenuEntry(targetMenu);
       // utils.delayMouseClick(summerPieItem.getCanvasBounds(), sleepDelay());
    }
*/
    private boolean shouldRestock() {
        if (!config.highAlch() ||
                config.alchItemID() == 0 ||
                !restockBank ||
                client.getBoostedSkillLevel(Skill.MAGIC) < 55) {
            return false;
        }
        return !utils.inventoryContains(ItemID.NATURE_RUNE) || !utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1)));
    }

    private void restockItems() {
        if (utils.isBankOpen()) {
            //if (client.getVarbitValue(Varbits.BANK_NOTE_FLAG.getId()) != 1)
            if (client.getVarbitValue(3958) != 1) {
                clientThread.invoke(() -> client.invokeMenuAction("", "",1, MenuAction.CC_OP.getId(), -1, 786455));

                //targetMenu = new MenuEntry("Note", "", 1, MenuAction.CC_OP.getId(), -1, 786455, false);
                //utils.setMenuEntry(targetMenu);
                //utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
                return;
            }
            if ((!utils.bankContains(ItemID.NATURE_RUNE, 1) && !utils.inventoryContains(ItemID.NATURE_RUNE)) ||
                    (!utils.bankContains(config.alchItemID(), 1) && !utils.inventoryContains(Set.of(config.alchItemID(), config.alchItemID() + 1)))) {
                log.debug("out of alching items");
                restockBank = false;
                return;
            } else {
                WidgetItem food = utils.getInventoryWidgetItemMenu(itemManager, "Eat", 33);
                if (food != null) {
                    inventoryItems.add(food.getId());
                }
                if (utils.inventoryContainsExcept(inventoryItems)) {
                    log.debug("depositing items");
                    utils.depositAllExcept(inventoryItems);
                    timeout = tickDelay();
                    return;
                }
                if (!utils.inventoryFull()) {
                    if (!utils.inventoryContains(ItemID.NATURE_RUNE)) {
                        log.debug("withdrawing Nature runes");
                        utils.withdrawAllItem(ItemID.NATURE_RUNE);
                        return;
                    }
                    if (!utils.inventoryContains(Set.of(config.alchItemID(), config.alchItemID() + 1))) {
                        log.debug("withdrawing Config Alch Item");
                        utils.withdrawAllItem(config.alchItemID());
                        return;
                    }
                } else {
                    log.debug("inventory is full but trying to withdraw items");
                }
            }
        } else {
            GameObject bankBooth = utils.findNearestGameObject(getCurrentObstacle().getBankID());
            if (bankBooth != null) {
                clientThread.invoke(() -> client.invokeMenuAction("", "",bankBooth.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), bankBooth.getSceneMinLocation().getX(), bankBooth.getSceneMinLocation().getY()));

                // targetMenu = new MenuEntry("", "", bankBooth.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), bankBooth.getSceneMinLocation().getX(), bankBooth.getSceneMinLocation().getY(), false);
               // utils.setMenuEntry(targetMenu);
               // utils.delayMouseClick(bankBooth.getConvexHull().getBounds(), sleepDelay());
                timeout = tickDelay();
            }
        }
    }

    private ARooftopsObstacles getCurrentObstacle() {
        return ARooftopsObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
    }

    private void findObstacle() {
        ARooftopsObstacles obstacle = getCurrentObstacle();
        if (obstacle != null) {
            log.debug(String.valueOf(obstacle.getObstacleId()));
            if (obstacle.getObstacleType() == ARooftopsObstacleType.DECORATION) {
                DecorativeObject decObstacle = utils.findNearestDecorObject(obstacle.getObstacleId());
                if (decObstacle != null) {
                    clientThread.invoke(() -> client.invokeMenuAction("", "",decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY()));

                    //targetMenu = new MenuEntry("", "", decObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), decObstacle.getLocalLocation().getSceneX(), decObstacle.getLocalLocation().getSceneY(), false);
                    //utils.setMenuEntry(targetMenu);
                    //Rectangle clickPoint = (decObstacle.getConvexHull() != null) ? decObstacle.getConvexHull().getBounds() :
                          //  new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                   // utils.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            if (obstacle.getObstacleType() == ARooftopsObstacleType.GROUND_OBJECT) {
                GroundObject groundObstacle = utils.findNearestGroundObject(obstacle.getObstacleId());
                if (groundObstacle != null) {
                    clientThread.invoke(() -> client.invokeMenuAction("", "",groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY()));

                    // targetMenu = new MenuEntry("", "", groundObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), groundObstacle.getLocalLocation().getSceneX(), groundObstacle.getLocalLocation().getSceneY(), false);
                    //utils.setMenuEntry(targetMenu);
                   // Rectangle clickPoint = (groundObstacle.getConvexHull() != null) ? groundObstacle.getConvexHull().getBounds() :
                    //        new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
                   // utils.delayMouseClick(clickPoint, sleepDelay());
                    return;
                }
            }
            GameObject objObstacle = utils.findNearestGameObject(obstacle.getObstacleId());
            if (objObstacle != null) {
                clientThread.invoke(() -> client.invokeMenuAction("", "",objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY()));

                // targetMenu = new MenuEntry("", "", objObstacle.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), objObstacle.getSceneMinLocation().getX(), objObstacle.getSceneMinLocation().getY(), false);
               // utils.setMenuEntry(targetMenu);
               // Rectangle clickPoint = (objObstacle.getConvexHull() != null) ? objObstacle.getConvexHull().getBounds() :
               //         new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
               // utils.delayMouseClick(clickPoint, sleepDelay());
                return;
            }
        } else {
            log.debug("Not in obstacle area");
        }
    }

    private Portals getCurrentPortal() {
        //We provide the current varbit value and the enum returns the correlating Portal. i.e. we now have access to the ID of the active portal
        return Portals.getPortal(client.getVarbitValue(9298));
    }

    public ARooftopsState getState() {
        if (timeout > 0) {
            if (alchTimeout <= 0 && shouldAlch() && utils.inventoryContains(ItemID.NATURE_RUNE) &&
                    utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1)))) {
                timeout--;
                return HIGH_ALCH;
            }
            if (alchClick) {
                ARooftopsObstacles currentObstacle = getCurrentObstacle();
                if (currentObstacle != null) {
                    if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!utils.inventoryFull() || utils.inventoryContains(ItemID.MARK_OF_GRACE))) {
                        if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                            if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                            {
                                if (config.course().name().equals("ARDOUGNE") && config.alchMogStack() > 1) {
                                    if (markOfGrace.getQuantity() >= config.alchMogStack()) {
                                        return MARK_OF_GRACE;
                                    }
                                } else {
                                    return MARK_OF_GRACE;
                                }
                            } else {
                                log.info("Mark of grace not found and markOfGrace was not null");
                                markOfGrace = null;
                            }
                        }
                    }
                    //if (shouldEatSummerPie()) {
                     //   timeout--;
                       // return EAT_SUMMER_PIE;
                    //}
                    if (currentObstacle.getBankID() == 0 || !shouldRestock()) {
                        timeout--;
                        return (shouldCastTeleport()) ? CAST_CAMELOT_TELEPORT : FIND_OBSTACLE;
                    }
                }
            }
            return TIMEOUT;
        }
        if (shouldCastTeleport()) {
            return CAST_CAMELOT_TELEPORT;
        }
        if (utils.isMoving(beforeLoc)) {
            if (alchTimeout <= 0 && shouldAlch() && (utils.inventoryContains(ItemID.NATURE_RUNE) &&
                    utils.inventoryContains(Set.of(config.alchItemID(), (config.alchItemID() + 1))))) {
                timeout = tickDelay();
                return HIGH_ALCH;
            }
            timeout = tickDelay();
            return MOVING;
        }
        //if (shouldEatSummerPie()) {
           // return EAT_SUMMER_PIE;
        //}
        ARooftopsObstacles currentObstacle = ARooftopsObstacles.getObstacle(client.getLocalPlayer().getWorldLocation());
        if (currentObstacle == null) {
            timeout = tickDelay();
            return MOVING;
        }
        if (currentObstacle.getBankID() > 0 && shouldRestock()) {
            if (utils.findNearestGameObject(currentObstacle.getBankID()) != null) {
                return RESTOCK_ITEMS;
            } else {
                log.debug("should restock but couldn't find bank");
            }
        }
        if (markOfGrace != null && markOfGraceTile != null && config.mogPickup() && (!utils.inventoryFull() || utils.inventoryContains(ItemID.MARK_OF_GRACE))) {
            if (currentObstacle.getLocation().distanceTo(markOfGraceTile.getWorldLocation()) == 0) {
                if (markOfGraceTile.getGroundItems().contains(markOfGrace)) //failsafe sometimes onItemDespawned doesn't capture mog despawn
                {
                    if (config.course().name().equals("ARDOUGNE") && config.mogStack() > 1) {
                        if (markOfGrace.getQuantity() >= config.mogStack()) {
                            return MARK_OF_GRACE;
                        }
                    } else {
                        return MARK_OF_GRACE;
                    }
                } else {
                    log.info("Mark of grace not found and markOfGrace was not null");
                    markOfGrace = null;
                }
            }
        }
        if (client.getVarbitValue(9298) != 0) {
            log.info("Portal spawned");
            priffPortal = getCurrentPortal();
            spawnedPortal = utils.findNearestGameObject(priffPortal.getPortalID());
            if (spawnedPortal != null) {
                if (currentObstacle.getLocation().distanceTo(spawnedPortal.getWorldLocation()) == 0) {
                    return PRIFF_PORTAL;
                }
            }
        }
        /*if (chinBreakHandler.shouldBreak(this)) {
            return HANDLE_BREAK;
        }*/
        if (!utils.isMoving(beforeLoc)) {
            return FIND_OBSTACLE;
        }
        return ANIMATING;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!startAgility){// || chinBreakHandler.isBreakActive(this)) {
            return;
        }
        if (chinBreakHandler.isBreakActive(this)){
            return;
        }
        if (chinBreakHandler.shouldBreak(this))
        {
            chinBreakHandler.startBreak(this);
        }
        player = client.getLocalPlayer();
        if (alchTimeout > 0) {
            alchTimeout--;
        }
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN && client.getBoostedSkillLevel(Skill.HITPOINTS) > config.lowHP()) {
            if (!client.isResized()) {
                utils.sendGameMessage("client must be set to resizable");
                startAgility = false;
                return;
            }
            if (!REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
                log.debug("not in agility course region");
                return;
            }
            marksPerHour = (int) getMarksPH();
           // utils.handleRun(40, 20);
            state = getState();
            beforeLoc = client.getLocalPlayer().getLocalLocation();
            switch (state) {
                case TIMEOUT:
                    timeout--;
                    break;
                case MARK_OF_GRACE:
                    log.debug("Picking up mark of grace");
                    clientThread.invoke(() -> client.invokeMenuAction("", "",ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY()));
                    //targetMenu = new MenuEntry("", "", ItemID.MARK_OF_GRACE, 20, markOfGraceTile.getSceneLocation().getX(), markOfGraceTile.getSceneLocation().getY(), false);
                    //utils.setMenuEntry(targetMenu);
                    //utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
                    break;
                case FIND_OBSTACLE:
                    findObstacle();
                    break;

                case RESTOCK_ITEMS:
                    restockItems();
                    break;
                case MOVING:
                    break;
                case CAST_CAMELOT_TELEPORT:
                    clientThread.invoke(() -> client.invokeMenuAction("", "",2, MenuAction.CC_OP.getId(), -1, 14286879));

                    /*targetMenu = new MenuEntry("", "", 2, MenuAction.CC_OP.getId(), -1, 14286879, false);
                    Widget spellWidget = client.getWidget(WidgetInfo.SPELL_CAMELOT_TELEPORT);
                    if (spellWidget != null) {
                        utils.setMenuEntry(targetMenu);
                        utils.delayMouseClick(spellWidget.getBounds(), sleepDelay());
                    } else {
                        utils.setMenuEntry(targetMenu);
                        utils.delayClickRandomPointCenter(-200, 200, sleepDelay());
                    }*/

                    timeout = 2 + tickDelay();
                    break;
                case PRIFF_PORTAL:
                    log.info("Using Priff portal");
                    clientThread.invoke(() -> client.invokeMenuAction("", "",spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY()));

                    //targetMenu = new MenuEntry("", "", spawnedPortal.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), spawnedPortal.getSceneMinLocation().getX(), spawnedPortal.getSceneMinLocation().getY(), false);
                    //utils.setMenuEntry(targetMenu);
                   //utils.delayMouseClick(spawnedPortal.getConvexHull().getBounds(), sleepDelay());
                    break;
                case EAT_SUMMER_PIE:
                    //if (!utils.inventoryContains(SUMMER_PIE_IDS)) {
                        log.info("Out of Summer Pies");
                        state = OUT_OF_SUMMER_PIES;
                        startAgility = false;
                       // return;
                    //}
                    //eatSummerPie();
                    break;
            }
        } else {
            log.debug("client/ player is null or bot isn't started");
            return;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && startAgility) {
            markOfGraceTile = null;
            markOfGrace = null;
            state = TIMEOUT;
            timeout = 2;
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if (!startAgility) {
            return;
        }
        if (targetMenu != null) {
            log.debug("MenuEntry string event: " + targetMenu.toString());
            alchClick = (targetMenu.getOption().equals("Cast"));
            timeout = tickDelay();
        }
    }
    /*@Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
            return;
        }

        if (client.getVarbitValue(9298) != 0) {
            log.info("Portal spawned");
            priffPortal = getCurrentPortal();
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID())) {
            return;
        }

        if (Portals.values() PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            log.info("Portal spawned");
            priffPortal = null;
        }
    }*/

    @Subscribe
    private void onItemSpawned(ItemSpawned event) {
        if (!startAgility || !REGION_IDS.contains(client.getLocalPlayer().getWorldLocation().getRegionID()) ||
                !config.mogPickup()) {
            return;
        }

        TileItem item = event.getItem();
        Tile tile = event.getTile();

        if (item.getId() == ItemID.MARK_OF_GRACE) {
            log.debug("Mark of grace spawned");
            markOfGrace = item;
            markOfGraceTile = tile;
            WidgetItem mogInventory = utils.getInventoryWidgetItem(Collections.singletonList(ItemID.MARK_OF_GRACE));
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
            log.debug("Mark of grace despawned");
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
