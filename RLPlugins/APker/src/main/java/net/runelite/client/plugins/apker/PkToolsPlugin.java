package net.runelite.client.plugins.apker;

import com.google.inject.Provides;
//import io.reactivex.rxjava3.core.Single;
import net.runelite.api.widgets.WidgetItem;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.WeaponMap;
import net.runelite.client.util.WeaponStyle;
import net.runelite.client.ui.overlay.OverlayManager;

//import org.pf4j.Extension;

import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Utils.Core;

import static java.awt.event.KeyEvent.*;


//@Extension
//@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "APKer",
		description = "Anarchise' PKing Tools",
		tags = {"combat", "player", "enemy", "pvp", "overlay"},
		enabledByDefault = false
)
public class PkToolsPlugin extends Plugin
{
	private static final Duration WAIT = Duration.ofSeconds(5);

	public Queue<String> commandList = new ConcurrentLinkedQueue<>();

	@Inject
	Core core;

	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	private PkToolsConfig configpk;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;


	@Inject
	private PkToolsHotkeyListener pkToolsHotkeyListener;

	@Inject
	private KeyManager keyManager;

	@Getter(AccessLevel.PACKAGE)



	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	public Player lastEnemy;
	private NPCManager npcManager;
	private Instant lastTime;
	private int nextRestoreVal = 0;
	Integer lastMaxHealth;
	Player target;

