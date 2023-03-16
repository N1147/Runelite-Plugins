package net.runelite.client.plugins.awyverns;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import net.runelite.client.ui.overlay.RenderableEntity;

public class TextComponent implements RenderableEntity {
	private String text;
	private Point position = new Point();
	private Color color;

	public TextComponent() {
		this.color = Color.WHITE;
	}

	public Dimension render(Graphics2D graphics) {
		graphics.setColor(Color.BLACK);
		graphics.drawString(this.text, this.position.x + 1, this.position.y + 1);
		graphics.setColor(this.color);
		graphics.drawString(this.text, this.position.x, this.position.y);
		FontMetrics fontMetrics = graphics.getFontMetrics();
		return new Dimension(fontMetrics.stringWidth(this.text), fontMetrics.getHeight());
	}

	void setText(String text) {
		this.text = text;
	}

	void setPosition(Point position) {
		this.position = position;
	}

	void setColor(Color color) {
		this.color = color;
	}
}
