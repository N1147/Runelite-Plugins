package net.runelite.client.plugins.NInfernoHelper;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NInfernoHelper",
	description = "Helps with inferno",
	tags = {"inferno","numb","ninfernohelper","ztd"},
	enabledByDefault = false
)
public class NInfernoHelper extends Plugin
{

	public static final int JALTOK_JAD_MAGE_ATTACK = 7592;
	public static final int JALTOK_JAD_RANGE_ATTACK = 7593;

	private WorldPoint lastLocation = new WorldPoint(0, 0, 0);

	@Getter(AccessLevel.PACKAGE)
	private int currentWaveNumber;

	@Getter(AccessLevel.PACKAGE)
	private final List<InfernoUtils> infernoNpcs = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final Map<Integer, Map<InfernoUtils.Attack, Integer>> upcomingAttacks = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	private InfernoUtils.Attack closestAttack = null;

	@Getter(AccessLevel.PACKAGE)
	private final List<WorldPoint> obstacles = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private boolean finalPhase = false;
	private boolean finalPhaseTick = false;
	private int ticksSinceFinalPhase = 0;
	@Getter(AccessLevel.PACKAGE)
	private NPC zukShield = null;
	private NPC zuk = null;
	private int zukShieldCornerTicks = -2;

	@Getter(AccessLevel.PACKAGE)
	private InfernoUtils centralNibbler = null;
	public static final int JAL_NIB = 7574;
	public static final int JAL_MEJRAH = 7578;
	public static final int JAL_MEJRAH_STAND = 7577;
	public static final int JAL_AK_RANGE_ATTACK = 7581;
	public static final int JAL_AK_MELEE_ATTACK = 7582;
	public static final int JAL_AK_MAGIC_ATTACK = 7583;
	public static final int JAL_IMKOT = 7597;
	public static final int JAL_XIL_MELEE_ATTACK = 7604;
	public static final int JAL_XIL_RANGE_ATTACK = 7605;
	public static final int JAL_ZEK_MAGE_ATTACK = 7610;
	public static final int JAL_ZEK_MELEE_ATTACK = 7612;
	public static final int TZKAL_ZUK = 7566;

