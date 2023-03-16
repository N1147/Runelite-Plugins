package net.runelite.client.plugins.zulrah;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.FontType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Utils.Core;
import net.runelite.client.plugins.apker.QuickEatType;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.Point;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.VK_F2;

@PluginDescriptor(
		name = "AZulrah (Helper)",
		description = "Zulrah tools.",
		tags = {"zulrah", "zul", "andra", "snakeling"},
		enabledByDefault = false
)

public class ZulrahPlugin extends Plugin implements KeyListener
{
	private static final Logger log = LoggerFactory.getLogger(ZulrahPlugin.class);
	@Inject
	private Client client;
	@Inject
	private KeyManager keyManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private InstanceTimerOverlay instanceTimerOverlay;
	@Inject
	private PhaseOverlay phaseOverlay;
	@Inject
	private PrayerHelperOverlay prayerHelperOverlay;
	@Inject
	private PrayerMarkerOverlay prayerMarkerOverlay;
	@Inject
	private SceneOverlay sceneOverlay;
	@Inject
	private ZulrahConfig config;
	@Inject Core core;
	private NPC zulrahNpc = null;
	private int stage = 0;
	private int phaseTicks = -1;
	private int attackTicks = -1;
	private int totalTicks = 0;
	private RotationType currentRotation = null;
	private List<RotationType> potentialRotations = new ArrayList<RotationType>();
	private final Map<LocalPoint, Integer> projectilesMap = new HashMap<LocalPoint, Integer>();
	private final Map<GameObject, Integer> toxicCloudsMap = new HashMap<GameObject, Integer>();
	private static boolean flipStandLocation = false;
	private static boolean flipPhasePrayer = false;
	private static boolean zulrahReset = false;
	private final Collection<NPC> snakelings = new ArrayList<NPC>();
	private boolean holdingSnakelingHotkey = false;
	private Counter zulrahTotalTicksInfoBox;
	public static final BufferedImage[] ZULRAH_IMAGES = new BufferedImage[3];
	private static final BufferedImage CLOCK_ICON = ImageUtil.loadImageResource(ZulrahPlugin.class, "clock.png");
	private final BiConsumer<RotationType, RotationType> phaseTicksHandler = (current, potential) -> {
		if (zulrahReset) 
	{
			phaseTicks = 38;
		}
		else
		{
			ZulrahPhase p = current != null ? getCurrentPhase((RotationType)((Object)current)) : getCurrentPhase((RotationType)((Object)potential));
			Preconditions.checkNotNull(p, "Attempted to set phase ticks but current Zulrah phase was somehow null. Stage: " + stage);
			phaseTicks = p.getAttributes().getPhaseTicks();
		}
	};
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

	@Provides
	ZulrahConfig provideConfig(ConfigManager configManager) 
	{
		return configManager.getConfig(ZulrahConfig.class);
	}

	protected void startUp() 
	{
		overlayManager.add(instanceTimerOverlay);
		overlayManager.add(phaseOverlay);
		overlayManager.add(prayerHelperOverlay);
		overlayManager.add(prayerMarkerOverlay);
		overlayManager.add(sceneOverlay);
		keyManager.registerKeyListener(this);
	}

	protected void shutDown() 
	{
		reset();
		overlayManager.remove(instanceTimerOverlay);
		overlayManager.remove(phaseOverlay);
		overlayManager.remove(prayerHelperOverlay);
		overlayManager.remove(prayerMarkerOverlay);
		overlayManager.remove(sceneOverlay);
		keyManager.unregisterKeyListener(this);
	}

	private void reset() 
	{
		zulrahNpc = null;
		stage = 0;
		phaseTicks = -1;
		attackTicks = -1;
		totalTicks = 0;
		currentRotation = null;
		potentialRotations.clear();
		projectilesMap.clear();
		toxicCloudsMap.clear();
		flipStandLocation = false;
		flipPhasePrayer = false;
		instanceTimerOverlay.resetTimer();
		zulrahReset = false;
		clearSnakelingCollection();
		holdingSnakelingHotkey = false;
		handleTotalTicksInfoBox(true);
		log.debug("Zulrah Reset!");
	}

	public void keyTyped(KeyEvent e) 
	{
	}

