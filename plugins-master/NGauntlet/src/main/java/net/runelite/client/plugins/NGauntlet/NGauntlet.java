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
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.GroundObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
//import java.util.*;

@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
		name = "NGauntlet",
		description = "Automatic Gauntlet.",
		tags = {"ztd","numb","gauntlet", "hunllef"},
		enabledByDefault = false
)

public class NGauntlet extends Plugin
{
	private final Set<Integer> ORES = Set.of(ItemID.CORRUPTED_ORE, ItemID.CRYSTAL_ORE);
	private final Set<Integer> LINUM = Set.of(ItemID.LINUM_TIRINUM_23876, ItemID.LINUM_TIRINUM);
	private final Set<Integer> BARK = Set.of(ItemID.PHREN_BARK_23878, ItemID.PHREN_BARK);
	private final Set<Integer> HERBS = Set.of(ItemID.GRYM_LEAF, ItemID.GRYM_LEAF_23875);
	public List<WorldPoint> gauntPath = new LinkedList<WorldPoint>();
	private final Set<Integer> HALBERDS = Set.of(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED);
	private final Set<Integer> BOWS = Set.of(ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED);
	private final Set<Integer> STAVES = Set.of(ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED);

	private final Set<Integer> HALBERDS_NOBASIC = Set.of(ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED);
	private final Set<Integer> BOWS_NOBASIC = Set.of(ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED);
	private final Set<Integer> STAVES_NOBASIC = Set.of(ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED);

	private final Set<Integer> HALBERDS_PERFECT = Set.of(ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_PERFECTED);
	private final Set<Integer> BOWS_PERFECT = Set.of(ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CORRUPTED_BOW_PERFECTED);
	private final Set<Integer> STAVES_PERFECT = Set.of(ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_PERFECTED);

	private final Set<Integer> ORBS = Set.of(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB);
	private final Set<Integer> BOWSTRINGS = Set.of(ItemID.CORRUPTED_BOWSTRING, ItemID.CRYSTALLINE_BOWSTRING);

	private final Set<Integer> POTIONS_FULL = Set.of(ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);
	private final Set<Integer> POTIONS_ALL = Set.of(ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);



	private final Set<Integer> WEAPON_FRAMES = Set.of(ItemID.WEAPON_FRAME, ItemID.WEAPON_FRAME_23871);
	private final Set<Integer> WEAPON_EXTRAS = Set.of(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB, ItemID.CORRUPTED_BOWSTRING, ItemID.CRYSTALLINE_BOWSTRING);
	private final Set<Integer> ATTUNED_BOWS = Set.of(ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CORRUPTED_BOW_ATTUNED);
	private final Set<Integer> SCEPTRES = Set.of(ItemID.CRYSTAL_SCEPTRE, ItemID.CORRUPTED_SCEPTRE);
	private final Set<Integer> HELMS = Set.of(ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC);
	private final Set<Integer> BODIES = Set.of(ItemID.CRYSTAL_BODY_ATTUNED, ItemID.CORRUPTED_BODY_ATTUNED);
	private final Set<Integer> LEGS = Set.of(ItemID.CRYSTAL_LEGS_ATTUNED, ItemID.CORRUPTED_LEGS_ATTUNED);
	private final Set<Integer> SHARDS = Set.of(ItemID.CRYSTAL_SHARDS, ItemID.CORRUPTED_SHARDS);
	private final Set<Integer> FISH = Set.of(ItemID.RAW_PADDLEFISH, ItemID.PADDLEFISH, ItemID.CORRUPTED_PADDLEFISH, ItemID.CRYSTAL_PADDLEFISH);
	private final Set<Integer> COOKED_FISH = Set.of(ItemID.PADDLEFISH, ItemID.CORRUPTED_PADDLEFISH, ItemID.CRYSTAL_PADDLEFISH);
	private final Set<Integer> SAFE_TILES = Set.of(36149, 36046);
	private final Set<Integer> DANGEROUS_TILES = Set.of(36048, 36047, 36151, 36150);
	private final Set<Integer> EMPTY_VIALS = Set.of(ItemID.VIAL_23879, ItemID.VIAL_23839);
	private final Set<Integer> WATER_FILLED_VIALS = Set.of(ItemID.WATERFILLED_VIAL);
	private boolean hasOres = false;
	private boolean hasHerbs = false;
	private boolean hasBark = false;
	private boolean hasLinum = false;
	private boolean firstWeapon = true;
	private boolean hasFish = false;
	private boolean hasWeapons = false;
	private boolean hasWeaponFrames = false;
	private boolean hasArmour = false;
	private boolean hasArmourSupplies = false;
	private boolean hasWeaponExtra = false;

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

	NGauntletState state = null;
	int timeout=0;

	private static final Set<Integer> MELEE_ANIM_IDS = Set.of(
		ONEHAND_STAB_SWORD_ANIMATION, ONEHAND_SLASH_SWORD_ANIMATION,
		ONEHAND_SLASH_AXE_ANIMATION, ONEHAND_CRUSH_PICKAXE_ANIMATION,
		ONEHAND_CRUSH_AXE_ANIMATION, UNARMED_PUNCH_ANIMATION,
		UNARMED_KICK_ANIMATION, ONEHAND_STAB_HALBERD_ANIMATION,
		ONEHAND_SLASH_HALBERD_ANIMATION
	);

	private static final Set<Integer> ATTACK_ANIM_IDS = new HashSet<>();