	@Provides
	NInfernoHelperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(NInfernoHelperConfig.class);
	}
	@Inject
	private NInfernoHelperConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ItemManager itemManager;
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;
	@Inject
	private PUtils utils;
	Player enemy;
	Instant timer;

	private void reset() throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
		closestAttack = null;
	}

	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("infernohelper"))
		{
			return;
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}
	boolean started = false;
	int timeout = 0;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException {
		if (timeout > 0) {
			timeout--;
		}
		if (!isInInferno()) {
			return;
		}
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			reset();
			return;
		}
		upcomingAttacks.clear();
		calculateUpcomingAttacks();
		closestAttack = null;
		calculateClosestAttack();
		centralNibbler = null;
		calculateCentralNibbler();
		if (finalPhaseTick) {
			finalPhaseTick = false;
		} else if (finalPhase) {
			ticksSinceFinalPhase++;
		}
		if (getClosestAttack() != null) {
			InfernoUtils.Attack prayerForAttack = null;
			if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
				prayerForAttack = InfernoUtils.Attack.MAGIC;
			} else if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
				prayerForAttack = InfernoUtils.Attack.RANGED;
			} else if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
				prayerForAttack = InfernoUtils.Attack.MELEE;
			}
			if (getClosestAttack() != prayerForAttack) {
				if (!client.isPrayerActive(getClosestAttack().getPrayer())) {
					activatePrayer(getClosestAttack().getPrayer().getWidgetInfo());
				}
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event) {
		if (!isInInferno()) {
			return;
		}

		if (event.getActor() instanceof NPC) {
			final NPC npc = (NPC) event.getActor();

			if (ArrayUtils.contains(InfernoUtils.Type.NIBBLER.getNpcIds(), npc.getId())
					&& npc.getAnimation() == 7576) {
				infernoNpcs.removeIf(infernoNPC -> infernoNPC.getNpc() == npc);
			}
		}

		Actor actor = event.getActor();

		if (actor == null) {
			return;
		}
		switch (actor.getAnimation()) {
			case AnimationID.TZTOK_JAD_MAGIC_ATTACK:
			case JALTOK_JAD_MAGE_ATTACK:
				if (client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 0) {
						activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
				}
				break;
			case AnimationID.TZTOK_JAD_RANGE_ATTACK:
			case JALTOK_JAD_RANGE_ATTACK:
				if (client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 0) {
						activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
				}
				break;
			default:
				break;
		}
	}

	private boolean isInInferno()
	{
		return ArrayUtils.contains(client.getMapRegions(), 9043);
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		if (!isInInferno()) {
			return;
		}
		final int npcId = event.getNpc().getId();
		if (npcId == NpcID.ANCESTRAL_GLYPH) {
			zukShield = event.getNpc();
			return;
		}
		final InfernoUtils.Type infernoNPCType = InfernoUtils.Type.typeFromId(npcId);
		if (infernoNPCType == null) {
			return;
		}
		switch (infernoNPCType) {
			case BLOB:
				infernoNpcs.add(new InfernoUtils(event.getNpc()));
				return;
			case MAGE:
				break;
			case ZUK:
				finalPhase = false;
				zukShieldCornerTicks = -2;
				break;
			case HEALER_ZUK:
				finalPhase = true;
				ticksSinceFinalPhase = 1;
				finalPhaseTick = true;
				for (InfernoUtils infernoNPC : infernoNpcs) {
					if (infernoNPC.getType() == InfernoUtils.Type.ZUK) {
						infernoNPC.setTicksTillNextAttack(-1);
					}
				}
				break;
		}
		infernoNpcs.add(0, new InfernoUtils(event.getNpc()));
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event) {
		if (!isInInferno()) {
			return;
		}
		int npcId = event.getNpc().getId();
		switch (npcId) {
			case NpcID.ANCESTRAL_GLYPH:
				zukShield = null;
				return;
			case NpcID.TZKALZUK:
				zuk = null;
				break;
			default:
				break;
		}
		infernoNpcs.removeIf(infernoNPC -> infernoNPC.getNpc() == event.getNpc());
	}

	private void calculateUpcomingAttacks()
	{
		for (InfernoUtils infernoNPC : infernoNpcs)
		{
			infernoNPC.gameTick(client, lastLocation, finalPhase, ticksSinceFinalPhase);

			if (infernoNPC.getType() == InfernoUtils.Type.ZUK && zukShieldCornerTicks == -1)
			{
				infernoNPC.updateNextAttack(InfernoUtils.Attack.UNKNOWN, 12);
				zukShieldCornerTicks = 0;
			}

			if (infernoNPC.getTicksTillNextAttack() > 0
					&& (infernoNPC.getNextAttack() != InfernoUtils.Attack.UNKNOWN
					|| infernoNPC.getType() == InfernoUtils.Type.BLOB
					&& infernoNPC.getTicksTillNextAttack() >= 4))
			{
				upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack(), k -> new HashMap<>());

				if (infernoNPC.getType() == InfernoUtils.Type.BLOB
						&& infernoNPC.getTicksTillNextAttack() >= 4)
				{
					upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 3, k -> new HashMap<>());
					upcomingAttacks.computeIfAbsent(infernoNPC.getTicksTillNextAttack() - 4, k -> new HashMap<>());

					// If there's already a magic attack on the detection tick, group them
					if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoUtils.Attack.MAGIC))
					{
						if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoUtils.Attack.MAGIC) > InfernoUtils.Type.BLOB.getPriority())
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoUtils.Attack.MAGIC, InfernoUtils.Type.BLOB.getPriority());
						}
					}
					// If there's already a ranged attack on the detection tick, group them
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoUtils.Attack.RANGED))
					{
						if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoUtils.Attack.RANGED) > InfernoUtils.Type.BLOB.getPriority())
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoUtils.Attack.RANGED, InfernoUtils.Type.BLOB.getPriority());
						}
					}
					// If there's going to be a magic attack on the blob attack tick, pray range on the detect tick so magic is prayed on the attack tick
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(InfernoUtils.Attack.MAGIC)
							|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey(InfernoUtils.Attack.MAGIC))
					{
						if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoUtils.Attack.RANGED)
								|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoUtils.Attack.RANGED) > InfernoUtils.Type.BLOB.getPriority())
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoUtils.Attack.RANGED, InfernoUtils.Type.BLOB.getPriority());
						}
					}
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(InfernoUtils.Attack.RANGED)
							|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey(InfernoUtils.Attack.RANGED))
					{
						if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoUtils.Attack.MAGIC)
								|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoUtils.Attack.MAGIC) > InfernoUtils.Type.BLOB.getPriority())
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoUtils.Attack.MAGIC, InfernoUtils.Type.BLOB.getPriority());
						}
					}
					else
					{
						upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoUtils.Attack.MAGIC, InfernoUtils.Type.BLOB.getPriority());
					}
				}
				else
				{
					final InfernoUtils.Attack attack = infernoNPC.getNextAttack();
					final int priority = infernoNPC.getType().getPriority();

					if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(attack)
							|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).get(attack) > priority)
					{
						upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).put(attack, priority);
					}
				}
			}
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event) {
		if (!isInInferno() || event.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}

		String message = event.getMessage();

		if (event.getMessage().contains("Wave:")) {
			message = message.substring(message.indexOf(": ") + 2);
			currentWaveNumber = Integer.parseInt(message.substring(0, message.indexOf('<')));
		}
	}

	private void calculateClosestAttack()
	{
		int closestTick = 999;
		int closestPriority = 999;

		for (Integer tick : upcomingAttacks.keySet())
		{
			final Map<InfernoUtils.Attack, Integer> attackPriority = upcomingAttacks.get(tick);

			for (InfernoUtils.Attack currentAttack : attackPriority.keySet())
			{
				final int currentPriority = attackPriority.get(currentAttack);
				if (tick < closestTick || (tick == closestTick && currentPriority < closestPriority))
				{
					closestAttack = currentAttack;
					closestPriority = currentPriority;
					closestTick = tick;
				}
			}
		}
	}

	private void calculateCentralNibbler()
	{
		InfernoUtils bestNibbler = null;
		int bestAmountInArea = 0;
		int bestDistanceToPlayer = 999;

		for (InfernoUtils infernoNPC : infernoNpcs)
		{
			if (infernoNPC.getType() != InfernoUtils.Type.NIBBLER)
			{
				continue;
			}

			int amountInArea = 0;
			final int distanceToPlayer = infernoNPC.getNpc().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());

			for (InfernoUtils checkNpc : infernoNpcs)
			{
				if (checkNpc.getType() != InfernoUtils.Type.NIBBLER
						|| checkNpc.getNpc().getWorldArea().distanceTo(infernoNPC.getNpc().getWorldArea()) > 1)
				{
					continue;
				}

				amountInArea++;
			}

			if (amountInArea > bestAmountInArea
					|| (amountInArea == bestAmountInArea && distanceToPlayer < bestDistanceToPlayer))
			{
				bestNibbler = infernoNPC;

				// update tracked values
				bestAmountInArea = amountInArea;
				bestDistanceToPlayer = distanceToPlayer;
			}
		}

		if (bestNibbler != null)
		{
			centralNibbler = bestNibbler;
		}
	}
	public void activatePrayer(WidgetInfo widgetInfo)
	{
		Widget prayer_widget = client.getWidget(widgetInfo);

		if (prayer_widget == null)
		{
			return;
		}

		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return;
		}

		clientThread.invoke(() ->
				client.invokeMenuAction(
						"Activate",
						prayer_widget.getName(),
						1,
						MenuAction.CC_OP.getId(),
						prayer_widget.getItemId(),
						prayer_widget.getId()
				)
		);
	}
}