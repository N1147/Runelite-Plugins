/*
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, kThisIsCvpv <https://github.com/kThisIsCvpv>
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
 * Copyright (c) 2019, kyle <https://github.com/xKylee>
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

package net.runelite.client.plugins.NGauntlet;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "NGauntlet",
	enabledByDefault = false,
	description = "Gauntlet prayer swapper.",
	tags = {"numb","gauntlet"}
)
@Singleton
public class NGauntlet extends Plugin
{
	public static final int ONEHAND_SLASH_AXE_ANIMATION = 395;
	public static final int ONEHAND_CRUSH_PICKAXE_ANIMATION = 400;
	public static final int ONEHAND_CRUSH_AXE_ANIMATION = 401;
	public static final int UNARMED_PUNCH_ANIMATION = 422;
	public static final int UNARMED_KICK_ANIMATION = 423;
	public static final int BOW_ATTACK_ANIMATION = 426;
	public static final int ONEHAND_STAB_HALBERD_ANIMATION = 428;
	public static final int ONEHAND_SLASH_HALBERD_ANIMATION = 440;
	public static final int ONEHAND_SLASH_SWORD_ANIMATION = 390;
	public static final int ONEHAND_STAB_SWORD_ANIMATION = 386;
	public static final int HIGH_LEVEL_MAGIC_ATTACK = 1167;
	public static final int HUNLEFF_TORNADO = 8418;

	private static final Set<Integer> MELEE_ANIM_IDS = Set.of(
		ONEHAND_STAB_SWORD_ANIMATION, ONEHAND_SLASH_SWORD_ANIMATION,
		ONEHAND_SLASH_AXE_ANIMATION, ONEHAND_CRUSH_PICKAXE_ANIMATION,
		ONEHAND_CRUSH_AXE_ANIMATION, UNARMED_PUNCH_ANIMATION,
		UNARMED_KICK_ANIMATION, ONEHAND_STAB_HALBERD_ANIMATION,
		ONEHAND_SLASH_HALBERD_ANIMATION
	);

	private static final Set<Integer> ATTACK_ANIM_IDS = new HashSet<>();

	static
	{
		ATTACK_ANIM_IDS.addAll(MELEE_ANIM_IDS);
		ATTACK_ANIM_IDS.add(BOW_ATTACK_ANIMATION);
		ATTACK_ANIM_IDS.add(HIGH_LEVEL_MAGIC_ATTACK);
	}

	private static final Set<Integer> PROJECTILE_MAGIC_IDS = Set.of(
		ProjectileID.HUNLLEF_MAGE_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_MAGE_ATTACK
	);

	private static final Set<Integer> PROJECTILE_RANGE_IDS = Set.of(
		ProjectileID.HUNLLEF_RANGE_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_RANGE_ATTACK
	);

	private static final Set<Integer> PROJECTILE_PRAYER_IDS = Set.of(
		ProjectileID.HUNLLEF_PRAYER_ATTACK, ProjectileID.HUNLLEF_CORRUPTED_PRAYER_ATTACK
	);

	private static final Set<Integer> PROJECTILE_IDS = new HashSet<>();

	static
	{
		PROJECTILE_IDS.addAll(PROJECTILE_MAGIC_IDS);
		PROJECTILE_IDS.addAll(PROJECTILE_RANGE_IDS);
		PROJECTILE_IDS.addAll(PROJECTILE_PRAYER_IDS);
	}

	private static final Set<Integer> HUNLLEF_IDS = Set.of(
		NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
		NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
		NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
		NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038
	);

	private static final Set<Integer> UTILITY_IDS = Set.of(
		ObjectID.SINGING_BOWL_35966, ObjectID.SINGING_BOWL_36063,
		ObjectID.RANGE_35980, ObjectID.RANGE_36077,
		ObjectID.WATER_PUMP_35981, ObjectID.WATER_PUMP_36078
	);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private NGauntletConfig config;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PUtils utils;

	@Getter
	private final Set<GameObject> utilities = new HashSet<>();

	private final List<Set<?>> entitySets = Arrays.asList(utilities);

	@Getter
	private Hunllef hunllef;

	@Getter
	@Setter
	private boolean wrongAttackStyle;

	@Getter
	@Setter
	private boolean switchWeapon;

	private boolean inHunllef;

	@Provides
	NGauntletConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(NGauntletConfig.class);
	}

	@Override
	protected void startUp() throws IOException {
		reset();
	}

	@Override
	protected void shutDown() throws IOException {
		reset();
	}
	private boolean started = false;
	private void reset() throws IOException {
		start = false;
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
		inHunllef = false;
		hunllef = null;
		wrongAttackStyle = false;
		switchWeapon = false;

		entitySets.forEach(Set::clear);
	}

	private boolean start = false;

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NGauntlet")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!start) {
				start = true;
				clientThread.invoke(this::pluginEnabled);
			} else {
				reset();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals("NGauntlet"))
		{
			return;
		}

	}

	private void pluginEnabled()
	{
		if (isGauntletVarbitSet())
		{
			addSpawnedEntities();
		}

		if (isHunllefVarbitSet())
		{
			initHunllef();
		}
	}

	@Subscribe
	private void onVarbitChanged(final VarbitChanged event) throws IOException {
		if (isHunllefVarbitSet())
		{
			if (!inHunllef)
			{
				initHunllef();
			}
		}
		else
		{
			if (inHunllef)
			{
				shutDown();
			}
		}
	}

	private final Set<Integer> HALBERDS = Set.of(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED);
	private final Set<Integer> BOWS = Set.of(ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED);
	private final Set<Integer> STAVES = Set.of(ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED);

	public void OffensivePrayers() {
		if (utils.isItemEquipped(HALBERDS) && !client.isPrayerActive(Prayer.PIETY)) {
			activatePrayer(Prayer.PIETY);
		}
		if (utils.isItemEquipped(BOWS) && !client.isPrayerActive(Prayer.RIGOUR)) {
			activatePrayer(Prayer.RIGOUR);
		}
		if (utils.isItemEquipped(STAVES) && !client.isPrayerActive(Prayer.AUGURY)) {
			activatePrayer(Prayer.AUGURY);
		}
	}
	public void PrayerSwap() {
		final HeadIcon currentIcon = hunllef.getNpc().getComposition().getOverheadIcon();
		if (currentIcon == null) {
			utils.sendGameMessage("Overhead is null");
			return;
		}
		WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED);
		if (AllWeapons == null){
			utils.sendGameMessage("No weapon found");
			return;
		}
		switch (currentIcon) {
			case MELEE:
				if (utils.isItemEquipped(HALBERDS)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
					utils.sendGameMessage("Switching");
				}
				break;
			case RANGED:
				if (utils.isItemEquipped(BOWS)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
					utils.sendGameMessage("Switching");
				}
				break;
			case MAGIC:
				if (utils.isItemEquipped(STAVES)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
					utils.sendGameMessage("Switching");
				}
				break;
		}
	}


	@Subscribe
	private void onGameTick(GameTick event) throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			reset();
			return;
		}

		if (hunllef == null)
		{
			return;
		}

		hunllef.CycleTicks();

		if (!inHunllef) {
			return;
		}

		NPC npc = hunllef.getNpc();

		if (npc == null || npc.isDead()) {
			return;
		}

		Hunllef.AttackPhase phase = hunllef.getAttackPhase();

		if (phase == null) {
			return;
		}

		Prayer prayer = phase.getPrayer();

		if (prayer == null) {
			return;
		}

		if (client.getVar(prayer.getVarbit()) == 0) {
			activatePrayer(prayer);
		}

		if (config.offensive()) {
			if (utils.isItemEquipped(HALBERDS) && !client.isPrayerActive(Prayer.PIETY)) {
				activatePrayer(Prayer.PIETY);
			}
			if (utils.isItemEquipped(BOWS) && !client.isPrayerActive(Prayer.RIGOUR)) {
				activatePrayer(Prayer.RIGOUR);
			}
			if (utils.isItemEquipped(STAVES) && !client.isPrayerActive(Prayer.AUGURY)) {
				activatePrayer(Prayer.AUGURY);
			}
		}

		PrayerSwap();

	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event) throws IOException {
		switch (event.getGameState())
		{
			case LOADING:
				utilities.clear();
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				shutDown();
				break;
		}
	}

	@Subscribe
	private void onWidgetLoaded(final WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.GAUNTLET_TIMER_GROUP_ID)
		{

		}
	}

	@Subscribe
	private void onGameObjectSpawned(final GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();

		final int id = gameObject.getId();

		/*if (RESOURCE_IDS.contains(id))
		{
			resources.add(new Resource(gameObject, skillIconManager, config.resourceIconSize()));
		}
		else*/
		if (UTILITY_IDS.contains(id))
		{
			utilities.add(gameObject);
		}
	}

	@Subscribe
	private void onGameObjectDespawned(final GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();

		final int id = gameObject.getId();

		if (UTILITY_IDS.contains(id))
		{
			utilities.remove(gameObject);
		}
	}

	@Subscribe
	private void onNpcSpawned(final NpcSpawned event)
	{
		final NPC npc = event.getNpc();

		final int id = npc.getId();

		if (HUNLLEF_IDS.contains(id))
		{
			hunllef = new Hunllef(npc, skillIconManager);
		}
	}

	@Subscribe
	private void onNpcDespawned(final NpcDespawned event)
	{
		final NPC npc = event.getNpc();

		final int id = npc.getId();

		if (HUNLLEF_IDS.contains(id))
		{
			hunllef = null;
		}
	}

	@Subscribe
	private void onProjectileSpawned(final ProjectileSpawned event)
	{
		if (hunllef == null)
		{
			return;
		}

		final Projectile projectile = event.getProjectile();

		final int id = projectile.getId();

		if (!PROJECTILE_IDS.contains(id))
		{
			return;
		}

		hunllef.CycleAttackCount();
	}

	/*@Subscribe
	private void onChatMessage(final ChatMessage event)
	{
		final ChatMessageType type = event.getType();

		if (type == ChatMessageType.SPAM || type == ChatMessageType.GAMEMESSAGE)
		{
			//resourceManager.parseChatMessage(event.getMessage());
		}
	}

	@Subscribe
	private void onActorDeath(final ActorDeath event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}
	}*/


	@Subscribe
	private void onAnimationChanged(final AnimationChanged event)
	{
		if (!isHunllefVarbitSet() || hunllef == null)
		{
			return;
		}

		final Actor actor = event.getActor();

		final int animationId = actor.getAnimation();

		if (actor instanceof Player)
		{
			if (!ATTACK_ANIM_IDS.contains(animationId))
			{
				return;
			}

			final boolean validAttack = isAttackAnimationValid(animationId);

			if (validAttack)
			{
				wrongAttackStyle = false;
				hunllef.CyclePlayerAttacks();

				if (hunllef.getPlayerAttackCount() == 1)
				{
					switchWeapon = true;
				}
			}
			else
			{
				wrongAttackStyle = true;
			}
		}
		else if (actor instanceof NPC)
		{
			if (animationId == HUNLEFF_TORNADO)
			{
				hunllef.CycleAttackCount();
			}
		}
	}

	private boolean isAttackAnimationValid(final int animationId)
	{
		final HeadIcon headIcon = hunllef.getNpc().getComposition().getOverheadIcon();

		if (headIcon == null)
		{
			return true;
		}

		switch (headIcon)
		{
			case MELEE:
				if (MELEE_ANIM_IDS.contains(animationId))
				{
					return false;
				}
				break;
			case RANGED:
				if (animationId == BOW_ATTACK_ANIMATION)
				{
					return false;
				}
				break;
			case MAGIC:
				if (animationId == HIGH_LEVEL_MAGIC_ATTACK)
				{
					return false;
				}
				break;
		}

		return true;
	}

	public void activatePrayer(Prayer prayer)
	{
		if (prayer == null)
		{
			return;
		}

		if (client.isPrayerActive(prayer))
		{
			return;
		}

		WidgetInfo widgetInfo = prayer.getWidgetInfo();

		if (widgetInfo == null)
		{
			return;
		}

		Widget prayer_widget = client.getWidget(widgetInfo);

		if (prayer_widget == null)
		{
			return;
		}

		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return;
		}

		clientThread.invoke(() -> client.invokeMenuAction("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId()));
	}

	private void addSpawnedEntities()
	{
		for (final GameObject gameObject : new GameObjectQuery().result(client))
		{
			GameObjectSpawned gameObjectSpawned = new GameObjectSpawned();
			gameObjectSpawned.setTile(null);
			gameObjectSpawned.setGameObject(gameObject);
			onGameObjectSpawned(gameObjectSpawned);
		}

		for (final NPC npc : client.getNpcs())
		{
			onNpcSpawned(new NpcSpawned(npc));
		}
	}

	private void initHunllef()
	{
		inHunllef = true;
	}

	private boolean isGauntletVarbitSet()
	{
		return client.getVarbitValue(9178) == 1;
	}

	private boolean isHunllefVarbitSet()
	{
		return client.getVarbitValue(9177) == 1;
	}
}
