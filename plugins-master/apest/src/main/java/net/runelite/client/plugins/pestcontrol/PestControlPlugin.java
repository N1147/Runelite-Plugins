/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
package net.runelite.client.plugins.pestcontrol;

import com.google.inject.Provides;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.NUtils.PUtils.*;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;


@Extension
@PluginDescriptor(
	name = "APest",
	description = "Automated Pest Control",
	tags = {"anarchise","pest control","pest"}
)

@Slf4j
@PluginDependency(PUtils.class)
public class PestControlPlugin extends Plugin
{

	private static final int[] PEST_CONTROL_MAP_REGION = {10536, 10537, 10538};

	@Inject
	private Client client;

	@Inject
	public ReflectBreakHandler chinBreakHandler;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PestControlConfig config;

	@Inject
	private PUtils utils;

	@Inject
	private Notifier notifier;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	PestControlOverlay overlay;

	@Inject
	ExecutorService executorService;
	Player player;
	NPC currentNPC;
	MenuEntry targetMenu;
	public boolean run = false;
	boolean started = false;
	Instant botTimer;
	int timeout = 0;
	long sleepLength;
	int tickLength;
	boolean threadFix = true;
	int count = 0;
	LocalPoint beforeLoc = new LocalPoint(0, 0);

	@Provides
	PestControlConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PestControlConfig.class);
	}

	@Override
	protected void startUp() {
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown() {
		chinBreakHandler.unregisterPlugin(this);
		overlayManager.remove(overlay);
	}

	public void enterLobby() {
		if (config.boatSelect() == Boat.NOVICE){
			utils.useGameObject(14315, 3, sleepDelay());
		}
		if (config.boatSelect() == Boat.INTERMEDIATE){
			utils.useGameObject(25631, 3, sleepDelay());
		}
		if (config.boatSelect() == Boat.VETERAN){
			utils.useGameObject(25632, 3, sleepDelay());
		}
	}
	@Inject
	private ClientThread clientThread;

	public void findEnemiesLoop(){
		if (client.getVar(Varbits.QUICK_PRAYER) == 0 && client.getBoostedSkillLevel(Skill.PRAYER) > 0)
		{
			clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1,  MenuAction.CC_OP.getId(), -1, 10485775));
		}
		NPC target = utils.findNearestNpc("Brawler", "Defiler", "Ravager", "Shifter", "Torcher");

		NPC priorityTarget = utils.findNearestNpc(NpcID.PORTAL, NpcID.PORTAL_1740, NpcID.PORTAL_1741, NpcID.PORTAL_1742, NpcID.PORTAL_1747, NpcID.PORTAL_1748, NpcID.PORTAL_1749, NpcID.PORTAL_1750,
				NpcID.SPINNER, NpcID.SPINNER_1710, NpcID.SPINNER_1711, NpcID.SPINNER_1712, NpcID.SPINNER_1713);


		if (priorityTarget != null)
		{
			target = priorityTarget;
		}
		if (target != null)
		{
			utils.attackNPCDirect(target);
		}
		else
		{
			WallObject gate = utils.findWallObjectWithin(client.getLocalPlayer().getWorldLocation(), 20, 14235, 14233);
			if (gate != null){
				utils.useWallObjectDirect(gate, sleepDelay(), 3);
			}
			else {
				utils.walk(new WorldPoint(client.getLocalPlayer().getWorldLocation().getX(), client.getLocalPlayer().getWorldLocation().getY() - 10, 0));
			}
		}
	}

	PestControlState state;

	public PestControlState getState(){
		if (timeout > 0) {
			return PestControlState.TIMEOUT;
		}
		if (!isInBoat() && isInPestControl()) {
			return PestControlState.ENTER_LOBBY;
		}
		if (!isInBoat() && GameStarted()){
			return PestControlState.PLAY_GAME;
		}
		if (!isInBoat() && !isInPestControl() && !GameStarted()){
			return PestControlState.TIMEOUT;
		}
		return PestControlState.TIMEOUT;
	}

	@Subscribe
	public void onGameTick(GameTick tick){
		if (!started){
			return;
		}
		if (chinBreakHandler.isBreakActive(this)){
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state) {
				case TIMEOUT:
					log.info("TIMEOUT");
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
					log.info("ANIMATING");
					if (player.getAnimation() == 6752) {
						timeout = 7;
					} else {
						timeout = tickDelay();
					}
					break;
				case MOVING:
					log.info("MOVING");
					timeout = tickDelay();
					break;
				case PLAY_GAME:
					log.info("FINDING TARGET");
					findEnemiesLoop();
					timeout = tickDelay();
					break;
				case ENTER_LOBBY:
					if (chinBreakHandler.shouldBreak(this))
					{
						chinBreakHandler.startBreak(this);
					}
					log.info("NEW LOBBY");
					enterLobby();
					timeout = tickDelay();
					break;
			}
		}
	}

	private long sleepDelay(){
		sleepLength = utils.randomDelay(false, 100, 700, 300, 300);
		return utils.randomDelay(false, 100, 700, 300, 300);
	}

	private int tickDelay(){
		tickLength = (int) utils.randomDelay(false, 1, 2, 1, 1);
		return tickLength;
	}

	private boolean isInBoat(){
		Widget boatWidget = client.getWidget(WidgetInfo.PEST_CONTROL_BOAT_INFO);
		return boatWidget != null;
	}
	private boolean GameStarted(){
		Widget boatWidget = client.getWidget(WidgetInfo.PEST_CONTROL_ACTIVITY_BAR);
		return boatWidget != null;
	}
	private boolean isInPestControl(){
		if (client.getLocalPlayer().getWorldLocation().getRegionID() == 10537)
		return true;
		else return false;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("apestconfig")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (started)
			{
				chinBreakHandler.stopPlugin(this);
				started = false;
				run = false;
				overlayManager.remove(overlay);
				currentNPC = null;
				targetMenu = null;
				executorService.shutdown();
			}
			else
			{
				chinBreakHandler.startPlugin(this);
				overlayManager.add(overlay);
				executorService = Executors.newSingleThreadExecutor();
				started = true;
				run = true;
				botTimer = Instant.now();
			}
		}
	}
}
