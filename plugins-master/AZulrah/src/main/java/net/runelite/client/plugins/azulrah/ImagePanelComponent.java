package net.runelite.client.plugins.azulrah;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.RenderableEntity;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

@Singleton
public class ImagePanelComponent implements RenderableEntity {
	private static final int TOP_BORDER = 3;
	private static final int SIDE_BORDER = 6;
	private static final int BOTTOM_BORDER = 6;
	private static final int SEPARATOR = 4;
	@Nullable
	private String title;
	private Color titleColor;
	private Color backgroundColor;
	private BufferedImage image;
	private Point position;

	public ImagePanelComponent() {
		this.titleColor = Color.WHITE;
		this.backgroundColor = new Color(70, 61, 50, 156);
		this.position = new Point();
	}

	public Dimension render(Graphics2D graphics) {
		Dimension dimension = new Dimension();
		FontMetrics metrics = graphics.getFontMetrics();
		int height = 3 + (Strings.isNullOrEmpty(this.title) ? 0 : metrics.getHeight()) + 4 + this.image.getHeight() + 6;
		int width = Math.max(Strings.isNullOrEmpty(this.title) ? 0 : metrics.stringWidth(this.title), this.image.getWidth()) + 12;
		dimension.setSize(width, height);
		if (dimension.height == 0) {
			return null;
		} else {
			int y = this.position.y + 3 + metrics.getHeight();
			BackgroundComponent backgroundComponent = new BackgroundComponent();
			backgroundComponent.setBackgroundColor(this.backgroundColor);
			backgroundComponent.setRectangle(new Rectangle(this.position.x, this.position.y, dimension.width, dimension.height));
			backgroundComponent.render(graphics);
			if (!Strings.isNullOrEmpty(this.title)) {
				TextComponent titleComponent = new TextComponent();
				titleComponent.setText(this.title);
				titleComponent.setColor(this.titleColor);
				titleComponent.setPosition(new Point(this.position.x + (width - metrics.stringWidth(this.title)) / 2, y));
				titleComponent.render(graphics);
				y += 4;
			}

			graphics.drawImage(this.image, this.position.x + (width - this.image.getWidth()) / 2, y, (ImageObserver)null);
			return dimension;
		}
	}

	public void setTitle(@Nullable String title) {
		this.title = title;
	}

	void setTitleColor(Color titleColor) {
		this.titleColor = titleColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	void setPosition(Point position) {
		this.position = position;
	}
}