	public void keyPressed(KeyEvent e) 
	{
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES && config.snakelingMesHotkey().matches(e)) 
	{
			holdingSnakelingHotkey = true;
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES && config.snakelingMesHotkey().matches(e)) 
	{
			holdingSnakelingHotkey = false;
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event) 
	{
		if (event.getGroup().equalsIgnoreCase("znzulrah")) 
	{
			switch (event.getKey()) 
	{
				case "snakelingSetting":
				{
					if (config.snakelingSetting() != ZulrahConfig.SnakelingSettings.ENTITY) 
	{
						clearSnakelingCollection();
					}
					if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.MES) break;
					holdingSnakelingHotkey = false;
					break;
				}
				case "totalTickCounter":
				{
					if (config.totalTickCounter()) break;
					handleTotalTicksInfoBox(true);
				}
			}
		}
	}

	private void clearSnakelingCollection() 
	{
		snakelings.forEach(npc -> ZulrahPlugin.setHidden(npc, false));
		snakelings.clear();
	}

	@Subscribe
	private void onClientTick(ClientTick event) 
	{
		if (client.getGameState() != GameState.LOGGED_IN || zulrahNpc == null) 
	{
			return;
		}
		if (config.snakelingSetting() == ZulrahConfig.SnakelingSettings.ENTITY) 
	{
			snakelings.addAll(client.getNpcs().stream().filter(npc -> npc != null && npc.getName() != null && npc.getName().equalsIgnoreCase("snakeling") && npc.getCombatLevel() == 90).collect(Collectors.toList()));
			snakelings.forEach(npc -> ZulrahPlugin.setHidden(npc, true));
		}
	}

	public WidgetItem getBrew() {
		return QuickEatType.BREWS.getItemFromInventory(client);
	}

	@Subscribe
	private void onNpcChanged(NpcChanged event) {
		final int npcId = event.getNpc().getId();
		if (npcId == 2043 && client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) != 0) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MISSILES;
			//Timer2 = Instant.now();
		}
		if (npcId == 2043 && client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) != 0) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MAGIC;
			//Timer2 = Instant.now();
		}
		if (npcId == 2042 && client.getVar(Prayer.PROTECT_FROM_MISSILES.getVarbit()) == 0) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MISSILES;
			//Timer2 = Instant.now();

		} else if (npcId == 2044 && client.getVar(Prayer.PROTECT_FROM_MAGIC.getVarbit()) == 0) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MAGIC;
			//Timer2 = Instant.now();
		}
		if (event.getNpc().getName().equalsIgnoreCase("zulrah")) {
			zulrahNpc = event.getNpc();
		}
		 else if (npcId == 2044) {

		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event) {
		NPC npc = event.getNpc();
		final int npcId = event.getNpc().getId();
		if (npcId == 2042 && client.getLocalPlayer().getOverheadIcon() != HeadIcon.RANGED) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MISSILES;
			//Timer2 = Instant.now();
		} else if (npcId == 2044 && client.getLocalPlayer().getOverheadIcon() != HeadIcon.MAGIC) {
			activatePrayer(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
			//CurrentPrayer = WidgetInfo.PRAYER_PROTECT_FROM_MAGIC;
			//Timer2 = Instant.now();
		}
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (client.getGameState() != GameState.LOGGED_IN || zulrahNpc == null) {
			return;
		}
		if (Timer2 != null) {
			core.activatePrayer(CurrentPrayer);
			//CurrentPrayer = null;
			Timer2 = null;
		}
		for (ZulrahData data : getZulrahData()) {
			if (data.getCurrentPhase().isPresent()) {
				//standPos = data.getCurrentDynamicStandLocation().get().toLocalPoint();
				if (data.getCurrentPhasePrayer().isPresent()) {
					if (client.getVar(data.getCurrentPhasePrayer().get().getVarbit()) == 0) {
						CurrentPrayer = data.getCurrentPhasePrayer().get().getWidgetInfo();
						activatePrayer(CurrentPrayer);
					}
					//else {
					//	CurrentPrayer = null;
					//}
				}
				//if (client.getLocalPlayer().getLocalLocation() != standPos) {
				//if (!utils.isMoving()) {
				//utils.walk(standPos);
				//}
				//}
			}
		}
		/*for (ZulrahData data : getZulrahData()) {
			if (data.getCurrentPhase().isPresent()) {
				if (data.getCurrentPhasePrayer().isPresent()) {
					Prayer currentPrayer = data.getCurrentPhasePrayer().get();
					if (currentPrayer != null) {
						if (client.getVar(currentPrayer.getVarbit()) == 0) {
							Timer2 = Instant.now();
							activatePrayer(currentPrayer.getWidgetInfo());
						}
					}
				}
			}
		}*/

		int health = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
		if (health <= this.config.tripleHP()) {
			core.useItem(config.food1(), MenuAction.ITEM_USE);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (config.brews()) {
				WidgetItem restoreItem = getBrew();
				core.useItem(restoreItem.getId(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.CC_OP.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
				core.useItem(config.food3(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!config.brews()) {
				core.useItem(config.food2(),MenuAction.ITEM_USE);
				core.useItem(config.food3(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food3(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food3()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (health <= this.config.doubleHP() && health > this.config.tripleHP()) {
			core.useItem(config.food1(),MenuAction.ITEM_USE);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
			if (config.brews()) {
				WidgetItem restoreItem = getBrew();
				core.useItem(restoreItem.getId(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.CC_OP.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (!config.brews()) {
				core.useItem(config.food2(),MenuAction.ITEM_USE);
				//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food2(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food2()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (health < this.config.singleHP() && health > this.config.doubleHP()) {
			core.useItem(config.food1(),MenuAction.ITEM_USE);
			//clientThread.invoke(() -> client.invokeMenuAction("", "", config.food1(), MenuAction.CC_OP.getId(), InventoryWidgetItem(config.food1()).getIndex(), WidgetInfo.INVENTORY.getId()));
		}

		++totalTicks;
		if (attackTicks >= 0) {
			--attackTicks;
		}
		if (phaseTicks >= 0) {
			--phaseTicks;
		}
		if (projectilesMap.size() > 0) {
			projectilesMap.values().removeIf(v -> v <= 0);
			projectilesMap.replaceAll((k, v) -> v - 1);
		}
		if (toxicCloudsMap.size() > 0) {
			toxicCloudsMap.values().removeIf(v -> v <= 0);
			toxicCloudsMap.replaceAll((k, v) -> v - 1);
		}
		handleTotalTicksInfoBox(false);
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event) 
	{
		if (!(event.getActor() instanceof NPC)) 
	{
			return;
		}
		NPC npc = (NPC)((Object)event.getActor());
		if (npc.getName() != null && !npc.getName().equalsIgnoreCase("zulrah")) 
	{
			return;
		}
		switch (npc.getAnimation()) 
	{
			case 5071:
			{
				zulrahNpc = npc;
				instanceTimerOverlay.setTimer();
				potentialRotations = RotationType.findPotentialRotations(npc, stage);
				phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));
				log.debug("New Zulrah Encounter Started");
				break;
			}
			case 5073:
			{
				++stage;
				if (currentRotation == null) 
	{
					potentialRotations = RotationType.findPotentialRotations(npc, stage);
					currentRotation = potentialRotations.size() == 1 ? potentialRotations.get(0) : null;
				}
				phaseTicksHandler.accept(currentRotation, potentialRotations.get(0));
				break;
			}
			case 5072:
			{
				if (zulrahReset) 
	{
					zulrahReset = false;
				}
				if (currentRotation == null || !isLastPhase(currentRotation)) break;
				stage = -1;
				currentRotation = null;
				potentialRotations.clear();
				snakelings.clear();
				flipStandLocation = false;
				flipPhasePrayer = false;
				zulrahReset = true;
				log.debug("Resetting Zulrah");
				break;
			}
			case 5069:
			{
				attackTicks = 4;
				if (currentRotation == null || !getCurrentPhase(currentRotation).getZulrahNpc().isJad()) break;
				flipPhasePrayer = !flipPhasePrayer;
				break;
			}
			case 5806:
			case 5807:
			{
				attackTicks = 8;
				flipStandLocation = !flipStandLocation;
				break;
			}
			case 5804:
			{
				reset();
			}
		}
	}

	@Subscribe
	private void onFocusChanged(FocusChanged event) 
	{
		if (!event.isFocused()) 
	{
			holdingSnakelingHotkey = false;
		}
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event) 
	{
		if (config.snakelingSetting() != ZulrahConfig.SnakelingSettings.MES || zulrahNpc == null || zulrahNpc.isDead()) 
	{
			return;
		}
		if (!holdingSnakelingHotkey && event.getTarget().contains("Snakeling") && event.getOption().equalsIgnoreCase("attack")) 
	{
			NPC npc = client.getCachedNPCs()[event.getIdentifier()];
			if (npc == null) 
	{
				return;
			}
			client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event) 
	{
		if (zulrahNpc == null) 
	{
			return;
		}
		Projectile p = event.getProjectile();
		switch (p.getId()) 
	{
			case 1045:
			case 1047:
			{
				projectilesMap.put(event.getPosition(), p.getRemainingCycles() / 30);
			}
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event) 
	{
		if (zulrahNpc == null) 
	{
			return;
		}
		GameObject obj = event.getGameObject();
		if (obj.getId() == 11700) 
	{
			toxicCloudsMap.put(obj, 30);
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) 
	{
		if (zulrahNpc == null) 
	{
			return;
		}
		switch (event.getGameState()) 
	{
			case LOADING:
			case CONNECTION_LOST:
			case HOPPING:
			{
				reset();
			}
		}
	}

	@Nullable
	private ZulrahPhase getCurrentPhase(RotationType type) 
	{
		return stage >= type.getZulrahPhases().size() ? null : type.getZulrahPhases().get(stage);
	}

	@Nullable
	private ZulrahPhase getNextPhase(RotationType type) 
	{
		return isLastPhase(type) ? null : type.getZulrahPhases().get(stage + 1);
	}

	private boolean isLastPhase(RotationType type) 
	{
		return stage == type.getZulrahPhases().size() - 1;
	}

	public Set<ZulrahData> getZulrahData() 
	{
		LinkedHashSet<ZulrahData> zulrahDataSet = new LinkedHashSet<ZulrahData>();
		if (currentRotation == null) 
	{
			potentialRotations.forEach(type -> zulrahDataSet.add(new ZulrahData(getCurrentPhase((RotationType)((Object)type)), getNextPhase((RotationType)((Object)type)))));
		}
		else
		{
			zulrahDataSet.add(new ZulrahData(getCurrentPhase(currentRotation), getNextPhase(currentRotation)));
		}
		return zulrahDataSet.size() > 0 ? zulrahDataSet : Collections.emptySet();
	}

	private void handleTotalTicksInfoBox(boolean remove) 
	{
		if (remove) 
	{
			infoBoxManager.removeInfoBox(zulrahTotalTicksInfoBox);
			zulrahTotalTicksInfoBox = null;
		}
		else if (config.totalTickCounter())
	{
			if (zulrahTotalTicksInfoBox == null) 
	{
				zulrahTotalTicksInfoBox = new Counter(CLOCK_ICON, this, totalTicks);
				zulrahTotalTicksInfoBox.setTooltip("Total Ticks Alive");
				infoBoxManager.addInfoBox(zulrahTotalTicksInfoBox);
			}
			else
			{
				zulrahTotalTicksInfoBox.setCount(totalTicks);
			}
		}
	}

	private static void setHidden(Renderable renderable, boolean hidden) 
	{
		Method setHidden = null;
		try
		{
			setHidden = renderable.getClass().getMethod("setHidden", Boolean.TYPE);
		}
		catch (NoSuchMethodException e) 
	{
			log.debug("Couldn't find method setHidden for class {}", renderable.getClass());
			return;
		}
		try
		{
			setHidden.invoke(renderable, hidden);
		}
		catch (IllegalAccessException | InvocationTargetException e) 
	{
			log.debug("Couldn't call method setHidden for class {}", renderable.getClass());
		}
	}

	public NPC getZulrahNpc() 
	{
		return zulrahNpc;
	}

	public int getPhaseTicks() 
	{
		return phaseTicks;
	}

	public int getAttackTicks() 
	{
		return attackTicks;
	}

	public RotationType getCurrentRotation() 
	{
		return currentRotation;
	}

	public Map<LocalPoint, Integer> getProjectilesMap() 
	{
		return projectilesMap;
	}

	public Map<GameObject, Integer> getToxicCloudsMap() 
	{
		return toxicCloudsMap;
	}

	public static boolean isFlipStandLocation() 
	{
		return flipStandLocation;
	}

	public static boolean isFlipPhasePrayer() 
	{
		return flipPhasePrayer;
	}

	public static boolean isZulrahReset() 
	{
		return zulrahReset;
	}

	static
	{
		ZulrahPlugin.ZULRAH_IMAGES[0] = ImageUtil.loadImageResource(ZulrahPlugin.class, "zulrah_range.png");
		ZulrahPlugin.ZULRAH_IMAGES[1] = ImageUtil.loadImageResource(ZulrahPlugin.class, "zulrah_melee.png");
		ZulrahPlugin.ZULRAH_IMAGES[2] = ImageUtil.loadImageResource(ZulrahPlugin.class, "zulrah_magic.png");
	}
}

class ZulrahNpc
{
	@Nonnull
	private final ZulrahType type;
	@Nonnull
	private final ZulrahLocation zulrahLocation;
	private final boolean jad;

	public ZulrahNpc(@Nonnull ZulrahType type, @Nonnull ZulrahLocation zulrahLocation, boolean jad)
	{
		if (type == null)
		{
			throw new NullPointerException("type is marked non-null but is null");
		}
		else if (zulrahLocation == null)
		{
			throw new NullPointerException("zulrahLocation is marked non-null but is null");
		}
		else
		{
			this.type = type;
			this.zulrahLocation = zulrahLocation;
			this.jad = jad;
		}
	}

	public static ZulrahNpc valueOf(NPC zulrah, boolean jad)
	{
		return new ZulrahNpc(ZulrahType.valueOf(zulrah.getId()), ZulrahLocation.valueOf(zulrah.getLocalLocation()), jad);
	}

	@Nonnull
	public ZulrahType getType()
	{
		return type;
	}

	@Nonnull
	public ZulrahLocation getZulrahLocation()
	{
		return zulrahLocation;
	}

	public boolean isJad()
	{
		return jad;
	}

	public String toString()
	{
		ZulrahType type = getType();
		return "ZulrahNpc(type=" + type + ", zulrahLocation=" + getZulrahLocation() + ", jad=" + isJad() + ")";
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (!(o instanceof ZulrahNpc))
		{
			return false;
		}
		else
		{
			ZulrahNpc other = (ZulrahNpc) o;
			Object this$type = getType();
			Object other$type = other.getType();
			if (this$type == null)
			{
				if (other$type != null)
				{
					return false;
				}
			}
			else if (!this$type.equals(other$type))
			{
				return false;
			}

			label29:
			{
				Object this$zulrahLocation = getZulrahLocation();
				Object other$zulrahLocation = other.getZulrahLocation();
				if (this$zulrahLocation == null)
				{
					if (other$zulrahLocation == null)
					{
						break label29;
					}
				}
				else if (this$zulrahLocation.equals(other$zulrahLocation))
				{
					break label29;
				}

				return false;
			}

			if (isJad() != other.isJad())
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}

	public int hashCode()
	{
		int PRIME = 59;
		int result = 1;
		Object $type = getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		Object $zulrahLocation = getZulrahLocation();
		result = result * PRIME + ($zulrahLocation == null ? 43 : $zulrahLocation.hashCode());
		result = result * PRIME + (isJad() ? 79 : 97);
		return result;
	}
}
enum RotationType
{
	ROT_A("Rotation A", ImmutableList.of(add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 21), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 18), add(ZulrahType.RANGE, ZulrahLocation.SOUTH, StandLocation.WEST_PILLAR_N, StandLocation.WEST_PILLAR_N2, Prayer.PROTECT_FROM_MISSILES, 39), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.WEST_PILLAR_N, (StandLocation) null, (Prayer) null, 22), add(ZulrahType.MAGIC, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_S, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 20), add(ZulrahType.RANGE, ZulrahLocation.SOUTH, StandLocation.EAST_PILLAR, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.MAGIC, ZulrahLocation.SOUTH, StandLocation.EAST_PILLAR, StandLocation.EAST_PILLAR_N2, Prayer.PROTECT_FROM_MAGIC, 36), addJad(ZulrahType.RANGE, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_S, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MISSILES, 48), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 21))),
	ROT_B("Rotation B", ImmutableList.of(add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 21), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 18), add(ZulrahType.RANGE, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_S, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.MAGIC, ZulrahLocation.SOUTH, StandLocation.WEST_PILLAR_N, StandLocation.WEST_PILLAR_N2, Prayer.PROTECT_FROM_MAGIC, 39), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.WEST_PILLAR_N, (StandLocation) null, (Prayer) null, 21), add(ZulrahType.RANGE, ZulrahLocation.EAST, StandLocation.CENTER, StandLocation.WEST_PILLAR_S, Prayer.PROTECT_FROM_MISSILES, 20), add(ZulrahType.MAGIC, ZulrahLocation.SOUTH, StandLocation.WEST_PILLAR_S, StandLocation.WEST_PILLAR_N2, Prayer.PROTECT_FROM_MAGIC, 36), addJad(ZulrahType.RANGE, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_S, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MISSILES, 48), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 21))),
	ROT_C("Rotation C", ImmutableList.of(add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.RANGE, ZulrahLocation.EAST, StandLocation.NORTHEAST_TOP, (StandLocation) null, Prayer.PROTECT_FROM_MISSILES, 30), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.WEST, (StandLocation) null, (Prayer) null, 40), add(ZulrahType.MAGIC, ZulrahLocation.WEST, StandLocation.WEST, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 20), add(ZulrahType.RANGE, ZulrahLocation.SOUTH, StandLocation.EAST_PILLAR_S, StandLocation.EAST_PILLAR_N2, Prayer.PROTECT_FROM_MISSILES, 20), add(ZulrahType.MAGIC, ZulrahLocation.EAST, StandLocation.EAST_PILLAR_S, StandLocation.WEST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 20), add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.WEST_PILLAR_N, (StandLocation) null, (Prayer) null, 25), add(ZulrahType.RANGE, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_N, (StandLocation) null, Prayer.PROTECT_FROM_MISSILES, 20), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 36), addJad(ZulrahType.MAGIC, ZulrahLocation.EAST, StandLocation.EAST_PILLAR_N, (StandLocation) null, Prayer.PROTECT_FROM_MAGIC, 35), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 18))),
	ROT_D("Rotation D", ImmutableList.of(add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.MAGIC, ZulrahLocation.EAST, StandLocation.NORTHEAST_TOP, (StandLocation) null, Prayer.PROTECT_FROM_MAGIC, 36), add(ZulrahType.RANGE, ZulrahLocation.SOUTH, StandLocation.WEST_PILLAR_N, StandLocation.WEST_PILLAR_N2, Prayer.PROTECT_FROM_MISSILES, 24), add(ZulrahType.MAGIC, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_N, (StandLocation) null, Prayer.PROTECT_FROM_MAGIC, 30), add(ZulrahType.MELEE, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, (StandLocation) null, (Prayer) null, 28), add(ZulrahType.RANGE, ZulrahLocation.EAST, StandLocation.EAST_PILLAR, (StandLocation) null, Prayer.PROTECT_FROM_MISSILES, 17), add(ZulrahType.RANGE, ZulrahLocation.SOUTH, StandLocation.EAST_PILLAR, (StandLocation) null, (Prayer) null, 34), add(ZulrahType.MAGIC, ZulrahLocation.WEST, StandLocation.WEST_PILLAR_S, (StandLocation) null, Prayer.PROTECT_FROM_MAGIC, 33), add(ZulrahType.RANGE, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MISSILES, 20), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.EAST_PILLAR_N, StandLocation.EAST_PILLAR_S, Prayer.PROTECT_FROM_MAGIC, 27), addJad(ZulrahType.MAGIC, ZulrahLocation.EAST, StandLocation.EAST_PILLAR_N, (StandLocation) null, Prayer.PROTECT_FROM_MAGIC, 29), add(ZulrahType.MAGIC, ZulrahLocation.NORTH, StandLocation.NORTHEAST_TOP, (StandLocation) null, (Prayer) null, 18)));

	private static final List<RotationType> lookup = new ArrayList<>();

	static
	{
		lookup.addAll(EnumSet.allOf(RotationType.class));
	}

	private final String rotationName;
	private final List<ZulrahPhase> zulrahPhases;

	RotationType(String rotationName, List<ZulrahPhase> zulrahPhases)
	{
		this.rotationName = rotationName;
		this.zulrahPhases = zulrahPhases;
	}

	public static List<RotationType> findPotentialRotations(NPC npc, int stage)
	{
		return lookup.stream().filter(type -> type.getZulrahPhases().get(stage).getZulrahNpc().equals(ZulrahNpc.valueOf(npc, false))).collect(Collectors.toList());
	}

	private static ZulrahPhase add(ZulrahType type, ZulrahLocation zulrahLocation, StandLocation standLocation, StandLocation stallLocation, Prayer prayer, int phaseTicks)
	{
		return new ZulrahPhase(new ZulrahNpc(type, zulrahLocation, false), new ZulrahAttributes(standLocation, stallLocation, prayer, phaseTicks));
	}

	private static ZulrahPhase addJad(ZulrahType type, ZulrahLocation zulrahLocation, StandLocation standLocation, StandLocation stallLocation, Prayer prayer, int phaseTicks)
	{
		return new ZulrahPhase(new ZulrahNpc(type, zulrahLocation, true), new ZulrahAttributes(standLocation, stallLocation, prayer, phaseTicks));
	}

	public String getRotationName()
	{
		return this.rotationName;
	}

	public List<ZulrahPhase> getZulrahPhases()
	{
		return this.zulrahPhases;
	}

	public String toString()
	{
		return this.rotationName;
	}
}


