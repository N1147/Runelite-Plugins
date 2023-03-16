
package net.runelite.client.plugins.ATempoross;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;

import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.Utils.Walking;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@PluginDescriptor(
	name = "ATempoross (Auto)",
	enabledByDefault = false,
	description = "Fights Tempoross skilling boss.",
	tags = {"numb","anarchise","tempoross","runel33t"}
)
@Slf4j
public class NTempoross extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private NTemporossConfig config;
	@Inject
	private Core core;
	@Inject
	private ConfigManager configManager;
	@Inject
	PluginManager pluginManager;
	@Inject
	OverlayManager overlayManager;
	@Inject
	ClientThread clientThread;
	NTemporossState state;
	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	NTemporossState previousState;
	int amount = 7;
	boolean waveIsIncoming = false;
	boolean firstInventory = true;
	GameObject Hammers;
	GameObject Ropes;
	GameObject Harpoon;
	GameObject Buckets;
	GameObject Hose;
	GameObject Box1;
	WorldPoint HammerLoc;
	NPC Box;
	boolean GotLocations = false;
	boolean hasTethered = false;
	boolean startedFishing = false;
	boolean roundStarted = false;
	boolean StartedDepositing = false;
	boolean StartedCooking = false;
	boolean StormBlowing = false;
	WorldPoint StartLocation;
	private static final String WAVE_END_SAFE = "as the wave washes over you";
	private static final String WAVE_END_DANGEROUS = "the wave slams into you";
	private static final String GAME_END = "retreats to the depths";
	private static final String GAME_BEGIN = "weigh anchor and sail you out";
	int timeout = 0;
	long sleepLength;
	boolean startBarbarianFisher;
	private final Set<Integer> rawFishIds = new HashSet<>();
	private final Set<Integer> requiredIds = new HashSet<>();
	private static final String WAVE_INCOMING_MESSAGE = "a colossal wave closes in...";

	@Provides
	NTemporossConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NTemporossConfig.class);
	}

	@Override
	protected void startUp() throws IOException, ClassNotFoundException {
		if (!startBarbarianFisher)
		{
			startBarbarianFisher = true;
			state = null;
			targetMenu = null;
			botTimer = Instant.now();
		}
		else
		{
			startBarbarianFisher=false;
			resetVals();
		}
	}

	@Override
	protected void shutDown() throws IOException, ClassNotFoundException {
		resetVals();
	}

	private void resetVals() throws IOException, ClassNotFoundException {
		if (!started) {
			started = true;
		}
		state = null;
		timeout = 0;
		botTimer = null;
		firstInventory = true;
		startBarbarianFisher = false;
		requiredIds.clear();
		rawFishIds.clear();
	}

	private long sleepDelay()
	{
		sleepLength = core.randomDelay(false, 100, 400, 100, 200);
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) core.randomDelay(false, 1, 2, 1, 1);
		return tickLength;
	}

	private void enterWaitingLobby(){
		core.useGameObject(41305, 3, 0);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains(WAVE_INCOMING_MESSAGE))
		{
			waveIsIncoming = true;
			if (core.inventoryItemContainsAmount(25564, 5, false, false)) {
				if (config.OnlyDepositCooked()) {
					StartedCooking = true;
				}
				else {
					StartedDepositing = true;
				}
			}
		}
		else if (message.contains(WAVE_END_SAFE) || message.contains(WAVE_END_DANGEROUS))
		{
			waveIsIncoming = false;
			hasTethered = false;
		}
		else if (message.contains(GAME_END)){
			roundStarted = false;
			core.attackNPC(core.findNearestNpc(10593, 10595, 10597, 10596).getId());
		}
	}

	public NTemporossState getState()
	{
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			if (timeout > 0) {
				return NTemporossState.TIMEOUT;
			}
			/*if (!config.useBuckets() && core.findNearestObject(NullObjectID.NULL_41006) != null && player.getWorldArea().intersectsWith(core.findNearestObject(NullObjectID.NULL_41006).getWorldLocation().toWorldArea())) {
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}
			if (!config.useBuckets() && core.findNearestNpc(8643) != null && player.getWorldArea().intersectsWith(core.findNearestNpc(8643).getWorldArea())) {
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}*/
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3136, 2818, 0), new WorldPoint(3168, 2853, 0)))) {
				previousState = NTemporossState.ENTER_LOBBY;
				firstInventory = true;
				GotLocations = false;
				StartedDepositing = false;
				StartedCooking = false;
				startedFishing = false;
				return NTemporossState.ENTER_LOBBY;
			}
			if (waveIsIncoming && !hasTethered){
				previousState = NTemporossState.TETHER;
				return NTemporossState.TETHER;
			}
			if (client.getWidget(437, 45) == null) {
				previousState = NTemporossState.TIMEOUT;
				return NTemporossState.TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 1%")) {
				previousState = NTemporossState.TIMEOUT;
				return NTemporossState.TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 0%")) {
				previousState = NTemporossState.TIMEOUT;
				return NTemporossState.TIMEOUT;
			}
			if (client.getWidget(437, 35) != null)  {
				roundStarted = true;
			}
			else if (client.getWidget(437, 35) == null)  {
				roundStarted = false;
				GotLocations = false;
			}
			if (client.getWidget(437, 35) != null && !waveIsIncoming) {
				if (!GotLocations)
				{
					firstInventory = true;
					amount = config.amount();
					StartLocation = client.getLocalPlayer().getWorldLocation();
					Hammers = core.findNearestGameObject(40964);
					Harpoon = core.findNearestGameObject(40967);
					Ropes = core.findNearestGameObject(40965);
					HammerLoc = Hammers.getWorldLocation();
					Buckets = core.findNearestGameObject(40966);
					Box = core.findNearestNpc(10576, 10578, 10579, 10577);
					Box1 = core.findNearestGameObject(40970, 40979, 40976, 40969);
					GotLocations = true;
				}
				if (firstInventory) {
					//if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25565, client.getRealSkillLevel(Skill.FISHING) < 85 ? 7 : 12, false, false)) {
						//StartedDepositing = true;
						//firstInventory = false;
					//}
					if (core.inventoryItemContainsAmount(25564, client.getRealSkillLevel(Skill.FISHING) < 85 ? 7 : 12, false, false)) {
						if (config.OnlyDepositCooked()) {
							StartedCooking = true;
							firstInventory = false;
						}
						else {
							StartedDepositing = true;
							firstInventory = false;
						}
					}
				}
				if (core.findNearestNpc(10571) != null) {
					StartedCooking = false;
					StartedDepositing = false;
					return NTemporossState.SPIRIT_POOL;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 1%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return NTemporossState.SPIRIT_POOL;
				}
				else if (client.getWidget(437, 35).getText().equals("Energy: 0%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return NTemporossState.SPIRIT_POOL;
				}
				if (config.useBuckets() && core.inventoryContains(1929) && core.findNearestAttackableNpcWithin(player.getWorldLocation(), 6, 8643, false) != null)  {
					previousState = NTemporossState.DOUSE_FIRE;
					return NTemporossState.DOUSE_FIRE;
				}
				if (StormBlowing) {
					core.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
					StormBlowing = false;
				}
				if (core.findNearestNpc(10571) == null) {
					if (client.getWidget(437, 35).getText().equals("Energy: 2%") || client.getWidget(437, 35).getText().equals("Energy: 3%") || client.getWidget(437, 35).getText().equals("Energy: 4%") || client.getWidget(437, 35).getText().equals("Energy: 5%") || client.getWidget(437, 35).getText().equals("Energy: 6%") || client.getWidget(437, 35).getText().equals("Energy: 7%") || client.getWidget(437, 35).getText().equals("Energy: 8%") || client.getWidget(437, 35).getText().equals("Energy: 9%") || client.getWidget(437, 35).getText().equals("Energy: 10%") || client.getWidget(437, 35).getText().equals("Energy: 11%") || client.getWidget(437, 35).getText().equals("Energy: 12%") || client.getWidget(437, 35).getText().equals("Energy: 13%") || client.getWidget(437, 35).getText().equals("Energy: 14%") || client.getWidget(437, 35).getText().equals("Energy: 15%") || client.getWidget(437, 35).getText().equals("Energy: 16%") || client.getWidget(437, 35).getText().equals("Energy: 17%") || client.getWidget(437, 35).getText().equals("Energy: 18%") || client.getWidget(437, 35).getText().equals("Energy: 19%") || client.getWidget(437, 35).getText().equals("Energy: 20%") || client.getWidget(437, 35).getText().equals("Energy: 21%") || client.getWidget(437, 35).getText().equals("Energy: 22%") || client.getWidget(437, 35).getText().equals("Energy: 23%") || client.getWidget(437, 35).getText().equals("Energy: 24%") || client.getWidget(437, 35).getText().equals("Energy: 25%")) {
						if (core.inventoryItemContainsAmount(25564, 5, false, false)) {
							StartedDepositing = true;
						}
						if (core.inventoryItemContainsAmount(25565, 5, false, false)) {
							StartedDepositing = true;
						}
					}
					if (client.getWidget(437, 35).getText().equals("Energy: 26%") || client.getWidget(437, 35).getText().equals("Energy: 27%") || client.getWidget(437, 35).getText().equals("Energy: 28%") || client.getWidget(437, 35).getText().equals("Energy: 29%") || client.getWidget(437, 35).getText().equals("Energy: 30%")) {
						if (core.inventoryItemContainsAmount(25564, 10, false, false)) {
							StartedDepositing = true;
						}
						if (core.inventoryItemContainsAmount(25565, 10, false, false)) {
							StartedDepositing = true;
						}
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 89%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 88%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 87%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 86%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 85%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 84%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 83%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 82%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 81%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 80%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 99%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 98%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 97%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 96%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 95%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 94%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 93%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 92%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 91%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 90%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 79%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 78%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 77%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 76%")) {
						StartedDepositing = true;
					}
					if (client.getWidget(437, 55).getText().equals("Storm Intensity: 75%")) {
						StartedDepositing = true;
					}
				}
				if (!StartedDepositing && core.findNearestNpc(10571) == null) {
					if (config.OnlyDepositCooked() && core.inventoryFull() && core.inventoryContains(25565) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = NTemporossState.DEPOSIT_FISH;
						return NTemporossState.DEPOSIT_FISH;
					}
					if (!config.OnlyDepositCooked() && core.inventoryFull() && core.inventoryContains(25564) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = NTemporossState.DEPOSIT_FISH;
						return NTemporossState.DEPOSIT_FISH;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25565, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = NTemporossState.DEPOSIT_FISH;
						return NTemporossState.DEPOSIT_FISH;
					}
					if (!config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = NTemporossState.DEPOSIT_FISH;
						return NTemporossState.DEPOSIT_FISH;
					}
				}
				if (StartedDepositing && !core.inventoryContains(25564) && !core.inventoryContains(25565)) {
					StartedDepositing = false;
				}
				if (StartedDepositing && core.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 7, core.findNearestNpc(10576, 10577, 10578, 10579).getId(), false) != null){
					if (core.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896){
						return NTemporossState.DEPOSIT_FISH;
					}
					if (core.inventoryContains(25565) && client.getLocalPlayer().getAnimation() != 896){
						return NTemporossState.DEPOSIT_FISH;
					}
				}
				if (StartedDepositing && core.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 7, core.findNearestNpc(10576, 10577, 10578, 10579).getId(), false) == null && core.findNearestNpc(10571) == null) {
					walking.walkTileOnScreen(Hammers.getWorldLocation());
					return NTemporossState.TIMEOUT;
				}
				if (config.useBuckets() && !core.inventoryContains(1925) && !core.inventoryContains(1929) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
					previousState = NTemporossState.GET_BUCKETS;
					return NTemporossState.GET_BUCKETS;
				}
				if (!core.inventoryContains(2347) && client.getLocalPlayer().getAnimation() != 896){
					previousState = NTemporossState.GET_HAMMER;
					return NTemporossState.GET_HAMMER;
				}
				if (core.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011) != null ) {
					previousState = NTemporossState.REPAIR_MAST;
					return NTemporossState.REPAIR_MAST;
				}
				if (!config.getRopes() && !core.inventoryContains(954) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = NTemporossState.GET_ROPES;
					return NTemporossState.GET_ROPES;
				}
				if (!core.inventoryContains(311) && !core.isItemEquipped(Collections.singleton(21028))&& !core.isItemEquipped(Collections.singleton(23762))&& !core.isItemEquipped(Collections.singleton(23864)) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = NTemporossState.GET_HARPOON;
					return NTemporossState.GET_HARPOON;
				}
				if (config.useBuckets() && core.inventoryContains(1925) && !core.inventoryContains(1929) && client.getLocalPlayer().getAnimation() != 896) {
					previousState = NTemporossState.FILL_BUCKETS;
					return NTemporossState.FILL_BUCKETS;
				}
				if (!StartedCooking && core.findNearestNpc(10571) == null) {
					if (config.OnlyDepositCooked() && core.inventoryFull() && core.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
					if (config.OnlyDepositCooked() && core.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = NTemporossState.COOKING;
						return NTemporossState.COOKING;
					}
				}
				if (StartedCooking && core.findNearestNpc(10571) == null){
					if (core.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896){
						return NTemporossState.COOKING;
					}
					if (!core.inventoryContains(25564)){
						StartedCooking = false;
					}
				}
				if (core.findNearestNpc(10569) != null && client.getLocalPlayer().getInteracting() != core.findNearestNpc(10569) && client.getLocalPlayer().getAnimation() != 896 && !core.inventoryItemContainsAmount(25564, amount, false, false)) {
					core.attackNPC(10569);
					return NTemporossState.TIMEOUT;
				}
				if (core.findNearestNpc(10568) != null && core.findNearestNpc(10569) == null && client.getLocalPlayer().getInteracting() == null && client.getLocalPlayer().getAnimation() != 896 && !core.inventoryItemContainsAmount(25564, amount, false, false)) {
					core.attackNPC(10568);
					return NTemporossState.TIMEOUT;
				}
				if (core.findNearestNpc(10568) == null && core.findNearestNpc(10569) == null && core.findNearestNpc(10565) == null && client.getLocalPlayer().getAnimation() != 896) {
					walking.walkTileOnScreen(core.findNearestObject(41236).getWorldLocation());
				}
				if (!core.inventoryItemContainsAmount(25564, amount, false, false)) {
					return NTemporossState.FISHING;
				}
				return NTemporossState.TIMEOUT;
			}
		}
		return NTemporossState.NULL_STATE;
	}
	@Inject
	Walking walking;
	public boolean started = false;
	@Subscribe
	private void onGameTick(GameTick tick) throws IOException, ClassNotFoundException {
		if (!startBarbarianFisher) {
			return;
		}
		player = client.getLocalPlayer();
		if (!started) {
			started = true;
			startBarbarianFisher = false;
			return;
		}
		if (client != null && player != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					//core.handleRun(30, 20);
					timeout--;
					break;
				case ITERATING:
					timeout = tickDelay();
					break;
				case LEAVE_LOBBY:
					core.attackNPC(10595);
					break;
				case FILL_BUCKETS:
					//TODO: HUMIDIFY
					//clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14286955));
					break;
				case GET_BUCKETS:
					if (Buckets.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						walking.walkTileOnScreen(Buckets.getWorldLocation());
						timeout = tickDelay();
						break;
					}
					GameObject Empty2 = core.findNearestGameObject(40966);
					if (Empty2 != null) {
						core.useGameObjectDirect(Empty2);
					}
					break;
				case GET_ROPES:
					if (Ropes.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						walking.walkTileOnScreen(Ropes.getWorldLocation());
						timeout = tickDelay();
						break;
					}
					GameObject Empty = core.findNearestGameObject(40965);
					if (Empty != null) {
						core.useGameObjectDirect(Empty);
					}
					break;
				case REPAIR_MAST:
					GameObject Objecter = core.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011);
					if (Objecter != null) {
						core.useGameObjectDirect(Objecter);
					}
					timeout = tickDelay();
					break;
				case GET_HAMMER:
					if (Hammers.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						walking.walkTileOnScreen(Hammers.getWorldLocation());
						timeout = tickDelay();
						break;
					}
					GameObject Empty3 = core.findNearestGameObject(40964);
					if (Empty3 != null) {
						core.useGameObjectDirect(Empty3);
					}
					break;
				case GET_HARPOON:
					if (Harpoon.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						walking.walkTileOnScreen(Harpoon.getWorldLocation());
						timeout = tickDelay();
						break;
					}
					GameObject Emptyy = core.findNearestGameObject(40967);
					if (Emptyy != null) {
						core.useGameObjectDirect(Emptyy);
					}
					break;
				case TETHER:
					if (waveIsIncoming && !hasTethered) {
						GameObject Object = core.findNearestGameObject(41354, 41355, 41353, 41352);
						if (Object != null) {
							core.useGameObjectDirect(Object);
							hasTethered = true;
							break;
						}
					}
					timeout = tickDelay();
					break;
				case DEPOSIT_FISH:
					if (StartedDepositing && !core.inventoryContains(25564) && !core.inventoryContains(25565)) {
						StartedDepositing = false;
						break;
					}
					NPC Empty1 = core.findNearestNpc(10576, 10578, 10579, 10577);
					if (Empty1.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						walking.walkTileOnScreen(Empty1.getWorldLocation());
						timeout = tickDelay();
						break;
					}
					if (Empty1 != null) {
						core.attackNPCDirect(Empty1);
						timeout = tickDelay();
						break;
					}
					break;
				case FIND_NPC:
					core.attackNPC(10565);
					timeout = tickDelay();
					break;
				case COOKING:
					if (StartedCooking && !core.inventoryContains(25564)) {
						StartedCooking = false;
						break;
					}
					if (client.getLocalPlayer().getAnimation() != 896) {
						core.useGameObject(41236, 3,0);
						timeout = 4;
					}
					break;
				case DOUSE_FIRE:
					if (config.useBuckets()) {
						NPC nearestFire = core.findNearestAttackableNpcWithin(player.getWorldLocation(), 6, 8643, false);
						if (nearestFire != null) {
							//clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
							core.attackNPCDirect(nearestFire);
							//timeout = tickDelay();
						}
					}
					break;
				case RUN_FROM_FIRE:
					core.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
					StormBlowing = false;
					break;
				case SPIRIT_POOL:
					StartedDepositing = false;
					StartedCooking = false;
					NPC spiritPool2 = core.findNearestNpc(10571);
					if (client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BAREHAND) {
						if (spiritPool2 != null) {
							core.attackNPC(10571);
							timeout = tickDelay();
							break;
						} else {
							core.walk(core.findNearestObject(41004).getWorldLocation());
							timeout = tickDelay();
							break;
						}
					}
					timeout = tickDelay();
					break;
				case FISHING:
					if (client.getLocalPlayer().getAnimation() != 896 && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BAREHAND) {
						NPC nearestFire1 = core.findNearestAttackableNpcWithin(player.getWorldLocation(), 6, 8643, false);
						NPC spiritPool = core.findNearestNpc(10571);
						NPC targetSpot = core.findNearestNpc(10569);
						NPC targetSpot2 = core.findNearestNpc(10568);
						if (nearestFire1 != null) {
							if (config.useBuckets()) {
								core.attackNPCDirect(nearestFire1);
								//clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire1.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
							} else {
								if (player.getWorldArea().intersectsWith(nearestFire1.getWorldArea())) {
									walking.walkTileOnScreen(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
									break;
								}
							}
						}
						if (spiritPool != null) {
							core.attackNPC(10571);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (targetSpot != null) {
							core.attackNPC(10569);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (targetSpot2 != null) {
							core.attackNPC(10568);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (core.findNearestNpc(10565) != null) {
							core.attackNPC(10565);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (core.findNearestNpc(10568) != null) {
							core.attackNPC(10568);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (core.findNearestNpc(10568) == null && core.findNearestNpc(10565) == null && targetSpot == null && targetSpot2 == null) {
							if (!core.isMoving()) {
								walking.walkTileOnScreen(core.findNearestObject(41236).getWorldLocation());
							}
							startedFishing = false;
							timeout = tickDelay();
							break;
						}
					}
					break;
				case ENTER_LOBBY:
					enterWaitingLobby();
					timeout = tickDelay();
					break;
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (core.findNearestNpc(10580) != null) {
			final Actor actor = event.getActor();
			NPC npc = (NPC) actor;
			if (npc.getId() == 10580) {
				switch (npc.getAnimation()) {
					case 8877:
						StormBlowing = true;
						break;
				}
			}
		}
	}
}