	int timeout = 0;
	private Random r = new Random();
	@Provides
	PkToolsConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(PkToolsConfig.class);
	}

	@Override
	protected void startUp() throws IOException {
		keyManager.registerKeyListener(pkToolsHotkeyListener);

	}

	@Override
	protected void shutDown()
	{
		lastTime = null;
		keyManager.unregisterKeyListener(pkToolsHotkeyListener);
	}

	@Subscribe
	public void onInteractingChanged(final InteractingChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		final Actor opponent = event.getTarget();

		if (opponent == null)
		{
			lastTime = Instant.now();
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		final List<Player> players = client.getPlayers();

		for (final Player player : players)
		{
			if (localPlayer != null && player == localPlayer.getInteracting())
			{
				lastEnemy = player;
			}
		}
	}
	long timeRan;
	int timeRun;
	int resetTime = 1;
	int timeRuns;
	@Subscribe
	public void onClientTick(ClientTick event) {
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		/*if (!MenuWasOpen) {
			Widget prayerWidget = client.getWidget(CurrentPrayer);

			if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null) {
				if (prayerWidget.isHidden() || prayerWidget == null) {
					MenuWasOpen = false;
					core.pressKey(VK_F2);
				}
				else {
					activatePrayer(CurrentPrayer);
					CurrentPrayer = null;
					MenuWasOpen = true;
				}
			}
			else {
				CurrentPrayer = null;
				MenuWasOpen = true;
			}
		}*/
		lastEnemyTimer();
		processCommands();
	}
	//int timeout = 0;
	WidgetInfo CurrentPrayer;
	Instant Timer;
	Instant Timer2;
	boolean MenuWasOpen = true;
	public void activatePrayer(WidgetInfo prayer) {
		Widget prayerWidget = client.getWidget(prayer);

		if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null) {
			if (prayerWidget.isHidden() || prayerWidget == null) {
				MenuWasOpen = false;
				CurrentPrayer = prayer;
				core.pressKey(VK_F2);
				Timer = Instant.now();
			} else {
				if (prayerWidget != null) {
					core.doInvoke(null, prayerWidget.getBounds());
				}
			}
		}
	}
	Instant Timer4;
	WidgetInfo CurrentSpell;
	public void clickSpell(WidgetInfo spell) {
		Widget spellWidgt = client.getWidget(spell);
		if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null) {
			if (spellWidgt.isHidden() || spellWidgt == null) {
				CurrentSpell = spell;
				core.pressKey(VK_F4);
				Timer4 = Instant.now();
				//sleep(500);
			}
			else {
				if (spellWidgt != null) {
					core.doInvoke(null, spellWidgt.getBounds());
				}
			}
		}
	}
	public void useItem(int ID, String option) {
		WidgetItem item = core.getWidgetItem(ID);
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if ((client.getVarcIntValue(VarClientInt.INVENTORY_TAB) != 3 && client.getWidget(WidgetInfo.BANK_CONTAINER) == null)) {
			if (item.getWidget().isHidden() || item == null) {
				CurrentItem = ID;
				core.pressKey(VK_F3);
				Timer2 = Instant.now();
			}
		}
		if (client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == 3 && item != null) {
			core.doInvoke(null, item.getCanvasBounds());
		}
	}
	public WidgetItem getBrew() {
		return QuickEatType.BREWS.getItemFromInventory(client);
	}

	public WidgetItem InventoryWidgetItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null) {
			Collection<WidgetItem> items = core.getAllInventoryItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}

	public WidgetItem GetAntifireItem()
	{
		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.ANTIFIRE_POTION1, ItemID.ANTIFIRE_POTION2, ItemID.ANTIFIRE_POTION3, ItemID.ANTIFIRE_POTION4,ItemID.EXTENDED_SUPER_ANTIFIRE1,ItemID.EXTENDED_SUPER_ANTIFIRE2, ItemID.EXTENDED_SUPER_ANTIFIRE3, ItemID.EXTENDED_SUPER_ANTIFIRE4, ItemID.EXTENDED_ANTIFIRE1, ItemID.EXTENDED_ANTIFIRE2, ItemID.EXTENDED_ANTIFIRE3, ItemID.EXTENDED_ANTIFIRE4, ItemID.SUPER_ANTIFIRE_POTION1, ItemID.SUPER_ANTIFIRE_POTION2, ItemID.SUPER_ANTIFIRE_POTION3, ItemID.SUPER_ANTIFIRE_POTION4);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntiVenomItem() {

		WidgetItem item;

		item = core.getInventoryWidgetItem(ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952, ItemID.ANTIVENOM1_12919 ,ItemID.ANTIVENOM2_12917,ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM4_12913, ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4, ItemID.SUPERANTIPOISON1, ItemID.SUPERANTIPOISON2, ItemID.SUPERANTIPOISON3, ItemID.SUPERANTIPOISON4, ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4);

		if (item != null) {
			return item;
		}

		return item;
	}
	int CurrentItem;
	Instant Timer3;
	@Subscribe
	public void onGameTick(GameTick event) throws IOException {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (client.getBoostedSkillLevel(Skill.PRAYER) <= configpk.prayer()) {
			WidgetItem restoreItem = getRestoreItem();
			core.useItem(restoreItem.getId(), "drink");
		}
		int health = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
		if (health <= this.configpk.tripleHP()) {
			core.useItem(configpk.food1(), MenuAction.ITEM_USE);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (configpk.brews()) {
				WidgetItem restoreItem = getBrew();
				core.useItem(restoreItem.getId(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.CC_OP.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
				core.useItem(configpk.food3(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!configpk.brews()) {
				core.useItem(configpk.food2(),MenuAction.ITEM_USE);
				core.useItem(configpk.food3(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			return;
		}
		if (health <= this.configpk.doubleHP() && health > this.configpk.tripleHP()) {
			core.useItem(configpk.food1(),MenuAction.ITEM_USE);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (configpk.brews()) {
				WidgetItem restoreItem = getBrew();
				core.useItem(restoreItem.getId(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.CC_OP.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!configpk.brews()) {
				core.useItem(configpk.food2(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			return;
		}
		if (health < this.configpk.singleHP() && health > this.configpk.doubleHP()) {
			core.useItem(configpk.food1(),MenuAction.ITEM_USE);
			return;
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
		}
		if (configpk.Antifire() && client.getVarbitValue(Varbits.ANTIFIRE) == 0 && client.getVarbitValue(Varbits.SUPER_ANTIFIRE) == 0) {
			WidgetItem overload = GetAntifireItem();
			if (overload != null) {
				core.useItem(overload.getId(), "drink");
			}
		}
		if (configpk.Antivenom() && client.getVar(VarPlayer.IS_POISONED) > 0 ) {
			WidgetItem ven = GetAntiVenomItem();
			if (ven != null) {
				core.useItem(ven.getId(), "drink");
			}
		}
		if (Timer2 != null) {
			core.useItem(CurrentItem, MenuAction.ITEM_USE);
			Timer2 = null;
			//return;
		}
		if (Timer != null) {
			core.activatePrayer(CurrentPrayer);
			Timer = null;
		}
		if (Timer3 != null) {
			Widget spec = client.getWidget(593, 39);
			Timer3 = null;
			boolean spec_enabled = (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1);
			if (spec_enabled) {
				return;
			}
			core.clickWidget(spec);
		}
		if (Timer4 != null) {
			Widget spellWidget = client.getWidget(CurrentSpell);
			core.doInvoke(null, spellWidget.getBounds());
			Timer4 = null;
		}
		doAutoSwapPrayers();
		doSwapGear();
		//processCommands();
	}

	private void processCommands() {
		while (commandList.peek() != null)
		{
			String result = commandList.poll();
			if (result.toLowerCase().contains("blood")){
				int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);
				if (boosted_level >= 80 && boosted_level < 92) {
					clickSpell(WidgetInfo.SPELL_BLOOD_BLITZ);
				} else {
					clickSpell(WidgetInfo.SPELL_BLOOD_BARRAGE);
				}
			}
			if (result.toLowerCase().contains("mystic might")) {
				if (client.getVar(Prayer.MYSTIC_MIGHT.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_MYSTIC_MIGHT);
			}
			if (result.toLowerCase().contains("freeze")) {
				int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);

				if (boosted_level >= 82 && boosted_level < 94) {
					clickSpell(WidgetInfo.SPELL_ICE_BLITZ);
				} else {
					clickSpell(WidgetInfo.SPELL_ICE_BARRAGE);
				}
			}
			if (result.toLowerCase().contains("invent")) {
				core.pressKey(VK_F2);
			}
			if (result.toLowerCase().contains("incredible reflexes")) {
				if (client.getVar(Prayer.INCREDIBLE_REFLEXES.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_INCREDIBLE_REFLEXES);
			}
			if (result.toLowerCase().contains("eagle eye")) {
				if (client.getVar(Prayer.EAGLE_EYE.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_EAGLE_EYE);
			}
			if (result.toLowerCase().contains("spec")) {
				core.pressKey(VK_F1);
				Timer3 =  Instant.now();
			}
			if (result.toLowerCase().contains("id")) {

				int item = Integer.parseInt(result.replace("id_", ""));
				useItem(item, "wield");
			}
			if (result.toLowerCase().contains("superglass")) {
				clickSpell(WidgetInfo.SPELL_SUPERGLASS_MAKE);
			}
			if (result.toLowerCase().contains("crumble")) {
				clickSpell(WidgetInfo.SPELL_CRUMBLE_UNDEAD);
			}
			if (result.toLowerCase().contains("humidify")) {
				clickSpell(WidgetInfo.SPELL_HUMIDIFY);
			}
			if (result.toLowerCase().contains("string jewellery")) {
				clickSpell(WidgetInfo.SPELL_STRING_JEWELLERY);
			}
			if (result.toLowerCase().contains("plank make")) {
				clickSpell(WidgetInfo.SPELL_PLANK_MAKE);
			}
			if (result.toLowerCase().contains("alch")) {
				clickSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);
			}
			if (result.toLowerCase().contains("firesurge")) {
				int boosted_level = client.getBoostedSkillLevel(Skill.MAGIC);
				if (boosted_level >= 95) {
					clickSpell(WidgetInfo.SPELL_FIRE_SURGE);
				}
			}
			if (result.toLowerCase().contains("clickenemy")) {
				if (lastEnemy != null) {
					core.attackPlayerDirect(lastEnemy);
				}
			}
			if (result.toLowerCase().contains("entangle")) {
				if (client.getBoostedSkillLevel(Skill.MAGIC) < 79) {
					return;
				}
				clickSpell(WidgetInfo.SPELL_ENTANGLE);
			}
			if (result.toLowerCase().contains("augury")) {
				if (client.getVar(Prayer.AUGURY.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_AUGURY);
			}
			if (result.toLowerCase().contains("rigour")) {
				if (client.getVar(Prayer.RIGOUR.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_RIGOUR);
			}
			if (result.toLowerCase().contains("piety")) {
				if (client.getVar(Prayer.PIETY.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_PIETY);
			}
			if (result.toLowerCase().contains("protectfrommelee")) {
				if (client.getVar(Prayer.PROTECT_FROM_MELEE.getVarbit()) == 1) {
					return;
				}
				/*Widget prayerWidget = client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
				CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MELEE;
				if (client.getWidget(WidgetInfo.BANK_CONTAINER) == null) {
					if (prayerWidget.isHidden() || prayerWidget == null) {
						MenuWasOpen = false;
						timeout = 5;
						core.pressKey(VK_F2);
					}
				}*/
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
			}
			if (result.toLowerCase().contains("protectfrommagic")) {
				if (client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
			}
			if (result.toLowerCase().contains("protectfrommissiles")) {
				if (client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
			}
			if (result.toLowerCase().contains("protectitem")){
				if (client.getVar(Prayer.PROTECT_ITEM.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_PROTECT_ITEM);
			}
			if (result.toLowerCase().contains("steelskin")) {
				if (client.getVar(Prayer.STEEL_SKIN.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_STEEL_SKIN);
			}
			if (result.toLowerCase().contains("teleblock")) {
				if (client.getBoostedSkillLevel(Skill.MAGIC) < 85) {
					return;
				}
				clickSpell(WidgetInfo.SPELL_TELEBLOCK);
			}
			if (result.toLowerCase().contains("ultimatestrength")) {
				if (client.getVar(Prayer.ULTIMATE_STRENGTH.getVarbit()) == 1) {
					return;
				}
				activatePrayer(WidgetInfo.PRAYER_ULTIMATE_STRENGTH);
			}
			if (result.toLowerCase().contains("vengeance")) {
				if (client.getBoostedSkillLevel(Skill.MAGIC) < 94) {
					return;
				}
				clickSpell(WidgetInfo.SPELL_VENGEANCE);
			}
			if (result.toLowerCase().contains("wait")){
				//Thread.sleep(500);
			}
		}
	}
	int lll = 999990;
	public void lastEnemyTimer()
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		if (lastEnemy == null)
		{
			return;
		}

		if (localPlayer.getInteracting() == null)
		{
			if (Duration.between(lastTime, Instant.now()).compareTo(PkToolsPlugin.WAIT) > 0)
			{
				lastEnemy = null;
			}
		}
	}

	public void activatePrayer(Prayer prayer) throws InterruptedException {
		if (prayer == null)
		{
			return;
		}

		//check if prayer is already active this tick
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
		core.activatePrayer(widgetInfo);
		//entryList.add(new MenuEntry("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId(), false));
		//clientThread.invoke(() -> client.invokeMenuAction("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId()));
		//click();
	}
	/*public void doSpecOnHealth()
	{
		if (!configpk.specOnHealth()){
			return;
		}
		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}
			//
			if (lastEnemy instanceof NPC)
			{
				lastMaxHealth = npcManager.getHealth(((NPC) lastEnemy).getId());
			}
			else if (lastEnemy instanceof Player)
			{
				if (lastEnemy.getName() != null && lastEnemy.getHealthScale() > 0) {
					String opponentName = Text.removeTags(lastEnemy.getName());
					final HiscoreResult hiscoreResult = hiscoreManager.lookupAsync(opponentName, getHiscoreEndpoint());
					if (hiscoreResult != null) {
						final int hp = hiscoreResult.getHitpoints().getLevel();
						if (hp > 0) {
							lastMaxHealth = hp;
						}
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/


	public WidgetItem getRestoreItem()
	{
		WidgetItem item;

		item = core.getItemFromInventory(PrayerRestoreType.PRAYER_POTION.ItemIDs);

		if (item != null)
		{
			return item;
		}

		item = core.getItemFromInventory(PrayerRestoreType.SANFEW_SERUM.ItemIDs);

		if (item != null)
		{
			return item;
		}

		item = core.getItemFromInventory(PrayerRestoreType.SUPER_RESTORE.ItemIDs);

		return item;
	}

	public WidgetItem getBrewItem()
	{
		WidgetItem item;
		//item = PrayerRestoreType.SARA_BREWS.getItemFromInventory(client);
		item = core.getItemFromInventory(PrayerRestoreType.SARA_BREWS.ItemIDs);
		if (item != null)
		{
			return item;
		}
		return item;
	}
	private void addCommands(String command)
	{
		for (String c : command.split("\\s*\n\\s*"))
		{
			commandList.add(PkToolsHotkeyListener.builder(c));
		}
	}
	public int getBoostAmount(WidgetItem restoreItem, int prayerLevel)
	{
		if (PrayerRestoreType.PRAYER_POTION.containsId(restoreItem.getId()))
		{
			return 7 + (int) Math.floor(prayerLevel * .25);
		}
		else if (PrayerRestoreType.SANFEW_SERUM.containsId(restoreItem.getId()))
		{
			return 4 + (int) Math.floor(prayerLevel * (double)(3 / 10));
		}
		else if (PrayerRestoreType.SUPER_RESTORE.containsId(restoreItem.getId()))
		{
			return 8 + (int) Math.floor(prayerLevel * .25);
		}

		return 0;
	}
	public void doSwapGear()
	{
		if (!configpk.autoGearSwap())
		{
			return;
		}

		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			/*if (client.getLocalPlayer().getInteracting() != lastEnemy){
				return;
			}*/

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}
			if (!configpk.swapFromPray()){
				WeaponStyle weaponStyle = WeaponMap.StyleMap.getOrDefault(lastEnemyAppearance.getEquipmentId(KitType.WEAPON), null);
				if (weaponStyle == null)
				{
					return;
				}

				switch (weaponStyle)
				{
					case MELEE:
						WeaponStyle localWeaponStyle = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle == WeaponStyle.MELEE){
							break;
						}
						addCommands("group1");	//group1 = melee
						addCommands("clickenemy");
						break;
					case RANGE:
						WeaponStyle localWeaponStyle2 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle2 == WeaponStyle.MAGIC){
							break;
						}
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case MAGIC:
						WeaponStyle localWeaponStyle3 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle3 == WeaponStyle.RANGE){
							break;
						}
						addCommands("group2");	//group2 = ranged
						addCommands("clickenemy");
						break;
					default:
						break;
				}
			}

			if (configpk.swapFromPray()) {
				HeadIcon currentIcon = lastEnemy.getOverheadIcon();
				if (currentIcon == null) {
					return;
				}
				switch (currentIcon) {
					case MELEE:
						WeaponStyle localWeaponStyle2 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle2 == WeaponStyle.MAGIC){
							break;
						}
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case RANGED:
						WeaponStyle localWeaponStyle1 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle1 == WeaponStyle.MAGIC){
							break;
						}
						/*addCommands("group1");	//group1 = melee
						addCommands("clickenemy");*/
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case MAGIC:
						WeaponStyle localWeaponStyle3 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle3 == WeaponStyle.RANGE){
							break;
						}
						addCommands("group2");	//group2 = ranged
						addCommands("clickenemy");
						break;
					default:
						break;
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void doAutoSwapPrayers()
	{
		if (!configpk.autoPrayerSwitcher())
		{
			return;
		}

		if (!configpk.autoPrayerSwitcher())
		{
			return;
		}

		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}

			WeaponStyle weaponStyle = WeaponMap.StyleMap.getOrDefault(lastEnemyAppearance.getEquipmentId(KitType.WEAPON), null);

			if (weaponStyle == null)
			{
				return;
			}

			switch (weaponStyle)
			{
				case MELEE:
					if (configpk.enableMeleePrayer()) {
						activatePrayer(Prayer.PROTECT_FROM_MELEE);
					}
					break;
				case RANGE:
					if (configpk.enableRangedPrayer()) {
					activatePrayer(Prayer.PROTECT_FROM_MISSILES);
					}
					break;
				case MAGIC:
					if (configpk.enableMagicPrayer()) {
						activatePrayer(Prayer.PROTECT_FROM_MAGIC);
					}
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("apker"))
		{
			return;
		}
	}
}

enum PrayerRestoreType
{
	PRAYER_POTION(ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4),
	SUPER_RESTORE(ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4,
			ItemID.BLIGHTED_SUPER_RESTORE1, ItemID.BLIGHTED_SUPER_RESTORE2, ItemID.BLIGHTED_SUPER_RESTORE3,
			ItemID.BLIGHTED_SUPER_RESTORE4),
	SANFEW_SERUM(ItemID.SANFEW_SERUM1, ItemID.SANFEW_SERUM2, ItemID.SANFEW_SERUM3, ItemID.SANFEW_SERUM4),


	SARA_BREWS(ItemID.SARADOMIN_BREW1, ItemID.SARADOMIN_BREW2, ItemID.SARADOMIN_BREW3, ItemID.SARADOMIN_BREW4);
	public int[] ItemIDs;

	PrayerRestoreType(int... ids)
	{
		this.ItemIDs = ids;
	}

	public boolean containsId(int id)
	{
		return Arrays.stream(this.ItemIDs).anyMatch(x -> x == id);
	}


}