class ZulrahPhase
{
	private final ZulrahNpc zulrahNpc;
	private final ZulrahAttributes attributes;

	public ZulrahPhase(ZulrahNpc zulrahNpc, ZulrahAttributes attributes)
	{
		this.zulrahNpc = zulrahNpc;
		this.attributes = attributes;
	}

	public ZulrahNpc getZulrahNpc()
	{
		return zulrahNpc;
	}

	public ZulrahAttributes getAttributes()
	{
		return attributes;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (!(o instanceof ZulrahPhase))
		{
			return false;
		}
		else
		{
			ZulrahPhase other = (ZulrahPhase) o;
			Object this$zulrahNpc = getZulrahNpc();
			Object other$zulrahNpc = other.getZulrahNpc();
			if (this$zulrahNpc == null)
			{
				if (other$zulrahNpc != null)
				{
					return false;
				}
			}
			else if (!this$zulrahNpc.equals(other$zulrahNpc))
			{
				return false;
			}

			Object this$attributes = getAttributes();
			Object other$attributes = other.getAttributes();
			if (this$attributes == null)
			{
				if (other$attributes != null)
				{
					return false;
				}
			}
			else if (!this$attributes.equals(other$attributes))
			{
				return false;
			}

			return true;
		}
	}

	public int hashCode()
	{
		int PRIME = 59;
		int result = 1;
		ZulrahNpc $zulrahNpc = getZulrahNpc();
		result = result * PRIME + ($zulrahNpc == null ? 43 : ((Object) $zulrahNpc).hashCode());
		ZulrahAttributes $attributes = getAttributes();
		result = result * PRIME + ($attributes == null ? 43 : ((Object) $attributes).hashCode());
		return result;
	}

