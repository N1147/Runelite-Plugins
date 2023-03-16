package net.runelite.client.plugins.NGauntlet;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class NGauntletOverlay extends OverlayPanel
{
	private final PQuickEat plugin;
	private final PQuickEatConfig config;

	String timeFormat;
	private String infoStatus = "Starting...";

	@Inject
	private NGauntletOverlay(final Client client, final PQuickEat plugin, final PQuickEatConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "airs overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow(" ");
		TableComponent tableDelayComponent = new TableComponent();
		tableDelayComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		if (!tableComponent.isEmpty())
		{
			panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
			panelComponent.setPreferredSize(new Dimension(2000, 2000));
			panelComponent.setBorder(new Rectangle(1, 1, 2000, 2000));
			panelComponent.getChildren().add(TitleComponent.builder().text("Loading...").color(ColorUtil.fromHex("#FFFF00")).build());
			panelComponent.getChildren().add(tableComponent);
			panelComponent.getChildren().add(tableDelayComponent);
		}
		return super.render(graphics);
	}
}
