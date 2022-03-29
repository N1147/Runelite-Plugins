
package net.runelite.client.plugins.NTempoross;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.client.plugins.NTempoross.NTemporossState.*;


@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NTempoross",
	enabledByDefault = false,
	description = "Fights Tempoross skilling boss.",
	tags = {"numb","ztd","tempoross","nplugins"}
)
@Slf4j
public class NTempoross extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private NTemporossConfig config;
	@Inject
	private PUtils utils;
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
	protected void startUp() throws IOException {
		resetVals();
	}

	@Override
	protected void shutDown() throws IOException {
		resetVals();
	}

	private void resetVals() throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
		state = null;
		timeout = 0;
		botTimer = null;
		firstInventory = true;
		startBarbarianFisher = false;
		requiredIds.clear();
		rawFishIds.clear();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NTempoross"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
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
	}

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(false, 100, 400, 100, 200);
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(false, 1, 2, 1, 1);
		return tickLength;
	}
	private void useGameObject(int id, int opcode)
	{
		GameObject targetObject = utils.findNearestGameObject(id);
		if(targetObject!=null){
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));
		}
	}
	private void enterWaitingLobby(){
		useGameObject(41305, 3);
	}

	private void interactNPC(int objectIds) {
		NPC targetNPC = utils.findNearestNpc(objectIds);
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
		} else {
			log.info("NPC is null");
		}
	}
	private void interactNPCDirect(NPC targetNPC) {
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
		} else {
			log.info("NPC is null");
		}
	}
	private void interactNPCOption2(int objectIds) {
		NPC targetNPC = utils.findNearestNpc(objectIds);
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));
		} else {
			log.info("NPC is null");
		}
	}
	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains(WAVE_INCOMING_MESSAGE))
		{
			waveIsIncoming = true;
			if (utils.inventoryItemContainsAmount(25564, 5, false, false)) {
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
			interactNPCOption2(utils.findNearestNpc(10593, 10595, 10597, 10596).getId());
		}
	}

	public NTemporossState getState()
	{
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			if (timeout > 0) {
				return TIMEOUT;
			}
			/*if (!config.useBuckets() && utils.findNearestObject(NullObjectID.NULL_41006) != null && player.getWorldArea().intersectsWith(utils.findNearestObject(NullObjectID.NULL_41006).getWorldLocation().toWorldArea())) {
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}
			if (!config.useBuckets() && utils.findNearestNpc(8643) != null && player.getWorldArea().intersectsWith(utils.findNearestNpc(8643).getWorldArea())) {
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}*/
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3136, 2818, 0), new WorldPoint(3168, 2853, 0)))) {
				previousState = ENTER_LOBBY;
				firstInventory = true;
				GotLocations = false;
				StartedDepositing = false;
				StartedCooking = false;
				startedFishing = false;
				return ENTER_LOBBY;
			}
			if (waveIsIncoming && !hasTethered){
				previousState = TETHER;
				return TETHER;
			}
			if (client.getWidget(437, 45) == null) {
				previousState = TIMEOUT;
				return TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 1%")) {
				previousState = TIMEOUT;
				return TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 0%")) {
				previousState = TIMEOUT;
				return TIMEOUT;
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
					Hammers = utils.findNearestGameObject(40964);
					Harpoon = utils.findNearestGameObject(40967);
					Ropes = utils.findNearestGameObject(40965);
					HammerLoc = Hammers.getWorldLocation();
					Buckets = utils.findNearestGameObject(40966);
					Box = utils.findNearestNpc(10576, 10578, 10579, 10577);
					Box1 = utils.findNearestGameObject(40970, 40979, 40976, 40969);
					GotLocations = true;
				}
				if (firstInventory) {
					//if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25565, client.getRealSkillLevel(Skill.FISHING) < 85 ? 7 : 12, false, false)) {
						//StartedDepositing = true;
						//firstInventory = false;
					//}
					if (utils.inventoryItemContainsAmount(25564, client.getRealSkillLevel(Skill.FISHING) < 85 ? 7 : 12, false, false)) {
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
				if (utils.findNearestNpc(10571) != null) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 1%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}
				else if (client.getWidget(437, 35).getText().equals("Energy: 0%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}
				if (config.useBuckets() && utils.inventoryContains(1929) && utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643)) != null)  {
					previousState = DOUSE_FIRE;
					return DOUSE_FIRE;
				}
				if (StormBlowing) {
					utils.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
					StormBlowing = false;
				}
				if (utils.findNearestNpc(10571) == null) {
					if (client.getWidget(437, 35).getText().equals("Energy: 2%") || client.getWidget(437, 35).getText().equals("Energy: 3%") || client.getWidget(437, 35).getText().equals("Energy: 4%") || client.getWidget(437, 35).getText().equals("Energy: 5%") || client.getWidget(437, 35).getText().equals("Energy: 6%") || client.getWidget(437, 35).getText().equals("Energy: 7%") || client.getWidget(437, 35).getText().equals("Energy: 8%") || client.getWidget(437, 35).getText().equals("Energy: 9%") || client.getWidget(437, 35).getText().equals("Energy: 10%") || client.getWidget(437, 35).getText().equals("Energy: 11%") || client.getWidget(437, 35).getText().equals("Energy: 12%") || client.getWidget(437, 35).getText().equals("Energy: 13%") || client.getWidget(437, 35).getText().equals("Energy: 14%") || client.getWidget(437, 35).getText().equals("Energy: 15%") || client.getWidget(437, 35).getText().equals("Energy: 16%") || client.getWidget(437, 35).getText().equals("Energy: 17%") || client.getWidget(437, 35).getText().equals("Energy: 18%") || client.getWidget(437, 35).getText().equals("Energy: 19%") || client.getWidget(437, 35).getText().equals("Energy: 20%") || client.getWidget(437, 35).getText().equals("Energy: 21%") || client.getWidget(437, 35).getText().equals("Energy: 22%") || client.getWidget(437, 35).getText().equals("Energy: 23%") || client.getWidget(437, 35).getText().equals("Energy: 24%") || client.getWidget(437, 35).getText().equals("Energy: 25%")) {
						if (utils.inventoryItemContainsAmount(25564, 5, false, false)) {
							StartedDepositing = true;
						}
						if (utils.inventoryItemContainsAmount(25565, 5, false, false)) {
							StartedDepositing = true;
						}
					}
					if (client.getWidget(437, 35).getText().equals("Energy: 26%") || client.getWidget(437, 35).getText().equals("Energy: 27%") || client.getWidget(437, 35).getText().equals("Energy: 28%") || client.getWidget(437, 35).getText().equals("Energy: 29%") || client.getWidget(437, 35).getText().equals("Energy: 30%")) {
						if (utils.inventoryItemContainsAmount(25564, 10, false, false)) {
							StartedDepositing = true;
						}
						if (utils.inventoryItemContainsAmount(25565, 10, false, false)) {
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
				if (!StartedDepositing && utils.findNearestNpc(10571) == null) {
					if (config.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25565) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (!config.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25565, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (!config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
				}
				if (StartedDepositing && !utils.inventoryContains(25564) && !utils.inventoryContains(25565)) {
					StartedDepositing = false;
				}
				if (StartedDepositing && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 7,10576, 10577, 10578, 10579) != null){
					if (utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896){
						return DEPOSIT_FISH;
					}
					if (utils.inventoryContains(25565) && client.getLocalPlayer().getAnimation() != 896){
						return DEPOSIT_FISH;
					}
				}
				if (StartedDepositing && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 7,10576, 10577, 10578, 10579) == null && utils.findNearestNpc(10571) == null) {
					utils.walk(Hammers.getWorldLocation().toWorldArea().toWorldPoint());
					return TIMEOUT;
				}
				if (config.useBuckets() && !utils.inventoryContains(1925) && !utils.inventoryContains(1929) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
					previousState = GET_BUCKETS;
					return GET_BUCKETS;
				}
				if (!utils.inventoryContains(2347) && client.getLocalPlayer().getAnimation() != 896){
					previousState = GET_HAMMER;
					return GET_HAMMER;
				}
				if (utils.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011) != null ) {
					previousState = REPAIR_MAST;
					return REPAIR_MAST;
				}
				if (!config.getRopes() && !utils.inventoryContains(954) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = GET_ROPES;
					return GET_ROPES;
				}
				if (!utils.inventoryContains(311) && !utils.isItemEquipped(Collections.singleton(21028))&& !utils.isItemEquipped(Collections.singleton(23762))&& !utils.isItemEquipped(Collections.singleton(23864)) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = GET_HARPOON;
					return GET_HARPOON;
				}
				if (config.useBuckets() && utils.inventoryContains(1925) && !utils.inventoryContains(1929) && client.getLocalPlayer().getAnimation() != 896) {
					previousState = FILL_BUCKETS;
					return FILL_BUCKETS;
				}
				if (!StartedCooking && utils.findNearestNpc(10571) == null) {
					if (config.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (config.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
				}
				if (StartedCooking && utils.findNearestNpc(10571) == null){
					if (utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896){
						return COOKING;
					}
					if (!utils.inventoryContains(25564)){
						StartedCooking = false;
					}
				}
				if (utils.findNearestNpc(10569) != null && client.getLocalPlayer().getInteracting() != utils.findNearestNpc(10569) && client.getLocalPlayer().getAnimation() != 896 && !utils.inventoryItemContainsAmount(25564, amount, false, false)) {
					interactNPC(10569);
					return TIMEOUT;
				}
				if (utils.findNearestNpc(10568) != null && utils.findNearestNpc(10569) == null && client.getLocalPlayer().getInteracting() == null && client.getLocalPlayer().getAnimation() != 896 && !utils.inventoryItemContainsAmount(25564, amount, false, false)) {
					interactNPC(10568);
					return TIMEOUT;
				}
				if (utils.findNearestNpc(10568) == null && utils.findNearestNpc(10569) == null && utils.findNearestNpc(10565) == null && client.getLocalPlayer().getAnimation() != 896) {
					utils.walk(utils.findNearestObject(41236).getWorldLocation().toWorldArea().toWorldPoint());
				}
				if (!utils.inventoryItemContainsAmount(25564, amount, false, false)) {
					return FISHING;
				}
				return TIMEOUT;
			}
		}
		return NULL_STATE;
	}
	public boolean started = false;
	@Subscribe
	private void onGameTick(GameTick tick) throws IOException {
		if (!startBarbarianFisher) {
			return;
		}
		player = client.getLocalPlayer();
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			startBarbarianFisher = false;
			return;
		}
		if (client != null && player != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ITERATING:
					timeout = tickDelay();
					break;
				case LEAVE_LOBBY:
					interactNPCOption2(10595);
					break;
				case FILL_BUCKETS:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14286955));
					break;
				case GET_BUCKETS:
					if (Buckets.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						utils.walk(Buckets.getWorldLocation().toWorldArea().toWorldPoint());
						timeout = tickDelay();
						break;
					}
					GameObject Empty2 = utils.findNearestGameObject(40966);
					if (Empty2 != null) {
						utils.useGameObjectDirect(Empty2, sleepDelay(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId());
					}
					break;
				case GET_ROPES:
					if (Ropes.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						utils.walk(Ropes.getWorldLocation().toWorldArea().toWorldPoint());
						timeout = tickDelay();
						break;
					}
					GameObject Empty = utils.findNearestGameObject(40965);
					if (Empty != null) {
						utils.useGameObjectDirect(Empty, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					}
					break;
				case REPAIR_MAST:
					GameObject Objecter = utils.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011);
					if (Objecter != null) {
						useGameObject(Objecter.getId(), 3);
					}
					timeout = tickDelay();
					break;
				case GET_HAMMER:
					if (Hammers.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						utils.walk(Hammers.getWorldLocation().toWorldArea().toWorldPoint());
						timeout = tickDelay();
						break;
					}
					GameObject Empty3 = utils.findNearestGameObject(40964);
					if (Empty3 != null) {
						utils.useGameObjectDirect(Empty3, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					}
					break;
				case GET_HARPOON:
					if (Harpoon.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						utils.walk(Harpoon.getWorldLocation().toWorldArea().toWorldPoint());
						timeout = tickDelay();
						break;
					}
					GameObject Emptyy = utils.findNearestGameObject(40967);
					if (Emptyy != null) {
						utils.useGameObjectDirect(Emptyy, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					}
					break;
				case TETHER:
					if (waveIsIncoming && !hasTethered) {
						GameObject Object = utils.findNearestGameObject(41354, 41355, 41353, 41352);
						if (Object != null) {
							useGameObject(Object.getId(), 3);
							hasTethered = true;
							break;
						}
					}
					timeout = tickDelay();
					break;
				case DEPOSIT_FISH:
					if (StartedDepositing && !utils.inventoryContains(25564) && !utils.inventoryContains(25565)) {
						StartedDepositing = false;
						break;
					}
					NPC Empty1 = utils.findNearestNpc(10576, 10578, 10579, 10577);
					if (Empty1.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldLocation()) > 4) {
						utils.walk(Empty1.getWorldLocation().toWorldArea().toWorldPoint());
						timeout = tickDelay();
						break;
					}
					if (Empty1 != null) {
						interactNPCDirect(Empty1);
						timeout = tickDelay();
						break;
					}
					break;
				case FIND_NPC:
					interactNPC(10565);
					timeout = tickDelay();
					break;
				case COOKING:
					if (StartedCooking && !utils.inventoryContains(25564)) {
						StartedCooking = false;
						break;
					}
					if (client.getLocalPlayer().getAnimation() != 896) {
						useGameObject(41236, 3);
						timeout = 4;
					}
					break;
				case DOUSE_FIRE:
					if (config.useBuckets()) {
						NPC nearestFire = utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643));
						if (nearestFire != null) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
							timeout = tickDelay();
						}
					}
					break;
				case RUN_FROM_FIRE:
					utils.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
					StormBlowing = false;
					break;
				case SPIRIT_POOL:
					StartedDepositing = false;
					StartedCooking = false;
					NPC spiritPool2 = utils.findNearestNpc(10571);
					if (client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BAREHAND) {
						if (spiritPool2 != null) {
							interactNPC(10571);
							timeout = tickDelay();
							break;
						} else {
							utils.walk(utils.findNearestObject(41004).getWorldLocation());
							timeout = tickDelay();
							break;
						}
					}
					timeout = tickDelay();
					break;
				case FISHING:
					if (client.getLocalPlayer().getAnimation() != 896 && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BAREHAND) {
						NPC nearestFire1 = utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643));
						NPC spiritPool = utils.findNearestNpc(10571);
						NPC targetSpot = utils.findNearestNpc(10569);
						NPC targetSpot2 = utils.findNearestNpc(10568);
						if (nearestFire1 != null) {
							if (config.useBuckets()) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire1.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
							} else {
								if (player.getWorldArea().intersectsWith(nearestFire1.getWorldArea())) {
									utils.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
									break;
								}
							}
						}
						if (spiritPool != null) {
							interactNPC(10571);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (targetSpot != null) {
							interactNPC(10569);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (targetSpot2 != null) {
							interactNPC(10568);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (utils.findNearestNpc(10565) != null) {
							interactNPC(10565);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (utils.findNearestNpc(10568) != null) {
							interactNPC(10568);
							startedFishing = true;
							timeout = tickDelay();
							break;
						} else if (utils.findNearestNpc(10568) == null && utils.findNearestNpc(10565) == null && targetSpot == null && targetSpot2 == null) {
							if (!utils.isMoving()) {
								utils.walk(utils.findNearestObject(41236).getWorldLocation().toWorldArea().toWorldPoint());
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
		if (utils.findNearestNpc(10580) != null) {
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