	public String toString()
	{
		ZulrahNpc zulrahNpc = getZulrahNpc();
		return "ZulrahPhase(zulrahNpc=" + zulrahNpc + ", attributes=" + getAttributes() + ")";
	}
}

class ZulrahData
{
	@Nullable
	private final ZulrahPhase current;
	@Nullable
	private final ZulrahPhase next;

	public ZulrahData(@Nullable ZulrahPhase current, @Nullable ZulrahPhase next)
	{
		this.current = current;
		this.next = next;
	}

	public Optional<ZulrahPhase> getCurrentPhase()
	{
		return Optional.ofNullable(current);
	}

	public Optional<ZulrahPhase> getNextPhase()
	{
		return Optional.ofNullable(next);
	}

	public Optional<ZulrahNpc> getCurrentZulrahNpc()
	{
		return current == null ? Optional.empty() : Optional.ofNullable(current.getZulrahNpc());
	}

	public Optional<ZulrahNpc> getNextZulrahNpc()
	{
		return next == null ? Optional.empty() : Optional.ofNullable(next.getZulrahNpc());
	}

	public Optional<StandLocation> getCurrentDynamicStandLocation()
	{
		if (current == null)
		{
			return Optional.empty();
		}
		else if (current.getZulrahNpc().getType() == ZulrahType.MELEE)
		{
			switch (current.getAttributes().getStandLocation())
			{
				case NORTHEAST_TOP:
					return ZulrahPlugin.isFlipStandLocation() ? Optional.of(StandLocation.NORTHEAST_BOTTOM) : Optional.of(current.getAttributes().getStandLocation());
				case WEST:
					return ZulrahPlugin.isFlipStandLocation() ? Optional.of(StandLocation.NORTHWEST_BOTTOM) : Optional.of(current.getAttributes().getStandLocation());
				default:
					return Optional.of(current.getAttributes().getStandLocation());
			}
		}
		else
		{
			return Optional.of(current.getAttributes().getStandLocation());
		}
	}

	public Optional<StandLocation> getNextStandLocation()
	{
		return next == null ? Optional.empty() : Optional.of(next.getAttributes().getStandLocation());
	}

