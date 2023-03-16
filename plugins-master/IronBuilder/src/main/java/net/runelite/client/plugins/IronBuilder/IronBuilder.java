package net.runelite.client.plugins.IronBuilder;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.NUtils.PUtils;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.Format;
@Slf4j
@Extension
@PluginDependency(PUtils.class)
@PluginDescriptor(
	name = "Ironman Builder",
	description = "Progressive Starter Ironman Account Builder",
	tags = {"iron","ztd"},
	enabledByDefault = false
)
public class IronBuilder extends Plugin
{
	@Provides
	IronBuilderConfig getConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(IronBuilderConfig.class);
	}

	@Inject
	private IronBuilderConfig config;
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
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private IronBuilderOverlay overlay;

	private boolean started = false;
	private boolean hasitems = false;
	private boolean deposit = false;
	WorldArea LUMBRIDGE = new WorldArea(new WorldPoint(3201, 3203, 0), new WorldPoint(3225, 3231, 0));
	WorldArea LUMBRIDGE_1ST = new WorldArea(new WorldPoint(3203, 3206, 1), new WorldPoint(3220, 3234, 1));
	WorldArea LUMBRIDGE_BANK = new WorldArea(new WorldPoint(3203, 3206, 2), new WorldPoint(3220, 3234, 2));

	@Subscribe
	private void onGameTick(final GameTick event) throws IOException, ClassNotFoundException {
		if (!started && client.getGameState() == GameState.LOGGED_IN) {
			//overlayManager.add(overlay);
			started = true;
			return;
		}
		if (client.getLocalPlayer().getAnimation() != -1) {
			return;
		}
		if (!hasitems && !utils.isBankOpen() && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_BANK)) {
			GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
			if (bankTarget != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
			}
			return;
			//open bank
		}
		if (!hasitems && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE)) {
			GameObject Pool = utils.findNearestGameObject(16671);
			utils.useGameObjectDirect(Pool, 100, MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			return;
			//climb up to 1ST floor
		}
		if (!hasitems && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_1ST)) {
			GameObject Pool = utils.findNearestGameObject(16672);
			utils.useGameObjectDirect(Pool, 100, MenuAction.GAME_OBJECT_SECOND_OPTION.getId());
			return;
			//climb up to 2nd floor
		}
		if (hasitems && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_BANK)) {
			GameObject Pool = utils.findNearestGameObject(16673);
			utils.useGameObjectDirect(Pool, 100, MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
			//climb down from bank
			return;
		}
		if (hasitems && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_1ST)) {
			GameObject Pool = utils.findNearestGameObject(16672);
			utils.useGameObjectDirect(Pool, 100, MenuAction.GAME_OBJECT_THIRD_OPTION.getId());
			//climb down from 1st floor
			return;
		}
		if (!deposit && !hasitems && utils.isBankOpen() && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_BANK)) {
			utils.depositAll();
			utils.depositEquipped();
			deposit = true;
			return;
		}
		if (deposit && !hasitems && utils.isBankOpen() && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE_BANK)) {
			if (utils.inventoryFull()) {
				hasitems = true;
			}
			if (utils.bankContains(20014)){
				utils.withdrawAllItem(20014);
				return;
			}
			if (utils.bankContains(20011)){
				utils.withdrawAllItem(20011);
				return;
			}
			if (utils.bankContains(23336)) {
				utils.withdrawAllItem(23336);
				return;
			}
			if (utils.bankContains(23339)) {
				utils.withdrawAllItem(23339);
				return;
			}
			if (utils.bankContains(23345)) {
				utils.withdrawAllItem(23345);
				return;
			}
			if (utils.bankContains(12424)) {
				utils.withdrawAllItem(12424);
				return;
			}
			if (utils.bankContains(12426)) {
				utils.withdrawAllItem(12426);
				return;
			}
			//TODO: 3a melee / rng / mage
			if (utils.bankContains(ItemID.HARMONISED_ORB)) {
				utils.withdrawAllItem(ItemID.HARMONISED_ORB);
				return;
			}
			if (utils.bankContains(ItemID.HARMONISED_NIGHTMARE_STAFF)) {
				utils.withdrawAllItem(ItemID.HARMONISED_NIGHTMARE_STAFF);
				return;
			}
			if (utils.bankContains(ItemID.ELYSIAN_SPIRIT_SHIELD)) {
				utils.withdrawAllItem(ItemID.ELYSIAN_SPIRIT_SHIELD);
				return;
			}
			if (utils.bankContains(ItemID.ELYSIAN_SIGIL)) {
				utils.withdrawAllItem(ItemID.ELYSIAN_SIGIL);
				return;
			}
			if (utils.bankContains(ItemID.ZARYTE_CROSSBOW)) {
				utils.withdrawAllItem(ItemID.ZARYTE_CROSSBOW);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_PLATEBODY)) {
				utils.withdrawAllItem(ItemID.TORVA_PLATEBODY);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_PLATEBODY_DAMAGED)) {
				utils.withdrawAllItem(ItemID.TORVA_PLATEBODY_DAMAGED);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_FULL_HELM)) {
				utils.withdrawAllItem(ItemID.TORVA_FULL_HELM);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_FULL_HELM_DAMAGED)) {
				utils.withdrawAllItem(ItemID.TORVA_FULL_HELM_DAMAGED);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_PLATELEGS)) {
				utils.withdrawAllItem(ItemID.TORVA_PLATELEGS);
				return;
			}
			if (utils.bankContains(ItemID.TORVA_PLATELEGS_DAMAGED)) {
				utils.withdrawAllItem(ItemID.TORVA_PLATELEGS_DAMAGED);
				return;
			}
			if (utils.bankContains(ItemID.ZARYTE_VAMBRACES)) {
				utils.withdrawAllItem(ItemID.ZARYTE_VAMBRACES);
				return;
			}
			if (utils.bankContains(ItemID.ARCANE_SPIRIT_SHIELD)) {
				utils.withdrawAllItem(ItemID.ARCANE_SPIRIT_SHIELD);
				return;
			}
			if (utils.bankContains(ItemID.ARCANE_SIGIL)) {
				utils.withdrawAllItem(ItemID.ARCANE_SIGIL);
				return;
			}
			if (utils.bankContains(ItemID.SPECTRAL_SPIRIT_SHIELD)) {
				utils.withdrawAllItem(ItemID.SPECTRAL_SPIRIT_SHIELD);
				return;
			}
			if (utils.bankContains(ItemID.SPECTRAL_SIGIL)) {
				utils.withdrawAllItem(ItemID.SPECTRAL_SIGIL);
				return;
			}
			if (utils.bankContains(ItemID.TWISTED_BOW)) {
				utils.withdrawAllItem(ItemID.TWISTED_BOW);
				return;
			}
			if (utils.bankContains(ItemID.HOLY_SCYTHE_OF_VITUR)) {
				utils.withdrawAllItem(ItemID.HOLY_SCYTHE_OF_VITUR);
				return;
			}
			if (utils.bankContains(ItemID.SANGUINE_SCYTHE_OF_VITUR)) {
				utils.withdrawAllItem(ItemID.SANGUINE_SCYTHE_OF_VITUR);
				return;
			}
			if (utils.bankContains(ItemID.SCYTHE_OF_VITUR)) {
				utils.withdrawAllItem(ItemID.SCYTHE_OF_VITUR);
				return;
			}
			if (utils.bankContains(ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED)) {
				utils.withdrawAllItem(ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED);
				return;
			}
			if (utils.bankContains(ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED)) {
				utils.withdrawAllItem(ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED);
				return;
			}
			if (utils.bankContains(ItemID.SCYTHE_OF_VITUR_UNCHARGED)) {
				utils.withdrawAllItem(ItemID.SCYTHE_OF_VITUR_UNCHARGED);
				return;
			}
			if (utils.bankContains(ItemID.VOLATILE_ORB)) {
				utils.withdrawAllItem(ItemID.VOLATILE_ORB);
				return;
			}
			if (utils.bankContains(ItemID.VOLATILE_NIGHTMARE_STAFF)) {
				utils.withdrawAllItem(ItemID.VOLATILE_NIGHTMARE_STAFF);
				return;
			}
			if (utils.bankContains(ItemID.ELDRITCH_ORB)) {
				utils.withdrawAllItem(ItemID.ELDRITCH_ORB);
				return;
			}
			if (utils.bankContains(ItemID.ELDRITCH_NIGHTMARE_STAFF)) {
				utils.withdrawAllItem(ItemID.ELDRITCH_NIGHTMARE_STAFF);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_24553)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_24553);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25870)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25870);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25872)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25872);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25874)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25874);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25876)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25876);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25878)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25878);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25880)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25880);
				return;
			}
			if (utils.bankContains(ItemID.BLADE_OF_SAELDOR_C_25882)) {
				utils.withdrawAllItem(ItemID.BLADE_OF_SAELDOR_C_25882);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25869)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25869);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25884)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25884);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25886)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25886);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25888)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25888);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25890)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25890);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25892)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25892);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25894)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25894);
				return;
			}
			if (utils.bankContains(ItemID.BOW_OF_FAERDHINEN_C_25896)) {
				utils.withdrawAllItem(ItemID.BOW_OF_FAERDHINEN_C_25896);
				return;
			}
			if (utils.bankContains(ItemID.DRAGON_CLAWS)) {
				utils.withdrawAllItem(ItemID.DRAGON_CLAWS);
				return;
			}
			if (utils.bankContains(ItemID.DRAGON_HUNTER_LANCE)) {
				utils.withdrawAllItem(ItemID.DRAGON_HUNTER_LANCE);
				return;
			}
			if (utils.bankContains(ItemID.DRAGON_HUNTER_CROSSBOW)) {
				utils.withdrawAllItem(ItemID.DRAGON_HUNTER_CROSSBOW);
				return;
			}
			if (utils.bankContains(ItemID.INQUISITORS_MACE)) {
				utils.withdrawAllItem(ItemID.INQUISITORS_MACE);
				return;
			}
			if (utils.bankContains(ItemID.INQUISITORS_HAUBERK)) {
				utils.withdrawAllItem(ItemID.INQUISITORS_HAUBERK);
				return;
			}
			if (utils.bankContains(ItemID.INQUISITORS_PLATESKIRT)) {
				utils.withdrawAllItem(ItemID.INQUISITORS_PLATESKIRT);
				return;
			}
			if (utils.bankContains(ItemID.INQUISITORS_GREAT_HELM)) {
				utils.withdrawAllItem(ItemID.INQUISITORS_GREAT_HELM);
				return;
			}
			if (utils.bankContains(ItemID.GHRAZI_RAPIER)) {
				utils.withdrawAllItem(ItemID.GHRAZI_RAPIER);
				return;
			}
			if (utils.bankContains(ItemID.HOLY_GHRAZI_RAPIER)) {
				utils.withdrawAllItem(ItemID.HOLY_GHRAZI_RAPIER);
				return;
			}
			if (utils.bankContains(ItemID.ANCESTRAL_ROBE_TOP)) {
				utils.withdrawAllItem(ItemID.ANCESTRAL_ROBE_TOP);
				return;
			}
			if (utils.bankContains(ItemID.ANCESTRAL_ROBE_BOTTOM)) {
				utils.withdrawAllItem(ItemID.ANCESTRAL_ROBE_BOTTOM);
				return;
			}
			if (utils.bankContains(ItemID.PEGASIAN_BOOTS)) {
				utils.withdrawAllItem(ItemID.PEGASIAN_BOOTS);
				return;
			}
			if (utils.bankContains(ItemID.RANGER_BOOTS)) {
				utils.withdrawAllItem(ItemID.RANGER_BOOTS);
				return;
			}
			if (utils.bankContains(ItemID.DRAGON_WARHAMMER)) {
				utils.withdrawAllItem(ItemID.DRAGON_WARHAMMER);
				return;
			}
			if (utils.bankContains(ItemID.PRIMORDIAL_BOOTS)) {
				utils.withdrawAllItem(ItemID.PRIMORDIAL_BOOTS);
				return;
			}
			if (utils.bankContains(ItemID.RING_OF_ENDURANCE)) {
				utils.withdrawAllItem(ItemID.RING_OF_ENDURANCE);
				return;
			}
			if (utils.bankContains(ItemID.RING_OF_ENDURANCE_UNCHARGED)) {
				utils.withdrawAllItem(ItemID.RING_OF_ENDURANCE_UNCHARGED);
				return;
			}
			if (utils.bankContains(ItemID.KODAI_WAND)) {
				utils.withdrawAllItem(ItemID.KODAI_WAND);
				return;
			}
			if (utils.bankContains(ItemID.KODAI_INSIGNIA)) {
				utils.withdrawAllItem(ItemID.KODAI_INSIGNIA);
				return;
			}
			if (utils.bankContains(ItemID.ARMADYL_CHESTPLATE)) {
				utils.withdrawAllItem(ItemID.ARMADYL_CHESTPLATE);
				return;
			}
			if (utils.bankContains(ItemID.ARMADYL_CHAINSKIRT)) {
				utils.withdrawAllItem(ItemID.ARMADYL_CHAINSKIRT);
				return;
			}
			if (utils.bankContains(ItemID.PRIMORDIAL_CRYSTAL)) {
				utils.withdrawAllItem(ItemID.PRIMORDIAL_CRYSTAL);
				return;
			}
			return;
			//withdraw
		}
		if (hasitems && client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE) && client.getWorld() != 548) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 11927555));
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), 548, 4522000));
			return;
			//hop worlds
		}
		if (!hasitems && !client.getLocalPlayer().getWorldArea().intersectsWith(LUMBRIDGE)) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14286854));
			return;
			//home teleport
		}
	}
}