	static {
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

	static {
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
	String[] values;
	String lootnames = "shards,frame,ore,tirinum,bark,leaf,bowstring,orb,spike,teleport";

	private void reset() throws IOException {
		start = false;
		if (!started) {
			if (utils.util()) {
				started = true;
			}
		}
		inHunllef = false;
		loot.clear();
		values = lootnames.toLowerCase().split("\\s*,\\s*");
		if (!lootnames.isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
		}
		inGauntlet = false;
		hunllef = null;
		wrongAttackStyle = false;
		switchWeapon = false;
		hasPotions = false;
		hasOres = false;
		hasHerbs = false;
		hasBark = false;
		hasLinum = false;
		firstWeapon = true;
		hasFish = false;
		hasWeapons = false;
		hasWeaponFrames = false;
		hasArmour = false;
		hasArmourSupplies = false;
		hasWeaponExtra = false;
		hasWeaponSupplies = false;
		entitySets.forEach(Set::clear);
	}

	private boolean inGauntlet = false;
	private boolean start = false;

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) throws IOException {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NGauntletConfig")) {
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
		if (isGauntletVarbitSet())
		{
			initGauntlet();
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
		else if (isGauntletVarbitSet())
		{
			if (!inGauntlet)
			{
				initGauntlet();
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
	public void PrayerSwap() {
		final HeadIcon currentIcon = hunllef.getNpc().getComposition().getOverheadIcon();
		//if (currentIcon == null) {
		//	utils.sendGameMessage("Overhead is null");
		//	return;
		//}
		WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED);
		//if (AllWeapons == null){
		///	utils.sendGameMessage("No weapon found");
		/////	return;
		//}
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
	private boolean hasWeaponSupplies = false;
	private boolean hasPotions = true;
	@Subscribe
	private void onClientTick(ClientTick event) throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			reset();
			return;
		}
		if (!inGauntlet) {
			return;
		}
		if (!hasFish && utils.inventoryItemContainsAmount(COOKED_FISH, config.foodAmt(), false, false)) {
			hasFish = true;
		}
		if (hunllefSpawnLocation == null && utils.findNearestNpc("Hunllef") != null) {
			hunllefSpawnLocation = utils.findNearestNpc("Hunllef").getWorldLocation();
		}
		if (!hasPotions && utils.inventoryItemContainsAmount(POTIONS_ALL, 3, false, false)) {
			hasPotions = true;
		}
		if (config.AttunedArmour()) {
			if (hasBark && hasLinum && hasOres && utils.inventoryItemContainsAmount(ItemID.CRYSTAL_SHARDS, hasWeapons ? 250 : 500, true, false)) {
				hasArmourSupplies = true;
			}
			if (hasBark && hasLinum && hasOres && utils.inventoryItemContainsAmount(ItemID.CORRUPTED_SHARDS, hasWeapons ? 250 : 500, true, false)) {
				hasArmourSupplies = true;
			}
			if (utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_BODY_ATTUNED)) && utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_LEGS_ATTUNED)) && utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_HELM_BASIC))) {
				hasArmour = true;
			}
			if (utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_BODY_ATTUNED)) && utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_LEGS_ATTUNED)) && utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_HELM_BASIC))) {
				hasArmour = true;
			}
			if (utils.inventoryContains(ItemID.CRYSTAL_BODY_ATTUNED) && utils.inventoryContains(ItemID.CRYSTAL_LEGS_ATTUNED) && utils.inventoryContains(ItemID.CRYSTAL_HELM_BASIC)) {
				hasArmour = true;
			}
			if (utils.inventoryContains(ItemID.CORRUPTED_BODY_ATTUNED) && utils.inventoryContains(ItemID.CORRUPTED_LEGS_ATTUNED) && utils.inventoryContains(ItemID.CORRUPTED_HELM_BASIC)) {
				hasArmour = true;
			}
		}
		if (!config.AttunedArmour()) {
			if (hasBark && hasLinum && hasOres && utils.inventoryItemContainsAmount(ItemID.CRYSTAL_SHARDS, hasWeapons ? 250 : 400, true, false)) {
				hasArmourSupplies = true;																					// 500
			}
			if (hasBark && hasLinum && hasOres && utils.inventoryItemContainsAmount(ItemID.CORRUPTED_SHARDS, hasWeapons ? 250 : 400, true, false)) {
				hasArmourSupplies = true;																					//	500
			}
			if (utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_BODY_BASIC)) && utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_LEGS_BASIC)) && utils.isItemEquipped(Collections.singleton(ItemID.CRYSTAL_HELM_BASIC))) {
				hasArmour = true;
			}
			if (utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_BODY_BASIC)) && utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_LEGS_BASIC)) && utils.isItemEquipped(Collections.singleton(ItemID.CORRUPTED_HELM_BASIC))) {
				hasArmour = true;
			}
			if (utils.inventoryContains(ItemID.CRYSTAL_BODY_BASIC) && utils.inventoryContains(ItemID.CRYSTAL_LEGS_BASIC) && utils.inventoryContains(ItemID.CRYSTAL_HELM_BASIC)) {
				hasArmour = true;
			}
			if (utils.inventoryContains(ItemID.CORRUPTED_BODY_BASIC) && utils.inventoryContains(ItemID.CORRUPTED_LEGS_BASIC) && utils.inventoryContains(ItemID.CORRUPTED_HELM_BASIC)) {
				hasArmour = true;
			}
		}
		//Armour
		/////////
		//Weapons
		if (config.PerfectWeapons()) {
			if (hasWeaponFrames && utils.isItemEquipped(BOWS_NOBASIC) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING) && utils.inventoryContains(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB) && utils.inventoryItemContainsAmount(SHARDS, hasArmour ? 250 : 400, true, false)) {
				hasWeaponSupplies = true;
			}
			if (hasWeaponFrames && utils.inventoryContains(BOWS_NOBASIC) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING) && utils.inventoryContains(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB) && utils.inventoryItemContainsAmount(SHARDS, hasArmour ? 250 : 400, true, false)) {
				hasWeaponSupplies = true;
			}
			if (utils.inventoryItemContainsAmount(WEAPON_FRAMES, 2, false, false)) {
				hasWeaponFrames = true;
			}
			if (utils.inventoryItemContainsAmount(WEAPON_EXTRAS, 2, false, false)) {
				hasWeaponExtra = true;
			}
			if (utils.isItemEquipped(STAVES) && utils.inventoryContains(WEAPON_FRAMES)) {
				hasWeaponFrames = true;
			}
			if (utils.isItemEquipped(BOWS) && utils.inventoryContains(WEAPON_FRAMES)) {
				hasWeaponFrames = true;
			}
			if (utils.inventoryContains(BOWS_PERFECT) && utils.isItemEquipped(STAVES_PERFECT)) {
				hasWeapons = true;
			}
			if (utils.inventoryContains(STAVES_PERFECT) && utils.isItemEquipped(BOWS_PERFECT)) {
				hasWeapons = true;
			}
		}
		if (!config.PerfectWeapons()) {
			if (hasWeaponFrames && utils.inventoryContains(BOWS_NOBASIC) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING, ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB) && utils.inventoryItemContainsAmount(SHARDS, hasArmour ? 250 : 400, true, false)) {
				hasWeaponSupplies = true;
			}
			if (hasWeaponFrames && utils.isItemEquipped(BOWS_NOBASIC) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING, ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB) && utils.inventoryItemContainsAmount(SHARDS, hasArmour ? 250 : 400, true, false)) {
				hasWeaponSupplies = true;
			}
			if (utils.inventoryItemContainsAmount(WEAPON_FRAMES, 2, false, false)) {
				hasWeaponFrames = true;
			}
			if (utils.inventoryItemContainsAmount(WEAPON_EXTRAS, 1, false, false)) {
				hasWeaponExtra = true;
			}
			if (utils.isItemEquipped(STAVES) && utils.inventoryContains(WEAPON_FRAMES)) {
				hasWeaponFrames = true;
			}
			if (utils.isItemEquipped(BOWS) && utils.inventoryContains(WEAPON_FRAMES)) {
				hasWeaponFrames = true;
			}
			if (utils.inventoryContains(BOWS_PERFECT) && utils.isItemEquipped(STAVES_NOBASIC)) {
				hasWeapons = true;
			}
			if (utils.isItemEquipped(BOWS_PERFECT) && utils.inventoryContains(STAVES_NOBASIC)) {
				hasWeapons = true;
			}
			if (utils.inventoryContains(BOWS_NOBASIC) && utils.isItemEquipped(STAVES_PERFECT)) {
				hasWeapons = true;
			}
			if (utils.isItemEquipped(BOWS_NOBASIC) && utils.inventoryContains(STAVES_PERFECT)) {
				hasWeapons = true;
			}

		}
		//Weapons
		//////////
		//Armour Supplies
		if (utils.inventoryItemContainsAmount(ORES, config.AttunedArmour() ? 6 : 3, false, false)) {
			hasOres = true;
		}
		if (utils.inventoryItemContainsAmount(LINUM, config.AttunedArmour() ? 6 : 3, false, false)) {
			hasLinum = true;
		}
		if (utils.inventoryItemContainsAmount(BARK, config.AttunedArmour() ? 6 : 3, false, false)) {
			hasBark = true;
		}
		if (utils.inventoryItemContainsAmount(HERBS, 3, false, false)) {
			hasHerbs = true;	//3 herbs
		}
		if (utils.inventoryContains(HERBS) && utils.inventoryItemContainsAmount(POTIONS_ALL, 2, false, false)) {
			hasHerbs = true;	//1 herb 2 pots
		}
		if (utils.inventoryItemContainsAmount(HERBS, 2, false, false) && utils.inventoryContains(POTIONS_ALL)) {
			hasHerbs = true;	//2 herbs 1 pot
		}
		if (firstWeapon && utils.inventoryContains(ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CORRUPTED_BOW_ATTUNED)) {
			firstWeapon = false;
		}
		if (firstWeapon && utils.isItemEquipped(ATTUNED_BOWS)) {
			firstWeapon = false;
		}

	}
	private WorldPoint hunllefSpawnLocation = null;
	private WorldArea LOBBY = new WorldArea(new WorldPoint(3025, 6116, 1), new WorldPoint(3040, 6130, 1));
	@Subscribe
	private void onGameTick(GameTick event) throws IOException {
		if (!started) {
			if (utils.util()) {
				started = true;
			}
			reset();
			return;
		}
		//if (!start) {
		//	return;
		//}

			//if (hunllef == null) {
				//return;
			//}
			hunllef.CycleTicks();
			//NPC npc = hunllef.getNpc();
			//if (npc == null || npc.isDead()) {
				//return;
			//}
			Hunllef.AttackPhase phase = hunllef.getAttackPhase();
			//if (phase == null) {
				//return;
			//}
			Prayer prayer = phase.getPrayer();
			//if (prayer == null) {
				//return;
			//}

		if (inHunllef) {
			if (client.getVar(prayer.getVarbit()) == 0) {
				activatePrayer(prayer);
			}
			//^^^^overheads
			if (config.Piety() && utils.isItemEquipped(HALBERDS) && !client.isPrayerActive(Prayer.PIETY)) {
				activatePrayer(Prayer.PIETY);
			}
			if (!config.Piety() && utils.isItemEquipped(HALBERDS) && !client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
				activatePrayer(Prayer.ULTIMATE_STRENGTH);
			}
			if (!config.Piety() && utils.isItemEquipped(HALBERDS) && !client.isPrayerActive(Prayer.INCREDIBLE_REFLEXES)) {
				activatePrayer(Prayer.INCREDIBLE_REFLEXES);
			}
			if (config.Rigour() && utils.isItemEquipped(BOWS) && !client.isPrayerActive(Prayer.RIGOUR)) {
				activatePrayer(Prayer.RIGOUR);
			}
			if (!config.Rigour() && utils.isItemEquipped(BOWS) && !client.isPrayerActive(Prayer.EAGLE_EYE)) {
				activatePrayer(Prayer.EAGLE_EYE);
			}
			if (config.Augury() && utils.isItemEquipped(STAVES) && !client.isPrayerActive(Prayer.AUGURY)) {
				activatePrayer(Prayer.AUGURY);
			}
			if (!config.Augury() && utils.isItemEquipped(STAVES) && !client.isPrayerActive(Prayer.MYSTIC_MIGHT)) {
				activatePrayer(Prayer.MYSTIC_MIGHT);
			}
			//^^^^^offensives
			PrayerSwap();
		}
		if (!inHunllef && inGauntlet) {
			if (utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13, NpcID.CRYSTALLINE_DRAGON) != null || utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13,NpcID.CORRUPTED_DRAGON) != null) {
				if (client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 0) {
					activatePrayer(Prayer.PROTECT_FROM_MAGIC);
				}
			}
			if (utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13, NpcID.CRYSTALLINE_DARK_BEAST) != null || utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13, NpcID.CORRUPTED_DARK_BEAST) != null) {
				if (client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 0) {
					activatePrayer(Prayer.PROTECT_FROM_MISSILES);
				}
			}
			if (utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST) == null && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 9, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN) != null || utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 9,NpcID.CRYSTALLINE_BEAR) != null || utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 9,NpcID.CORRUPTED_BEAR) != null) {
				if (client.getVar(Prayer.PROTECT_FROM_MELEE.getVarbit()) == 0) {
					activatePrayer(Prayer.PROTECT_FROM_MELEE);
				}
			}
			if (utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 13, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) == null) {
				if (client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) != 0) {
					deactivatePrayer(Prayer.PROTECT_FROM_MAGIC);
				}
				if (client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) != 0) {
					deactivatePrayer(Prayer.PROTECT_FROM_MISSILES);
				}
				if (client.getVar(Prayer.PROTECT_FROM_MELEE.getVarbit()) != 0) {
					deactivatePrayer(Prayer.PROTECT_FROM_MELEE);
				}
			}
		}

		if (!inHunllef && client != null && client.getLocalPlayer() != null) {
			state = getState();
			switch (state) {
				case TIMEOUT:
					utils.handleRun(20, 10);
					timeout--;
					break;
				case IDLE:
					utils.handleRun(20, 10);
					//timeout = 1;
					break;
				case IDLE_2:
					timeout = 3;
					break;
			}
		}

		if (inHunllef && client != null && client.getLocalPlayer() != null) {
			state = getStateBoss();
			switch (state) {
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case IDLE:
					timeout = 1;
					break;
				case IDLE_2:
					timeout = 3;
					break;
			}
		}

	}

	private NGauntletState getStateBoss() {
		if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 1, 37339, 37339, 36097, 36095, 35992, 35994) != null) {
			utils.walk(hunllefSpawnLocation);
		}
		if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.healthMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.PADDLEFISH, ItemID.CORRUPTED_PADDLEFISH, ItemID.CRYSTAL_PADDLEFISH);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(Skill.PRAYER) <= config.prayMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getEnergy() <= config.runMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}

		if (utils.findNearestGroundObjectWithin(client.getLocalPlayer().getWorldLocation(), 0, DANGEROUS_TILES) != null) {
			//utils.walk(utils.findNearestGroundObjectAtleastAwayAndNotUnderMe(utils.findNearestNpc("Hunllef").getWorldLocation(), 2, 1, SAFE_TILES).getWorldLocation());
			GroundObject SAFEST_SPOT = findGauntSafeTile(findNearestGroundObject(DANGEROUS_TILES), utils.findNearestNpc("Hunllef").getWorldArea(), 2, SAFE_TILES, utils.findNearestGameObject(37339, 37337, 36095, 36099, 36098, 35995, 35996));//36099, 36103, 36102, 36101, 36100, 37339, 36098, 36097, 36096, 36095, 36094));
			utils.walk(SAFEST_SPOT.getWorldLocation());
			return NGauntletState.IDLE;
		}
		if (utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 3, 9025, 9039) != null) {
			//utils.walk(utils.findNearestGroundObjectAtleastAwayAndNotUnderMe(utils.findNearestNpc("Hunllef").getWorldLocation(), utils.getRandomIntBetweenRange(2, 7), 3, SAFE_TILES).getWorldLocation());
			GroundObject SAFEST_SPOT = findGauntSafeTile(findNearestGroundObject(DANGEROUS_TILES), utils.findNearestNpc("Hunllef").getWorldArea(), 2, SAFE_TILES, utils.findNearestGameObject(37339, 37337, 36095, 36099, 36098, 35995, 35996));//36099, 36103, 36102, 36101, 36100, 37339, 36098, 36097, 36096, 36095, 36094));
			utils.walk(SAFEST_SPOT.getWorldLocation());
			return NGauntletState.IDLE;
		}
		if (client.getLocalPlayer().getInteracting() != utils.findNearestNpc("Hunllef")) {
			utils.attackNPC("Hunllef");
			//return NGauntletState.UNHANDLED_STATE;
		}
		return NGauntletState.UNHANDLED_STATE;
	}
	private boolean FirstAction = true;
	private NGauntletState getState() {
		if (!inHunllef && !inGauntlet && client.getLocalPlayer().getWorldArea().intersectsWith(LOBBY)) {
			if (findOpenableChest(37341) != null){
				GameObject Linum = utils.findNearestGameObject(37341);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObject(37340) != null){
				GameObject Linum = utils.findNearestGameObject(37340);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), config.EnterCorrupted() ? MenuAction.GAME_OBJECT_SECOND_OPTION.getId() : MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
		}

		if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.healthMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.PADDLEFISH, ItemID.CORRUPTED_PADDLEFISH, ItemID.CRYSTAL_PADDLEFISH);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getBoostedSkillLevel(Skill.PRAYER) <= config.prayMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (client.getEnergy() <= config.runMin()) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4);
			if (AllWeapons != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}

		if (client.getLocalPlayer().getAnimation() != -1) {
			return NGauntletState.ANIMATING;
		}
		if (!loot.isEmpty() && !utils.inventoryFull()) {
			utils.lootItem(loot);
			return NGauntletState.IDLE;
		}
		if (hasWeapons && hasArmour && hasFish && hasPotions) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 20, 37339) != null) {
				GameObject Linum = utils.findNearestGameObject(37339);
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}////////
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),20,37339) == null) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
		}
		//Enter boss room
		///////////////
		///////////////
		//Wear stuff
		if (config.AttunedArmour() && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") == null && utils.inventoryContains(ItemID.CRYSTAL_BODY_ATTUNED, ItemID.CORRUPTED_BODY_ATTUNED, ItemID.CRYSTAL_LEGS_ATTUNED, ItemID.CORRUPTED_LEGS_ATTUNED, ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC)) {
			WidgetItem Armour = utils.getItemFromInventory(ItemID.CRYSTAL_BODY_ATTUNED, ItemID.CORRUPTED_BODY_ATTUNED, ItemID.CRYSTAL_LEGS_ATTUNED, ItemID.CORRUPTED_LEGS_ATTUNED, ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC);
			clientThread.invoke(() -> client.invokeMenuAction("", "", Armour.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Armour.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;	//Armour not equipped ^
		}
		if (!config.AttunedArmour() && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") == null && utils.inventoryContains(ItemID.CRYSTAL_BODY_BASIC, ItemID.CORRUPTED_BODY_BASIC, ItemID.CRYSTAL_LEGS_BASIC, ItemID.CORRUPTED_LEGS_BASIC, ItemID.CRYSTAL_BODY_ATTUNED, ItemID.CORRUPTED_BODY_ATTUNED, ItemID.CRYSTAL_LEGS_ATTUNED, ItemID.CORRUPTED_LEGS_ATTUNED, ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC)) {
			WidgetItem Armour = utils.getItemFromInventory(ItemID.CRYSTAL_BODY_BASIC, ItemID.CORRUPTED_BODY_BASIC, ItemID.CRYSTAL_LEGS_BASIC, ItemID.CORRUPTED_LEGS_BASIC, ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC);
			clientThread.invoke(() -> client.invokeMenuAction("", "", Armour.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), Armour.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;	//Armour not equipped ^
		}

		if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") == null && utils.isItemEquipped(SCEPTRES) && utils.inventoryContains(STAVES_NOBASIC)) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED);
			clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;	//Wearing sceptre ^
		}
		if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") == null && utils.isItemEquipped(SCEPTRES) && utils.inventoryContains(BOWS_NOBASIC)) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED);
			clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;	//Wearing sceptre ^
		}
		if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") == null && utils.isItemEquipped(SCEPTRES) && utils.inventoryContains(HALBERDS_NOBASIC)) {
			WidgetItem AllWeapons = utils.getItemFromInventory(ItemID.CRYSTAL_HALBERD_BASIC, ItemID.CRYSTAL_HALBERD_ATTUNED, ItemID.CRYSTAL_HALBERD_PERFECTED, ItemID.CORRUPTED_HALBERD_BASIC, ItemID.CORRUPTED_HALBERD_ATTUNED, ItemID.CORRUPTED_HALBERD_PERFECTED, ItemID.CORRUPTED_BOW_BASIC, ItemID.CORRUPTED_BOW_ATTUNED, ItemID.CORRUPTED_BOW_PERFECTED, ItemID.CORRUPTED_STAFF_BASIC, ItemID.CORRUPTED_STAFF_ATTUNED, ItemID.CORRUPTED_STAFF_PERFECTED, ItemID.CRYSTAL_BOW_BASIC, ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CRYSTAL_BOW_PERFECTED, ItemID.CRYSTAL_STAFF_BASIC, ItemID.CRYSTAL_STAFF_ATTUNED, ItemID.CRYSTAL_STAFF_PERFECTED);
			clientThread.invoke(() -> client.invokeMenuAction("", "", AllWeapons.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), AllWeapons.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;	//Wearing sceptre ^
		}
		//Wear stuff
		/////////////////
		/////////////////
		//Potions
		if (!hasPotions) {
			if (!firstWeapon) {
				if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
					if (utils.inventoryItemContainsAmount(EMPTY_VIALS, 2, false, true)) {
						utils.typeString("2");
						return NGauntletState.IDLE;
					}
					if (utils.inventoryItemContainsAmount(EMPTY_VIALS, 1, false, true)) {
						utils.typeString("2");
						return NGauntletState.IDLE;
					}
					if (!utils.inventoryContains(WATER_FILLED_VIALS) && !utils.inventoryContains(EMPTY_VIALS)) {
						utils.typeString("2");
						return NGauntletState.IDLE;
					}
				}
				if (utils.inventoryContains(ItemID.WATERFILLED_VIAL) && utils.inventoryContains(ItemID.GRYM_LEAF_23875)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.GRYM_LEAF_23875, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(ItemID.GRYM_LEAF_23875).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.WATERFILLED_VIAL, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.WATERFILLED_VIAL).getIndex(), WidgetInfo.INVENTORY.getId()));
					//return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.WATERFILLED_VIAL) && utils.inventoryContains(ItemID.GRYM_LEAF)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.GRYM_LEAF, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(ItemID.GRYM_LEAF).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.WATERFILLED_VIAL, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.WATERFILLED_VIAL).getIndex(), WidgetInfo.INVENTORY.getId()));
					//return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.GRYM_POTION_UNF) && !utils.inventoryContains(ItemID.CRYSTAL_DUST_23867, ItemID.CORRUPTED_DUST) && utils.inventoryItemContainsAmount(SHARDS, 10, true, false)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.PESTLE_AND_MORTAR_23865, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(ItemID.PESTLE_AND_MORTAR_23865).getIndex(), WidgetInfo.INVENTORY.getId()));
					if (utils.inventoryContains(ItemID.CRYSTAL_SHARDS)) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.CRYSTAL_SHARDS, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.CRYSTAL_SHARDS).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(ItemID.CORRUPTED_SHARDS)) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.CORRUPTED_SHARDS, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.CORRUPTED_SHARDS).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					//return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.GRYM_POTION_UNF) && utils.inventoryItemContainsAmount(ItemID.CRYSTAL_DUST_23867, 10, true, false)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.GRYM_POTION_UNF, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(ItemID.GRYM_POTION_UNF).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.CRYSTAL_DUST_23867, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.CRYSTAL_DUST_23867).getIndex(), WidgetInfo.INVENTORY.getId()));
					//return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.GRYM_POTION_UNF) && utils.inventoryItemContainsAmount(ItemID.CORRUPTED_DUST, 10, true, false)) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.GRYM_POTION_UNF, MenuAction.ITEM_USE.getId(), utils.getInventoryWidgetItem(ItemID.GRYM_POTION_UNF).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", ItemID.CORRUPTED_DUST, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), utils.getInventoryWidgetItem(ItemID.CORRUPTED_DUST).getIndex(), WidgetInfo.INVENTORY.getId()));
					//return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.VIAL_23879) && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 1, "Water Pump") == null) {
					GameObject Linum = utils.findNearestGameObject("Water Pump");
					clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.VIAL_23879) && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 2, "Water Pump") != null) {
					return NGauntletState.IDLE;    ///// < Just wait bro.... lmao
				}
				if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 100, true, false) && !utils.inventoryContains(EMPTY_VIALS) && !utils.inventoryContains(WATER_FILLED_VIALS) && !utils.inventoryContains(POTIONS_ALL)) {
					WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
					clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
					return NGauntletState.IDLE;
				}
				if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null && utils.inventoryItemContainsAmount(SHARDS, 100, true, false) && !utils.inventoryContains(EMPTY_VIALS) && !utils.inventoryContains(WATER_FILLED_VIALS) && !utils.inventoryContains(POTIONS_ALL)) {
					GameObject Linum = utils.findNearestGameObject("Singing Bowl");
					clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
					utils.sendGameMessage("MAKING POTIONS");
					return NGauntletState.IDLE;
				}
			}
		}
		//Potions
		///////




		if (hasWeapons && hasArmour && !hasFish) {
			if (utils.inventoryItemContainsAmount(ItemID.RAW_PADDLEFISH, config.foodAmt(), false, false) && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Range") == null) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.inventoryItemContainsAmount(ItemID.RAW_PADDLEFISH, config.foodAmt(), false, false) && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Range") != null) {
				GameObject Linum = utils.findNearestGameObject("Range");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
			if (!utils.inventoryItemContainsAmount(FISH, config.foodAmt(), false, false) && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 100, "Fishing Spot") != null) {
				GameObject Linum = utils.findNearestGameObject("Fishing Spot");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
		}
		//Food
		/////////////////

		if (hasBark && utils.inventoryContains(ItemID.CRYSTAL_AXE_23862, ItemID.CORRUPTED_AXE)) {
			WidgetItem TOOLS = utils.getItemFromInventory(ItemID.CRYSTAL_AXE_23862, ItemID.CORRUPTED_AXE);
			clientThread.invoke(() -> client.invokeMenuAction("", "", TOOLS.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), TOOLS.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;
		}
		if (hasOres && utils.inventoryContains(ItemID.CRYSTAL_PICKAXE_23863, ItemID.CORRUPTED_PICKAXE)) {
			WidgetItem TOOLS = utils.getItemFromInventory(ItemID.CRYSTAL_PICKAXE_23863, ItemID.CORRUPTED_PICKAXE);
			clientThread.invoke(() -> client.invokeMenuAction("", "", TOOLS.getId(), MenuAction.ITEM_FIFTH_OPTION.getId(), TOOLS.getIndex(), WidgetInfo.INVENTORY.getId()));
			return NGauntletState.IDLE;
		}

		//////
		//Weapons
		if (firstWeapon && utils.inventoryContains(ItemID.WEAPON_FRAME, ItemID.WEAPON_FRAME_23871, ItemID.CRYSTAL_BOW_BASIC, ItemID.CORRUPTED_BOW_BASIC)) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CORRUPTED_BOW_ATTUNED)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 150, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null && utils.inventoryItemContainsAmount(SHARDS, 150, true, false)) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				utils.sendGameMessage("MAKING FIRST WEAPON");
				return NGauntletState.IDLE;
			}
		}
		if (firstWeapon && utils.isItemEquipped(BOWS)) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_BOW_ATTUNED, ItemID.CORRUPTED_BOW_ATTUNED) && !utils.isItemEquipped(BOWS_NOBASIC)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 150, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null && utils.inventoryItemContainsAmount(SHARDS, 150, true, false)) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				utils.sendGameMessage("MAKING FIRST WEAPON");
				return NGauntletState.IDLE;
			}
		}
		/*if (!firstWeapon && utils.isItemEquipped(ATTUNED_BOWS) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING) && utils.inventoryItemContainsAmount(SHARDS,150,true,false)) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (utils.isItemEquipped(ATTUNED_BOWS)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 150, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
		}*/

		if (!firstWeapon && hasWeaponSupplies && !hasWeapons) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB) && !utils.inventoryContains(ItemID.CRYSTAL_STAFF_PERFECTED, ItemID.CORRUPTED_STAFF_PERFECTED)) {
					utils.typeString("7");
					return NGauntletState.IDLE;
				}
				if (utils.isItemEquipped(ATTUNED_BOWS) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ATTUNED_BOWS) && utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
				if (utils.isItemEquipped(BOWS_PERFECT) && !utils.inventoryContains(STAVES_NOBASIC)) {
					utils.typeString("7");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				utils.sendGameMessage("MAKING SECOND WEAPON");
				return NGauntletState.IDLE;
			}
		}

		/*if (hasWeaponFrames && hasWeaponExtra && !hasWeapons) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.CRYSTAL_SPIKE, ItemID.CORRUPTED_SPIKE)) {
					utils.typeString("6");
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.CRYSTAL_ORB, ItemID.CORRUPTED_ORB)) {
					utils.typeString("7");
					return NGauntletState.IDLE;
				}
				if (utils.inventoryContains(ItemID.CRYSTALLINE_BOWSTRING, ItemID.CORRUPTED_BOWSTRING)) {
					utils.typeString("8");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 200, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				return NGauntletState.IDLE;
			}
		}*/
		/*if (!utils.inventoryContains(ItemID.WATERFILLED_VIAL, ItemID.VIAL_23879, ItemID.EGNIOL_POTION_1, ItemID.EGNIOL_POTION_2, ItemID.EGNIOL_POTION_3, ItemID.EGNIOL_POTION_4)) {
			utils.typeString("2");
			return NGauntletState.IDLE;
		}*/ ////// TODO: FIX ^
		if (config.AttunedArmour() && hasArmourSupplies && !hasArmour) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC) && !utils.isItemEquipped(HELMS)) {
					utils.typeString("3");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_BODY_ATTUNED, ItemID.CORRUPTED_BODY_ATTUNED) && !utils.isItemEquipped(BODIES)) {
					utils.typeString("4");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_LEGS_ATTUNED, ItemID.CORRUPTED_LEGS_ATTUNED) && !utils.isItemEquipped(LEGS)) {
					utils.typeString("5");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 300, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				utils.sendGameMessage("MAKING ARMOUR");
				return NGauntletState.IDLE;
			}
		}
		////////////////////
		//////////////////
		//////////////////////
		///////////////////////
		if (!config.AttunedArmour() && hasArmourSupplies && !hasArmour) {
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),2,"Singing Bowl") != null) {
				if (!utils.inventoryContains(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL)) {
					utils.typeString("1");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_HELM_BASIC, ItemID.CORRUPTED_HELM_BASIC) && !utils.isItemEquipped(HELMS)) {
					utils.typeString("3");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_BODY_BASIC, ItemID.CORRUPTED_BODY_BASIC) && !utils.isItemEquipped(BODIES)) {
					utils.typeString("4");
					return NGauntletState.IDLE;
				}
				if (!utils.inventoryContains(ItemID.CRYSTAL_LEGS_BASIC, ItemID.CORRUPTED_LEGS_BASIC) && !utils.isItemEquipped(LEGS)) {
					utils.typeString("5");
					return NGauntletState.IDLE;
				}
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") == null && utils.inventoryItemContainsAmount(SHARDS, 300, true, false)) {
				WidgetItem CRYSTAL = utils.getInventoryWidgetItem(ItemID.TELEPORT_CRYSTAL, ItemID.CORRUPTED_TELEPORT_CRYSTAL);
				clientThread.invoke(() -> client.invokeMenuAction("", "",CRYSTAL.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), CRYSTAL.getIndex(), WidgetInfo.INVENTORY.getId()));
				return NGauntletState.IDLE;
			}
			if (utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),10,"Singing Bowl") != null) {
				GameObject Linum = utils.findNearestGameObject("Singing Bowl");
				clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
				utils.sendGameMessage("MAKING ARMOUR");
				return NGauntletState.IDLE;
			}
		}

		if (!utils.inventoryItemContainsAmount(SHARDS,200,true,false) && hasWeaponFrames && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}
		if (!utils.inventoryItemContainsAmount(SHARDS,200,true,false) && hasWeaponExtra && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}
		if (!utils.inventoryItemContainsAmount(SHARDS,200,true,false) && hasArmour && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30, NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}
		if (!hasWeaponExtra && utils.findNearestNpc(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;	//find any in the dungeon because we don't wanna leave em ^
		}
		/*if (!hasWeaponFrames && utils.findNearestNpc(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}*/
		if (!hasArmour && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30,NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}
		if (!hasWeaponFrames && utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 30,NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF) != null) {
			NPC NEAREST = utils.findNearestNpc(NpcID.CRYSTALLINE_BAT, NpcID.CORRUPTED_BAT, NpcID.CRYSTALLINE_BEAR, NpcID.CORRUPTED_BEAR, NpcID.CRYSTALLINE_DARK_BEAST, NpcID.CORRUPTED_DARK_BEAST, NpcID.CRYSTALLINE_DRAGON, NpcID.CORRUPTED_DRAGON, NpcID.CRYSTALLINE_RAT, NpcID.CORRUPTED_RAT, NpcID.CRYSTALLINE_SCORPION, NpcID.CORRUPTED_SCORPION, NpcID.CRYSTALLINE_SPIDER, NpcID.CORRUPTED_SPIDER, NpcID.CRYSTALLINE_UNICORN, NpcID.CORRUPTED_UNICORN, NpcID.CRYSTALLINE_WOLF, NpcID.CORRUPTED_WOLF);
			clientThread.invoke(() -> client.invokeMenuAction("", "", NEAREST.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0));
			return NGauntletState.IDLE;
		}




		if (!hasArmour && !hasLinum && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 30, "Linum Tirinum") != null) {
			GameObject Linum = utils.findNearestGameObject("Linum Tirinum");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		if (!hasArmour && !hasOres && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 30,"Crystal Deposit") != null) {
			GameObject Linum = utils.findNearestGameObject("Crystal Deposit");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		if (!hasArmour && !hasOres && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 30,"Corrupt Deposit") != null) {
			GameObject Linum = utils.findNearestGameObject("Corrupt Deposit");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		if (!hasArmour && !hasBark && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 30,"Phren Roots") != null) {
			GameObject Linum = utils.findNearestGameObject("Phren Roots");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		if (!hasHerbs && !hasPotions && utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 40,"Grym Root") != null) {
			GameObject Linum = utils.findNearestGameObject("Grym Root");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		if (utils.findNearestGameObject("Node") != null) {
			GameObject Linum = utils.findNearestGameObject("Node");
			clientThread.invoke(() -> client.invokeMenuAction("", "", Linum.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), Linum.getSceneMinLocation().getX(), Linum.getSceneMinLocation().getY()));
			return NGauntletState.IDLE;
		}
		return NGauntletState.UNHANDLED_STATE;
	}
	List<String> lootableItems = new ArrayList<>();
	List<TileItem> loot = new ArrayList<>();

	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {
			if (itemName.contains("bark") && !hasBark) {
				loot.add(item);
			}
			else if (itemName.contains("leaf") && !hasHerbs) {
				loot.add(item);
			}
			else if (itemName.contains("ore") && !hasOres) {
				loot.add(item);
			}
			else if (itemName.contains("linum") && !hasLinum) {
				loot.add(item);
			}
			else if (itemName.contains("frame") && !hasWeaponFrames) {
				loot.add(item);
			}
			else if (itemName.contains("orb") && !hasWeaponExtra) {
				loot.add(item);
			}
			//else if (itemName.contains("spike") && !hasWeaponExtra) {
			//	loot.add(item);
			//}
			else if (itemName.contains("raw") && hasWeapons && hasArmour && !utils.inventoryItemContainsAmount(FISH, config.foodAmt(), false, false)) {
				loot.add(item);
			}
			else if (itemName.contains("bowstring") && !hasWeaponExtra) {
				loot.add(item);
			}
			else if (itemName.contains("shard")) {
				loot.add(item);
			}
			else if (itemName.contains("teleport")) {
				loot.add(item);
			}
			else {
				//loot.add(item);
			}
		}

	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		loot.remove(event.getItem());
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

	public void deactivatePrayer(Prayer prayer)
	{
		if (prayer == null)
		{
			return;
		}

		if (!client.isPrayerActive(prayer))
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

		clientThread.invoke(() -> client.invokeMenuAction("", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId()));
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
	private void initGauntlet() { inGauntlet = true; }

	private boolean isGauntletVarbitSet()
	{
		return client.getVarbitValue(9178) == 1;
	}

	private boolean isHunllefVarbitSet()
	{
		return client.getVarbitValue(9177) == 1;
	}


	@Nullable
	public GroundObject findNearestGroundObject(Collection<Integer> ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GroundObjectQuery()
				.idEquals(ids)
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}
	@Nullable
	public GameObject findOpenableChest(int ids)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GameObjectQuery()
				.idEquals(ids)
				.actionEquals("Open")
				.result(client)
				.nearestTo(client.getLocalPlayer());
	}
	@Nullable
	public GroundObject findGauntSafeTile(GroundObject dangerousTiles, WorldArea worldArea, int distFrom, Collection<Integer> ids, GameObject OBJECT)
	{
		assert client.isClientThread();

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		return new GroundObjectQuery()
				.idEquals(ids)
				.filter(obj -> !obj.getWorldLocation().toWorldArea().intersectsWith(dangerousTiles.getWorldLocation().toWorldArea()) && !obj.getWorldLocation().toWorldArea().intersectsWith(OBJECT.getWorldLocation().toWorldArea()) && !obj.getWorldLocation().toWorldArea().intersectsWith(worldArea) && !obj.getWorldLocation().toWorldArea().intersectsWith(client.getLocalPlayer().getWorldArea())  && obj.getWorldLocation().toWorldArea().distanceTo(worldArea) >= distFrom  && obj.getWorldLocation().toWorldArea().distanceTo(client.getLocalPlayer().getWorldArea()) >= distFrom)
				.result(client)
				.nearestTo(client.getLocalPlayer());//Arrays.stream(ids).anyMatch(i -> i == item.getId()
	}
}