	public Optional<StandLocation> getCurrentStallLocation()
	{
		return current == null ? Optional.empty() : Optional.ofNullable(current.getAttributes().getStallLocation());
	}

	public Optional<StandLocation> getNextStallLocation()
	{
		return next == null ? Optional.empty() : Optional.ofNullable(next.getAttributes().getStallLocation());
	}

	public Optional<Prayer> getCurrentPhasePrayer()
	{
		if (ZulrahPlugin.isZulrahReset())
		{
			return Optional.of(Prayer.PROTECT_FROM_MISSILES);
		}
		else if (current != null && current.getAttributes().getPrayer() != null)
		{
			Prayer phasePrayer = current.getAttributes().getPrayer();
			Prayer invertedPhasePrayer = phasePrayer == Prayer.PROTECT_FROM_MAGIC ? Prayer.PROTECT_FROM_MISSILES : Prayer.PROTECT_FROM_MAGIC;
			return isJad() ? (ZulrahPlugin.isFlipPhasePrayer() ? Optional.of(invertedPhasePrayer) : Optional.of(phasePrayer)) : Optional.of(phasePrayer);
		}
		else
		{
			return Optional.empty();
		}
	}

	public boolean standLocationsMatch()
	{
		return getCurrentDynamicStandLocation().isPresent() && getNextStandLocation().isPresent() && (getCurrentDynamicStandLocation().get()).equals(getNextStandLocation().get());
	}

	public boolean stallLocationsMatch()
	{
		return isPhasesNotNull() && current.getAttributes().getStallLocation() != null && next.getAttributes().getStallLocation() != null && current.getAttributes().getStallLocation().equals(next.getAttributes().getStallLocation());
	}

	public boolean isJad()
	{
		return current != null && current.getZulrahNpc().isJad();
	}

	private boolean isPhasesNotNull()
	{
		return current != null && next != null;
	}
}

class ZulrahAttributes
{
	@Nonnull
	private final StandLocation standLocation;
	@Nullable
	private final StandLocation stallLocation;
	@Nullable
	private final Prayer prayer;
	private final int phaseTicks;

	public ZulrahAttributes(@Nonnull StandLocation standLocation, @Nullable StandLocation stallLocation, @Nullable Prayer prayer, int phaseTicks)
	{
		if (standLocation == null)
		{
			throw new NullPointerException("standLocation is marked non-null but is null");
		}
		else
		{
			this.standLocation = standLocation;
			this.stallLocation = stallLocation;
			this.prayer = prayer;
			this.phaseTicks = phaseTicks;
		}
	}

	@Nonnull
	public StandLocation getStandLocation()
	{
		return this.standLocation;
	}

	@Nullable
	public StandLocation getStallLocation()
	{
		return this.stallLocation;
	}

	@Nullable
	public Prayer getPrayer()
	{
		return this.prayer;
	}

	public int getPhaseTicks()
	{
		return this.phaseTicks;
	}

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (!(o instanceof ZulrahAttributes))
		{
			return false;
		}
		else
		{
			ZulrahAttributes other = (ZulrahAttributes) o;
			Object this$standLocation = getStandLocation();
			Object other$standLocation = other.getStandLocation();
			if (this$standLocation == null)
			{
				if (other$standLocation != null)
				{
					return false;
				}
			}
			else if (!this$standLocation.equals(other$standLocation))
			{
				return false;
			}

			label41:
			{
				Object this$stallLocation = getStallLocation();
				Object other$stallLocation = other.getStallLocation();
				if (this$stallLocation == null)
				{
					if (other$stallLocation == null)
					{
						break label41;
					}
				}
				else if (this$stallLocation.equals(other$stallLocation))
				{
					break label41;
				}

				return false;
			}

			Object this$prayer = getPrayer();
			Object other$prayer = other.getPrayer();
			if (this$prayer == null)
			{
				if (other$prayer != null)
				{
					return false;
				}
			}
			else if (!this$prayer.equals(other$prayer))
			{
				return false;
			}

			if (getPhaseTicks() != other.getPhaseTicks())
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}

	public int hashCode()
	{
		byte PRIME = 59;
		int result = 1;
		Object $standLocation = getStandLocation();
		result = result * PRIME + $standLocation.hashCode();
		Object $stallLocation = getStallLocation();
		result = result * PRIME + ($stallLocation == null ? 43 : $stallLocation.hashCode());
		Object $prayer = getPrayer();
		result = result * PRIME + ($prayer == null ? 43 : $prayer.hashCode());
		result = result * PRIME + getPhaseTicks();
		return result;
	}

	public String toString()
	{
		StandLocation standLocation = getStandLocation();
		return "ZulrahAttributes(standLocation=" + standLocation + ", stallLocation=" + getStallLocation() + ", prayer=" + getPrayer() + ", phaseTicks=" + getPhaseTicks() + ")";
	}
}


class Constants
{
	public static final int ZULRAH_RANGE = 2042;
	public static final int ZULRAH_MELEE = 2043;
	public static final int ZULRAH_MAGIC = 2044;
	public static final int ATTACK_ANIM_ID = 5069;
	public static final int MELEE = 5806;
	public static final int MELEE_TWO = 5807;
	public static final int DEATH_ANIM_ID = 5804;
	public static final int INITIAL_RISE_ANIM_ID = 5071;
	public static final int RISE_ANIM_ID = 5073;
	public static final int DIG_ANIM_ID = 5072;
	public static final int TOXIC_CLOUD_PROJECTILE_ID = 1045;
	public static final int SNAKELING_PROJECTILE_ID = 1047;
	public static final int TOXIC_CLOUD_OBJECT_ID = 11700;
}

enum StandLocation
{
	SOUTHWEST(6208, 6848),
	WEST(5952, 7616),
	CENTER(6848, 6976),
	NORTHEAST_TOP(7488, 7872),
	NORTHEAST_BOTTOM(7360, 7872),
	NORTHWEST_TOP(5952, 7744),
	NORTHWEST_BOTTOM(6208, 7872),
	EAST_PILLAR_S(7232, 6976),
	EAST_PILLAR(7232, 7104),
	EAST_PILLAR_N(7232, 7232),
	EAST_PILLAR_N2(7232, 7360),
	WEST_PILLAR_S(6208, 6976),
	WEST_PILLAR(6208, 7104),
	WEST_PILLAR_N(6208, 7232),
	WEST_PILLAR_N2(6208, 7360);

	private final int localX;
	private final int localY;

	private StandLocation(int localX, int localY)
	{
		this.localX = localX;
		this.localY = localY;
	}

	public LocalPoint toLocalPoint()
	{
		return new LocalPoint(this.localX, this.localY);
	}
}

enum ZulrahLocation {
	NORTH(6720, 7616),
	EAST(8000, 7360),
	SOUTH(6720, 6208),
	WEST(5440, 7360);

	private final int localX;
	private final int localY;

	public LocalPoint toLocalPoint() {
		return new LocalPoint(this.localX, this.localY);
	}

	public static ZulrahLocation valueOf(final LocalPoint localPoint) {
		for (final ZulrahLocation loc : values()) {
			if (loc.toLocalPoint().equals(localPoint)) {
				return loc;
			}
		}
		return null;
	}

	private ZulrahLocation(final int localX, final int localY) {
		this.localX = localX;
		this.localY = localY;
	}

}

enum ZulrahType
{
	RANGE("Range", 2042, Skill.RANGED, Color.YELLOW),
	MELEE("Melee", 2043, Skill.ATTACK, Color.RED),
	MAGIC("Magic", 2044, Skill.MAGIC, Color.CYAN);

	private static final Logger log;
	private final String name;
	private final int npcId;
	private final Skill skill;
	private final Color color;

	public static ZulrahType valueOf(final int npcId)
	{
		switch (npcId)
		{
			case 2042:
			{
				return ZulrahType.RANGE;
			}
			case 2043:
			{
				return ZulrahType.MELEE;
			}
			case 2044:
			{
				return ZulrahType.MAGIC;
			}
			default:
			{
				return null;
			}
		}
	}

	public BufferedImage getImage()
	{
		try
		{
			return ZulrahPlugin.ZULRAH_IMAGES[this.ordinal()];
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			ZulrahType.log.debug("Attempted to get Zulrah Image but was out of bounds... Cause -> {}", ex.getMessage());
			return null;
		}
	}

	public Color getColorWithAlpha(final int alpha)
	{
		return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha);
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	private ZulrahType(final String name, final int npcId, final Skill skill, final Color color)
	{
		this.name = name;
		this.npcId = npcId;
		this.skill = skill;
		this.color = color;
	}

	public String getName()
	{
		return this.name;
	}

	public int getNpcId()
	{
		return this.npcId;
	}

	public Skill getSkill()
	{
		return this.skill;
	}

	public Color getColor()
	{
		return this.color;
	}

	static
	{
		log = LoggerFactory.getLogger(ZulrahType.class);
	}
}

class InstanceTimerOverlay extends OverlayPanel
{
	private final ZulrahConfig config;
	private Instant instanceTimer;

	@Inject
	private InstanceTimerOverlay(ZulrahConfig config)
	{
		this.config = config;
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.instanceTimer() && instanceTimer != null)
		{
			Duration elapsed = Duration.between(instanceTimer, Instant.now());

			panelComponent.getChildren().add(LineComponent.builder().left("Instance Timer:").leftColor(Color.WHITE).right(String.format("%d:%02d", elapsed.getSeconds() / 60, elapsed.getSeconds())).rightColor(Color.GREEN).build());
			return super.render(graphics);
		}
		else
		{
			return null;
		}
	}

	public void setTimer()
	{
		instanceTimer = Instant.now();
	}

	public void resetTimer()
	{
		instanceTimer = null;
	}
}


class PhaseOverlay
		extends OverlayPanel
{
	private static final int DEFAULT_DIMENSION = 55;
	private static final int GAP = 1;
	private final ZulrahPlugin plugin;
	private final ZulrahConfig config;

	@Inject
	private PhaseOverlay(ZulrahPlugin plugin, ZulrahConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		this.setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		this.setPriority(OverlayPriority.HIGH);
		this.setResizable(false);
		this.panelComponent.setPreferredSize(new Dimension(56, 56));
		this.panelComponent.setBorder(new Rectangle());
		this.panelComponent.setGap(new Point(1, 1));
		this.panelComponent.setOrientation(net.runelite.client.ui.overlay.components.ComponentOrientation.HORIZONTAL);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.phaseDisplayType() == ZulrahConfig.DisplayType.OFF || config.phaseDisplayType() == ZulrahConfig.DisplayType.TILE || plugin.getZulrahNpc() == null || plugin.getZulrahNpc().isDead())
		{
			return null;
		}
		LinkedHashMultimap<ZulrahType, MutablePair<String, Boolean>> zulrahPhases = LinkedHashMultimap.create();
		plugin.getZulrahData().forEach(data -> {
			switch (config.phaseDisplayMode())
			{
				case CURRENT:
				{
					data.getCurrentPhase().ifPresent(phase -> zulrahPhases.put(phase.getZulrahNpc().getType(), new MutablePair<String, Boolean>("Current", false)));
					break;
				}
				case NEXT:
				{
					data.getNextPhase().ifPresent(phase -> zulrahPhases.put(phase.getZulrahNpc().getType(), new MutablePair<String, Boolean>(getNextString(), true)));
					break;
				}
				case BOTH:
				{
					data.getCurrentPhase().ifPresent(phase -> zulrahPhases.put(phase.getZulrahNpc().getType(), new MutablePair<String, Boolean>("Current", false)));
					data.getNextPhase().ifPresent(phase -> zulrahPhases.put(phase.getZulrahNpc().getType(), new MutablePair<String, Boolean>(getNextString(), true)));
					break;
				}
				default:
				{
					throw new IllegalStateException("[PhaseOverlay] Invalid 'phaseDisplayMode' config state");
				}
			}
		});
		if (zulrahPhases.size() <= 0)
		{
			return null;
		}

		List<InfoBoxComponent> components = zulrahPhases.entries().stream().map(this::infoBoxComponent).collect(Collectors.toList());
		components.forEach(i -> panelComponent.getChildren().add(i));
		if (config.phaseRotationName())
		{
			displayRotationName(graphics);
		}
		return super.render(graphics);
	}

	private String getNextString()
	{
		return plugin.getCurrentRotation() != null ? "Next" : "P. Next";
	}

	private InfoBoxComponent infoBoxComponent(Map.Entry<ZulrahType, MutablePair<String, Boolean>> entry)
	{
		InfoBoxComponent infoBox = new InfoBoxComponent();
		infoBox.setText(entry.getValue().getLeft());
		infoBox.setOutline(config.textOutline());
		infoBox.setColor(entry.getValue().getRight() == false ? Color.GREEN : Color.RED);
		infoBox.setImage(entry.getKey().getImage());
		infoBox.setBackgroundColor(entry.getKey().getColorWithAlpha(50));
		return infoBox;
	}

	private void displayRotationName(Graphics2D graphics)
	{
		Rectangle bounds = panelComponent.getBounds();
		String text = plugin.getCurrentRotation() != null ? plugin.getCurrentRotation().getRotationName() : "Unidentified";
		TextComponent textComponent = new TextComponent();
		textComponent.setPosition(new Point(bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(text)) / 2, bounds.y + 1));
		textComponent.setText(text);
		textComponent.setColor(plugin.getCurrentRotation() != null ? Color.GREEN : Color.YELLOW);
		textComponent.setOutline(config.textOutline());
		textComponent.render(graphics);
	}
}

class PrayerHelperOverlay extends OverlayPanel
{
	@Inject Core core;
	private final Client client;
	private final ZulrahPlugin plugin;
	private final ZulrahConfig config;
	private final SpriteManager spriteManager;
	private final Color RED = new Color(255, 0, 0, 25);
	private final Color GREEN = new Color(0, 255, 0, 25);


	@Inject
	private PrayerHelperOverlay(Client client, ZulrahPlugin plugin, ZulrahConfig config, SpriteManager spriteManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
		setResizable(false);
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.prayerHelper() || plugin.getZulrahNpc() == null || plugin.getZulrahNpc().isDead())
		{
			return null;
		}
		Prayer prayer = null;
		for (ZulrahData data : plugin.getZulrahData())
		{
			if (!data.getCurrentPhasePrayer().isPresent()) continue;
			prayer = data.getCurrentPhasePrayer().get();
		}//TODO activate prayers

		InfoBoxComponent prayComponent = new InfoBoxComponent();
		prayComponent.setImage(spriteManager.getSprite(prayerToSpriteId(prayer), 0));
		prayComponent.setBackgroundColor(!client.isPrayerActive(prayer) ? RED : GREEN);
		prayComponent.setPreferredSize(new Dimension(40, 40));
		panelComponent.getChildren().add(prayComponent);
		panelComponent.setPreferredSize(new Dimension(40, 0));
		panelComponent.setBorder(new Rectangle(0, 0, 0, 0));
		return super.render(graphics);
	}

	private int prayerToSpriteId(Prayer prayer)
	{
		switch (prayer)
		{
			case PROTECT_FROM_MELEE:
			{
				return 129;
			}
			case PROTECT_FROM_MISSILES:
			{
				return 128;
			}
			case PROTECT_FROM_MAGIC:
			{
				return 127;
			}
		}
		return -1;
	}
}

class PrayerMarkerOverlay extends Overlay
{
	private final Client client;
	private final ZulrahPlugin plugin;
	private final ZulrahConfig config;

	@Inject
	private PrayerMarkerOverlay(Client client, ZulrahPlugin plugin, ZulrahConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.setPosition(OverlayPosition.DYNAMIC);
		this.setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.setPriority(OverlayPriority.HIGH);
	}
	//@Inject
    Core core;
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.prayerMarker() && plugin.getZulrahNpc() != null && !plugin.getZulrahNpc().isDead())
		{
			plugin.getZulrahData().forEach((data) -> {
				data.getCurrentPhasePrayer().ifPresent((prayer) -> {
					if (client.getVar(VarClientInt.INVENTORY_TAB) == 5)
					{
						Widget widget = client.getWidget(541, prayerToChildId(prayer));
						Color color = !client.isPrayerActive(prayer) ? Color.RED : Color.GREEN;
						if (!client.isPrayerActive(prayer)) {
							core.moveClick(widget.getBounds());
						}
						OverlayUtils.renderWidgetPolygon(graphics, widget, color, true, false);
					}

				});
			});
			return null;
		}
		else
		{
			return null;
		}
	}

	private int prayerToChildId(Prayer prayer)
	{
		switch (prayer)
		{
			case PROTECT_FROM_MELEE:
				return 19;
			case PROTECT_FROM_MISSILES:
				return 18;
			case PROTECT_FROM_MAGIC:
				return 17;
			default:
				return -1;
		}
	}
}

class SceneOverlay extends Overlay
{
	private final Client client;
	private final ZulrahPlugin plugin;
	private final ZulrahConfig config;
	private final SkillIconManager skillIconManager;

	@Inject
	private SceneOverlay(Client client, ZulrahPlugin plugin, ZulrahConfig config, SkillIconManager skillIconManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.skillIconManager = skillIconManager;
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Font prevFont = graphics.getFont();
		graphics.setFont(config.fontType().getFont());
		if (plugin.getZulrahNpc() != null && !plugin.getZulrahNpc().isDead())
		{
			renderZulrahPhaseTiles(graphics);
			renderStandAndStallTiles(graphics);
			renderPrayerConservation(graphics);
			renderZulrahTicks(graphics);
			renderZulrahTile(graphics);
			renderProjectiles(graphics);
			renderToxicClouds(graphics);
		}

		graphics.setFont(prevFont);
		return null;
	}

	private void renderZulrahPhaseTiles(Graphics2D graphics)
	{
		if (config.phaseDisplayType() != ZulrahConfig.DisplayType.OFF && config.phaseDisplayType() != ZulrahConfig.DisplayType.OVERLAY)
		{
			SetMultimap<ZulrahLocation, MutablePair<String, ZulrahNpc>> zulrahLocationsGrouped = LinkedHashMultimap.create();
			plugin.getZulrahData().forEach((data) -> {
				switch (config.phaseDisplayMode())
				{
					case CURRENT:
						data.getCurrentZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>("Current", npc));
						});
						break;
					case NEXT:
						data.getNextZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>(getZulrahNextString(), npc));
						});
						break;
					case BOTH:
						data.getCurrentZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>("Current", npc));
						});
						data.getNextZulrahNpc().ifPresent((npc) -> {
							zulrahLocationsGrouped.put(npc.getZulrahLocation(), new MutablePair<>(getZulrahNextString(), npc));
						});
						break;
					default:
						throw new IllegalStateException("[SceneOverlay] Invalid 'phaseDisplayMode' config state");
				}

			});
			Iterator location = zulrahLocationsGrouped.keys().iterator();

			while (location.hasNext())
			{
				ZulrahLocation zulrahLocation = (ZulrahLocation) location.next();
				int offset = 0;

				for (Iterator groupedLocation = zulrahLocationsGrouped.get(zulrahLocation).iterator(); groupedLocation.hasNext(); offset += graphics.getFontMetrics().getHeight())
				{
					Pair pair = (Pair) groupedLocation.next();
					drawZulrahTile(graphics, (ZulrahNpc) pair.getRight(), (String) pair.getLeft(), offset);
				}
			}
		}
	}

	private String getZulrahNextString()
	{
		return plugin.getCurrentRotation() != null ? "Next" : "P. Next";
	}

	private void drawZulrahTile(Graphics2D graphics, ZulrahNpc zulrahNpc, String addonText, int offset)
	{
		if (zulrahNpc != null)
		{
			LocalPoint localPoint = zulrahNpc.getZulrahLocation().toLocalPoint();
			Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(client, localPoint, 5);
			OverlayUtils.renderPolygon(graphics, tileAreaPoly, zulrahNpc.getType().getColor(), config.outlineWidth(), config.fillAlpha());
			net.runelite.api.Point basePoint = Perspective.localToCanvas(client, localPoint, client.getPlane(), 0);
			if (basePoint != null)
			{
				int bx = basePoint.getX();
				int by = basePoint.getY();
				String text = getZulrahPhaseString(zulrahNpc, addonText);
				Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(text, graphics);
				net.runelite.api.Point textLocation = new net.runelite.api.Point(bx - (int) textBounds.getWidth() / 2, by - offset);
				Color color = zulrahNpc.getType().getColor();
				ZulrahConfig config = this.config;
				Objects.requireNonNull(config);
				OverlayUtils.renderTextLocation(graphics, textLocation, text, color, config::textOutline);
				if (this.config.phaseHats())
				{
					BufferedImage icon = skillIconManager.getSkillImage(zulrahNpc.getType().getSkill(), this.config
							.fontType() != FontType.BOLD);
					int imgPX = bx - icon.getWidth() / 2;
					int imgPY = by - icon.getHeight() / 2 - offset;
					net.runelite.api.Point imgPoint = new net.runelite.api.Point(imgPX, imgPY);
					int imgX = imgPoint.getX() - graphics.getFontMetrics().stringWidth(text) / 2 - 15;
					int imgY = imgPoint.getY() - icon.getHeight() / 2 + 1;
					graphics.drawImage(icon, imgX, imgY, (ImageObserver) null);
				}
			}

		}
	}

	private String getZulrahPhaseString(ZulrahNpc npc, String addonText)
	{
		boolean strip = !config.phaseTags() || Strings.isNullOrEmpty(addonText);
		if (npc.isJad())
		{
			return strip ? "Jad" : "[" + addonText + "] Jad";
		}
		else
		{
			return strip ? npc.getType().getName() : "[" + addonText + "] " + npc.getType().getName();
		}
	}

	private void renderStandAndStallTiles(Graphics2D graphics)
	{
		if (config.standLocations() || config.stallLocations())
		{
			SetMultimap standLocationsGrouped = HashMultimap.create();
			plugin.getZulrahData().forEach((data) -> {
				if (config.standLocations())
				{
					if (data.standLocationsMatch())
					{
						data.getCurrentDynamicStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Stand / Next", config.standAndNextTileColor()));
						});
					}
					else
					{
						data.getCurrentDynamicStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Stand", config.standTileColor()));
						});
						data.getNextStandLocation().ifPresent((loc) -> {
							standLocationsGrouped.put(loc, new MutablePair("Next", config.nextTileColor()));
						});
					}
				}

				if (config.stallLocations())
				{
					data.getCurrentStallLocation().ifPresent((loc) -> {
						standLocationsGrouped.put(loc, new MutablePair("Stall", config.stallTileColor()));
					});
					data.getNextStallLocation().ifPresent((loc) -> {
						standLocationsGrouped.put(loc, new MutablePair("Stall / next", config.nextTileColor()));
					});
				}

			});
			Iterator location = standLocationsGrouped.keys().iterator();

			while (location.hasNext())
			{
				StandLocation standLocation = (StandLocation) location.next();
				int offset = 0;

				for (Iterator locationGrouped = standLocationsGrouped.get(standLocation).iterator(); locationGrouped.hasNext(); offset += graphics.getFontMetrics().getHeight())
				{
					Pair pair = (Pair) locationGrouped.next();
					drawTile(graphics, standLocation.toLocalPoint(), (String) pair.getLeft(), (Color) pair.getRight(), offset);
				}
			}

		}
	}

	private void drawTile(Graphics2D graphics, LocalPoint localPoint, String text, Color color, int offset)
	{
		if (localPoint != null && !Strings.isNullOrEmpty(text))
		{
			net.runelite.api.Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, text, 0);
			net.runelite.api.Point txtLoc = new net.runelite.api.Point(textLocation.getX(), textLocation.getY() - offset);
			Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue());
			ZulrahConfig config = this.config;
			Objects.requireNonNull(config);
			OverlayUtils.renderTextLocation(graphics, txtLoc, text, color2, config::textOutline);
			Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
			OverlayUtils.renderPolygon(graphics, tilePoly, color, this.config.outlineWidth(), this.config.fillAlpha());
		}
	}

	private void renderPrayerConservation(Graphics2D graphics)
	{
		if (config.prayerConservation())
		{
			Player player = client.getLocalPlayer();
			if (player != null && (plugin.getZulrahNpc().getInteracting() == null || plugin.getZulrahNpc().getInteracting() != client.getLocalPlayer()) && player.getOverheadIcon() != null)
			{
				String conserveStr = "Turn off overheads to conserve prayer!";
				net.runelite.api.Point textLocation = player.getCanvasTextLocation(graphics, "Turn off overheads to conserve prayer!", player.getLogicalHeight() - 22);
				Color color = Color.RED;
				ZulrahConfig config = this.config;
				Objects.requireNonNull(config);
				OverlayUtils.renderTextLocation(graphics, textLocation, "Turn off overheads to conserve prayer!", color, config::textOutline);
			}

		}
	}

	private void renderZulrahTicks(Graphics2D graphics)
	{
		if (config.phaseTickCounter() || config.attackTickCounter())
		{
			StringBuilder sb = new StringBuilder();
			sb = sb.append(config.phaseTickCounter() && plugin.getPhaseTicks() >= 0 ? plugin.getPhaseTicks() : "").append(config.phaseTickCounter() && config.attackTickCounter() && plugin.getPhaseTicks() >= 0 && plugin.getAttackTicks() >= 0 ? " : " : "").append(config.attackTickCounter() && plugin.getAttackTicks() >= 0 ? plugin.getAttackTicks() : "");
			if (!Strings.isNullOrEmpty(sb.toString()))
			{
				net.runelite.api.Point textLocation = plugin.getZulrahNpc().getCanvasTextLocation(graphics, sb.toString(), plugin.getZulrahNpc().getLogicalHeight() - 130);
				String string = sb.toString();
				Color tickCounterColors = config.tickCounterColors();
				ZulrahConfig config = this.config;
				Objects.requireNonNull(config);
				OverlayUtils.renderTextLocation(graphics, textLocation, string, tickCounterColors, config::textOutline);
			}
		}
	}

	private void renderZulrahTile(Graphics2D graphics)
	{
		if (config.displayZulrahTile())
		{
			Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(client, plugin.getZulrahNpc().getLocalLocation(), 5);
			OverlayUtils.renderPolygon(graphics, tileAreaPoly, config.zulrahTileColor(), config.outlineWidth(), config.fillAlpha());
		}
	}

	private void renderProjectiles(Graphics2D graphics)
	{
		if (config.displayProjectiles() && plugin.getProjectilesMap().size() > 0)
		{
			plugin.getProjectilesMap().forEach((localPoint, ticks) -> {
				net.runelite.api.Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, "#", 0);
				String string = Integer.toString(ticks);
				Color color = ticks > 0 ? Color.WHITE : Color.RED;
				ZulrahConfig config = this.config;
				Objects.requireNonNull(config);
				OverlayUtils.renderTextLocation(graphics, textLocation, string, color, config::textOutline);
				Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
				OverlayUtils.renderPolygon(graphics, tilePoly, this.config.projectilesColor(), this.config.outlineWidth(), this.config
						.fillAlpha());
			});
		}
	}

	private void renderToxicClouds(Graphics2D graphics)
	{
		if (!config.displayToxicClouds() || plugin.getToxicCloudsMap().size() <= 0)
		{
			return;
		}
		plugin.getToxicCloudsMap().forEach((obj, ticks) -> {
			LocalPoint localPoint = obj.getLocalLocation();
			Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(client, localPoint, 3);
			OverlayUtils.renderPolygon(graphics, tileAreaPoly, config.toxicCloudsColor(), config.outlineWidth(), config.fillAlpha());
			String ticksString = Integer.toString(ticks);
			net.runelite.api.Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, ticksString, 0);
			OverlayUtils.renderTextLocation(graphics, textLocation, ticksString, ticks > 0 ? Color.RED : Color.GREEN, config::textOutline);
		});
	}
}


class OverlayUtils
{
	public static float getAlphaTime()
	{
		return (float) Math.abs(System.currentTimeMillis() % 2000L - 1000L) / 1000.0F;
	}

	public static void renderImageLocation(Graphics2D graphics, net.runelite.api.Point imgLoc, BufferedImage image)
	{
		if (imgLoc != null && image != null)
		{
			int x = imgLoc.getX();
			int y = imgLoc.getY();
			graphics.drawImage(image, x, y, null);
		}
	}

	public static void renderTextLocation(Graphics2D graphics, net.runelite.api.Point txtLoc, String text, Color color, Supplier outline)
	{
		if (txtLoc != null && !Strings.isNullOrEmpty(text))
		{
			int x = txtLoc.getX();
			int y = txtLoc.getY();
			graphics.setColor(Color.BLACK);
			if ((Boolean) outline.get())
			{
				graphics.drawString(text, x, y + 1);
				graphics.drawString(text, x, y - 1);
				graphics.drawString(text, x + 1, y);
				graphics.drawString(text, x - 1, y);
			}
			else
			{
				graphics.drawString(text, x + 1, y + 1);
			}

			graphics.setColor(color);
			graphics.drawString(text, x, y);
		}
	}

	public static void renderPolygon(Graphics2D graphics, Shape polygon, Color color, int outlineStroke, int fillAlpha)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke((float) outlineStroke));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
			graphics.fill(polygon);
		}
	}

	public static void renderWidgetPolygon(Graphics2D graphics, Widget widget, Color color, boolean outlineOnly, boolean flash)
	{
		if (widget != null)
		{
			Rectangle widgetBounds = widget.getBounds();
			if (widgetBounds != null)
			{
				if (flash)
				{
					graphics.setComposite(AlphaComposite.getInstance(3, getAlphaTime()));
					graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				}

				if (outlineOnly)
				{
					graphics.setColor(color);
					graphics.setStroke(new BasicStroke(1.0F));
					graphics.draw(widgetBounds);
				}
				else
				{
					graphics.setColor(color);
					graphics.fill(widgetBounds);
				}

			}
		}
	}
}
